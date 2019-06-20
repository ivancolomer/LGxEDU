package com.lglab.ivan.lgxeducontroller.legacy.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.POIEntry;

import static com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.CONTENT_AUTHORITY;
import static com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.CategoryEntry;
import static com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.LGTaskEntry;
import static com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.PATH_CATEGORY;
import static com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.PATH_LG_TASK;
import static com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.PATH_POI;
import static com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.PATH_TOUR;
import static com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.PATH_TOUR_POIS;
import static com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.TourEntry;
import static com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.TourPOIsEntry;


public class POIsProvider extends ContentProvider {
    static final int ALL_CATEGORIES = 300;
    static final int ALL_POIS = 100;
    static final int ALL_TOURS = 200;
    static final int ALL_TOUR_POIS = 400;
    static final int ALL_TASKS = 500;
    static final int SINGLE_CATEGORY = 301;
    static final int SINGLE_POI = 101;
    static final int SINGLE_TOUR = 201;
    static final int SINGLE_TOUR_POIS = 401;
    static final int SINGLE_TASK = 501;
    private static final String Category_IDselection = "category._id = ?";
    private static final String POI_IDselection = "poi._id = ?";
    private static final String LGTasks_IDselection = "LG_TASK._id = ?";
    private static final String TourPOIs_IDselection = "Tour_POIs._id = ?";
    private static final String Tour_IDselection = "tour._id = ?";
    private static final UriMatcher sUriMatcher;
    private static POIsDbHelper mOpenHelper;

    static {
        sUriMatcher = buildUriMatcher();
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(-1);
        String authority = CONTENT_AUTHORITY;
        matcher.addURI(CONTENT_AUTHORITY, PATH_POI, ALL_POIS);
        matcher.addURI(CONTENT_AUTHORITY, "poi/#", SINGLE_POI);
        matcher.addURI(CONTENT_AUTHORITY, PATH_CATEGORY, ALL_CATEGORIES);
        matcher.addURI(CONTENT_AUTHORITY, "category/#", SINGLE_CATEGORY);
        matcher.addURI(CONTENT_AUTHORITY, PATH_TOUR, ALL_TOURS);
        matcher.addURI(CONTENT_AUTHORITY, "tour/#", SINGLE_TOUR);
        matcher.addURI(CONTENT_AUTHORITY, PATH_TOUR_POIS, ALL_TOUR_POIS);
        matcher.addURI(CONTENT_AUTHORITY, "tourPois/#", SINGLE_TOUR_POIS);

        matcher.addURI(CONTENT_AUTHORITY, PATH_LG_TASK, ALL_TASKS);
        matcher.addURI(CONTENT_AUTHORITY, "lgTask/#", SINGLE_TASK);
        return matcher;
    }

    public static Cursor queryByTaskId(String itemSelectedID) {
        return mOpenHelper.getReadableDatabase().rawQuery("SELECT t._id,t.title, t.description, t.script,t.shutdown_script,t.image, t.ip,t.user,t.password,t.url, t.isrunning FROM LG_TASK t WHERE t._id = ?", new String[]{itemSelectedID});
    }

    public static void updateTaskStateByTaskId(String itemSelectedID, boolean isRunning) {
        String sql = "UPDATE LG_TASK SET " + LGTaskEntry.COLUMN_LG_ISRUNNING + "=" + (isRunning ? 1 : 0) + " WHERE _id = ?";

        mOpenHelper.getReadableDatabase().execSQL(sql, new String[]{itemSelectedID});
    }

    public static void updateTaskUrlById(String itemSelectedID, String taskUrl) {
        String sql = "UPDATE LG_TASK SET " + LGTaskEntry.COLUMN_LG_BROWSER_URL + "='" + (taskUrl) + "' WHERE _id = ?";

        mOpenHelper.getReadableDatabase().execSQL(sql, new String[]{itemSelectedID});
    }

    public static Cursor getAllLGTasks() {
        String sql = "SELECT t._id,t.title, t.description, t.script,t.shutdown_script,t.image, t.ip,t.user,t.password,t.url, t.isrunning FROM LG_TASK t";
        return mOpenHelper.getReadableDatabase().rawQuery(sql, new String[]{});
    }

    public static Cursor queryByPoiJOINTourPois(String itemSelectedID) {
        String sql = "SELECT t.POI, p.Name, t.POI_Duration FROM poi p " +
                "INNER JOIN Tour_POIs t ON p._id = t.POI " +
                "WHERE t.Tour = ? ORDER BY t.POI_Order ASC";
        return mOpenHelper.getReadableDatabase().rawQuery(sql, new String[]{itemSelectedID});
    }

