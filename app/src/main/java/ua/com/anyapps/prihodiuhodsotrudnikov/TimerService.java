package ua.com.anyapps.prihodiuhodsotrudnikov;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.anyapps.prihodiuhodsotrudnikov.Server.ServerConnect;
import ua.com.anyapps.prihodiuhodsotrudnikov.Server.UploadEmployee.UploadEmployee;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TimerService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "ua.com.anyapps.prihodiuhodsotrudnikov.action.FOO";
    private static final String ACTION_BAZ = "ua.com.anyapps.prihodiuhodsotrudnikov.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "ua.com.anyapps.prihodiuhodsotrudnikov.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "ua.com.anyapps.prihodiuhodsotrudnikov.extra.PARAM2";

    private static final String SERVER_TIME_RECEIVED = "SERVER_TIME_RECEIVED";
    private static final String LONG_SERVER_TIME = "LONG_SERVER_TIME";
    private static final String TAG = "debapp";

    public static Long serverTime = null;

    public static boolean activeService = true;
    public int counter = 0;



    private Context receiveContext = null;

    DBHelper dbHelper;
    SQLiteDatabase db = null;
    Cursor cursor = null;

    Timer T;
    byte[] employeePhotoBytes = null; // фото сотрудника из базы
    String employeePhotoEndodedToB64 = ""; // фото сотрудника из базы
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            receiveContext = context;
            // internet lost alert dialog method call from here...
            serverTime = Long.parseLong(intent.getStringExtra("server_time"));
            Log.d(TAG, "8888888888" + serverTime);
            try{
                T.cancel();
                T = null;
            }catch (Exception ex){
                //
            }
            T = new Timer();
            T.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    serverTime = serverTime+1;
                    counter++;
                    //Log.d(TAG, "Server Time in Sec " + serverTime);
                    if(counter>=600){
                        //
                        Log.d(TAG, "Сработал TimerService (10min)");
                        counter = 0;

                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_EMPLOYEE_ACTIVITY, null);
                        if(cursor.moveToFirst()) {
                            do {
                                Log.d(TAG, "Выборка " + cursor.getString(cursor.getColumnIndex("employeeid")));
                                employeePhotoBytes = cursor.getString(cursor.getColumnIndex("employeephoto")).getBytes();
                                employeePhotoEndodedToB64 = cursor.getString(cursor.getColumnIndex("employeephoto"));

                                //Log.d(TAG, "Отправка " + employeePhotoEndodedToB64);

                                RequestBody body = RequestBody.create(MediaType.parse("image/*"), employeePhotoBytes);

                                ServerConnect.getInstance()
                                        .getJSONApi()
                                        .uploadEmployees(body, "{\"_id\":\""+cursor.getString(cursor.getColumnIndex("_id"))+"\", \"employeeid\":\""+cursor.getString(cursor.getColumnIndex("employeeid"))+"\", \"enteredpin\":\""+cursor.getString(cursor.getColumnIndex("enteredpin"))+"\", \"visittime\":\""+cursor.getString(cursor.getColumnIndex("visittime"))+"\", \"timezone\":\""+cursor.getString(cursor.getColumnIndex("timezone"))+"\", \"comegone\":\""+cursor.getString(cursor.getColumnIndex("comegone"))+"\"}")
                                        .enqueue(new Callback<UploadEmployee>() {
                                            @Override
                                            public void onResponse(Call<UploadEmployee> call, Response<UploadEmployee> response) {

                                                Log.d(TAG, "Во время отправки сотрудника, сервер вернул данные: " + response.code() + " RESPONSE: " + response.toString() + " BODY: " + response.body().toString());

                                                if (response.isSuccessful()) {
                                                    UploadEmployee uploadEmployeeInfo = response.body();
                                                    // сохранение ключа доступа в настройках и переход на гланое актиивити
                                                    //Log.d(TAG, "Во время отправки сотрудника, сервер успешно вернул данные: " + response.code() + " RESPONSE: " + response.toString() + " BODY: " + response.body().toString());
                                                    if (uploadEmployeeInfo.getSucess()) {
                                                        Log.d(TAG, "EMPLOYEE UPLOADED");

                                                        int delCount = db.delete(DBHelper.TABLE_EMPLOYEE_ACTIVITY, "_id = " + uploadEmployeeInfo.getData(), null);
                                                        Log.d(TAG, "Удалено записей = " + delCount);
                                                        // обработка списка
                                                        /*employeesArr = employeesInfo.getData();
                                                        for(int i = 0; i<employeesArr.length; i++){
                                                            Log.d(TAG, "Employe: " + employeesArr[i].getSurname());
                                                        }
                                                        if(employeesListAdapter == null){
                                                            employeesListAdapter = new EmployeesListAdapter(getApplicationContext(), employeesArr);
                                                        }
                                                        if(lvEmployeesList == null){
                                                            lvEmployeesList = (ListView) findViewById(R.id.lvEmployeesList);

                                                            // выбранный сотрудник Tag - id сотрудника по базе
                                                            lvEmployeesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                                @Override
                                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                                    //Log.d(TAG, "Your favorite : " + view.getTag());
                                                                    Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                                                                    Bundle b = new Bundle();
                                                                    b.putString("tags", view.getTag().toString());
                                                                    b.putString("employeeName", employeesArr[position].getName());
                                                                    b.putString("employeeSurname", employeesArr[position].getSurname());
                                                                    intent.putExtras(b);
                                                                    startActivity(intent);
                                                                    //finish();
                                                                }
                                                            });
                                                        }
                                                        lvEmployeesList.setAdapter(employeesListAdapter);*/
                                                    }


                                                } else {
                                                    Log.e(TAG, "Во время отправки сотрудника сотрудников, сервер вернул ошибку " + response.code());
                                                    //Toast.makeText(MainActivity.this, getResources().getStringArray(R.array.app_errors)[2] + " " + response.code(), Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<UploadEmployee> call, Throwable t) {
                                                Log.e(TAG, "Отказ. Не удалось достучаться до сервера во время отправки сотрудника - " + t);
                                                //Toast.makeText(MainActivity.this, getResources().getStringArray(R.array.app_errors)[3], Toast.LENGTH_LONG).show();
                                            }
                                        });

                            }while(cursor.moveToNext());
                        }
                        /*Intent broadcastedIntent = new Intent(LONG_SERVER_TIME);
                        broadcastedIntent.putExtra("server_time2", "44444444444444444"); //Long.parseLong(serverTimeInfo.getData().getServer_time())
                        receiveContext.sendBroadcast(broadcastedIntent);*/


                    }
                }
            }, 1000, 1000);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Сервис OnCreate");

        //dbHelper = new DBHelper(this, DBHelper.DB_NAME, null, 1);
        db = SQLiteDatabase.openDatabase("/data/data/" + getApplicationContext().getPackageName() + "/databases/" + "prihodiuhodsotrudnikov", null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);

        registerReceiver(broadcastReceiver, new IntentFilter(SERVER_TIME_RECEIVED));
    }

    public TimerService() {
        super("TimerService");


        Log.d(TAG, "TimerService конструктор");
        //registerReceiver(broadcastReceiver, new IntentFilter(SERVER_TIME_RECEIVED));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            T.cancel();
            T = null;
        }catch (Exception ex){
            //
        }

        if(db!=null){
            db.close();
        }

        if(cursor!=null){
            cursor.close();
        }
        Log.d(TAG, "Сервис отключился");
        //unregisterReceiver(broadcastReceiver);
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, TimerService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, TimerService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //SystemClock.sleep(90000);
        while(activeService){}

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
