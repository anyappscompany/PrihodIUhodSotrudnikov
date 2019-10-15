package ua.com.anyapps.prihodiuhodsotrudnikov;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class PreSettingsActivity extends AppCompatActivity {

    private EditText preSettingsAccessKey;
    private SharedPreferences spPreferences;
    private String accessKey;
    private static final String TAG = "debapp";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_settings);

        preSettingsAccessKey = findViewById(R.id.etPreSettingsAccessKey);
        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        accessKey = spPreferences.getString("access_key", "");
    }

    public void btnEnterToSettingsClick(View view) {
        Log.d(TAG, "ET: " + preSettingsAccessKey.getText() + " PREF: " + accessKey);

        if(preSettingsAccessKey.getText().toString().equals(accessKey)){
            Log.d(TAG, "Значения равны");
            Intent intent = new Intent(this, SettingsForAdminActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
