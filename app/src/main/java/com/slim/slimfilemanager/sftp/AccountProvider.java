package com.slim.slimfilemanager.sftp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AccountProvider extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "sftp";
    private static final String TABLE = "accounts";

    public AccountProvider(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_STRING = "CREATE TABLE " + TABLE + "(" + SftpAccount.COLUMNS_STRING + ")";

        db.execSQL(CREATE_STRING);
    }

    public void addAccount(SftpAccount account) {
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE, null, account.getContentValues());
        db.close();
    }

    public SftpAccount getAccount(int id) {
        //SQLiteDatabase db = getReadableDatabase();

        Log.d("TEST", "id=" + id);

        List<SftpAccount> accounts = getAllAccounts();
        for (SftpAccount account : accounts) {
            if (account.id == id) {
                return account;
            }
        }
        return null;

        /*Cursor cursor = db.rawQuery("SELECT " + id + " FROM " + TABLE, null);

        if (cursor != null)
            cursor.moveToFirst();

        SftpAccount account = new SftpAccount(cursor);
        if (cursor != null && !cursor.isClosed()) cursor.close();
        return account;*/
    }

    public List<SftpAccount> getAllAccounts() {
        List<SftpAccount> accounts = new ArrayList<>();

        String select = "SELECT * FROM " + TABLE;

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(select, null);
        Log.d("TEST", "getAllAccounts");

        if (cursor.moveToFirst()) {
            Log.d("TEST", "move to first");
            do {
                Log.d("TEST", "in while");
                accounts.add(new SftpAccount(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return accounts;
    }

    public int updateAccount(SftpAccount account) {
        SQLiteDatabase db = getWritableDatabase();

        return db.update(TABLE, account.getContentValues(), SftpAccount.COLUMN_ID + " = ?",
                new String[] { Integer.toString(account.id) });
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);

        // Create tables again
        onCreate(db);
    }
}
