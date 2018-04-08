package com.mongoose.app.moodio.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mongoose.app.moodio.model.PlayList;
import com.mongoose.app.moodio.model.Songs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vips on 7/12/15.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "songs";

    // table name
    private static final String TABLE_NAME = "songs";
    private static final String TABLE_PLAYLIST = "playlist";
    private static final String TABLE_TEMP_PLAYLIST = "tempplaylist";
    private static final String TABLE_PLAYLIST_SONGS = "playlist_songs";
    private static final String TABLE_NAME_RECENT = "recentsongs";
    private static final String TABLE_NAME_DOWN = "downloadedsongs";
    private static final String TABLE_NAME_LANG = "language";
    private static final String TABLE_SEARCH_HISTORY = "searchsuggestion";
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_LYRICS = "lyrics";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_PATH = "path";
    private static final String KEY_ALBUMARTURI = "albumArturi";
    private static final String KEY_PLAYLISTID = "playlist_id";
    private static final String KEY_SONGS_ID = "songs_id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_SOURCE = "source";

    public static final String KEY_SEARCH_VALUE = "searchvalue";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SONGS_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + KEY_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_SONGS_ID + " INTEGER,"
                + KEY_USER_ID + " INTEGER," + KEY_TITLE + " VARCHAR," + KEY_ARTIST + " VARCHAR,"
                + KEY_LYRICS + " VARCHAR " + ")";

        String CREATE_PLAYLIST_TABLE = "CREATE TABLE " + TABLE_PLAYLIST + "(" + KEY_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TITLE + " VARCHAR," + KEY_DESCRIPTION + " VARCHAR " + ")";

        String CREATE_TEMP_PLAYLIST_TABLE = "CREATE TABLE " + TABLE_TEMP_PLAYLIST + "(" + KEY_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TITLE + " VARCHAR," + KEY_DESCRIPTION + " VARCHAR, " + KEY_SOURCE + " VARCHAR " + ")";

        String CREATE_PLAYLIST_SONGS_TABLE = "CREATE TABLE " + TABLE_PLAYLIST_SONGS + "(" + KEY_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TITLE + " VARCHAR," + KEY_ARTIST + " VARCHAR, "
                + KEY_ALBUM + " VARCHAR, " + KEY_DURATION + " INTEGER, " + KEY_PATH + " VARCHAR, "
                + KEY_ALBUMARTURI + " VARCHAR, " + KEY_PLAYLISTID + " INTEGER, "
                + " FOREIGN KEY (" + KEY_PLAYLISTID + ") REFERENCES " + TABLE_PLAYLIST + "(" + KEY_ID + ")" + ")";

        String CREATE_RECENT_SONGS_TABLE ="CREATE TABLE " + TABLE_NAME_RECENT +"("+ KEY_PATH +" VARCHAR, " + KEY_TITLE
                + " VARCHAR, " + KEY_ARTIST + " VARCHAR, " + KEY_ALBUMARTURI + " VARCHAR "+")";

        String CREATE_DOWN_SONGS_TABLE ="CREATE TABLE " + TABLE_NAME_DOWN +"("+ KEY_PATH +" VARCHAR, " + KEY_TITLE
                + " VARCHAR, " + KEY_ARTIST + " VARCHAR, " + KEY_ALBUMARTURI + " VARCHAR "+")";

        String CREATE_LANG_TABLE = "CREATE TABLE " + TABLE_NAME_LANG + "(" + KEY_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TITLE + " VARCHAR " + ")";

        String CREATE_SEARCH_HISTORY_TABLE = "CREATE TABLE " + TABLE_SEARCH_HISTORY + "(" + KEY_SEARCH_VALUE + " VARCHAR " + ")";

        db.execSQL(CREATE_SONGS_TABLE);
        db.execSQL(CREATE_PLAYLIST_TABLE);
        db.execSQL(CREATE_TEMP_PLAYLIST_TABLE);
        db.execSQL(CREATE_PLAYLIST_SONGS_TABLE);
        db.execSQL(CREATE_RECENT_SONGS_TABLE);
        db.execSQL(CREATE_DOWN_SONGS_TABLE);
        db.execSQL(CREATE_LANG_TABLE);
        db.execSQL(CREATE_SEARCH_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    public void addSongs(List<Songs> songsList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);

        for (Songs songs : songsList) {
            ContentValues values = new ContentValues();
            values.put(KEY_SONGS_ID, songs.getSongs_id());
            values.put(KEY_USER_ID, songs.getUser_id());
            values.put(KEY_TITLE, songs.getTitle());
            values.put(KEY_ARTIST, songs.getArtist());
            values.put(KEY_LYRICS, songs.getLyrics());

            db.insert(TABLE_NAME, null, values);
        }
        db.close();
    }

    public List<Songs> getSongs() {
        List<Songs> songsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Songs songs = new Songs();
                songs.setSongs_id(cursor.getInt(1));
                songs.setUser_id(cursor.getInt(2));
                songs.setTitle(cursor.getString(3));
                songs.setArtist(cursor.getString(4));
                songs.setLyrics(cursor.getString(5));

                songsList.add(songs);
            } while (cursor.moveToNext());
        }
        return songsList;
    }

    public List<Songs> searchSongs(String newText) {
        List<Songs> songsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null,
                KEY_TITLE + " LIKE ?", new String[]{"%" + newText + "*"}, null, null, null);
        Log.d("Cursor", "" + cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                Songs songs = new Songs();
                songs.setSongs_id(cursor.getInt(1));
                songs.setUser_id(cursor.getInt(2));
                songs.setTitle(cursor.getString(3));
                songs.setArtist(cursor.getString(4));
                songs.setLyrics(cursor.getString(5));

                songsList.add(songs);
            } while (cursor.moveToNext());
        }

        return songsList;
    }

    public boolean addPlayList(PlayList playList, Songs songs) {
        Log.d("legend.ace18", "Add" + songs.getTitle());
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PLAYLIST + " WHERE " + KEY_TITLE + "=?";
        Cursor cur = db.rawQuery(query, new String[]{playList.getTitle()});
        if (cur.getCount() >= 1) {
            return false;
        } else {
            ContentValues values = new ContentValues();
            values.put(KEY_TITLE, playList.getTitle());
            values.put(KEY_DESCRIPTION, playList.description);
            db.insert(TABLE_PLAYLIST, null, values);
            getPlayListId(playList.getTitle(), songs);
        }
        db.close();
        return true;
    }

    public boolean addTempPlayList(List<PlayList> playLists) {
        //Log.d("legend.ace18", "Add" + songs.getTitle());
        SQLiteDatabase db = this.getWritableDatabase();
        //String query = "SELECT * FROM " + TABLE_PLAYLIST + " WHERE " + KEY_TITLE + "=?";
        //Cursor cur = db.rawQuery(query, new String[]{playList.getTitle()});
        //if (cur.getCount() >= 1) {
          //  return false;
        //} else {
        for (PlayList playList:playLists) {
            ContentValues values = new ContentValues();
            values.put(KEY_TITLE, playList.getTitle());
            values.put(KEY_DESCRIPTION, playList.description);
            values.put(KEY_SOURCE, playList.getSource());
            db.insert(TABLE_TEMP_PLAYLIST, null, values);
            //getPlayListId(playList.getTitle(), songs);
        }
        db.close();
        return true;
    }

    public List<PlayList> getTempPlayList() {
        List<PlayList> playLists = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TEMP_PLAYLIST;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                PlayList playList = new PlayList();
                playList.setId(Integer.toString(cursor.getInt(cursor.getColumnIndex(KEY_ID))));
                playList.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
                playList.setDescription(cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)));
                playList.setSource(cursor.getString(cursor.getColumnIndex(KEY_SOURCE)));
                playList.setViewType(1);

                playLists.add(playList);
            } while (cursor.moveToNext());
        }
        db.close();
        return playLists;
    }

    public boolean ClearTempPlayList() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM "+TABLE_TEMP_PLAYLIST;
        db.execSQL(query);
        db.close();
        return true;
    }

    private void getPlayListId(String title, Songs songs) {
        int id = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + KEY_ID + " FROM " + TABLE_PLAYLIST + " WHERE " + KEY_TITLE + "=?";
        Cursor cur = db.rawQuery(query, new String[]{title});
        if (cur.moveToFirst()) {
            id = cur.getInt(0);
        }
        addPlayListSongs(songs, id);
    }

    public List<PlayList> getPlayList() {
        List<PlayList> playLists = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PLAYLIST;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                PlayList playList = new PlayList();
                playList.setId(Integer.toString(cursor.getInt(cursor.getColumnIndex(KEY_ID))));
                playList.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
                playList.setDescription(cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)));
                playList.setViewType(1);

                playLists.add(playList);
            } while (cursor.moveToNext());
        }
        db.close();
        return playLists;
    }

    public void addPlayListSongs(Songs songs, int playlistId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, songs.getTitle());
        values.put(KEY_ARTIST, songs.getArtist());
        values.put(KEY_ALBUM, songs.getViewType());
        values.put(KEY_PATH, songs.getPath());
        values.put(KEY_DURATION, songs.getSourceType());
        values.put(KEY_ALBUMARTURI, songs.getAlbumArtUri().toString());
        values.put(KEY_PLAYLISTID, playlistId);
        db.insert(TABLE_PLAYLIST_SONGS, null, values);
        db.close();
    }

    public List<Songs> getPlayListSongs(int playListId) {
        List<Songs> songsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PLAYLIST_SONGS + " WHERE " + KEY_PLAYLISTID + "=?";
        Cursor cur = db.rawQuery(query, new String[]{String.valueOf(playListId)});
        if (cur.moveToFirst()) {
            do {
                Songs songs = new Songs();
                songs.setSongs_id(cur.getInt(cur.getColumnIndex(KEY_ID)));
                songs.setTitle(cur.getString(cur.getColumnIndex(KEY_TITLE)));
                songs.setArtist(cur.getString(cur.getColumnIndex(KEY_ARTIST)));
                songs.setViewType(Integer.valueOf(cur.getString(cur.getColumnIndex(KEY_ALBUM))));
                songs.setPath(cur.getString(cur.getColumnIndex(KEY_PATH)));
                songs.setSourceType(cur.getInt(cur.getColumnIndex(KEY_DURATION)));
                songs.setAlbumArtUri((cur.getString(cur.getColumnIndex(KEY_ALBUMARTURI))));
                songsList.add(songs);
            } while (cur.moveToNext());
        }
        db.close();
        return songsList;
    }

    public boolean removePlayList(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] whereArgs = new String[]{String.valueOf(id)};
        int count = db.delete(TABLE_PLAYLIST_SONGS, KEY_PLAYLISTID + "=?", whereArgs);
        if (count > 0)
            return db.delete(TABLE_PLAYLIST, KEY_ID + "=?", whereArgs) > 0;
        else
            return false;

    }

    public int removePlayListSong(int playListId, String songs_path) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] whereArgs = new String[] {String.valueOf(playListId), songs_path};
        int count = db.delete(TABLE_PLAYLIST_SONGS, KEY_PLAYLISTID + "=? AND " + KEY_PATH + "=?", whereArgs);
        return count;
    }

    public boolean addRecentSongs (Songs songs) {
        Log.d("legend.ace18", "Add" + songs.getTitle());
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_RECENT + " WHERE " + KEY_PATH + "=?";
        Cursor cur = db.rawQuery(query, new String[]{songs.getPath()});
        if (cur.getCount() >= 1) {
            return false;
        } else {
            ContentValues values = new ContentValues();
            values.put(KEY_PATH, songs.getPath());
            values.put(KEY_TITLE, songs.getTitle());
            values.put(KEY_ARTIST,songs.getArtist());
            values.put(KEY_ALBUMARTURI,songs.getAlbumArtUri());
            db.insert(TABLE_NAME_RECENT, null, values);
        }
        db.close();
        return true;
    }

    public List<Songs> getRecentSongs() {
        List<Songs> songsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME_RECENT;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Songs songs = new Songs();
                songs.setPath(cursor.getString(0));
                songs.setTitle(cursor.getString(1));
                songs.setArtist(cursor.getString(2));
                songs.setAlbumArtUri(cursor.getString(3));
                songs.setSourceType(2);
                songs.setViewType(1);
                songsList.add(songs);
            } while (cursor.moveToNext());
        }
        return songsList;
    }
    public void deleteRecentTable () {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM "+TABLE_NAME_RECENT;
        String query1 = "VACUUM";
        db.execSQL(query);
        db.execSQL(query1);
    }
    public void removeRecentSong(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM "+TABLE_NAME_RECENT+" WHERE "+KEY_PATH+" = '"+path+"'";
        db.execSQL(query);
    }

    public void addLang(List<String> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_LANG, null, null);
        for (String s : list) {
            ContentValues values = new ContentValues();
            values.put(KEY_TITLE, s);
            db.insert(TABLE_NAME_LANG, null, values);
        }
        db.close();
    }
    public List<String> getLangList() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME_LANG;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String s = cursor.getString(1);
                list.add(s);
            } while (cursor.moveToNext());
        }
        return list;
    }


    public boolean addDownSongs (List<Songs> songsList) {
        //Log.d("legend.ace18", "Add" + songs.getTitle());
        deleteDownTable();
        SQLiteDatabase db = this.getWritableDatabase();
        ///String query = "SELECT * FROM " + TABLE_NAME_DOWN + " WHERE " + KEY_PATH + "=?";
        //Cursor cur = db.rawQuery(query, new String[]{songs.getPath()});
        //if (cur.getCount() >= 1) {
          //  return false;
        for(Songs songs : songsList) {
            ContentValues values = new ContentValues();
            values.put(KEY_PATH, songs.getPath());
            values.put(KEY_TITLE, songs.getTitle());
            values.put(KEY_ARTIST,songs.getArtist());
            values.put(KEY_ALBUMARTURI,songs.getAlbumArtUri());
            db.insert(TABLE_NAME_DOWN, null, values);
        }
        db.close();
        return true;

    }

    public List<Songs> getDownSongs() {
        List<Songs> songsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME_DOWN;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Songs songs = new Songs();
                songs.setPath(cursor.getString(0));
                songs.setTitle(cursor.getString(1));
                songs.setArtist(cursor.getString(2));
                songs.setAlbumArtUri(cursor.getString(3));
                songs.setSourceType(2);
                songs.setViewType(1);
                songsList.add(songs);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songsList;
    }
    public void deleteDownTable () {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM "+TABLE_NAME_DOWN;
        String query1 = "VACUUM";
        db.execSQL(query);
        db.execSQL(query1);
    }

    public boolean addTerminSearchTable(String value){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_SEARCH_HISTORY + " WHERE " + KEY_SEARCH_VALUE + "=?";
        Cursor cur = db.rawQuery(query, new String[]{value});
        if (cur.getCount() >= 1) {
            return false;
        } else {
            ContentValues values = new ContentValues();
            values.put(KEY_SEARCH_VALUE, value);
            db.insert(TABLE_SEARCH_HISTORY, null, values);
        }
        cur.close();
        db.close();
        return true;
    }
    public String[] getSearchSuggestion(String query) {
        String [] list;
        SQLiteDatabase db = this.getWritableDatabase();
        String selquery = "SELECT * FROM " + TABLE_SEARCH_HISTORY + " WHERE " + KEY_SEARCH_VALUE + " LIKE '"+ query +"%'";
        int i =0;
        Cursor cursor = db.rawQuery(selquery,null);
        list = new String[cursor.getCount()];
        if (cursor.moveToFirst()) {
            do {
                list[i++] = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
