package ua.com.anyapps.prihodiuhodsotrudnikov;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import ua.com.anyapps.prihodiuhodsotrudnikov.Server.ServerConnect;

public class StartActivity extends AppCompatActivity {
    private static final String TAG = "debapp";
    private SharedPreferences spPreferences;
    private SharedPreferences.Editor prefEditor;
    private String secretKey;
    private RadioButton rbActivation;
    private RadioButton rbSignIn;
    private RadioGroup rgAction;
    private String accessKey;
    private Button btnStart;
    private EditText etAccessKey;
    private static final int ACTION_ACTIVATION = 0;
    private static final int ACTION_LOG_IN = 1;
    private static final int ACTION_NOT_CHOSEN = -1;
    private int getAccessKeyOrLogin = -1;



    ProgressDialog dialog;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // выбор по умолчанию
        getAccessKeyOrLogin = ACTION_ACTIVATION;

        // если ключ уже есть в настройках, то перейти на стартовое активити
        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        prefEditor = spPreferences.edit();
        accessKey = spPreferences.getString("access_key", "");
        /*if(accessKey.length()>0){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }*/
        checkAccessKey();

        rbActivation = findViewById(R.id.rbActivation);
        rbSignIn = findViewById(R.id.rbSignIn);
        rgAction = findViewById(R.id.rgAction);
        btnStart = findViewById(R.id.btnStart);
        etAccessKey = findViewById(R.id.etDialogAccessKey);

