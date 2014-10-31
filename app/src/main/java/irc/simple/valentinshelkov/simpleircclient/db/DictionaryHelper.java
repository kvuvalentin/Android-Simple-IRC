package irc.simple.valentinshelkov.simpleircclient.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DictionaryHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "DICT";
    private static final String TABLE_NAME = "words";
    private static final String ID = "_id";
    private static final String WORD = "word";
    private static final String TAG = DictionaryHelper.class.getSimpleName();
    private final Context context;
    private final String dbPath;
    private long maxID;

    public DictionaryHelper(Context context) {
        super(context, DB_NAME, null, 2);
        this.context = context;
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            dbPath = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            dbPath = "/data/data/" + context.getPackageName() + "/databases/";
        }
        maxID = 127142;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Ignored
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public String getWord(long id) {
        Log.w(TAG, "id = " + id);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "_id=" + id, null, null, null, null);
        String result = null;
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(WORD));
            cursor.close();
        }
        return result;
    }

    public long getMaxID() {
        Log.w(TAG, "maxID = " + maxID);
        return maxID;
    }

    public void init() {
        File existingDb = new File(dbPath + File.separator + DB_NAME);
        if (existingDb.exists()) return;
        SQLiteDatabase db = getWritableDatabase();
        db.close();
        try {
            FileUtils.copyInputStreamToFile(context.getAssets().open(DB_NAME), existingDb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
