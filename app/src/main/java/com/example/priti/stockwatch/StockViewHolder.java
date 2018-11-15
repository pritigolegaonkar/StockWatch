package com.example.priti.stockwatch;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class StockViewHolder extends RecyclerView.ViewHolder {


    public TextView symbol;
    public TextView companyName;
    public TextView priceSymbol;
    public TextView price;
    public TextView priceChange;
    public TextView changePercent;


    public StockViewHolder(View itemView) {
        super(itemView);
        symbol = (TextView) itemView.findViewById(R.id.stockSymbol);
        companyName = (TextView) itemView.findViewById(R.id.companyName);
      priceSymbol = (TextView) itemView.findViewById(R.id.priceArrow);
        price = (TextView) itemView.findViewById(R.id.stockPrice);
        priceChange = (TextView) itemView.findViewById(R.id.stockPriceChange);
        changePercent = (TextView) itemView.findViewById(R.id.percentChange);


    }
}
