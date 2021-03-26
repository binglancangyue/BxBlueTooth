package com.bixin.bluetooth.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.bixin.bluetooth.model.bean.BxBTApp;

public class PhoneBookDataBse extends SQLiteOpenHelper {
    public static PhoneBookDataBse mInstance = null;
    /**
     * 数据库名称
     **/
    public static final String DATABASE_NAME = "location.db";
    /**
     * 数据库版本号
     **/
    private static final int DATABASE_VERSION = 1;
    /**
     * DB对象
     **/
    SQLiteDatabase mDb = null;
    private static final String DB_PATH = "/data/data/com.bixin.bluetooth/BtPhone.db";
    private static final String SQL_CREATE_PHONEBOOK_TABLE = "create table if not exists phonebook(_id integer primary key autoincrement,name text,number text)";
    private static final String SQL_CREATE_CALLLOG_TABLE = "create table if not exists calllog(_id integer primary key autoincrement,name text,number text,type integer)";
    private static final String SQL_CLEAR_PHONEBOOK = "DELETE FROM phonebook";
    private static final String SQL_CLEAR_CALLLOG = "DELETE FROM calllog";
    public static final String COL_NAME = "name";
    public static final String COL_NUMBER = "number";
    public static final String COL_TYPE = "type";

    public PhoneBookDataBse(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * 单例模式
     **/

    public static synchronized PhoneBookDataBse getInstance() {
        if (mInstance == null) {
            mInstance = new PhoneBookDataBse(BxBTApp.getInstance(), "phone_book", null, 1);
        }
        return mInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PHONEBOOK_TABLE);
        db.execSQL(SQL_CREATE_CALLLOG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
