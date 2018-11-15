package com.example.priti.stockwatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.R.drawable.ic_dialog_alert;
import static android.R.drawable.ic_menu_delete;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener, View.OnLongClickListener{
    private static final String TAG = "MainActivity";
    private String enteredStock, companySymbolToStore, companyNameToStore;
    private ArrayList<Stock> stockList = new ArrayList<Stock>();
    private Stock stock;
    boolean duplicateStock = false;
    private int UPDATE_CODE = 2;
    private StockAdapter stockAdapter;
    private static String stockURL = "http://www.marketwatch.com/investing/stock/";
    HashMap<String, Stock> hmap = new HashMap<String, Stock>();
    private ArrayList<Stock> temporaryList;
    MainActivity mainActivity = this;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiper;
    private DatabaseHandler dbHandler;
    private AsyncNameDownloader nameLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isNetworkConnected("onAppStart");
        recyclerView = (RecyclerView) findViewById(R.id.recylerView);
        stockAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        swiper = (SwipeRefreshLayout) findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiperRefresh();
            }
        });
        nameLoader = new AsyncNameDownloader(this);
        nameLoader.execute();
        DatabaseHandler.getInstance(this).dumpDbToLog();
        if(isNetworkConnected("create")){
            ArrayList<Stock> list = DatabaseHandler.getInstance(mainActivity).loadStocks();
            stockList.clear();
            stockList.addAll(list);
            Log.d(TAG, "onCreate: " + list);
            Collections.sort(stockList, new StockSorter());
            stockAdapter.notifyDataSetChanged();

        } else{
            ArrayList<Stock> list = DatabaseHandler.getInstance(mainActivity).loadStocks();
            stockList.clear();
            if(list.size() > 0){
                for(int i=0;i<list.size();i++){
                    String symbol = list.get(i).getSymbol();
                    String name = list.get(i).getCompanyName();
                    Stock emptyStock = new Stock(symbol, name, 0,0,0);
                    stockList.add(emptyStock);
                }
                Collections.sort(stockList, new StockSorter());
                stockAdapter.notifyDataSetChanged();
            }
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addstock, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addNewStock:
                if(isNetworkConnected("newStock")){
                    addStock(this);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public  boolean isNetworkConnected(String activity){
        ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(null==connectionManager){
            return false;
        }
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        if(null!=networkInfo){
            return true;
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setIcon(ic_dialog_alert);
            if("newStock".equalsIgnoreCase(activity)){
                builder.setMessage("Stock cannot be added without a network connection");
            }else if("swipeRefresh".equalsIgnoreCase(activity)){
                builder.setMessage("Stock cannot be updated without a network connection");
            }else{
                builder.setMessage("You are not Connected to a Network");
            }
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
    }

    private void swiperRefresh() {

        if(isNetworkConnected("swipeRefresh")){
           // nameLoader = new AsyncNameDownloader(this);
            //nameLoader.execute();
            temporaryList = new ArrayList<Stock>();
            temporaryList.addAll(stockList);
            stockList.clear();

            for (Stock stock : temporaryList) {
                UPDATE_CODE = 9;
                hmap.put(stock.getSymbol(), stock);
                companyNameToStore = stock.getCompanyName();
                enteredStock = stock.getSymbol();
                new AsyncStockDownloader(mainActivity, UPDATE_CODE).execute(stock);
            }

            Collections.shuffle(stockList);
            stockAdapter.notifyDataSetChanged();

        }
        swiper.setRefreshing(false);

    }

    @Override
    protected void onResume() {
        DatabaseHandler.getInstance(this).dumpDbToLog();
        if(isNetworkConnected("Resume")){
            nameLoader = new AsyncNameDownloader(this);
            nameLoader.execute();
            ArrayList<Stock> list = DatabaseHandler.getInstance(mainActivity).loadStocks();
            stockList.clear();
            stockList.addAll(list);
            Log.d(TAG, "onResume: " + list);
            Collections.sort(stockList, new StockSorter());
            stockAdapter.notifyDataSetChanged();

        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        DatabaseHandler.getInstance(this).shutDown();
        super.onDestroy();
    }
    public void addStock(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stock Selection");
        builder.setMessage("Please enter a Stock Symbol");
        final EditText inputStock = new EditText(this);
        inputStock.setInputType(InputType.TYPE_CLASS_TEXT);
        inputStock.setGravity(Gravity.CENTER_HORIZONTAL);
        inputStock.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        builder.setView(inputStock);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                enteredStock = inputStock.getText().toString();
                for (Stock stock : stockList) {
                    if (stock.getSymbol().equals(enteredStock)) {
                        duplicateStock = true;
                    }
                }
                if (duplicateStock) {
                    alertBuilder(context, "Duplicate Stock", "Stock Symbol " + enteredStock + " is already displayed");
                    duplicateStock = false;

                } else {

                    HashMap<String, String> matchedHashMap = nameLoader.getSymbols(enteredStock);

                    if (matchedHashMap.size() == 1) {
                        //Log.d("entered stock",matchedHashMap.toString());
                        String companyName = matchedHashMap.values().toArray()[0].toString();
                        String symbol = enteredStock;
                        //Log.d("amzn",symbol + companyName);
                        companySymbolToStore = symbol;
                        companyNameToStore = companyName;
                        DatabaseHandler.getInstance(MainActivity.this).dumpDbToLog();

                        ArrayList<Stock> dblist = DatabaseHandler.getInstance(mainActivity).loadStocks();


                        for (Stock dbl : dblist) {

                            if (dbl.getSymbol().toString().equals(symbol)) {
                                duplicateStock = true;
                                break;
                            }

                        }
                        dblist.clear();

                        if (duplicateStock) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            Intent data = getIntent();
                            builder.setTitle("DUPLICATE STOCK");
                            builder.setIcon(R.drawable.danger);
                            builder.setMessage("Stock Symbol " + symbol + " is already displayed");
                            AlertDialog dialogDuplicate = builder.create();
                            dialogDuplicate.show();
                            duplicateStock = false;

                        } else {
                            companyNameToStore = companyName;
                            Stock temp = new Stock(companySymbolToStore, companyNameToStore, 0, 0, 0);
                            new AsyncStockDownloader(mainActivity, UPDATE_CODE).execute(temp);
                        }

                    } else if (matchedHashMap.size() > 1){
                        final CharSequence[] symbolNameList = new CharSequence[matchedHashMap.size()];
                        int i=0;
                        for(HashMap.Entry<String, String> entry : matchedHashMap.entrySet()){
                            symbolNameList[i] = entry.getKey().toString() + " - " + entry.getValue().toString();
                            i=i+1;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Make A Selection");
                        builder.setItems(symbolNameList, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String[] tempSplit = symbolNameList[which].toString().split("-");
                                ArrayList<Stock> dblist = DatabaseHandler.getInstance(mainActivity).loadStocks();
                                for (Stock dbl : dblist) {

                                    if (dbl.getSymbol().toString().equals(tempSplit[0].trim())) {
                                        duplicateStock = true;
                                        break;
                                    }

                                }
                                dblist.clear();
                                if (duplicateStock) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    Intent data = getIntent();
                                    builder.setTitle("DUPLICATE STOCK");
                                    builder.setIcon(R.drawable.danger);
                                    builder.setMessage("Stock Symbol " + tempSplit[0].trim() + " is already displayed");
                                    AlertDialog dialogDuplicate = builder.create();
                                    dialogDuplicate.show();
                                    duplicateStock = false;

                                } else {
                                    Stock temp = new Stock(tempSplit[0].trim(), tempSplit[1].trim(), 0, 0, 0);
                                    new AsyncStockDownloader(mainActivity, UPDATE_CODE).execute(temp);
                                }
                            }
                        });

                        builder.setNegativeButton("NEVERMIND", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog1 = builder.create();
                        dialog1.show();

                    } else if(matchedHashMap.size() == 0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        Intent data = getIntent();
                        builder.setTitle("SYMBOL NOT FOUND: " + enteredStock);
                        builder.setMessage("Data for stock symbol");
                        AlertDialog dialogDuplicate = builder.create();
                        dialogDuplicate.show();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void alertBuilder(Context context, String alertTitle, String alertText){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        Intent data = getIntent();
        builder.setTitle(alertTitle);
        if ("Duplicate Stock".equals(alertTitle)) {
            builder.setIcon(ic_dialog_alert);
        }
        builder.setMessage(alertText);
        AlertDialog dialogDuplicate = builder.create();
        dialogDuplicate.show();
    }

    public void processAsyncStockLoaderData(Stock stockData, int code) {
        Log.d("amzn", stockData.getSymbol());
        if ( stockData.getSymbol() != "*INVALID") {
            if (code == 9) {

                DatabaseHandler.getInstance(this).updateStock(stockData);
                stockList.add(stockData);
                Collections.sort(stockList, new StockSorter());
                stockAdapter.notifyDataSetChanged();

            } else {
                DatabaseHandler.getInstance(this).addStock(stockData);
                stockList.add(stockData);
                Collections.sort(stockList, new StockSorter());
                stockAdapter.notifyDataSetChanged();
            }

        } else {
            Log.d("amzn", enteredStock);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            Intent data = getIntent();
            builder.setTitle("SYMBOL NOT FOUND: " + enteredStock);
            builder.setMessage("No Data for " + enteredStock);
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        UPDATE_CODE = 2;
    }

    public void onClick(View v) {

        String symbolForURL = "";
        String URL;
        Log.d(TAG, "Running Good !!!");
        int pos = recyclerView.getChildLayoutPosition(v);
        symbolForURL = stockList.get(pos).getSymbol().toString();
        URL = stockURL + symbolForURL;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(URL));
        startActivity(i);

        Log.d(TAG, pos + "");
        //Toast.makeText(this, "View has been clicked", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onLongClick(View v) {

        final int pos = recyclerView.getChildLayoutPosition(v);
        stock = stockList.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Intent data = getIntent();
        builder.setTitle("Delete Stock!");
        builder.setIcon(R.drawable.deleteicon);
        builder.setMessage("Delete Stock" + "  " + stock.getSymbol() + " ?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {


                DatabaseHandler.getInstance(mainActivity).deleteStock(stockList.get(pos).getSymbol());
                stockList.remove(pos);
                Collections.sort(stockList, new StockSorter());
                stockAdapter.notifyDataSetChanged();


            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return false;

    }




}
