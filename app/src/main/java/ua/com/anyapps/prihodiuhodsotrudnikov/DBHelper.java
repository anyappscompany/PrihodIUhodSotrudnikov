package ua.com.anyapps.prihodiuhodsotrudnikov;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "debapp";
    public static String TABLE_EMPLOYEE_ACTIVITY = "employee_activity";
    public static String DB_NAME = "prihodiuhodsotrudnikov";

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Создание таблицы с полями");
        db.execSQL("CREATE TABLE '" + TABLE_EMPLOYEE_ACTIVITY + "' ('_id' INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, 'employeeid' INTEGER, 'enteredpin' INTEGER, 'visittime' INTEGER, 'timezone' TEXT, 'comegone' INTEGER, 'employeephoto' TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE_ACTIVITY);
        onCreate(db);
    }
}
