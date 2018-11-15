package com.example.priti.stockwatch;

public class Stock {
    String symbol;
    String companyName;
    double tradePrice;
    double stockPriceChange;
    double stockPercentChange;

    public Stock(String symbol, String companyName, double tradePrice, double stockPriceChange, double stockPercentChange) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.tradePrice = tradePrice;
        this.stockPriceChange = stockPriceChange;
        this.stockPercentChange = stockPercentChange;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(double tradePrice) {
        this.tradePrice = tradePrice;
    }

    public double getStockPriceChange() {
        return stockPriceChange;
    }

    public void setStockPriceChange(double stockPriceChange) {
        this.stockPriceChange = stockPriceChange;
    }

    public double getStockPercentChange() {
        return stockPercentChange;
    }

    public void setStockPercentChange(double stockPercentChange) {
        this.stockPercentChange = stockPercentChange;
    }
}