    public static Cursor queryPOIById(int poiId) {
        String sql = "SELECT p.* FROM POI p WHERE p._id = ?";
        return mOpenHelper.getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(poiId)});
    }

    public static Cursor getAllPOIs() {
        String sql = "SELECT p.* FROM POI p";
        return mOpenHelper.getReadableDatabase().rawQuery(sql, new String[]{});
    }

    public static Cursor getAllGames() {
        String sql = "SELECT q._id, q.Data FROM game q";
        return mOpenHelper.getReadableDatabase().rawQuery(sql, new String[]{});
    }

    public static Cursor getAllGameCategories() {
        String sql = "SELECT c._id, c.Name FROM game_category c ORDER BY c.Name ASC";
        return mOpenHelper.getReadableDatabase().rawQuery(sql, new String[]{});
    }

    public static Cursor queryGame(int quizId) {
        String sql = "SELECT q._id, q.Data FROM game q WHERE q._ID = ?";
        return mOpenHelper.getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(quizId)});
    }

    public static void updateGameById(int gameId, String data) {
        mOpenHelper.getReadableDatabase().execSQL("UPDATE game SET data = '" + data + "' WHERE _id = ?", new String[]{String.valueOf(gameId)});
    }

    public static void removeGameById(int gameId) {
        mOpenHelper.getReadableDatabase().execSQL("DELETE FROM game WHERE _ID = ?", new String[]{String.valueOf(gameId)});
    }

    public static void removeCategoryGameById(int categoryId) {
        mOpenHelper.getReadableDatabase().execSQL("DELETE FROM game_category WHERE _ID = ?", new String[]{String.valueOf(categoryId)});
    }

    public static Cursor getLGConnectionData() {
        String sql = "SELECT c.user, c.password, c.hostname, c.port FROM lg_connection_info c";
        return mOpenHelper.getReadableDatabase().rawQuery(sql, new String[]{});
    }

    public static void updateLGConnectionData(String user, String password, String hostname, int port) {
        mOpenHelper.getReadableDatabase().execSQL("UPDATE lg_connection_info SET user = '" + user + "', password = '" + password + "', hostname = '" + hostname + "', port = ?", new String[]{String.valueOf(port)});
    }

    public static long insertGame(String data) {
        ContentValues values = new ContentValues();
        values.put("data", data);
        return mOpenHelper.getReadableDatabase().insert("game", "", values);
    }

    public static long insertCategoryGame(String data) {
        ContentValues values = new ContentValues();
        values.put("Name", data);
        return mOpenHelper.getReadableDatabase().insert("game_category", "", values);
    }

    public static long insertPOI(POI poi) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(POIsContract.POIEntry.COLUMN_COMPLETE_NAME, poi.getName());
        contentValues.put(POIsContract.POIEntry.COLUMN_VISITED_PLACE_NAME, poi.getVisited_place());
        contentValues.put(POIsContract.POIEntry.COLUMN_LONGITUDE, poi.getLongitude());
        contentValues.put(POIsContract.POIEntry.COLUMN_LATITUDE, poi.getLatitude());
        contentValues.put(POIsContract.POIEntry.COLUMN_ALTITUDE, poi.getAltitude());
        contentValues.put(POIsContract.POIEntry.COLUMN_HEADING, poi.getHeading());
        contentValues.put(POIsContract.POIEntry.COLUMN_TILT, poi.getTilt());
        contentValues.put(POIsContract.POIEntry.COLUMN_RANGE, poi.getRange());
        contentValues.put(POIsContract.POIEntry.COLUMN_ALTITUDE_MODE, poi.getAltitudeMode());
        contentValues.put(POIsContract.POIEntry.COLUMN_HIDE, poi.isHidden() ? 1 : 0);
        contentValues.put(POIsContract.POIEntry.COLUMN_CATEGORY_ID, poi.getCategoryId());

        return mOpenHelper.getReadableDatabase().insert("poi", "", contentValues);
    }

    public boolean onCreate() {
        mOpenHelper = new POIsDbHelper(getContext());
        return true;
    }

    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case ALL_POIS /*100*/:
                cursor = mOpenHelper.getReadableDatabase().query(PATH_POI, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_POI /*101*/:
                cursor = mOpenHelper.getReadableDatabase().query(PATH_POI, projection, POI_IDselection, selectionArgs, null, null, sortOrder);
                break;
            case ALL_TOURS /*200*/:
                cursor = mOpenHelper.getReadableDatabase().query(PATH_TOUR, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_TOUR /*201*/:
                cursor = mOpenHelper.getReadableDatabase().query(PATH_TOUR, projection, Tour_IDselection, selectionArgs, null, null, sortOrder);
                break;
            case ALL_CATEGORIES /*300*/:
                cursor = mOpenHelper.getReadableDatabase().query(PATH_CATEGORY, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_CATEGORY /*301*/:
                cursor = mOpenHelper.getReadableDatabase().query(PATH_CATEGORY, projection, Category_IDselection, selectionArgs, null, null, sortOrder);
                break;
            case ALL_TOUR_POIS /*400*/:
                cursor = mOpenHelper.getReadableDatabase().query(TourPOIsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_TOUR_POIS /*401*/:
                cursor = mOpenHelper.getReadableDatabase().query(TourPOIsEntry.TABLE_NAME, projection, TourPOIs_IDselection, selectionArgs, null, null, sortOrder);
                break;
            case ALL_TASKS /*500*/:
                cursor = mOpenHelper.getReadableDatabase().query(LGTaskEntry.TABLE_NAME, projection, LGTasks_IDselection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_TASK /*501*/:
                cursor = mOpenHelper.getReadableDatabase().query(LGTaskEntry.TABLE_NAME, projection, LGTasks_IDselection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ALL_POIS /*100*/:
                return POIEntry.CONTENT_TYPE;
            case SINGLE_POI /*101*/:
                return POIEntry.CONTENT_ITEM_TYPE;
            case ALL_TOURS /*200*/:
                return TourEntry.CONTENT_TYPE;
            case SINGLE_TOUR /*201*/:
                return TourEntry.CONTENT_ITEM_TYPE;
            case ALL_CATEGORIES /*300*/:
                return CategoryEntry.CONTENT_TYPE;
            case SINGLE_CATEGORY /*301*/:
                return CategoryEntry.CONTENT_ITEM_TYPE;
            case ALL_TOUR_POIS /*400*/:
                return TourPOIsEntry.CONTENT_TYPE;
            case SINGLE_TOUR_POIS /*401*/:
                return TourPOIsEntry.CONTENT_ITEM_TYPE;
            case ALL_TASKS /*500*/:
                return LGTaskEntry.CONTENT_TYPE;
            case SINGLE_TASK /*501*/:
                return LGTaskEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri returnUri;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id;
        switch (sUriMatcher.match(uri)) {
            case ALL_POIS /*100*/:
                _id = db.insert(PATH_POI, null, values);
                if (_id > 0) {
                    returnUri = POIEntry.buildPOIUri(_id);
                    break;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case ALL_TOURS /*200*/:
                _id = db.insert(PATH_TOUR, null, values);
                if (_id > 0) {
                    returnUri = TourEntry.buildTourUri(_id);
                    break;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case ALL_CATEGORIES /*300*/:
                _id = db.insert(PATH_CATEGORY, null, values);
                if (_id > 0) {
                    returnUri = CategoryEntry.buildCategoryUri(_id);
                    break;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case ALL_TOUR_POIS /*400*/:
                _id = db.insert(TourPOIsEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = TourPOIsEntry.buildTourUri(_id);
                    break;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case ALL_TASKS /*500*/:
                _id = db.insert(LGTaskEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = LGTaskEntry.buildTourUri(_id);
                    break;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        if (selection == null) {
            selection = "1";
        }
        switch (match) {
            case ALL_POIS /*100*/:
                rowsDeleted = db.delete(PATH_POI, selection, selectionArgs);
                if (rowsDeleted <= 0) {
                    throw new SQLException("Failed to delete rows from " + uri);
                }
                break;
            case ALL_TOURS /*200*/:
                rowsDeleted = db.delete(PATH_TOUR, selection, selectionArgs);
                if (rowsDeleted <= 0) {
                    throw new SQLException("Failed to delete rows from " + uri);
                }
                break;
            case ALL_CATEGORIES /*300*/:
                rowsDeleted = db.delete(PATH_CATEGORY, selection, selectionArgs);
                if (rowsDeleted <= 0) {
                    throw new SQLException("Failed to delete rows from " + uri);
                }
                break;
            case ALL_TOUR_POIS /*400*/:
                rowsDeleted = db.delete(TourPOIsEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted < 0) {
                    throw new SQLException("Failed to delete rows from " + uri);
                }
                break;
            case ALL_TASKS /*500*/:
                rowsDeleted = db.delete(LGTaskEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted < 0) {
                    throw new SQLException("Failed to delete rows from " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case ALL_POIS /*100*/:
                rowsUpdated = db.update(PATH_POI, values, selection, selectionArgs);
                if (rowsUpdated < 0) {
                    throw new SQLException("Failed to update rows from " + uri);
                }
                break;
            case ALL_TOURS /*200*/:
                rowsUpdated = db.update(PATH_TOUR, values, selection, selectionArgs);
                if (rowsUpdated < 0) {
                    throw new SQLException("Failed to update rows from " + uri);
                }
                break;
            case ALL_CATEGORIES /*300*/:
                rowsUpdated = db.update(PATH_CATEGORY, values, selection, selectionArgs);
                if (rowsUpdated < 0) {
                    throw new SQLException("Failed to update rows from " + uri);
                }
                break;
            case ALL_TOUR_POIS /*400*/:
                rowsUpdated = db.update(TourPOIsEntry.TABLE_NAME, values, selection, selectionArgs);
                if (rowsUpdated < 0) {
                    throw new SQLException("Failed to update rows from " + uri);
                }
                break;
            case ALL_TASKS /*500*/:
                rowsUpdated = db.update(LGTaskEntry.TABLE_NAME, values, selection, selectionArgs);
                if (rowsUpdated < 0) {
                    throw new SQLException("Failed to update rows from " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    public void resetDatabase() {
        mOpenHelper.close();
        mOpenHelper = new POIsDbHelper(getContext());
        mOpenHelper.resetDatabase(mOpenHelper.getWritableDatabase());
    }


}
