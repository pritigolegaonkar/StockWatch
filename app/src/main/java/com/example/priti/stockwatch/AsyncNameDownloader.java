package com.example.priti.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class AsyncNameDownloader extends AsyncTask<String, Integer, String> {

    private static final String TAG = "AsyncNameDownloader";

    private MainActivity mainActivity;
    private final String dataURL = "https://api.iextrading.com/1.0/ref-data/symbols";
    private HashMap<String, String> data = new HashMap<>();


    public AsyncNameDownloader(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: ");
        data = parseJSONData(s);

    }

    @Override
    protected String doInBackground(String... params) {

        Uri.Builder buildURL = Uri.parse(dataURL).buildUpon();
        String completeURL=buildURL.build().toString();
        Log.d(TAG, "doInBackground: " + completeURL);

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
            Log.e(TAG, "doInBackground: In Exception ", e);
            e.printStackTrace();
        }
        Log.d(TAG, "doInBackground: " + sb.toString());
        return sb.toString();
    }

    private HashMap<String, String> parseJSONData(String data) {
        HashMap<String, String> dataHashMap = new HashMap<>();
        try {
            JSONArray dataArray = new JSONArray(data);
            for(int i=0;i<dataArray.length();i++){
                JSONObject object = dataArray.getJSONObject(i);
                dataHashMap.put(object.get("symbol").toString(), object.get("name").toString());
            }

        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return dataHashMap;
    }

    public HashMap<String, String> getSymbols( String enteredSymbol) {
        HashMap<String, String> matchedHashMap = new HashMap<>();
        for(HashMap.Entry<String, String> entry : this.data.entrySet()){
            if(entry.getKey().startsWith(enteredSymbol)){
                String symbol = entry.getKey();
                String name = entry.getValue();
                matchedHashMap.put(symbol, name);
            }
        }
        return matchedHashMap;
    }
}


