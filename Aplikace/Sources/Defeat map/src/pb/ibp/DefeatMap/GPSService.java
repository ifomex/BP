package pb.ibp.DefeatMap;

import java.util.ArrayList;
import java.util.Iterator;

import org.osmdroid.util.GeoPoint;

import pb.ibp.DefeatMap.R;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class GPSService extends Service implements LocationListener {

	private static final int MAXIMUM_LOC_DISTANCE = 10;
		
	private static final String TAG = "ibp.GPSService";
	
	private boolean mGPSEnable;
	private long mGPSInterval; //in ms
	private TrackDBAdapter mDbHelper;
	/** Current Track Id **/
	private Long mTraID;
	/** Type of current game **/
	private int mGameType;
	private Location mLastLoca;
	//private long mLastTime;
	
	/** Order of last checked point **/
	private int mLastOrd;
	/** Location Manager **/
	private LocationManager mLocationManager;
	public ArrayList<gPoint> pointlist; 
	
	private int poi_check_count;
	private double track_len;
	
	private SharedPreferences mPrefs;
	/** Local Binder **/
	private final LocalBinder mBinder = new LocalBinder();
	
	public class LocalBinder extends Binder {
		public GPSService getService() {
			return GPSService.this;
		}
	}
		
	public void onLocationChanged(Location location) {
		Intent update = new Intent(getString(R.string.gps_service_intent));
				
		if (mTraID == null || pointlist == null)
			return;
		
		//overit cas
		if (mLastLoca.getTime() + mGPSInterval <= location.getTime()) {
		//porovnat pozici s points  
			GeoPoint cur_loc = new GeoPoint(location);
			Log.i(TAG, "location changed");
			
			//minimum distance
			int mindist = Integer.MAX_VALUE;
			//nearest point
			gPoint nearPoint = null;
			for (Iterator<gPoint> it = pointlist.iterator(); it.hasNext(); ) {
				gPoint poi = it.next();
				
				//overi jestli uz bod nebyl splnen
				if (poi.check)
					continue;
				
				//overit order
				if (mGameType == Game_Act.GAME_TYPE_ORD) {
					if( poi.order != mLastOrd) {
						continue;
					}
				}
				
				int distance = cur_loc.distanceTo(poi.getGeoPoint());
				
				// overi maximalni vzdalenost k bodu
				if (distance <= MAXIMUM_LOC_DISTANCE) {
						Log.i(TAG, "Reached poi: " + poi.dbId + " , coord.: "+ poi.getGeoPoint().toString());
						poi.check = true;
						poi_check_count++;
						//posle aktualne dosazeny bod
						update.putExtra("ReachPoint", true);
						update.putExtra("rp_Id", poi.dbId);
						if (mGameType == Game_Act.GAME_TYPE_ORD) 
							mLastOrd++;
				}
				//najde nejblizsi bod				
				if (distance < mindist) {
					mindist = distance;
					nearPoint = poi;
				}
			}//end for

			//posle smer k nejblizsimu bodu
			if (mGameType == Game_Act.GAME_TYPE_BLI) {
				update.putExtra("NearestPoint", true);
				update.putExtra("np_bearing", (float) cur_loc.bearingTo(nearPoint.getGeoPoint()));
			}
			
			
			//ulozit hodnoty pro statistiku
			track_len += location.distanceTo(mLastLoca);
			update.putExtra("tr_length", track_len);
			
			//posle soucasnou pozici
			update.putExtra("cur_loc", location);
			
			//	ulozit soucasnou pozici
			mLastLoca = location;
		}//end if
		
		
		//overit pocet splnenych bodu a
		//prekroceni casu pro hru Na cas
		if (poi_check_count == pointlist.size()) {
			Log.i(TAG, "All Point passed");
			//zastavit èinnost
			update.putExtra("Win_Game", true);
		} else 
			update.putExtra("Win_Game", false);
		
		sendBroadcast(update);
	}

	public void onProviderDisabled(String arg0) {
		mGPSEnable = false;
	}

	public void onProviderEnabled(String arg0) {
		mGPSEnable = true;
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		//do nothing
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "Binded");
		mTraID = arg0.getExtras().getLong(TrackDBAdapter.T_KEY_ID);	//track Id
		mGameType = arg0.getExtras().getInt("Game_Type");			//typ hry
		
		pointlist = new ArrayList<GPSService.gPoint>();
		Cursor c = mDbHelper.fetchTrackPoints(mTraID);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			gPoint p = new gPoint(new GeoPoint(c.getInt(c.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_GPSLA)),
					c.getInt(c.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_GPSLO))),
					c.getInt(c.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_ORDER)),
					false, 
					c.getLong(c.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_ID)));
			pointlist.add(p);
		}
		
		mLastLoca = (Location) arg0.getExtras().get("MyLocation");
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
		
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "unbind");
		mLocationManager.removeUpdates(this);
		stopSelf();
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(GPSService.this);
		
		mDbHelper = new TrackDBAdapter(this);
		mDbHelper.open();  
		
		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		mLastLoca = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		mGPSInterval = Long.parseLong( mPrefs.getString("gps_interval", "5") )*1000;
		Log.i(TAG, "gpsinterval:"+mGPSInterval);
		poi_check_count = 0;
		track_len = 0;
		mLastOrd = 0;
		Log.i(TAG, "created");
	}


	
	/**
	 * Class for game points
	 * @author Petr
	 *
	 */
	public class gPoint {
		/** GPS coords. **/
		GeoPoint poi;
		/** Game point order **/
		int order;
		/** Shows if user pass this point **/
		boolean check;
		/** Database id **/
		long dbId;
		
		public gPoint(GeoPoint p, int o, boolean c, long i) {
			this.poi = p;
			this.order = o;
			this.check = c;
			this.dbId = i;
		}
		
		public GeoPoint getGeoPoint() {
			return this.poi;
		}
	}
}
