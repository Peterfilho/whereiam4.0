package com.example.whereiam40;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TestWorker extends Worker {

    private float tsi;
    private float c7;
    private float f5;
    private float d0;
    private float f6;
    private float c8;

    private WifiManager wifiManager;
    private boolean running = true;
    private List<ScanResult> results;
    private ArrayList<String> arraylist = new ArrayList<>();
    private BroadcastReceiver wifiReceiver;
    private Integer sleepTime = 5;
    private Integer scanTimes = 4;
    private ArrayList<ScanSignal> referenceSignals = new ArrayList<>();

    public TestWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

//        Log.d("DEBUG: ", "Serviço worker startando");
        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String ra = getInputData().getString("ra");
//        Log.d("DEBUG", "ra que chega: "+ra);
        ScanSignal busca = scanWifi();

        sendData(ra,busca.toString(),date);

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }

    private void sendData(final String user_id, final String busca, final String date) {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://200.134.18.125:5000/api/v1/resources/positions/app";

        JSONObject js = new JSONObject();
        try {
            js.put("user_id", user_id);
            js.put("search", busca);
            js.put("date", date);
            Log.d("DEBUG json: ", "" + js);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, js,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.d("DEBUG: ", response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.d("Error: ", "" + error.getMessage());
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


    private ScanSignal scanWifi () {

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        arraylist.clear();

        HashMap<String, ArrayList<Integer>> result = new HashMap<String, ArrayList<Integer>>();
        HashMap<String, Float> resultMedia = new HashMap<String, Float>();

        for (int scanIndex = 0; scanIndex < scanTimes; scanIndex++) {
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            wifiManager.startScan();

            results = wifiManager.getScanResults();

            //WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            Log.d("DEBUG", "Redes encontradas: "+results.size() + "");

            for (ScanResult scanResult : results) {

                String tokens[] = scanResult.BSSID.split(":");
                String finalMac = tokens[tokens.length - 1];

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

        //    Ordem da média de sinal
        //    Média TSI-2A	Média C7	Média F5	Média D0	Média F6	Média C8

        for (String hashKey : result.keySet()) {
            Log.d("DEBUG", "Hashmap key:" + hashKey);

            int i = 0;
            for (Integer level : result.get(hashKey)) {
                Log.d("DEBUG", "   Level (scan " + (++i) + ") : " + level);
            }
            Log.d("DEBUG", "   Média: " + resultMedia.get(hashKey));

            switch (hashKey) {
                case "2a":
                    tsi = resultMedia.get(hashKey);
                    break;

                case "c7":
                    c7 = resultMedia.get(hashKey);
                    break;

                case "f5":
                    f5 = resultMedia.get(hashKey);
                    break;

                case "d0":
                    d0 = resultMedia.get(hashKey);
                    break;

                case "f6":
                    f6 = resultMedia.get(hashKey);
                    break;

                case "c8":
                    c8 = resultMedia.get(hashKey);
                    break;

                 }
               }

        //Aqui monta a lista de sinais na ordem correta

        ScanSignal currentSignal = new ScanSignal(
                (int)Math.round(tsi),
                (int)Math.round(c7),
                (int)Math.round(f5),
                (int)Math.round(d0),
                (int)Math.round(f6),
                (int)Math.round(c8)
        );

        Log.d("DEBUG",  "Anterior: "+currentSignal.toString());
        ScanSignal currentSignalFake = new ScanSignal(

        //      -66,-76,-78,-62,-79,-81   //b6a
        //      -70,-60,-61,-69,-78,-72   //b9b
        //      -100,-67,-63,-49,-53,-48  //wc-m

                -66,
                -76,
                -78,
                -62,
                -79,
                -81
        );

        Log.d("DEBUG", "Fake: "+ currentSignalFake.toString());

        return currentSignalFake;
    }

    private void registerReceiver(BroadcastReceiver wifiReceiver, IntentFilter intentFilter) {
    }

}
