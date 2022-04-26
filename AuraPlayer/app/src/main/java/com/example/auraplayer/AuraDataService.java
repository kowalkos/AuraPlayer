package com.example.auraplayer;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class AuraDataService {
    public static final String AURA_DATA_API = "https://ids.aurafutures.com/api/v1/campaigns/7934/manifest/4";
    Context context;
    public AuraDataService(Context context)
    {
        this.context=context;
    }
    public interface VolleyResponseListener{
        void onError(String message);
        void onResponse(JSONObject response);
    }
    public void getData(VolleyResponseListener volleyResponseListener)
    {
        String url= AURA_DATA_API;
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                volleyResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    };
}
