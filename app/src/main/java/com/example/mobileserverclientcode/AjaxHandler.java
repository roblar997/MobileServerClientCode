package com.example.mobileserverclientcode;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AjaxHandler {


    @SuppressLint("StaticFieldLeak")
    public void request(String url, String method, String requestBody, final Map<String, String> expectedKeys, final AjaxCallback callback, boolean debugOn) {
        if (debugOn) {

            AjaxResponse ajaxResponse = new AjaxResponse();
            if (expectedKeys != null) {
                for (int i = 0; i < 5; i++) {
                    JSONObject item = generateRandomItem(expectedKeys);
                    ajaxResponse.addResponseData(item);
                }
            }
            callback.onResponse(200, ajaxResponse.getResponseData());
        } else {

            new AsyncTask<String, Void, AjaxResponse>() {
                @SuppressLint("StaticFieldLeak")
                @Override
                protected AjaxResponse doInBackground(String... params) {
                    String url = params[0];
                    String method = params[1].toUpperCase();
                    String requestBody = params[2];
                    AjaxResponse ajaxResponse = new AjaxResponse();

                    try {
                        URL requestUrl = new URL(url);
                        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

                        if (method.equals("GET") || method.equals("DELETE")) {
                            connection.setRequestMethod(method);
                        } else if (method.equals("POST") || method.equals("PUT")) {
                            connection.setRequestMethod(method);
                            connection.setRequestProperty("Content-Type", "application/json");
                            connection.setDoOutput(true);

                            if (requestBody != null) {
                                OutputStream outputStream = connection.getOutputStream();
                                outputStream.write(requestBody.getBytes());
                                outputStream.flush();
                                outputStream.close();
                            }
                        }

                        int responseCode = connection.getResponseCode();
                        ajaxResponse.setResponseCode(responseCode);

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }
                            br.close();
                            String responseData = sb.toString();

                            if (expectedKeys != null) {
                                try {
                                    JSONArray jsonArray = new JSONArray(responseData);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject item = jsonArray.getJSONObject(i);
                                        if (isValidItem(item, expectedKeys)) {
                                            ajaxResponse.addResponseData(item);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    JSONObject jsonObject = new JSONObject(responseData);
                                    ajaxResponse.addResponseData(jsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        connection.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return ajaxResponse;
                }

                private boolean isValidItem(JSONObject item, Map<String, String> expectedKeys) {
                    for (String key : expectedKeys.keySet()) {
                        if (!item.has(key) || !item.optString(key).equals(expectedKeys.get(key))) {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                protected void onPostExecute(AjaxResponse result) {
                    if (callback != null) {
                        callback.onResponse(result.getResponseCode(), result.getResponseData());
                    }
                }
            }.execute(url, method, requestBody);
        }
    }

    private JSONObject generateRandomItem(Map<String, String> expectedKeys) {
        JSONObject item = new JSONObject();
        try {
            for (String key : expectedKeys.keySet()) {
                item.put(key, generateRandomValue(expectedKeys.get(key)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return item;
    }

    private Object generateRandomValue(String type) {
        switch (type) {
            case "string":
                return "test";
            case "int":
                return new Random().nextInt(100);
            case "boolean":
                return new Random().nextBoolean();
            default:
                return null;
        }
    }

    // Custom class to hold Ajax response data and code
    private static class AjaxResponse {
        private int responseCode;
        private List<JSONObject> responseData;

        public AjaxResponse() {
            responseData = new ArrayList<>();
        }

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public List<JSONObject> getResponseData() {
            return responseData;
        }

        public void addResponseData(JSONObject data) {
            responseData.add(data);
        }
    }

    // Callback interface to handle response
    public interface AjaxCallback {
        void onResponse(int responseCode, List<JSONObject> responseData);
    }
}