package uw.studybuddy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.R.attr.content;
import static android.R.attr.version;

/**
 * Created by apalissery on 2017-06-10.
 */

public class DatabaseSQL extends SQLiteOpenHelper{

    public static final String db_name = "studybuddy";
    public static final String userTable = "user_info";
    public static final String colID = "ID";
    public static final String colName = "NAME";
    public static final String colUserName = "USERNAME";
    public static final String colPassword = "PASSWORD";
    public static String colGender;
    public static final String colEmail = "EMAIL";
    public static final String colFB = "FBLINK";
    public static final String colDesc = "USERDESC";
    //public static final String colCourses = "COURSES";
    //public static final String colFriends = "FRIENDS";


    public DatabaseSQL(Context context ) {
        super(context, userTable, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + userTable + "(" + colID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + colName + " TEXT," + colUserName + " TEXT NOT NULL UNIQUE," + colPassword + " TEXT NOT NULL UNIQUE, " +
                colGender + " TEXT," + colEmail + " TEXT NOT NULL UNIQUE" + colFB + " TEXT" + colDesc + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + userTable);
        onCreate(db);

    }

    public boolean insertData(String name, String username, String password, String gender, String email,
                              String fb, String userdesc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(colName, name);
        contentValues.put(colUserName, username);
        contentValues.put(colPassword, password);
        contentValues.put(colGender, gender);
        contentValues.put(colEmail, email);
        contentValues.put(colFB, fb);
        contentValues.put(colDesc, userdesc);
        long result = db.insert(userTable, null, contentValues);
        if(result == -1) {
            return false;
        } else {
            return true;
        }

    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + userTable, null);
        return res;
    }

    /*public String displayName(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + userTable + "WHERE colName = " + name, null);
        res.moveToFirst();
        String retName = res.getString(res.getColumnIndex(name));
        return retName;
    } */

    public Cursor getName(DatabaseSQL db) {
        SQLiteDatabase sq = db.getReadableDatabase();
        String[] columns = {db.colName};
        //Cursor res = sq.rawQuery("SELECT * FROM " + userTable + "WHERE colName = " + name, null);
        Cursor res = sq.query(db_name, columns, null, null, null, null, null, null);
    }

    public boolean updateData(String id, String name, String username, String password, String gender, String email,
                              String fb, String userdesc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(colName, name);
        contentValues.put(colUserName, username);
        contentValues.put(colPassword, password);
        contentValues.put(colGender, gender);
        contentValues.put(colEmail, email);
        contentValues.put(colFB, fb);
        contentValues.put(colDesc, userdesc);
        db.update(userTable, contentValues, "ID = ?", new String[] { id });
        return true;
    }

    public Integer deleteDate(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(userTable, "ID = ?", new String[] {id});
    }

}
