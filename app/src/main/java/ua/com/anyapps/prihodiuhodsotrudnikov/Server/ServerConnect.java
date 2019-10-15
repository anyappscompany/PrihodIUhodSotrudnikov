package ua.com.anyapps.prihodiuhodsotrudnikov.Server;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerConnect {
    private static ServerConnect mInstance;
    private static final String BASE_URL = "http://anyapps.cf/prihodiuhodsotrudnikov/api/";
    private Retrofit mRetrofit;

    private ServerConnect() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ServerConnect getInstance() {
        if (mInstance == null) {
            mInstance = new ServerConnect();
        }
        return mInstance;
    }

    public ServerApi getJSONApi() {
        return mRetrofit.create(ServerApi.class);
    }
}
