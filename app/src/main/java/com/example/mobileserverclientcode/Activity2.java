package com.example.mobileserverclientcode;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileserverclientcode.AjaxHandler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity2);

        // Get the TextView from the layout
        TextView textView = findViewById(R.id.textViewActivity2);

        // Expected keys for Activity2textViewActivity3
        Map<String, String> expectedKeys = new HashMap<>();
        expectedKeys.put("ja ", "string");
        expectedKeys.put("nei", "int");
        expectedKeys.put("treet", "int");

        // Call the AjaxHandler to get random data
        AjaxHandler ajaxHandler = new AjaxHandler();
        ajaxHandler.request("your_url_here", "GET", null, expectedKeys, new AjaxHandler.AjaxCallback() {
            @Override
            public void onResponse(int responseCode, List<JSONObject> responseData) {
                StringBuilder dataBuilder = new StringBuilder();

                // Append the expected keys with their corresponding types
                dataBuilder.append("Expected Keys:\n");
                for (Map.Entry<String, String> entry : expectedKeys.entrySet()) {
                    String key = entry.getKey();
                    String type = entry.getValue();
                    dataBuilder.append(key).append(": ").append(type).append("\n");
                }
                dataBuilder.append("\n");

                // Process the JSON data
                for (JSONObject item : responseData) {
                    dataBuilder.append("Item:\n");
                    for (Map.Entry<String, String> entry : expectedKeys.entrySet()) {
                        String key = entry.getKey();
                        String type = entry.getValue();

                        // Check if the key is present in the JSON response
                        if (item.has(key)) {
                            // Append the key-value pair to the dataBuilder
                            String value = item.optString(key);
                            dataBuilder.append(key).append(": ").append(value).append(", Type: ").append(type).append("\n");
                        } else {
                            // Append a message indicating the key is not present
                            dataBuilder.append(key).append(": ").append("Not Found").append(", Type: ").append(type).append("\n");
                        }
                    }
                    dataBuilder.append("\n");
                }

                textView.setText(dataBuilder.toString());
            }
        }, true); // Use debug mode with random data
    }
}