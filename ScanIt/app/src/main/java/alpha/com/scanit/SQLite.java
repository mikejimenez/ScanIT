package alpha.com.scanit;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SQLite extends SQLiteOpenHelper {

    // All Static variables

    // Database Variables
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BCData";
    private static final String TABLE_NAME = "Barcodes";
    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "Barcode";
    private static final String KEY_Barcode = "Company";

    public SQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " String,"
                + KEY_Barcode + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    public void onDrop() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    /**
     * Create, Read, Update, Delete Operations
     */

    // Adding new barcode
    void addBarcodes(Barcodes Barcodes) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, Barcodes.getBarcode()); // Barcode
        values.put(KEY_Barcode, Barcodes.getCompany()); // Company

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    // Getting single barcode
    Barcodes getBarcodes(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_ID,
                        KEY_NAME, KEY_Barcode}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null
        );
        if (cursor != null)
            cursor.moveToFirst();

        Barcodes Barcodes = new Barcodes(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2));

        return Barcodes;
    }

    // Getting All Barcodes
public List<Barcodes> getBarCodes() {

    List<Barcodes> BarcodesList = new ArrayList<>();
    // Select All Query
    String selectQuery = "SELECT  * FROM " + TABLE_NAME;
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    // looping through all rows and adding to list
    if (cursor.moveToFirst()) {
        do {
            Barcodes Barcodes = new Barcodes();
            Barcodes.setID(Integer.parseInt(cursor.getString(0)));
            Barcodes.setBarcode(cursor.getString(1));
            Barcodes.setCompany(cursor.getString(2));
            // Adding Barcode to list
            BarcodesList.add(Barcodes);
        } while (cursor.moveToNext());
    }


    return BarcodesList;
}

    // Deleting single Barcodes
    public void deleteBarcodes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE " + TABLE_NAME);
        onCreate(db);
        db.close();
    }

    public Cursor getBarcodesRaw() {
        Cursor c;
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        c = db.rawQuery(countQuery, null);

        return c;
    }
}
