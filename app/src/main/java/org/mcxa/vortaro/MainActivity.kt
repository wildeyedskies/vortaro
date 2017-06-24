package org.mcxa.vortaro

import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    val DB_FILENAME = "vortaro.db"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        val p = PreferenceManager.getDefaultSharedPreferences(this)
        if (p.getBoolean("firstRun", true)) {
            copyDB()
            val editor = p.edit()
            editor.putBoolean("firstRun", false)
            editor.apply()
        }
    }

    // this function copies the embeded database into the app's data directory
    // yes, this is incredibly stupid, but android doesn't allow us to directly
    // access a file embeded directly in the apk
    fun copyDB() {
        val dbFile = this.getDatabasePath(DB_FILENAME)
        val inStream = this.assets.open(DB_FILENAME)
        val outStream = FileOutputStream(dbFile)

        val buffer = ByteArray(4096)

        var len = inStream.read(buffer)
        while (len != -1) {
            outStream.write(buffer, 0, len)
            len = inStream.read(buffer)
        }
        inStream.close()
        outStream.close()
    }

    // open the SQL database, copies
    fun openDB(): SQLiteDatabase? {
        val dbFile = this.getDatabasePath(DB_FILENAME)

        return SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
    }
}