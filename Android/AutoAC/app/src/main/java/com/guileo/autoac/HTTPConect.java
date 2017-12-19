package com.guileo.autoac;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by Guilherme on 04/10/2017.
 */
public class HTTPConect {

    private String server;
    private Context context;
    private RequestQueue queue;
    private int temp;
    final static int MAX_TEMP = 30;
    final static int MIN_TEMP = 18;
    private TextView status;

    public HTTPConect(Context context, TextView status, String server) {
        this.context = context;
        this.server = server;
        this.queue = Volley.newRequestQueue(context);
        this.temp = 25;
        this.status = status;
    }

    public int getTemp() {
        return temp;
    }

    public void request(String GET, final Callback cb) {
        status.setText("Aguarde...");
        // Instantiate the RequestQueue.
        String url = this.server + GET;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        status.setText("OK!");
                        if(cb != null) {
                            cb.run(response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final String msg = "Servidor sem resposta :(";
                status.setText(msg);
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(1300, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void getTemp(final Callback cb){
        request("/temp", cb);
    }

    public void setOnOff(String onOff,  final Callback cb){
        request("/ac/power/" + onOff, cb);
    }

    private void setTemp(int temp, final Callback cb) {
        request("/ac/temp/" + String.valueOf(temp), cb);
    }

    public void incTemp(int diff, final Callback cb) {
        this.temp += diff;
        if(this.temp < MIN_TEMP){
            this.temp = MIN_TEMP;
        } else if(this.temp > MAX_TEMP){
            this.temp = MAX_TEMP;
        }
        setTemp(this.temp, cb);
    }

    public void getAgenda(final Callback cb){
        request("/cronjob/get", cb);
    }

    public void addAgendamento(String agendamento, final Callback cb){
        request(agendamento, cb);
    }

    public void excluirAgendamento(int id, final Callback cb){
        request("/cronjob/remove/" + id, cb);
    }
}
