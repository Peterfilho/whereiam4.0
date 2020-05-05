package com.example.whereiam40;

import android.content.Context;
import android.util.Log;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TestWorker extends Worker {

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
        try {
            testPost(ra, date);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }



    private void sendData(final String user_id, final String busca, final String data) {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
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

    private void testPost(String ra, String datetime) throws ExecutionException, InterruptedException {
        //String datetime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        //Log.d("DEBUG", "testando data: "+datetime);
        //String ra = "1111112";
        String search = "-100,-72,-74,-59,-79,-100"; //B8A
//        Log.d("DEBUG", "Busca: "+search+", ra: "+ra+", Data: "+datetime);
        sendData(ra,search, datetime);
    }
}
