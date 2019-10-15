package ua.com.anyapps.prihodiuhodsotrudnikov;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.anyapps.prihodiuhodsotrudnikov.Server.ServerConnect;
import ua.com.anyapps.prihodiuhodsotrudnikov.Server.ServerTime.ServerTime;

public class UpdateService extends BroadcastReceiver {

    private static final String TAG = "debapp";
    private Context receiveContext = null;
    private static final String SERVER_TIME_RECEIVED = "SERVER_TIME_RECEIVED";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Log.d(TAG, "ACTION " + intent.getAction().toString());
        receiveContext = context;
        ServerConnect.getInstance()
                .getJSONApi()
                .getServerTime()
                .enqueue(new Callback<ServerTime>() {
                    @Override
                    public void onResponse(Call<ServerTime> call, Response<ServerTime> response) {
                        if(response.isSuccessful()) {
                            ua.com.anyapps.prihodiuhodsotrudnikov.Server.ServerTime.ServerTime serverTimeInfo = response.body();
                            // сохранение серверного времени в файл
                            if(serverTimeInfo.getSucess()) {
                                // запись текущего времени на сервере в файл
                                saveTime(Long.parseLong(serverTimeInfo.getData().getServer_time()));

                                // отправка времени сервера в сервис
                                Intent broadcastedIntent = new Intent(SERVER_TIME_RECEIVED);
                                broadcastedIntent.putExtra("server_time", serverTimeInfo.getData().getServer_time()); //Long.parseLong(serverTimeInfo.getData().getServer_time())
                                receiveContext.sendBroadcast(broadcastedIntent);
                                //receiveContext.startService(new Intent(receiveContext, TimerService.class));
                            }

                            Log.d(TAG, "Сервер успешно вернул данные во время получения серверного времени: " + response.code() + " RESPONSE: " + response.toString() + " BODY: " + response.body().toString());
                        }else{
                            Log.e(TAG, "Во время получения серверного времени сервер вернул ошибку " + response.code());
                            //Toast.makeText(StartActivity.this, getResources().getStringArray(R.array.app_errors)[0] + " " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ua.com.anyapps.prihodiuhodsotrudnikov.Server.ServerTime.ServerTime> call, Throwable t) {
                        //dialog.dismiss();
                        Log.e(TAG, "Отказ. Не удалось достучаться до сервера во время получения серверного времени - " + t);
                        //Toast.makeText(StartActivity.this, getResources().getStringArray(R.array.app_errors)[1], Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveTime(Long _serverTime){
        Log.d(TAG, "ServerTime: " + _serverTime);
    }

    public Long getServerTime(){
        return 21474838L;
    }
}
