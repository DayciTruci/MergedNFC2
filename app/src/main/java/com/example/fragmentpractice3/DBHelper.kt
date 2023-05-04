package com.example.fragmentpractice3

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class DBHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun updateData(data: Data) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(ID_COLUMN, data.id)
        values.put(ACTIVITY_COLUMN, data.activity)
        values.put(TIME_COLUMN, data.time)
        values.put(STATUS_COLUMN, data.status)
        db.update(TABLE_NAME, values, "$ID_COLUMN = ?", arrayOf(data.id))
        db.close()
    }

    fun deleteData(data: Data) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$ID_COLUMN = ?", arrayOf(data.id))
        db.close()
    }

    fun getData(id: String?, time:String?): Data {
        val db = this.readableDatabase

        Log.v("TEST", "ID : $id || TIME : $time")

        var selection:String? = null
        var selector:String? = null

        if (time != null){
            Log.v("TIME", "TIME IS NOT NULL")
            selection = "$TIME_COLUMN=?"
            selector = time
        }

        else{
            Log.v("TIME", "TIME IS NULL")
            selection = "$ID_COLUMN=?"
            selector = id
        }

        Log.v("TEST", "SELECTION : $selection || SELECTOR : $selector")

        val cursor = db.query(
            TABLE_NAME,
            arrayOf(ID_COLUMN, ACTIVITY_COLUMN, TIME_COLUMN, STATUS_COLUMN),
            selection,
            arrayOf(selector),
            null,
            null,
            null,
            null
        )
        cursor?.moveToFirst()
        val data = Data(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3))
        cursor.close()
        db.close()
        return data
    }

    fun insertCheck(id: String): Boolean {
        var check = false
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(ID_COLUMN, ACTIVITY_COLUMN),
            "$ID_COLUMN=?",
            arrayOf(id),
            null,
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) check = true
        cursor.close()
        db.close()
        return check
    }

    fun getAllSpecificData(time:String): MutableList<Data>{
        val dataList = mutableListOf<Data>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(ID_COLUMN, ACTIVITY_COLUMN, TIME_COLUMN),
            "$TIME_COLUMN=?",
            arrayOf(time),
            null,
            null,
            null,
            null
        )
        while (cursor.moveToNext()){
            dataList.add(Data(cursor.getString(0), cursor.getString(1), cursor.getString(2), 1))
        }
        return dataList
    }

    @SuppressLint("Range")
    fun getAllData(): MutableList<Data> {
        val dataList = mutableListOf<Data>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        while(cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndex(ID_COLUMN))
            val activity = cursor.getString(cursor.getColumnIndex(ACTIVITY_COLUMN))
            val time = cursor.getString(cursor.getColumnIndex(TIME_COLUMN))
            dataList.add(Data(id, activity, time, 0))
        }

        cursor.close()
        db.close()
        return dataList
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "activity_db"
        private const val TABLE_NAME = "activity"
        private const val ID_COLUMN = "id"
        private const val TIME_COLUMN = "time"
        private const val STATUS_COLUMN = "status"
        private const val ACTIVITY_COLUMN = "activity"
        private const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + "("
                + ID_COLUMN + " TEXT PRIMARY KEY,"
                + ACTIVITY_COLUMN + " TEXT,"
                + TIME_COLUMN + " TEXT,"
                + STATUS_COLUMN + " BOOLEAN)")
    }
}