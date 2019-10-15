package ua.com.anyapps.prihodiuhodsotrudnikov;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.anyapps.prihodiuhodsotrudnikov.Server.ServerConnect;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "debapp";
    private SharedPreferences spPreferences;
    private SharedPreferences.Editor prefEditor;
    private String accessKey;
    ProgressDialog dialog;
    EmployeesListAdapter employeesListAdapter = null;
    ListView lvEmployeesList = null;

    private Long updateInterval;
    private static final int SERVER_TIME = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        accessKey = spPreferences.getString("access_key", "");

        fillEmployeesList();

        Log.d(TAG, "create");
        TimerService.activeService = true;
        // создание планировщика для получения времени
        createUpdateService();

        startService(new Intent(this, TimerService.class));
    }

    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    private void createUpdateService(){
        updateInterval = Long.parseLong(getString(R.string.default_delay_update_service));

        //проверить существует ли задание
        //определить время запуска: при установке или загрузке
        Intent i = new Intent(this, UpdateService.class);
        Boolean alarmup=(PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_NO_CREATE)!=null);

        if(!alarmup) {
            Intent intent = new Intent(this, UpdateService.class);
            pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), SERVER_TIME, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), updateInterval, pendingIntent);
            Log.d(TAG, "Создано задание в планировщике" );
        }else{
            Log.d(TAG, "Планировщик не создан т.к. уже создан" );
        }
    }

    @Override
    protected void onDestroy() {
        try {
            Log.d(TAG, "Destroy");
            alarmManager.cancel(pendingIntent);
            stopService(new Intent(this,TimerService.class));
            TimerService.activeService = false;
        }catch (Exception ex){

        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_update_employees:
                fillEmployeesList();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, PreSettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resume");




    }

    // заполнение списка сотрудниками организации
    ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Data employeesArr[];
    private void fillEmployeesList(){
        if(TextUtils.isEmpty(accessKey)){
            return;
        }

        // получение списка сотрудников
        showLoaderDialog();
        ServerConnect.getInstance()
                .getJSONApi()
                .getEmployees(accessKey)
                .enqueue(new Callback<ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Employees>() {
                    @Override
                    public void onResponse(Call<ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Employees> call, Response<ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Employees> response) {
                        dialog.dismiss();
                        //Log.d(TAG, "Сервер успешно вернул данные во время получения ключа доступа: " + response.code() + " RESPONSE: " + response.toString() + " BODY: " + response.body().toString());
                        //if(true) return;
                        if (response.isSuccessful()) {
                            ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Employees employeesInfo = response.body();
                            // сохранение ключа доступа в настройках и переход на гланое актиивити
                            Log.d(TAG, "Во время получения списка сотрудников, сервер успешно вернул данные: " + response.code() + " RESPONSE: " + response.toString() + " BODY: " + response.body().toString());
                            if (employeesInfo.getSucess()) {
                                // обработка списка
                                employeesArr = employeesInfo.getData();
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
                                lvEmployeesList.setAdapter(employeesListAdapter);
                            }


                        } else {
                            Log.e(TAG, "Во время получения списка сотрудников, сервер вернул ошибку " + response.code());
                            Toast.makeText(MainActivity.this, getResources().getStringArray(R.array.app_errors)[2] + " " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Employees> call, Throwable t) {
                        dialog.dismiss();
                        Log.e(TAG, "Отказ. Не удалось достучаться до сервера во время получения списка сотрудников - " + t);
                        Toast.makeText(MainActivity.this, getResources().getStringArray(R.array.app_errors)[3], Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoaderDialog(){
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.connect_dialog_title);
        dialog.setMessage(getResources().getString(R.string.connect_dialog_message));
        dialog.show();
    }
}
