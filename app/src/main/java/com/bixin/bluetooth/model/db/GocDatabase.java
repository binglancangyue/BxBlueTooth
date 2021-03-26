package com.bixin.bluetooth.model.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.bixin.bluetooth.model.bean.PhoneBook;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by www37 on 2017/5/9.
 */

public class GocDatabase {
    private static final String DB_PATH = "/data/data/com.bixin.bluetooth/BtPhone.db";
    private static final String SQL_CREATE_PHONEBOOK_TABLE = "create table if not exists phonebook(_id integer primary key autoincrement,name text,number text)";
    private static final String SQL_CREATE_CALLLOG_TABLE = "create table if not exists calllog(_id integer primary key autoincrement,name text,number text,type integer)";
    private static final String SQL_CLEAR_PHONEBOOK = "DELETE FROM phonebook";
    private static final String SQL_CLEAR_CALLLOG = "DELETE FROM calllog";
    private static final String PHONEBOOK_TABLE = "phonebook";
    private static final String CALLLOG_TABLE = "calllog";

    public static final String COL_NAME = "name";
    public static final String COL_NUMBER = "number";
    public static final String COL_TYPE = "type";

    private SQLiteDatabase mDb;
    private static GocDatabase DEFAULT = null;

    public GocDatabase() {
        mDb = SQLiteDatabase.openOrCreateDatabase(DB_PATH, null);
        createPhonebookTable();
        createCalllogTable();
    }

    public static synchronized GocDatabase getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new GocDatabase();
        }

        return DEFAULT;
    }

    public void close() {
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    private void createPhonebookTable() {
        if (mDb == null) {
            Log.e("goc", "createPhonebookTable error,mDb == null");
            return;
        }
        mDb.execSQL(SQL_CREATE_PHONEBOOK_TABLE);
    }

    private void createCalllogTable() {
        if (mDb == null) {
            Log.e("goc", "createCalllogTable error,mDb == null");
            return;
        }
        mDb.execSQL(SQL_CREATE_CALLLOG_TABLE);
    }

    public boolean insertPhonebook(String name, String number) {
        Log.d("app", "insertPhonebook name:" + name + " number:" + number);
        if (mDb == null) {
            Log.e("goc", "insertPhonebook error,mDb == null");
            return false;
        }

//        createPhonebookTable();
//        Cursor cursor = mDb.query(PHONEBOOK_TABLE, new String[]{COL_NAME}, COL_NAME + " = ?", new String[]{name}, null, null, null);
//        Log.d("tag", "insertPhonebook: getCount"+cursor.getCount());
        ContentValues cValue = new ContentValues();
        cValue.put(COL_NAME, name);
        cValue.put(COL_NUMBER, number);
//        mDb.insertWithOnConflict(PHONEBOOK_TABLE, number, cValue, SQLiteDatabase.CONFLICT_REPLACE);
        return (mDb.insert(PHONEBOOK_TABLE, null, cValue) != -1);
    }

    public void clearPhonebook() {
        if (mDb == null) {
            Log.e("goc", "clearPhonebook error,mDb == null");
            return;
        }
        createPhonebookTable();

        mDb.execSQL(SQL_CLEAR_PHONEBOOK);
    }

    public Cursor queryPhonebook() {
        if (mDb == null) {
            Log.e("goc", "queryCalllog error,mDb == null");
            return null;
        }
//        createPhonebookTable();

        return mDb.query(PHONEBOOK_TABLE,
                new String[]{COL_NAME, COL_NUMBER},
                null,
                null,
                null,
                null,
                null);
    }

    public void insetAll(ArrayList<PhoneBook> contacts) {
        for (PhoneBook book : contacts) {
            insertPhonebook(book.getName(), book.getNumber());
        }
    }

    public List<ContactInfo> getAllPhonebook() {
        List<ContactInfo> list = new ArrayList<ContactInfo>();
        ContactInfo contactInfo;
        Cursor cursor = queryPhonebook();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            contactInfo = new ContactInfo();
            contactInfo.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
            contactInfo.setNumber(cursor.getString(cursor.getColumnIndex(COL_NUMBER)));
            list.add(contactInfo);
        }
        return list;
    }

    public String getNameByNumber(String number) {
        String name;
        String num;

        Cursor cur = queryPhonebook();
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            name = cur.getString(cur.getColumnIndex(COL_NAME));
            num = cur.getString(cur.getColumnIndex(COL_NUMBER));
            if (num.equals(number)) {
                cur.close();
                return name;
            }
        }
        if (!cur.isClosed()) {
            cur.close();
            cur = null;
        }
        return null;
    }

    public Cursor queryCalllog(int type) {
        if (mDb == null) {
            Log.e("goc", "queryCalllog error,mDb == null");
            return null;
        }
        createCalllogTable();

        return mDb.query(CALLLOG_TABLE,
                new String[]{COL_NAME, COL_NUMBER},
                COL_TYPE + "=?",
                new String[]{"" + type},
                null,
                null,
                null); // "ORDEY BY ASC"
    }

    public List<CallLogInfo> getAllCallLog(int type) {
        List<CallLogInfo> list = new ArrayList<>();
        CallLogInfo info;
        Cursor cursor = queryCalllog(type);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            info = new CallLogInfo();
            info.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
            info.setNumber(cursor.getString(cursor.getColumnIndex(COL_NUMBER)));
            info.setType(type);
            list.add(info);
        }
        return list;
    }


    public boolean insertCalllog(int type, String name, String number) {
        Log.d("app", "insertCalllog name:" + name + " number:" + number + " type:" + type);
        if (mDb == null) {
            Log.e("goc", "insertCalllog error,mDb == null");
            return false;
        }

        createCalllogTable();

        ContentValues cValue = new ContentValues();
        cValue.put(COL_NAME, name);
        cValue.put(COL_NUMBER, number);
        cValue.put(COL_TYPE, type);
        return (mDb.insert(CALLLOG_TABLE, null, cValue) != -1);
    }

    public void clearCalllog() {
        if (mDb == null) {
            Log.e("goc", "clearPhonebook error,mDb == null");
            return;
        }
        createCalllogTable();

        mDb.execSQL(SQL_CLEAR_CALLLOG);
    }

    public String getName(String number) {
        Cursor cursor = mDb.query(PHONEBOOK_TABLE, new String[]{COL_NAME}, COL_NUMBER + " = ?",
                new String[]{number}, null, null, null);
        String name = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

}
