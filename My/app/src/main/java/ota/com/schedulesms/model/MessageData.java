package ota.com.schedulesms.model;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;

import ota.com.schedulesms.Constant;

public class MessageData {
    public int m_nID;
    public String mStrText;
    public String mStrPhone;
    public long m_lTime;
    public long m_lTimestamp;
    public String mStrType;
    public String mStrDbStatus;
    public String mStrProcessing;
    public String mStrPrevDbStatus;

    public MessageData(){
        m_nID = -1;
        mStrText = "";
        mStrPhone = "";
        m_lTime = 0;
        m_lTimestamp = 0;
        mStrType = "";
        mStrDbStatus = "";
        mStrProcessing = "";
        mStrPrevDbStatus = "";
    }

    public MessageData(int nID, String strText, String strPhone, long lTime, long lTimeStamp, String strType,
                       String strDBStatus, String strProcessing, String strPrevDbStatus){
        m_nID = nID;
        mStrText = strText;
        mStrPhone = strPhone;
        m_lTime = lTime;
        m_lTimestamp = lTimeStamp;
        mStrType = strType;
        mStrDbStatus = strDBStatus;
        mStrProcessing = strProcessing;
        mStrPrevDbStatus = strPrevDbStatus;
    }

    @SuppressWarnings("CatchMayIgnoreException")
    @SuppressLint("Range")
    public MessageData(Cursor cursor){
        try{
            m_nID = cursor.getInt(cursor.getColumnIndex(Constant.DB_FIELDNAME_SMS_ID));
        }catch(Exception e){}
        try{
            mStrText = cursor.getString(cursor.getColumnIndex(Constant.DB_FIELDNAME_SMS_TEXT));
        }catch(Exception e){}
        try{
            mStrPhone = cursor.getString(cursor.getColumnIndex(Constant.DB_FIELDNAME_SMS_PHONE));
        }catch(Exception e){}
        try{
            m_lTime = cursor.getLong(cursor.getColumnIndex(Constant.DB_FIELDNAME_SMS_TIME));
        }catch(Exception e){}
        try{
            m_lTimestamp = cursor.getLong(cursor.getColumnIndex(Constant.DB_FIELDNAME_SMS_TIMESTAMP));
        }catch(Exception e){}
        try{
            mStrType = cursor.getString(cursor.getColumnIndex(Constant.DB_FIELDNAME_SMS_TYPE));
        }catch(Exception e){}
        try{
            mStrDbStatus = cursor.getString(cursor.getColumnIndex(Constant.DB_FIELDNAME_SMS_DBSTATUS));
        }catch(Exception e){}
        try{
            mStrProcessing = cursor.getString(cursor.getColumnIndex(Constant.DB_FIELDNAME_SMS_PROCESSING));
        }catch(Exception e){}
        try{
            mStrPrevDbStatus = cursor.getString(cursor.getColumnIndex(Constant.DB_FIELDNAME_SMS_PREVDBSTATUS));
        }catch(Exception e){}
    }

    public ContentValues getAsContentValues(){
        ContentValues cv = new ContentValues();
        cv.put(Constant.DB_FIELDNAME_SMS_ID,            m_nID);
        cv.put(Constant.DB_FIELDNAME_SMS_TEXT,          mStrText);
        cv.put(Constant.DB_FIELDNAME_SMS_PHONE,         mStrPhone);
        cv.put(Constant.DB_FIELDNAME_SMS_TIME,          m_lTime);
        cv.put(Constant.DB_FIELDNAME_SMS_TIMESTAMP,     m_lTimestamp);
        cv.put(Constant.DB_FIELDNAME_SMS_TYPE,          mStrType);
        cv.put(Constant.DB_FIELDNAME_SMS_DBSTATUS, mStrDbStatus);
        cv.put(Constant.DB_FIELDNAME_SMS_PROCESSING,    mStrProcessing);
        cv.put(Constant.DB_FIELDNAME_SMS_PREVDBSTATUS,  mStrPrevDbStatus);
        return cv;
    }
}
