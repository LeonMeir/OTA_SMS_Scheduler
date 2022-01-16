package ota.com.schedulesms;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.util.ArrayList;

import ota.com.schedulesms.model.MessageData;

public class DBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "schedulesms.db";

    private static final String SQL_CREATE_ENTRIES_SMS =
            "CREATE TABLE " + Constant.TABLE_NAME_SMS + " (" +
                    Constant.DB_FIELDNAME_SMS_ID + " INTEGER PRIMARY KEY," +
                    Constant.DB_FIELDNAME_SMS_TEXT + " TEXT," +
                    Constant.DB_FIELDNAME_SMS_PHONE + " TEXT," +
                    Constant.DB_FIELDNAME_SMS_TIME + " LONG," +
                    Constant.DB_FIELDNAME_SMS_TIMESTAMP + " LONG," +
                    Constant.DB_FIELDNAME_SMS_TYPE + " TEXT," +
                    Constant.DB_FIELDNAME_SMS_DBSTATUS + " TEXT," +
                    Constant.DB_FIELDNAME_SMS_PROCESSING + " TEXT," +
                    Constant.DB_FIELDNAME_SMS_PREVDBSTATUS + " TEXT)";

    private static final String SQL_CREATE_ENTRIES_TOKEN =
            "CREATE TABLE " + Constant.TABLE_NAME_TOKEN + " (" +
                    Constant.DB_FIELDNAME_TOKEN_TOKEN + " TEXT)";

    private static final String SQL_DELETE_ENTRIES_SMS =
            "DROP TABLE IF EXISTS " + Constant.TABLE_NAME_SMS;

    private static final String SQL_DELETE_ENTRIES_TOKEN =
            "DROP TABLE IF EXISTS " + Constant.TABLE_NAME_TOKEN;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_SMS);
        db.execSQL(SQL_CREATE_ENTRIES_TOKEN);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES_SMS);
        db.execSQL(SQL_DELETE_ENTRIES_TOKEN);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @SuppressLint("Range")
    public String getAppToken() {
        String strRet = "";
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + Constant.TABLE_NAME_TOKEN, null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                strRet = cursor.getString(cursor.getColumnIndex(Constant.DB_FIELDNAME_TOKEN_TOKEN));
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strRet;
    }

    public void updateToken(String strToken) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + Constant.TABLE_NAME_TOKEN, null);
            ContentValues cv = new ContentValues();
            cv.put(Constant.DB_FIELDNAME_TOKEN_TOKEN, strToken);
            if (cursor.getCount() > 0){
                cursor.close();
                db.update(Constant.TABLE_NAME_TOKEN, cv, null, null);
            }else{
                cursor.close();
                db.insert(Constant.TABLE_NAME_TOKEN, null, cv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<MessageData> getAllMessageData(){
        ArrayList<MessageData> arrToRet = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + Constant.TABLE_NAME_SMS, null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                MessageData item = new MessageData(cursor);
                arrToRet.add(item);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrToRet;
    }

    public ArrayList<MessageData> getAllMessageDataForType(String strType){
        ArrayList<MessageData> arrToRet = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(Constant.TABLE_NAME_SMS,
                    null,
                    Constant.DB_FIELDNAME_SMS_TYPE + " = '" + strType + "'",
                    null, null, null, null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                MessageData item = new MessageData(cursor);
                arrToRet.add(item);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrToRet;
    }
    public MessageData getMessageData(int nID){
        MessageData dataToRet = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(Constant.TABLE_NAME_SMS,
                    null,
                    Constant.DB_FIELDNAME_SMS_ID + " = '" + nID + "'",
                    null, null, null, null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                dataToRet = new MessageData(cursor);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataToRet;
    }

    public int insertOrUpdateMessageData(MessageData messageData){
        ContentValues cv = messageData.getAsContentValues();
        MessageData itemOnDB = getMessageData(messageData.m_nID);
        SQLiteDatabase db = this.getReadableDatabase();
        int nID = messageData.m_nID;
        if (itemOnDB == null){
            nID = (int) db.insert(Constant.TABLE_NAME_SMS, null, cv);
        }else{
            db.update(Constant.TABLE_NAME_SMS, cv,
                    Constant.DB_FIELDNAME_SMS_ID + " = '" + nID + "'", null);
        }
        return nID;
    }

    public int getMaxIDOfMessageData(){
        int nRet = 0;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(Constant.TABLE_NAME_SMS,
                    null,
                    "MAX(ID)",
                    null, null, null, null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                nRet = cursor.getInt(0);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nRet;
    }
}
