package com.example.friendsfind.friendsfindme;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * Created by nicholaszetzl on 10/18/15.
 */
class GetRequest extends AsyncTask<String, Void, String> {
    private Exception exception;

    protected String doInBackground(String... request) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        try {
            response = httpclient.execute(new HttpGet(request[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    response.getEntity().writeTo(out);
                    String responseString = out.toString();
                    out.close();
                    return responseString;
                } catch (IOException e) {
                    Log.v("high", String.valueOf(e));
                }

            } else {
                //Closes the connection.
                try {
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                } catch (IOException e) {
                    Log.v("high", String.valueOf(e));
                }
            }
        } catch(IOException e) {
            Log.v("high", String.valueOf(e));
            return "";
        }
        return "";
    }
}
