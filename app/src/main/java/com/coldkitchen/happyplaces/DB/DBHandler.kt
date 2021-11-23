package com.coldkitchen.happyplaces.DB

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.content.contentValuesOf
import com.coldkitchen.happyplaces.models.PlaceModel

class DBHandler(context: Context): SQLiteOpenHelper(
        context, DATABASE_NAME,
        null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "HappyPlacesDatabase.db"
        private const val TABLE_HAPPY_PLACE = "HappyPlacesTable"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_IMAGE= "image"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_HAPPY_PLACE_TABLE = ("CREATE TABLE IF NOT EXISTS "+ TABLE_HAPPY_PLACE + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_IMAGE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_LATITUDE + " TEXT,"
                + COLUMN_LONGITUDE + " TEXT)")
        db?.execSQL(CREATE_HAPPY_PLACE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HAPPY_PLACE")
        onCreate(db)
    }

    fun addHappyPlace(happyPlace: PlaceModel): Long{

        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_TITLE, happyPlace.title)
        values.put(COLUMN_IMAGE, happyPlace.image)
        values.put(COLUMN_DESCRIPTION, happyPlace.description)
        values.put(COLUMN_DATE, happyPlace.date)
        values.put(COLUMN_LOCATION, happyPlace.location)
        values.put(COLUMN_LATITUDE, happyPlace.latitude)
        values.put(COLUMN_LONGITUDE, happyPlace.longitude)

        val result = db.insert(TABLE_HAPPY_PLACE, null, values)
        db.close()
        return result
    }

    fun updateHappyPlace(happyPlace: PlaceModel): Int{

        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_TITLE, happyPlace.title)
        values.put(COLUMN_IMAGE, happyPlace.image)
        values.put(COLUMN_DESCRIPTION, happyPlace.description)
        values.put(COLUMN_DATE, happyPlace.date)
        values.put(COLUMN_LOCATION, happyPlace.location)
        values.put(COLUMN_LATITUDE, happyPlace.latitude)
        values.put(COLUMN_LONGITUDE, happyPlace.longitude)

        val result = db.update(TABLE_HAPPY_PLACE,
            values,
            COLUMN_ID + "=" + happyPlace.id, null)
        db.close()
        return result
    }

    fun getHappyPlacesList(): ArrayList<PlaceModel>{
        val happyPlacesList = ArrayList<PlaceModel>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_HAPPY_PLACE"

        try{
            val cursor = db.rawQuery( selectQuery, null)
            if(cursor.moveToFirst()){
                do {
                    val place = PlaceModel(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE))
                    )
                    happyPlacesList.add(place)
                } while(cursor.moveToNext())
            }
            cursor.close()
        }catch (e: SQLException){
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return happyPlacesList
    }

    fun deleteHappyPlace(happyPlace: PlaceModel): Int {
        val db = this.writableDatabase

        val result = db.delete(TABLE_HAPPY_PLACE,
            COLUMN_ID + "=" + happyPlace.id, null)
        db.close()
        return result
    }
}