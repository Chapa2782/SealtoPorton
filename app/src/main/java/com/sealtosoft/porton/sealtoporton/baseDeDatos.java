package com.sealtosoft.porton.sealtoporton;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class baseDeDatos extends SQLiteOpenHelper {

    String sqlCreate = "CREATE TABLE codigos (dirMac TEXT, Descrip TEXT, Prioridad TEXT, Modo TEXT)";
    public baseDeDatos(Context context,String name,SQLiteDatabase.CursorFactory factory,int version) {
        super(context, name, factory, version);
    }
    //OK
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS codigos");

        db.execSQL(sqlCreate);
    }
}
