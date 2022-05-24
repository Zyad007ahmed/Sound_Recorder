package org.horaapps.soundrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import org.horaapps.soundrecorder.activities.MainActivity;

public class DBHelper extends SQLiteOpenHelper {

    Context context;

    public static final String DB_NAME = "saved_recordings.db";
    private static int DB_VER = 1;

    public static final String TABLE_NAME = "saved_recordings";

    public static final String _ID = "_id";
    public static final String COL_NAME_RECORDING_NAME = "recording_name";
    public static final String COL_NAME_RECORDING_FILE_PATH = "file_path";
    public static final String COL_NAME_RECORDING_LENGTH = "recording_length";
    public static final String COL_NAME_RECORDING_TIME_ADDED = "time_added";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + _ID + " INTEGER PRIMARY KEY " + COMMA_SEP +
                    COL_NAME_RECORDING_NAME + TEXT_TYPE + COMMA_SEP +
                    COL_NAME_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    COL_NAME_RECORDING_LENGTH + " INTEGER " + COMMA_SEP +
                    COL_NAME_RECORDING_TIME_ADDED + " INTEGER ) WITHOUT ROWID;";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VER);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addRecording(String recordingName, String recordingPath, long length) {
        SQLiteDatabase dp = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(_ID,getCount()+1);
        cv.put(COL_NAME_RECORDING_NAME, recordingName);
        cv.put(COL_NAME_RECORDING_FILE_PATH, recordingPath);
        cv.put(COL_NAME_RECORDING_LENGTH, length);
        cv.put(COL_NAME_RECORDING_TIME_ADDED, System.currentTimeMillis());

        dp.insert(TABLE_NAME, null, cv);
    }

    public void removeItemWithId(int id) {
        SQLiteDatabase dp = getWritableDatabase();
        String[] args = {String.valueOf(id)};
        dp.delete(TABLE_NAME, "_ID=?", args);
    }

    public void renameItem(int id, String name) {
        SQLiteDatabase dp = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME_RECORDING_NAME, name);
        cv.put(COL_NAME_RECORDING_FILE_PATH, MainActivity.folder.toPath().toString() + "/" + name);
        dp.update(TABLE_NAME, cv, _ID + "=" + id, null);
    }

    public int getCount() {
        SQLiteDatabase dp = getReadableDatabase();
        String[] projection = {_ID};
        Cursor c = dp.query(TABLE_NAME, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    public RecordingItem getItemAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {_ID,
                COL_NAME_RECORDING_NAME,
                COL_NAME_RECORDING_FILE_PATH,
                COL_NAME_RECORDING_LENGTH,
                COL_NAME_RECORDING_TIME_ADDED};
        Cursor c = db.query(TABLE_NAME, projection, null, null, null, null, null);
        if (c.moveToPosition(position)) {
            RecordingItem item = new RecordingItem();
            item.setmId(c.getInt(c.getColumnIndex(_ID)));
            item.setmName(c.getString(c.getColumnIndex(COL_NAME_RECORDING_NAME)));
            item.setmFilePath(c.getString(c.getColumnIndex(COL_NAME_RECORDING_FILE_PATH)));
            item.setmLength(c.getInt(c.getColumnIndex(COL_NAME_RECORDING_LENGTH)));
            item.setmTime(c.getLong(c.getColumnIndex(COL_NAME_RECORDING_TIME_ADDED)));
            c.close();
            return item;
        }

        return null;
    }
}
