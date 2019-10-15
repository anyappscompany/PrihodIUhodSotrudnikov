package ua.com.anyapps.prihodiuhodsotrudnikov;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends AppCompatActivity{
    private static final String TAG = "debapp";
    private TextView tvEmployeeInitials;
    private ImageView ivEmployeePhoto;
    final int REQUEST_CODE_PHOTO = 1;
    private static final String LONG_SERVER_TIME = "LONG_SERVER_TIME";
    private  TextView tvCurrentServerTitme;
    private RadioGroup rgEmployeeAction;
    private TextView etPinCode;

    private String timeZone = "";

    Timer T = null;
    DBHelper dbHelper;

    String employeeName = "";
    String employeeSurname = "";
    int employeeId = -1;
    byte[] employeePhoto = null;
    private String employeePhotoEndodedToB64 = "";
    String localPinCode;
    String serverPinCode;

    ProgressDialog employeeAddeddialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // временная зона по умолчанию
        String[] time_zones_array = getResources().getStringArray(R.array.time_zone_values);
        timeZone = time_zones_array[0];

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        timeZone = prefs.getString("current_time_zone", "+00:00");


        tvEmployeeInitials = findViewById(R.id.tvEmployeeInitials);
        ivEmployeePhoto = findViewById(R.id.ivEmployeePhoto);
        tvCurrentServerTitme = findViewById(R.id.tvCurrentServerTitme);
        rgEmployeeAction = findViewById(R.id.rgEmployeeAction);
        etPinCode = findViewById(R.id.etPinCode);

        Bundle b = getIntent().getExtras();
         // or other values

        if(b != null) {
            String tags = b.getString("tags");
            String[] tagsArr = tags.split(":");

            employeeId = Integer.parseInt(tagsArr[0].toString());
            serverPinCode = tagsArr[1].toString();
            employeeName = b.getString("employeeName");
            employeeSurname = b.getString("employeeSurname");
        }

        tvEmployeeInitials.setText(employeeSurname + " " + employeeName);
        //Log.d(TAG, "IDID " + employeeId + " " + employeeName + " " + employeeSurname);
        ivEmployeePhoto.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.v(TAG, "photo click");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_PHOTO));
                startActivityForResult(intent, REQUEST_CODE_PHOTO);
            }
        });

        //registerReceiver(broadcastReceiver, new IntentFilter(LONG_SERVER_TIME));

        initTimer();

        //dbHelper = new DBHelper(this, DBHelper.DB_NAME, null, 1);

    }

    /*BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "DDDDDDDDDDDDDDDDDDD");
        }
    };*/

    @Override
    protected void onPause() {
        super.onPause();
        try{
            T.cancel();
            T = null;
        }catch (Exception ex){
            //
        }

    }



    private void initTimer(){
        if(T==null) {
            T = new Timer();
            T.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    //Log.d(TAG, "Время сервера " + TimerService.serverTime);

                    // доступ к UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                tvCurrentServerTitme.setText(getDate(TimerService.serverTime).toString());
                            }catch (Exception ex){
                                tvCurrentServerTitme.setText(R.string.default_server_time_text);
                            }
                        }
                    });
                }
            }, 1000, 1000);
        }
    }

    private String getDate(long time) {
        Date date = new Date(time*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));

        return sdf.format(date);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            T.cancel();
            T = null;
        }catch (Exception ex){
            //
        }
    }

    private Bitmap bitmap = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                if (intent == null) {
                    Log.d(TAG, "Intent is null");
                } else {
                    Log.d(TAG, "Photo uri: " + intent.getData());
                    Bundle bndl = intent.getExtras();
                    if (bndl != null) {
                        Object obj = intent.getExtras().get("data");
                        if (obj instanceof Bitmap) {
                            bitmap = (Bitmap) obj;
                            Log.d(TAG, "bitmap " + bitmap.getWidth() + " x "
                                    + bitmap.getHeight());
                            ivEmployeePhoto.setImageBitmap(bitmap);

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                            employeePhoto = bos.toByteArray();
                            employeePhotoEndodedToB64 = Base64.encodeToString(employeePhoto, Base64.DEFAULT);
                        }
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Canceled");
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onResume() {
        // показать фото после разворота
        Log.d(TAG, "CAMERA RESUME");
        initTimer();
        if(bitmap!=null){ Log.d(TAG, "Resum not null");
            ivEmployeePhoto.setImageBitmap(bitmap);
        }
        super.onResume();

    }

    public void btnSaveEmployeeClick(View view)
    {
        localPinCode = etPinCode.getText().toString();

        int radioButtonId = rgEmployeeAction.getCheckedRadioButtonId();
        View radioButton = rgEmployeeAction.findViewById(radioButtonId);
        int idx = rgEmployeeAction.indexOfChild(radioButton);

        // фотка есть, действие выбрано, пин верный, время получено
        if(employeePhoto!=null && localPinCode.equals(serverPinCode) && rgEmployeeAction.getCheckedRadioButtonId()!=-1 && TimerService.serverTime!=null) {
            /*Log.d(TAG, "idx " + idx);
            Log.d(TAG, "employeeid " + employeeId);
            Log.d(TAG, "localPin " + localPinCode);
            Log.d(TAG, "origPin " + serverPinCode);
            Log.d(TAG, "visittime " + TimerService.serverTime);
            Log.d(TAG, "timezone " + timeZone);
            Log.d(TAG, "comegone " + idx);
            Log.d(TAG, "employeephoto " + employeePhoto);*/

            SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + getApplicationContext().getPackageName() + "/databases/" + "prihodiuhodsotrudnikov", null,
                    SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);//dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            // 'employeeid' INTEGER, 'visittime' INTEGER, 'timezone' TEXT, 'comegone' INTEGER, 'employeephoto' BLOB)"); timeZone
            values.put("employeeid", employeeId);
            values.put("enteredpin", localPinCode);
            values.put("visittime", TimerService.serverTime);
            values.put("timezone", timeZone);
            values.put("comegone", idx);
            //values.put("employeephoto", employeePhoto);
            values.put("employeephoto", employeePhotoEndodedToB64);

            if(db.insert(DBHelper.TABLE_EMPLOYEE_ACTIVITY, null, values)==-1){
                Toast.makeText(this, getResources().getStringArray(R.array.app_errors)[5], Toast.LENGTH_SHORT).show();
            }else{
                // приход/уход записан
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                LayoutInflater ltInflater = getLayoutInflater();
                alert.setMessage(R.string.employee_added_dialog_message);
                alert.setTitle(R.string.employee_added_dialog_title);

                alert.setPositiveButton(R.string.employee_added_dialog_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                });

                /*alert.setNegativeButton(R.string.alert_dialog_negative_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //
                    }
                });*/

                alert.show();
            }
        }else {
            // выводим сообщение
            Toast.makeText(this, getResources().getStringArray(R.array.app_errors)[4], Toast.LENGTH_SHORT).show();
        }
    }
}

