package com.example.avidovic.anavidovic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by avidovic on 1/26/18.
 */

public class DBAdapter {
    static final String KEY_ROWID = "_id";
    static final String KEY_ADDRESS = "address";
    static final String KEY_TYPE = "type";
    static final String KEY_OWNER_ID = "owner_id";
    static final String TAG = "DBAdapter";

    static final String DATABASE_NAME = "MyDB";
    static final String DATABASE_PAGES_TABLE = "pages";
    static final String DATABASE_OWNER_TABLE = "owner";
    static final int DATABASE_VERSION = 1;

    static final String DATABASE_CREATE_PAGES_TABLE=
            "create table pages (_id integer primary key autoincrement, "
                    + "address text not null, type text not null);";

    static final String DATABASE_CREATE_OWNER_TABLE=
            "create table owner (owner_id integer primary key autoincrement, "
                    + "_id integer not null);";


    final Context context;

    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public DBAdapter(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try {
                db.execSQL(DATABASE_CREATE_PAGES_TABLE);
                db.execSQL(DATABASE_CREATE_OWNER_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading db from" + oldVersion + "to"
                    + newVersion );
            db.execSQL("DROP TABLE IF EXISTS contacts");
            db.execSQL("DROP TABLE IF EXISTS address");
            onCreate(db);
        }
    }

    //---opens the database---
    public DBAdapter open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---
    public void close()
    {
        DBHelper.close();
    }

    public long insertPage(String address, String type)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ADDRESS, address);
        initialValues.put(KEY_TYPE, type);
        return db.insert(DATABASE_PAGES_TABLE, null, initialValues);
    }

    public long insertOwner(Integer pageId)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, pageId);
        return db.insert(DATABASE_OWNER_TABLE, null, initialValues);
    }

    public boolean deletePage(long rowId)
    {
        return db.delete(DATABASE_PAGES_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean deleteOwner(long rowId)
    {
        return db.delete(DATABASE_OWNER_TABLE, KEY_OWNER_ID + "=" + rowId, null) > 0;
    }

    public Cursor getAllPages()
    {
        return db.query(DATABASE_PAGES_TABLE, new String[] {KEY_ROWID, KEY_ADDRESS,
                KEY_TYPE}, null, null, null, null, null);
    }

    public Cursor getPage(long rowId) throws SQLException
    {
        Cursor mCursor =
                db.query(true, DATABASE_PAGES_TABLE, new String[] {KEY_ROWID,
                                KEY_ADDRESS, KEY_TYPE}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getOwnerByPage(long pageId) throws SQLException
    {
        Cursor mCursor =
                db.query(DATABASE_OWNER_TABLE, new String[] {KEY_OWNER_ID, KEY_ROWID},
                        KEY_ROWID + "=" + pageId, null,
                        null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updatePage(long rowId, String addr, String type)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_ADDRESS, addr);
        args.put(KEY_TYPE, type);
        return db.update(DATABASE_PAGES_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean updateOwner(long ownerId, long pageId)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_ROWID, pageId);
        return db.update(DATABASE_OWNER_TABLE, args, KEY_OWNER_ID + "=" + ownerId, null) > 0;
    }
}
