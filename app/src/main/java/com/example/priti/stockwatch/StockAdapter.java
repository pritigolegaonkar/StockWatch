package com.example.priti.stockwatch;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private static final String TAG = "StockAdapter";
    private ArrayList<Stock> stockList;
    private MainActivity mainAct;


    public StockAdapter(ArrayList<Stock> stockList, MainActivity mainActivity) {
        this.stockList = stockList;
        mainAct = mainActivity;
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_listview, parent, false);
       itemView.setOnClickListener(mainAct);
       itemView.setOnLongClickListener(mainAct);
        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        Stock stock = stockList.get(position);
        if(stock.getStockPriceChange() < 0) {
            holder.symbol.setTextColor(Color.RED);
            holder.symbol.setText(stock.getSymbol());
            holder.companyName.setTextColor(Color.RED);
            holder.companyName.setText(stock.getCompanyName());
            holder.priceSymbol.setTextColor(Color.RED);
            holder.priceSymbol.setText("▼");
            holder.price.setTextColor(Color.RED);
            holder.price.setText(String.valueOf(stock.getTradePrice()));
            holder.priceChange.setTextColor(Color.RED);
            holder.priceChange.setText(String.valueOf(stock.getStockPriceChange()));
            holder.changePercent.setTextColor(Color.RED);
            holder.changePercent.setText("( " + String.valueOf(stock.getStockPercentChange()) + "%)");
        }
        else{
            holder.symbol.setTextColor(Color.GREEN);
            holder.symbol.setText(stock.getSymbol());
            holder.companyName.setTextColor(Color.GREEN);
            holder.companyName.setText(stock.getCompanyName());
            holder.priceSymbol.setTextColor(Color.GREEN);
            holder.priceSymbol.setText("▲");
            holder.price.setTextColor(Color.GREEN);
            holder.price.setText(String.valueOf(stock.getTradePrice()));
            holder.priceChange.setTextColor(Color.GREEN);
            holder.priceChange.setText(String.valueOf(stock.getStockPriceChange()));
            holder.changePercent.setTextColor(Color.GREEN);
            holder.changePercent.setText("( " + String.valueOf(stock.getStockPercentChange()) + "%)");
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
