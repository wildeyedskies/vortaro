package org.mcxa.vortaro

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHelper(private val context: Context) :
        SQLiteOpenHelper(context, context.filesDir.path + "/org.mcxa.vortaro/databases", null, 1) {

    //The Android's default system path of your application database.
    private val DB_PATH = context.filesDir.path + "/org.mcxa.vortaro/databases"
    private val DB_NAME = "data.db"
    private val DB_VERSION = 1

    private var db: SQLiteDatabase? = null

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    @Throws(IOException::class)
    fun createDataBase() {
        // only execute if the database does not exist
        if (!File(DB_PATH + DB_NAME).exists()) {
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.readableDatabase

            //Open your local db as the input stream
            val inputStream = context.getAssets().open(DB_NAME)
            //Open the empty db as the output stream
            val outputStream = FileOutputStream(DB_PATH + DB_NAME)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    @Throws(SQLException::class)
    fun open() {
        db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY)
    }

    @Synchronized override fun close() {
        db?.close()
        super.close()
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}
    override fun onCreate(p0: SQLiteDatabase?) {}
}