package com.example.priti.stockwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    // DB Name
    private static final String DATABASE_NAME = "StockWatchAppDB";
    // DB Table Name
    private static final String TABLE_NAME = "StockWatchTable";
    ///DB Columns
    private static final String SYMBOL = "Symbol";
    private static final String COMPANY_NAME = "CompanyNqme";
    private static final String PRICE = "price";
    private static final String PRICE_CHANGE = "priceChange";
    private static final String CHANGE_PERCENT = "changePercent";

    // DB Table Create Code
    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                    SYMBOL + " TEXT not null unique," +
                    COMPANY_NAME + " TEXT not null, " +
                    PRICE + " TEXT not null, " +
                    PRICE_CHANGE + " TEXT not null, " +
                    CHANGE_PERCENT + " INT not null)";

    private SQLiteDatabase database;

    private static DatabaseHandler instance;

    public static DatabaseHandler getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHandler(context);
        return instance;
    }

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }
    public void setupDb() {
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // onCreate is only called is the DB does not exist
        Log.d(TAG, "onCreate: Creating new database");
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


    public ArrayList<Stock> loadStocks() {

        Log.d(TAG, "loadStocks: START");
        ArrayList<Stock > stockList = new ArrayList<>();

        Cursor cursor = database.query(
                TABLE_NAME,  // The table to query
                new String[]{SYMBOL, COMPANY_NAME, PRICE, PRICE_CHANGE, CHANGE_PERCENT}, // The columns to return
                null, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                null); // The sort order
        if (cursor != null) {
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                String symbol = cursor.getString(0);
                String companyName = cursor.getString(1);
                String price = cursor.getString(2);
                String priceChange = cursor.getString(3);
                String changePercent = cursor.getString(4);
                stockList.add(new Stock(symbol, companyName, Double.parseDouble(price), Double.parseDouble(priceChange), Double.parseDouble(changePercent)));
                cursor.moveToNext();
            }
            cursor.close();
            //instance = null;
        }
        Log.d(TAG, "loadCountries: DONE ");

        return stockList;
    }


    public void addStock(Stock stock) {
        Log.d(TAG, "addStock for: "+stock.getSymbol());
        ContentValues values = new ContentValues();
        values.put(SYMBOL, stock.getSymbol());
        values.put(COMPANY_NAME, stock.getCompanyName());
        values.put(PRICE, stock.getTradePrice());
        values.put(PRICE_CHANGE, stock.getStockPriceChange());
        values.put(CHANGE_PERCENT, stock.getStockPercentChange());
        long key = database.insert(TABLE_NAME, null, values);
    }

    public void updateStock(Stock stock) {
        Log.d(TAG, "updateStock for: "+stock.getSymbol());
        ContentValues values = new ContentValues();
        values.put(SYMBOL, stock.getSymbol());
        values.put(COMPANY_NAME, stock.getCompanyName());
        values.put(PRICE, stock.getTradePrice());
        values.put(PRICE_CHANGE, stock.getStockPriceChange());
        values.put(CHANGE_PERCENT, stock.getStockPercentChange());
        long key = database.update(
                TABLE_NAME, values, SYMBOL + " = ?", new String[]{stock.getSymbol()});
        Log.d(TAG, "updateStock: " + key);
    }

    //Deleting the stock from database
    public void deleteStock(String symbol)
    {
        Log.d(TAG, "deleteStock: Deleting stock "+ symbol);
        int count = database.delete(TABLE_NAME, SYMBOL + " = ?", new String[] {symbol});
        Log.d(TAG, "deleteStock: deleted "+ count);
    }

    public void dumpDbToLog() {
        Cursor cursor = database.rawQuery("select * from " + TABLE_NAME, null);
        if (cursor != null && !cursor.isClosed()) {
            cursor.moveToFirst();
            Log.d(TAG, "dumpLog: vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
            for (int i = 0; i < cursor.getCount(); i++) {
                String symbol = cursor.getString(0);
                String companyName = cursor.getString(1);
                String price = cursor.getString(2);
                String priceChange = cursor.getString(3);
                int changePercent = cursor.getInt(4);
                Log.d(TAG, "dumpLog: " +
                        String.format("%s %-18s", SYMBOL + ":", symbol) +
                        String.format("%s %-18s", COMPANY_NAME + ":", companyName) +
                        String.format("%s %-18s", PRICE + ":", price) +
                        String.format("%s %-18s", PRICE_CHANGE + ":", priceChange) +
                        String.format("%s %-18s", CHANGE_PERCENT + ":", changePercent));
                cursor.moveToNext();
            }
            cursor.close();
            //instance = null;
        }
        Log.d(TAG, "dumpLog: ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    }

    public void shutDown() {
        //database.close();
    }

}
