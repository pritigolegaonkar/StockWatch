package com.example.priti.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AsyncStockDownloader extends AsyncTask<Stock, Void, String> {

    private static final String TAG = "AsyncStockDownloader";
    private MainActivity mainActivity;
    private final String stockURL = "https://api.iextrading.com";
    Stock stock;
    int actionCode;

    public AsyncStockDownloader(MainActivity mainActivity, int code) {
        this.mainActivity = mainActivity;
        this.actionCode = code;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: ");
        Stock newStock = parseJSONData(s);
        mainActivity.processAsyncStockLoaderData(newStock, actionCode);
    }

    @Override
    protected String doInBackground(Stock ... params) {
        stock = params[0];
        Uri.Builder buildUri = Uri.parse(stockURL).buildUpon();
        buildUri.appendPath("1.0");
        buildUri.appendPath("stock");
        buildUri.appendPath(stock.getSymbol());
        buildUri.appendPath("quote");

        String completeURL = buildUri.build().toString();

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(completeURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            Log.d(TAG, "doInBackground: " + sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            e.printStackTrace();
            return null;
        }

        Log.d(TAG, "doInBackground: " + sb.toString());
        return sb.toString();
    }

    private Stock parseJSONData(String s) {

        Stock stockObj = new Stock("*INVALID", "unknown", 0, 0, 0);
        try {
            JSONObject stockObject = new JSONObject(s);
            String symbol = stockObject.getString("symbol");
            String name = stockObject.getString("companyName");
            String price = stockObject.getString("latestPrice");
            String priceChange = stockObject.getString("change");
            String changePercent = stockObject.getString("changePercent");
            stockObj.setSymbol(symbol);
            stockObj.setCompanyName(name);
            stockObj.setTradePrice(Double.parseDouble(price));
            stockObj.setStockPriceChange(Double.parseDouble(priceChange));
            stockObj.setStockPercentChange(Double.parseDouble(changePercent));
            Log.d(TAG, "parseJSON:  StockName: " + name+ " change: " +priceChange+ " percentChange: " +changePercent);
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return stockObj;
    }
}