        rgAction.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                newAction(checkedId);
            }
        });
        newAction(rgAction.getCheckedRadioButtonId());


        // создание базы
        dbHelper = new DBHelper(this, DBHelper.DB_NAME, null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
    }





    // если ключ есть, то вход
    private void checkAccessKey(){
        accessKey = spPreferences.getString("access_key", "");
        if(accessKey.length()>0){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void showLoaderDialog(){
        dialog = new ProgressDialog(StartActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.connect_dialog_title);
        dialog.setMessage(getResources().getString(R.string.connect_dialog_message));
        dialog.show();
    }

    // выбор действия
    private void newAction(int _checkedId){
        View selectedRadioButton = rgAction.findViewById(_checkedId);
        int selectedRadioButtonIndex = rgAction.indexOfChild(selectedRadioButton);

        switch (selectedRadioButtonIndex){
            // активация
            case 0:
                Log.d(TAG, "Активация");
                etAccessKey.getText().clear();
                etAccessKey.setVisibility(View.GONE);
                btnStart.setText(R.string.btn_generate_access_key_text);
                getAccessKeyOrLogin = ACTION_ACTIVATION;

                //etAccessKey
                break;
            // вход
            case 1:
                Log.d(TAG, "Вход");
                //etAccessKey
                etAccessKey.setVisibility(View.VISIBLE);
                etAccessKey.getText().clear();
                btnStart.setText(R.string.btn_sign_in_text);
                getAccessKeyOrLogin = ACTION_LOG_IN;
                break;
            default:
                Log.d(TAG, "Не выбрано");
                getAccessKeyOrLogin = ACTION_NOT_CHOSEN;
                break;
        }
    }

    private void alertDialogShowAccessKey(String _newAccessKey){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LayoutInflater ltInflater = getLayoutInflater();
        final View view = ltInflater.inflate(R.layout.alert_dialog_show_access_key_layout, null);
        final String access_key = _newAccessKey;

        final EditText etDialogAccessKey = (EditText) view.findViewById(R.id.etDialogAccessKey);
        etDialogAccessKey.setText(_newAccessKey);
        //etDialogAccessKey.setInputType(InputType.TYPE_NULL);

        //final EditText edittext = new EditText(this);
        alert.setMessage(R.string.alert_dialog_show_access_key_message);
        alert.setTitle(R.string.alert_dialog_show_access_key_title);

        alert.setView(view);

        alert.setPositiveButton(R.string.alert_dialog_positive_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // если "продолжить", то сохранить ключ в настройках
                prefEditor = spPreferences.edit();
                prefEditor.putString("access_key", access_key);
                prefEditor.commit();
                checkAccessKey();
            }
        });

        alert.setNegativeButton(R.string.alert_dialog_negative_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //
            }
        });

        alert.show();
    }
    public void btnStartClick(View view) {
        switch(getAccessKeyOrLogin){
            case ACTION_ACTIVATION:
                Log.d(TAG, "ACTION_ACTIVATION");
                showLoaderDialog();
                ServerConnect.getInstance()
                        .getJSONApi()
                        .getNewAccessKey()
                        .enqueue(new Callback<ua.com.anyapps.prihodiuhodsotrudnikov.Server.AccessKey.AccessKey>() {
                            @Override
                            public void onResponse(Call<ua.com.anyapps.prihodiuhodsotrudnikov.Server.AccessKey.AccessKey> call, Response<ua.com.anyapps.prihodiuhodsotrudnikov.Server.AccessKey.AccessKey> response) {
                                dialog.dismiss();
                                if(response.isSuccessful()) {
                                    ua.com.anyapps.prihodiuhodsotrudnikov.Server.AccessKey.AccessKey accessKeyInfo = response.body();
                                    // сохранение ключа доступа в настройках и переход на гланое актиивити
                                    if(accessKeyInfo.getSucess()) {
                                        // диалог с ключом
                                        String newAccessKey = "";
                                        newAccessKey = accessKeyInfo.getData().getAccess_key();
                                        alertDialogShowAccessKey(newAccessKey);
                                    }

                                    Log.d(TAG, "Сервер успешно вернул данные во время получения ключа доступа: " + response.code() + " RESPONSE: " + response.toString() + " BODY: " + response.body().toString());
                                }else{
                                    Log.e(TAG, "Во время получения ключа доступа сервер вернул ошибку " + response.code());
                                    Toast.makeText(StartActivity.this, getResources().getStringArray(R.array.app_errors)[0] + " " + response.code(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ua.com.anyapps.prihodiuhodsotrudnikov.Server.AccessKey.AccessKey> call, Throwable t) {
                                dialog.dismiss();
                                Log.e(TAG, "Отказ. Не удалось достучаться до сервера во время получения ключа доступа - " + t);
                                Toast.makeText(StartActivity.this, getResources().getStringArray(R.array.app_errors)[1], Toast.LENGTH_LONG).show();
                            }
                        });
                break;
            case ACTION_LOG_IN:
                Log.d(TAG, "ACTION_LOG_IN");

                String loginAccessKey = "";
                loginAccessKey = etAccessKey.getText().toString();

                if(loginAccessKey.length()==15) {
                    showLoaderDialog();
                    ServerConnect.getInstance()
                            .getJSONApi()
                            .deviceLogin(etAccessKey.getText().toString())
                            .enqueue(new Callback<ua.com.anyapps.prihodiuhodsotrudnikov.Server.DeviceLogin.AccessKey>() {
                                @Override
                                public void onResponse(Call<ua.com.anyapps.prihodiuhodsotrudnikov.Server.DeviceLogin.AccessKey> call, Response<ua.com.anyapps.prihodiuhodsotrudnikov.Server.DeviceLogin.AccessKey> response) {
                                    dialog.dismiss();
                                    //Log.d(TAG, "Сервер успешно вернул данные во время получения ключа доступа: " + response.code() + " RESPONSE: " + response.toString() + " BODY: " + response.body().toString());
                                    //if(true) return;
                                    if (response.isSuccessful()) {
                                        ua.com.anyapps.prihodiuhodsotrudnikov.Server.DeviceLogin.AccessKey accessKeyInfo = response.body();
                                        // сохранение ключа доступа в настройках и переход на гланое актиивити
                                        Log.d(TAG, "Во время входа, сервер успешно вернул данные: " + response.code() + " RESPONSE: " + response.toString() + " BODY: " + response.body().toString());
                                        if (accessKeyInfo.getSucess()) {
                                            prefEditor = spPreferences.edit();
                                            prefEditor.putString("access_key", accessKeyInfo.getData().getAccess_key());
                                            prefEditor.commit();
                                            checkAccessKey();
                                        }


                                    } else {
                                        Log.e(TAG, "Во время входа, сервер вернул ошибку " + response.code());
                                        Toast.makeText(StartActivity.this, getResources().getStringArray(R.array.app_errors)[2] + " " + response.code(), Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ua.com.anyapps.prihodiuhodsotrudnikov.Server.DeviceLogin.AccessKey> call, Throwable t) {
                                    dialog.dismiss();
                                    Log.e(TAG, "Отказ. Не удалось достучаться до сервера во время активации - " + t);
                                    Toast.makeText(StartActivity.this, getResources().getStringArray(R.array.app_errors)[3], Toast.LENGTH_LONG).show();
                                }
                            });
                }
                Log.d(TAG, etAccessKey.getText() + "");
                break;
            case ACTION_NOT_CHOSEN:
                Log.d(TAG, "ACTION_NOT_CHOSEN");
                break;
            default:
                Log.d(TAG, "ACTION_NOT_CHOSEN");
                break;
        }
    }
}
