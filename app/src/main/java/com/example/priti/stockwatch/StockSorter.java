package com.example.priti.stockwatch;

import java.util.Comparator;

public class StockSorter implements Comparator<Stock> {
    @Override
    public int compare(Stock stock1, Stock stock2) {
        return stock1.getSymbol().compareTo(stock2.getSymbol()  );
    }
}

