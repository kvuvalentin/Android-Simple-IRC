package irc.simple.valentinshelkov.simpleircclient.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import irc.simple.valentinshelkov.simpleircclient.data.MessageData;

public class ChatDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "chat_messages";
    private static final String TABLE_NAME = "messages";
    private static final String TEXT = "text";
    private static final String GRAVITY = "gravity";
    private static final String COLOR = "color";
    private static final String ID = "_id";
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + " " +
            "(" + ID + " integer primary key autoincrement," + TEXT + " text," + GRAVITY + " integer," + COLOR + " integer)";

    public ChatDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public List<MessageData> getMessages() {
        List<MessageData> result = new ArrayList<MessageData>(100);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                String text = c.getString(c.getColumnIndex(TEXT));
                int gravity = c.getInt(c.getColumnIndex(GRAVITY));
                int color = c.getInt(c.getColumnIndex(COLOR));
                MessageData m = new MessageData(text, gravity, color);
                m.setId(c.getLong(c.getColumnIndex("_id")));
                result.add(m);
            } while (c.moveToNext());
        }
        return result;
    }

    public void saveMessages(List<MessageData> messages) {
        SQLiteDatabase db = getWritableDatabase();
        for (MessageData m : messages) {
            Cursor c = db.query(TABLE_NAME, null, "_id=" + m.getId(), null, null, null, null);
            if (c.moveToFirst()) {
                c.close();
                continue;
            } else {
                c.close();
                saveMessage(m);
            }
        }
    }

    public void saveMessage(MessageData m) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TEXT, m.getText());
        cv.put(COLOR, m.getTextColor());
        cv.put(GRAVITY, m.getTextGravity());
        m.setId(db.insertOrThrow(TABLE_NAME, null, cv));
    }

    public MessageData getMessageById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, "_id=" + id, null, null, null, null);
        if (c.moveToFirst()) {
            String text = c.getString(c.getColumnIndex(TEXT));
            int gravity = c.getInt(c.getColumnIndex(GRAVITY));
            int color = c.getInt(c.getColumnIndex(COLOR));
            MessageData m = new MessageData(text, gravity, color);
            m.setId(c.getLong(c.getColumnIndex("_id")));
            return m;
        }
        return null;
    }
}
