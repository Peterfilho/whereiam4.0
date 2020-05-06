package com.example.whereiam40;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ScanService extends IntentService {

    private WifiManager wifiManager;
    private boolean running = true;
    private List<ScanResult> results;
    private ArrayList<String> arraylist = new ArrayList<>();
    private BroadcastReceiver wifiReceiver;
    private Integer sleepTime = 5;
    private Integer scanTimes = 4;
    private ArrayList<ScanSignal> referenceSignals = new ArrayList<>();

    public ScanService() {super ("ScanService");}

    @Override
    protected void onHandleIntent (Intent intent) {

        synchronized (this) {

            while (running) {
                String ra = intent.getStringExtra("ra");
                String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                try {
                    testPost(ra,date);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
    //            scanWifi();

                try {
                    wait(1000*60*3);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy (){

        running = false;

        super.onDestroy();

    }


    private void scanWifi () {

        Toast.makeText(this, "Procurando redes ..", Toast.LENGTH_SHORT).show();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        LocationManager service = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

//        boolean gpsEnabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        /* VERIFICA SE O GPS ESTÁ LIGADO */
//        if (!gpsEnabled) {
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(intent);
//        }

        arraylist.clear();

        HashMap<String, ArrayList<Integer>> result = new HashMap<String, ArrayList<Integer>>();
        HashMap<String, Float> resultMedia = new HashMap<String, Float>();

        for (int scanIndex = 0; scanIndex < scanTimes; scanIndex++) {
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            wifiManager.startScan();

            results = wifiManager.getScanResults();

            //WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            Log.d("DEBUG", results.size() + "");

            for (ScanResult scanResult : results) {

                String tokens[] = scanResult.BSSID.split(":");
                String finalMac = tokens[tokens.length - 1];

                //String finalMac = scanResult.BSSID;

                if (!result.containsKey(finalMac)) {
                    result.put(finalMac, new ArrayList<Integer>());
                    resultMedia.put(finalMac, 0.f);
                }

                result.get(finalMac).add(scanResult.level);
                resultMedia.put(finalMac, resultMedia.get(finalMac) + scanResult.level);
                Log.d("DEBUG", "MAC: " + finalMac + " e dBm:  " + scanResult.level);
            }

            try {
                TimeUnit.SECONDS.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (String hashKey : resultMedia.keySet())
            resultMedia.put(hashKey, resultMedia.get(hashKey) / scanTimes);

        //ScanSignal currentSignal = new ScanSignal();


       // Log.d("DEBUG", currentSignal.toString());


//        for (String hashKey : result.keySet()) {
//            Log.d("DEBUG", "Hashmap key:" + hashKey);
//
//            int i = 0;
//            for (Integer level : result.get(hashKey)) {
//                Log.d("DEBUG", "   Level (scan " + (++i) + ") : " + level);
//            }
//            Log.d("DEBUG", "   Média: " + resultMedia.get(hashKey));
//
//            switch (hashKey) {
//                case "b2":
//                    Log.d("DEBUG", "entrando no first case");
//                    tsi = resultMedia.get(hashKey).toString();
//                    break;
//
//                case "ff":
//
//                    Log.d("DEBUG", "entrando no second case");
//                    c7 = resultMedia.get(hashKey).toString();
//                    break;
//
//                case "f5":
//
//                    Log.d("DEBUG", "entrando no third case");
//                    f5 = resultMedia.get(hashKey).toString();
//                    break;
//
//                case "d0":
//
//                    Log.d("DEBUG", "entrando no fourth case");
//                    d0 = resultMedia.get(hashKey).toString();
//                    break;
//
//                case "f6":
//                    f6 = resultMedia.get(hashKey).toString();
//                    break;
//
//                case "c8":
//                    c8 = resultMedia.get(hashKey).toString();
//                    break;
//
//                case "le":
//                    tsi = resultMedia.get(hashKey).toString();
        //         }
        //       }



//        /*  EXIBINDO REDES DISPONÍVEIS EM UMA LISTVIEW DO APP */
//        for (ScanResult scanResult : results) {
//
//            arraylist.add(
//                    scanResult.SSID +
//                            "\n média do sinal dBm:\n  " + scanResult.level +
//                            "\n MAC: \n" + scanResult.BSSID);
//            //"\n Chave: " + scanResult.capabilities);
//            adapter.notifyDataSetChanged();

    }

    private void sendData(final String user_id, final String busca, final String data) {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://200.134.18.125:5000/api/v1/resources/positions/app";

        JSONObject js = new JSONObject();
        try {
            js.put("user_id", user_id);
            js.put("search", busca);
            js.put("date", data);
            Log.d("DEBUG json: ", "" + js);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, js,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DEBUG: ", response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error: ", "" + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void testPost(String ra, String datetime) throws ExecutionException, InterruptedException {
        //String datetime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        //Log.d("DEBUG", "testando data: "+datetime);
        //String ra = "1111112";
        String search = "-100,-72,-74,-59,-79,-100"; //B8A
        Log.d("DEBUG", "Busca: "+search+", ra: "+ra+", Data: "+datetime);
        sendData(ra,search, datetime);
    }
}

