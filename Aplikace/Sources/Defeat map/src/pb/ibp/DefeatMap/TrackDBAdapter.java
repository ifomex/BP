package pb.ibp.DefeatMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Tøída obsluhy databáze
 * @author Petr Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class TrackDBAdapter {

	// table Tracks 	
	public static final String T_KEY_ID = "_id";
	public static final String T_KEY_NAME = "name";
	
	// table Points
	public static final String P_KEY_ID = "_id";
	public static final String P_KEY_NAME = "point_name";
	public static final String P_KEY_GPSLO = "gps_lon";
	public static final String P_KEY_GPSLA = "gps_lat";
	public static final String P_KEY_ORDER = "p_order";
	public static final String P_KEY_QUESTION = "question";
	public static final String P_KEY_ANSWER = "answer";
	public static final String P_KEY_TRA_ID = "track_id";
	
	// table Stats
	public static final String S_KEY_ID = "_id";
	public static final String S_KEY_TRA_ID = "track_id";
	public static final String S_KEY_PLAY_COUNT = "play_count";
	public static final String S_KEY_SUM_TIME = "sum_time";
	public static final String S_KEY_BEST_TIME = "best_time";
	public static final String S_KEY_AVG_TIME = "avg_time";
	public static final String S_KEY_SUM_LEN = "sum_len";
	public static final String S_KEY_BEST_LEN = "best_len";
	public static final String S_KEY_AVG_LEN = "avg_len";
	
	private static final String TAG = "TrackDBAdapter";
	private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String DATABASE_NAME = "data";
    /** Name of Tracks table **/
    private static final String TRACK_TABLE_NAME = "tracks";
    /** Name of Points table **/
	private static final String POINTS_TABLE_NAME = "points";
	/** Name of Stats table **/
	private static final String STATS_TABLE_NAME = "stats"; 
    private static final int DATABASE_VERSION = 6;
    
    /** String for creating table Tracks **/
    private static final String CREATE_TABLE_TRACKS =
            "create table "+ TRACK_TABLE_NAME 
            + "(_id integer primary key autoincrement, "
            + T_KEY_NAME + " text not null);"
            ;
    
    /** String for creating table Points **/
    private static final String CREATE_TABLE_POINTS =
            "create table " + POINTS_TABLE_NAME 
            + "(_id integer primary key autoincrement, " 
            + P_KEY_NAME +" text not null, "
            + P_KEY_GPSLA + " integer not null, "
            + P_KEY_GPSLO + " integer not null, "
            + P_KEY_ORDER + " integer not null, "
            + P_KEY_QUESTION + " text, "
            + P_KEY_ANSWER + " text, "
            + P_KEY_TRA_ID + " integer, "
            + " foreign key("+P_KEY_TRA_ID+") references "+TRACK_TABLE_NAME+"("+T_KEY_ID+")"
            + ");"
            ;
    
    /** String for creating table Stats **/
    private static final String CREATE_TABLE_STATS = 
    		"create table " + STATS_TABLE_NAME + "("
    		+ S_KEY_ID + " integer primary key autoincrement, "
    		+ S_KEY_TRA_ID + " integer, "
    		+ S_KEY_PLAY_COUNT + " integer, "
    		+ S_KEY_SUM_TIME + " integer, "
    		+ S_KEY_BEST_TIME + " integer, "
    		+ S_KEY_AVG_TIME + " integer, "
    		+ S_KEY_SUM_LEN + " double, "
    		+ S_KEY_BEST_LEN + " double, "
    		+ S_KEY_AVG_LEN + " double, "
    		+ " foreign key("+S_KEY_TRA_ID+") references "+TRACK_TABLE_NAME+"("+T_KEY_ID+")"
    		+ ");"
    		;
    
    
    private final Context mCtx;
    
    private static final int SO_PLUS = 1;
    private static final int SO_MINUS = -1;
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
    	
    	DatabaseHelper(Context context) {
    		super(context, DATABASE_NAME, null, DATABASE_VERSION);
    	}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_TRACKS);
			db.execSQL(CREATE_TABLE_POINTS);
			db.execSQL(CREATE_TABLE_STATS);
			//create summary statistic record
			ContentValues values = new ContentValues();
			values.put(S_KEY_TRA_ID, 0);
			values.put(S_KEY_PLAY_COUNT, 0);
			values.put(S_KEY_BEST_TIME, 0);
			values.put(S_KEY_SUM_TIME, 0);
			values.put(S_KEY_AVG_TIME, 0);
			values.put(S_KEY_BEST_LEN, 0);
			values.put(S_KEY_SUM_LEN, 0);
			values.put(S_KEY_AVG_LEN, 0);
			long a = db.insert(STATS_TABLE_NAME, null, values);
			Log.i("DB", "SummaryStat Index:"+a);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TRACK_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + POINTS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + STATS_TABLE_NAME);
            onCreate(db);
		}
    }
	
    public TrackDBAdapter(Context ctx) {
    	this.mCtx = ctx;
    }
    
    public TrackDBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    /*
     * Metody pro praci s tabulkou Tracks
     */
    /**
     * Create record in Tracks table.
     * Autocreate Stat record.
     * @param name Name of new Track
     * @return Database ID of new Track
     */
    public long createTrack(String name) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(T_KEY_NAME, name);
    	long tId = mDb.insert(TRACK_TABLE_NAME, null, initialValues);
    	createStat(tId);
    	return tId;
    }
    
    public boolean updateTrack(long rowId, String name) {
    	ContentValues arg = new ContentValues();
    	arg.put(T_KEY_NAME, name);
    	
    	return mDb.update(TRACK_TABLE_NAME, arg, T_KEY_ID +"="+ rowId, null) > 0;
    }
    
    public boolean deleteTrack(long rowId) {
    	Cursor c = fetchTrackPoints(rowId);
    	for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
    		mDb.delete(POINTS_TABLE_NAME, P_KEY_ID +"="+ c.getInt(c.getColumnIndexOrThrow(P_KEY_ID)), null);
    	}
    	mDb.delete(STATS_TABLE_NAME, S_KEY_TRA_ID +"="+rowId, null);
    	return mDb.delete(TRACK_TABLE_NAME, T_KEY_ID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllTracks() {
    	return mDb.query(TRACK_TABLE_NAME, new String[] {T_KEY_ID, T_KEY_NAME}, null, null, null, null, null);
    }
    
    public Cursor fetchTrack(long rowId) throws SQLException {
    	Cursor mCursor = mDb.query(true, TRACK_TABLE_NAME, new String[] {T_KEY_ID,
                        T_KEY_NAME}, T_KEY_ID + "=" + rowId, null,
                        null, null, null, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
            }
            return mCursor;
    }
    
    /*
     * Metody pro praci s tabulkou Points
     */
    public long createPoint(String name, int lat, int lon, int ord, String que, String ans, long tid) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(P_KEY_NAME, name);
    	initialValues.put(P_KEY_GPSLA, lat);
    	initialValues.put(P_KEY_GPSLO, lon);
    	initialValues.put(P_KEY_ORDER, ord);
    	initialValues.put(P_KEY_QUESTION, que);
    	initialValues.put(P_KEY_ANSWER, ans);
    	initialValues.put(P_KEY_TRA_ID, tid);
    	solveOrder(tid, ord, SO_PLUS);
    	return mDb.insert(POINTS_TABLE_NAME, null, initialValues);
    }
    
    public long createPoint(ContentValues initialValues) {
    	return mDb.insert(POINTS_TABLE_NAME, null, initialValues);
    }
    
    public boolean updatePoint(long pId, String name, int ord, String que, String ans, long tId) {
    	solveOrder(tId, ord, SO_PLUS);
    	ContentValues val = new ContentValues();
    	val.put(P_KEY_NAME, name);
    	val.put(P_KEY_ORDER, ord);
    	val.put(P_KEY_QUESTION, que);
    	val.put(P_KEY_ANSWER, ans);
    	return mDb.update(POINTS_TABLE_NAME, val, P_KEY_ID +"="+ pId, null) > 0;
    }
    
    private void solveOrder(long tId, int ord, int plusminus) {
    	Cursor co = mDb.query(POINTS_TABLE_NAME, new String[] {P_KEY_ID, P_KEY_ORDER, P_KEY_TRA_ID},
    			P_KEY_TRA_ID + "=" + tId + " AND " + P_KEY_ORDER + ">=" + ord,
    			null, null, null, P_KEY_ORDER);
    	for (co.moveToFirst(); !co.isAfterLast(); co.moveToNext()) {
    		Log.i("DB", "menim order bodu:"+co.getLong(co.getColumnIndex(P_KEY_ID)));
    		ContentValues inval = new ContentValues();
    		inval.put(P_KEY_ORDER, co.getInt(co.getColumnIndex(P_KEY_ORDER))+plusminus);
    		mDb.update(POINTS_TABLE_NAME, inval, P_KEY_ID + "=" + co.getLong(co.getColumnIndex(P_KEY_ID)), null);
    	}
    }
    
    public boolean deletePoint(long pId) {
    	Cursor c = fetchPoint(pId);
    	solveOrder(c.getLong(c.getColumnIndex(P_KEY_TRA_ID)), c.getInt(c.getColumnIndex(P_KEY_ORDER)), SO_MINUS);
    	return mDb.delete(POINTS_TABLE_NAME, P_KEY_ID +"="+ pId, null) > 0;
    }
    
    public Cursor fetchTrackPoints(long tId) {
    	return mDb.query(POINTS_TABLE_NAME, new String[] {P_KEY_ID, P_KEY_GPSLA, P_KEY_GPSLO, P_KEY_NAME, P_KEY_ORDER, P_KEY_ORDER, P_KEY_QUESTION, P_KEY_ANSWER}, 
    			P_KEY_TRA_ID+" = "+tId, null, null, null, null);
    }
    
    public Cursor fetchPoint(long pId) {
    	Cursor mCursor = mDb.query(true, POINTS_TABLE_NAME, new String[] {P_KEY_ID, P_KEY_GPSLA, P_KEY_GPSLO, P_KEY_NAME, P_KEY_ORDER, P_KEY_QUESTION, P_KEY_ANSWER, P_KEY_TRA_ID},
                P_KEY_ID + "=" + pId, null, null, null, null, null);
    	if (mCursor != null) {
    		mCursor.moveToFirst();
    	}
    	return mCursor;
    }
    
    public Cursor fetchPoint(String pName, long tId) {
    	Cursor mCursor = mDb.query(POINTS_TABLE_NAME, new String[] {P_KEY_ID}, 
    			P_KEY_NAME+"=\""+pName+"\" AND "+P_KEY_TRA_ID+"="+tId,
    			null, null, null, null);
    	if (mCursor != null) {
    		mCursor.moveToFirst();
    	}
    	return mCursor;
    }
    
    
    /*
     * Metody pro praci s tabulkou Stats
     */
    /**
     * Create one Stat record. Call while create Track.
     * Initial values is null except Track ID. 
     * @param tid Track ID 
     * @return Database ID of created record.
     */
    public long createStat(long tid) {
    	Log.i(TAG, "Create Stat record for track "+tid);
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(S_KEY_PLAY_COUNT, 0);
    	initialValues.put(S_KEY_BEST_TIME, 0);
    	initialValues.put(S_KEY_SUM_TIME, 0);
    	initialValues.put(S_KEY_AVG_TIME, 0);
    	initialValues.put(S_KEY_BEST_LEN, 0);
    	initialValues.put(S_KEY_SUM_LEN, 0);
    	initialValues.put(S_KEY_AVG_LEN, 0);
    	initialValues.put(S_KEY_TRA_ID, tid);
    	return mDb.insert(STATS_TABLE_NAME, null, initialValues);
    }
    
    public boolean updateStat(long sId, long newtime, double newlen) {
    	//update summary statistic record too
    	Cursor sums = fetchSumStat();
    	sums.moveToFirst();
    	ContentValues sumval = new ContentValues();
		int sumpc = sums.getInt(sums.getColumnIndex(S_KEY_PLAY_COUNT))+1;
		//time sum
		long sumt = sums.getLong(sums.getColumnIndex(S_KEY_SUM_TIME))+newtime;
    	sumval.put(S_KEY_SUM_TIME, sumt);
    	//time best
    	if (sumpc > 1) {
    		if (sums.getLong(sums.getColumnIndex(S_KEY_BEST_TIME)) > newtime)
    			sumval.put(S_KEY_BEST_TIME, newtime);
    	} else {
    		sumval.put(S_KEY_BEST_TIME, newtime);
    	}
    	//time avg
		sumval.put(S_KEY_AVG_TIME, sumt/sumpc);
		//length sum
		double suml = sums.getDouble(sums.getColumnIndex(S_KEY_SUM_LEN))+newlen;
    	sumval.put(S_KEY_SUM_LEN, suml);
    	//len best
    	if (sumpc > 1) {
    		if (sums.getDouble(sums.getColumnIndex(S_KEY_BEST_LEN)) > newlen)
    			sumval.put(S_KEY_BEST_LEN, newlen);
    	} else {
    		sumval.put(S_KEY_BEST_LEN, newlen);
    	}
    	
    	//len avg
		sumval.put(S_KEY_AVG_LEN, suml/sumpc);
		//count
    	sumval.put(S_KEY_PLAY_COUNT, sumpc);
		sums.close();
    	mDb.update(STATS_TABLE_NAME, sumval, S_KEY_ID+"=1", null);
		
    	Cursor s = fetchStat(sId);
    	s.moveToFirst();
    	ContentValues val = new ContentValues();
    	//count
    	int pc = s.getInt(s.getColumnIndex(S_KEY_PLAY_COUNT))+1;
    	val.put(S_KEY_PLAY_COUNT, pc);
    	//time sum
    	long t = s.getLong(s.getColumnIndex(S_KEY_SUM_TIME))+newtime;
    	val.put(S_KEY_SUM_TIME, t);
    	//best time
    	if (pc > 1) {
    		if (s.getLong(s.getColumnIndex(S_KEY_BEST_TIME)) > newtime)
    			val.put(S_KEY_BEST_TIME, newtime);
    	} else {
    		val.put(S_KEY_BEST_TIME, newtime);
    	}
    	//time avg
    	val.put(S_KEY_AVG_TIME, t/pc);
    	//length sum
    	double l = s.getDouble(s.getColumnIndex(S_KEY_SUM_LEN))+newlen;
    	val.put(S_KEY_SUM_LEN, l);
    	//length best
    	if (pc > 1) {
    		if (s.getDouble(s.getColumnIndex(S_KEY_BEST_LEN)) > newlen)
    			val.put(S_KEY_BEST_LEN, newlen);
    	} else {
    		val.put(S_KEY_BEST_LEN, newlen);
    	}
    	//length avg
    	val.put(S_KEY_AVG_LEN, l/pc);
    	
    	return mDb.update(STATS_TABLE_NAME, val, S_KEY_ID + " = " + sId, null) > 0;
    }
    
    public boolean deleteStat(long sId) {
    	return mDb.delete(STATS_TABLE_NAME, S_KEY_ID + " = " + sId, null) > 0;
    }
    
    public Cursor fetchTrackStat(long tId) {
    	return mDb.query(STATS_TABLE_NAME, new String[] {S_KEY_ID, S_KEY_PLAY_COUNT, S_KEY_SUM_TIME, 
    			S_KEY_BEST_TIME, S_KEY_AVG_TIME, S_KEY_SUM_LEN, S_KEY_BEST_LEN, S_KEY_AVG_LEN}, 
    			S_KEY_TRA_ID+"="+tId, null, null, null, null);
    }
    
    public Cursor fetchStat(long sId) {
    	return mDb.query(STATS_TABLE_NAME, new String[] {S_KEY_ID, S_KEY_PLAY_COUNT, S_KEY_SUM_TIME, 
    			S_KEY_BEST_TIME, S_KEY_AVG_TIME, S_KEY_SUM_LEN, S_KEY_BEST_LEN, S_KEY_AVG_LEN}, 
    			S_KEY_ID+"="+sId, null, null, null, null);
    }
    
    public Cursor fetchSumStat() {
    	return mDb.query(STATS_TABLE_NAME, new String[] {S_KEY_ID, S_KEY_PLAY_COUNT, S_KEY_SUM_TIME, 
    			S_KEY_BEST_TIME, S_KEY_AVG_TIME, S_KEY_SUM_LEN, S_KEY_BEST_LEN, S_KEY_AVG_LEN}, 
    			S_KEY_ID+"=1", null, null, null, null);
    }
}
