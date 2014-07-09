package pb.ibp.DefeatMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DirectedLocationOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import pb.ibp.DefeatMap.stat_act.Tabhost_Act;
import pb.ibp.DefeatMap.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gamooga.client.ConnectCallback;
import com.gamooga.client.GamoogaClient;
import com.gamooga.client.MessageCallback;

/**
 * Aktivita pro herní obrazovku
 * @author Petr Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class Game_Act extends Activity{

	public static final String TAG = "ibp.Game_Act";
	
	public static final int GAME_TYPE_CLA = 0;
	public static final int GAME_TYPE_TIM = 1;
	public static final int GAME_TYPE_ORD = 2;
	public static final int GAME_TYPE_BLI = 3;
	
	public static final int GAME_SINGLE = 0;
	public static final int GAME_MULTI = 1;
	
	private static final int dial_que = 0x01;
	private static final int dial_win = 0x02;
	private static final int dial_wait = 0x03;
	private static final int dial_mult = 0x04;
	private static final int dial_user = 0x05;
	
	private static final int RESULT_STAT = 0x0005;
	
	public static final String KEY_GMTP = "gm_type";
	public static final String KEY_ISQU = "is_que";
	public static final String KEY_GMTI = "gm_tim";
	public static final String KEY_TRNM = "tr_name";
	public static final String KEY_POIS	= "tr_pois";
	
	public static final String KEY_P_NM = "p_name";
	public static final String KEY_P_LA = "p_lat";
	public static final String KEY_P_LO = "p_lon";
	public static final String KEY_P_OR = "p_odr";
	public static final String KEY_P_QE = "p_que";
	public static final String KEY_P_AN = "p_ans";
	
	/** map */
	private MapView osmView;						
	/** map controller **/
	private MapController mOsmViewController;		
	/** replace default bitmap by own from resource **/
	private ResourceProxy mResourceProxy;
	/** user location **/
	private SimpleLocationOverlay mLocationOverlay;			
	/** direction (game type Blind) **/
	private DirectedLocationOverlay mDirectionOverlay;
	/** opponents location **/
	private ItemizedOverlay<OverlayItem> mOponentOverlay;
	/** list of opponents overlay items **/
	private List<OverlayItem> mOponentList = new ArrayList<OverlayItem>();
	/** track points **/
	private ItemizedOverlay<OverlayItem> mItemPointOverlay;	
	/** list of points overlay items **/
	private List<OverlayItem> mPointsList = new ArrayList<OverlayItem>();
	/** List of reached points from track points **/
	private List<Long> mReachedPoints = new ArrayList<Long>();
	private LocationManager locationManager;
	private LocationListener loclis = new GeoUpdateHandler();
	private Location loc;
	Dialog dial;
	
	ArrayList<user_state_list> usl  = new ArrayList<Game_Act.user_state_list>(); 
	private int us_ind = 0; 
	
	public GamoogaClient gc;
	private static final int APPID = 66;
	private static final String UUID = "2c7ca30c-7c1b-11e1-a472-f23c91df4bc1";	
	
	private String mMyPlayerId;
	private String mPlayerName;
	private int mUrsWinOrd = 0;
	
	private TrackDBAdapter mDbHelper;
	/** Game state: Play/Pause = true/false **/
	private boolean mPPState;
	/** Is enable questions on point **/
	private boolean mIsEnableQue;
	/** Database id of current Track **/
	private Long mTraId;
	/** Game single/multi **/
	private long mGame;
	/** Game type **/
	private int mGmType;
	/** Time for "InTime" game type **/
	private Long mEndTim;
	/** Multiplayer role (server/client) **/
	private int mRole;
	public static final int ROLE_SER = 0x01;
	public static final int ROLE_CLI = 0x02;
	
	/** User list for multiplayer dialog **/
	ListView mDial_ulview;
	Button mDial_stbtn;
	boolean dialshown = false;
	
	private boolean mReceive;
	
	private String Question, Answer;
	private boolean CorrectAns = false;
	
	private SharedPreferences mPrefs;
	
	private TextView mTimeView;
	private Timer mTimer = new Timer();
	private myTimerTask mTimerTsk = new myTimerTask();
	private long mCurrTime;
	private double mTrLength;
	
	private Intent mIntentGPSService;
	private GPSService mBoundService;
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((GPSService.LocalBinder) service).getService();
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		
		mDbHelper = new TrackDBAdapter(this);
		mDbHelper.open();
		mPPState = true;
				
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    	Bundle extras = getIntent().getExtras();
        mTraId = extras.getLong(TrackDBAdapter.T_KEY_ID);
        mIsEnableQue = extras.getBoolean("QueEnable");
        
        mGame = extras.getInt("Game");
        mGmType = extras.getInt("GameType");
        mEndTim = extras.getLong("Game_Time");
        if (mGame == GAME_MULTI) {
        	mPlayerName = extras.getString("Player_Name");
        	mRole = extras.getInt("Role");
        	
        	if (mRole == ROLE_CLI) {
        		mTraId = null;
        	}
        }
              
        if (mGmType == GAME_TYPE_TIM)
        	mCurrTime = mEndTim;
        else
        	mCurrTime = 0;
        mTrLength = 0;
        Log.i(TAG, "type:"+mGmType+" maxtime:"+mEndTim +" isQueEna:"+mIsEnableQue);
        
        if (mGame == GAME_MULTI) {
	        this.gc = new GamoogaClient();
			gc.enableLogMsg();
			gc.onconnect(connectCallback);
			if (mRole == ROLE_CLI) {
				gc.onmessage("sendtrack", trackMessage);
				gc.onmessage("start", startMessage);
			}
			gc.onmessage("your_id", youridMessage);
			gc.onmessage("userlist", userlistMessage);
			gc.onmessage("ready", readyMessage);
			gc.onmessage("pos", posMessage);
			gc.onmessage("test", new MessageCallback() {
				public void handle(Object arg0) {
					Log.i(TAG, "test:"+arg0);
				}
			});		
		
			if (mRole == ROLE_SER) {
				gc.createConnectToSession(APPID, UUID);
			} else {
				int sess_id = extras.getInt("Sess_Id");
				gc.connectToSession(sess_id, UUID);
			}
		}
        

        initMyLocation();
		
        runOnUiThread(new Runnable() {
			public void run() {
				osmView = (MapView) findViewById(R.id.gm_mapView);
				osmView.setTileSource(TileSourceFactory.MAPNIK);
				osmView.setKeepScreenOn(mPrefs.getBoolean("gui_keep_screen_on", true));	
				osmView.setBuiltInZoomControls(true);
				osmView.setMultiTouchControls(true);
				mOsmViewController = osmView.getController();
				mResourceProxy = new ResourceProxyImpl(getApplicationContext());
			}
        });
		
		createOverlays();
		
		mReachedPoints.clear();
		Question = Answer = null;
		
		registerReceiver(GPointReceiver,
				new IntentFilter(getString(R.string.gps_service_intent)));
		mReceive = true;
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
	                0, loclis);
		
		mIntentGPSService = new Intent(this, GPSService.class);
		mIntentGPSService.putExtra(TrackDBAdapter.T_KEY_ID, mTraId);
		startService(mIntentGPSService);
		
		mTimeView = (TextView)findViewById(R.id.gm_TimeView);
		
		final Button pause_btn = (Button)findViewById(R.id.gm_pausebtn);
		if (mGame == GAME_MULTI)
			pause_btn.setEnabled(false);
		pause_btn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mPPState = !mPPState; 
				if (mPPState) {
					pause_btn.setText(R.string.gm_pause);
				} else {
					pause_btn.setText(R.string.gm_play);
				}
			}
		});
	}


	@Override
	protected void onStart() {
		super.onStart();
		
		refreshPointsOverlay();
		
	
		int minlat = Integer.MAX_VALUE;
		int maxlat = Integer.MIN_VALUE;
		int minlon = Integer.MAX_VALUE;
		int maxlon = Integer.MIN_VALUE;
		
		for (Iterator<OverlayItem> it = mPointsList.iterator(); it.hasNext(); ) {
			GeoPoint poi = it.next().getPoint();
			if (poi.getLatitudeE6() < minlat)
				minlat = poi.getLatitudeE6();
			if (poi.getLatitudeE6() > maxlat)
				maxlat = poi.getLatitudeE6();
			if (poi.getLongitudeE6() < minlon)
				minlon = poi.getLongitudeE6();
			if (poi.getLongitudeE6() > maxlon)
				maxlon = poi.getLongitudeE6();
		}
		
		int latspan = maxlat-minlat;
		int lonspan = maxlon-minlon;
		mOsmViewController.setCenter(new GeoPoint(minlat+latspan/2, minlon+lonspan/2));
		//mOsmViewController.zoomToSpan(latspan, lonspan); //nechce fungovat*/
		mOsmViewController.setZoom(17);
	}


	
	@Override
	protected void onStop() {
		super.onStop();
		mTimer.cancel();
		mTimer.purge();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
				
		if (mReceive)
			unregisterReceiver(GPointReceiver);
	}

	/**
	 * Metoda pro zjištìní poèáteèní pozice
	 */
	private void initMyLocation() {
		final Runnable showWaitDialog = new Runnable() {
			public void run() {
				while (loc == null) {
					;	//dokud nemá platnou pozici zobrazuje dialog èekání
				}
				if (mGame == GAME_SINGLE) {
					dismissDialog(dial_wait);
					locationManager.removeUpdates(loclis);
					doBindService();
				} else {
					locationManager.removeUpdates(loclis);
					try {
						GeoPoint p = new GeoPoint(loc);
						JSONObject jo = new JSONObject();
						jo.put("p_lat", p.getLatitudeE6());
						jo.put("p_lon", p.getLongitudeE6());
						gc.send("first_pos", jo);
						Log.i(TAG, "poslalo first_pos");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		};
		if (mGame == GAME_SINGLE)
			showDialog(dial_wait);
		Log.i(TAG, "Start init location");
		Thread t = new Thread(showWaitDialog);
		t.start();
		while (loc != null) {
			//mLocationOverlay.enableMyLocation();
		}
		
	}

	/**
	 * metoda vytvoøení pøekrytí mapy
	 */
	private void createOverlays() {
		/*mLocationOverlay = new MyLocationOverlay(this.getBaseContext(), this.osmView);
		mLocationOverlay.enableMyLocation();
		mLocationOverlay.followLocation(true);*/
		mLocationOverlay = new SimpleLocationOverlay(Game_Act.this);
		mLocationOverlay.setEnabled(true);
		osmView.getOverlays().add(mLocationOverlay);
		
		if (mGmType == GAME_TYPE_BLI) {
			mDirectionOverlay = new DirectedLocationOverlay(Game_Act.this, mResourceProxy);
			mDirectionOverlay.setShowAccuracy(false);
			osmView.getOverlays().add(mDirectionOverlay);
		}
		
		mItemPointOverlay = new ItemizedOverlay<OverlayItem>(this.getBaseContext(), mPointsList, 
				new ItemizedOverlay.OnItemGestureListener<OverlayItem>() {

					public boolean onItemSingleTapUp(int arg0, OverlayItem arg1) {
						return false;
					}

					public boolean onItemLongPress(int arg0, OverlayItem arg1) {
						return false;
					}
					
				});
		osmView.getOverlays().add(mItemPointOverlay);
		
		if (mGame == GAME_MULTI) {
			mOponentOverlay = new ItemizedOverlay<OverlayItem>(Game_Act.this, mOponentList, 
					new ItemizedOverlay.OnItemGestureListener<OverlayItem>() {

						public boolean onItemLongPress(int arg0, OverlayItem arg1) {
							return false;
						}

						public boolean onItemSingleTapUp(int arg0, OverlayItem arg1) {
							us_ind = arg0;
							showDialog(dial_user);
							return false;
						}
			});
			osmView.getOverlays().add(mOponentOverlay);
		}
	}
	
	
	private void doBindService() {
		mIntentGPSService.putExtra(TrackDBAdapter.T_KEY_ID, mTraId);
		mIntentGPSService.putExtra("Game_Type", mGmType);
		mIntentGPSService.putExtra("MyLocation", loc);	
		mIntentGPSService.putExtra("end_time", mEndTim);
		
		mTimer.schedule(mTimerTsk, 0, 1000);
		
		bindService(mIntentGPSService, mConnection, Context.BIND_AUTO_CREATE);		
	}
	
	private void doUnbindService() {
		if (mBoundService != null)
			unbindService(mConnection);
		unregisterReceiver(GPointReceiver);
		mReceive = false;
	}
	
	/**
	 * Refresh overlay item list of track points
	 */
	private void refreshPointsOverlay() {
		if (mGame == GAME_MULTI && mRole == ROLE_CLI && mTraId == null)
			return;
		
		mPointsList.clear();
		Cursor c = mDbHelper.fetchTrackPoints(mTraId);
		startManagingCursor(c);
		
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String p_name = c.getString(c.getColumnIndex(TrackDBAdapter.P_KEY_NAME)); //nazev bodu
			
			GeoPoint p = new GeoPoint(c.getInt(c.getColumnIndex(TrackDBAdapter.P_KEY_GPSLA)), 
					c.getInt(c.getColumnIndex(TrackDBAdapter.P_KEY_GPSLO)));		//souradnice bodu
			
			OverlayItem i = new OverlayItem(p_name, "", p);
			if (mGmType != GAME_TYPE_BLI) //zakazano pro debug TODO
				i.setMarker(getResources().getDrawable(R.drawable.star_yellow));	//nastavi neoznaceny bod
			long pid = c.getLong(c.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_ID));
			for (Iterator<Long> it = mReachedPoints.iterator(); it.hasNext(); ) {	//pokud je bod v seznamu dosazenych
				if (pid == it.next()) {
					i.setMarker(getResources().getDrawable(R.drawable.star_green));	//nastavi oznaceny bod
					break;
				}
			} 
			mPointsList.add(i);		//prida bod do seznamu
		}
		c.close();

	}
	
	/**
	 * Refresh overlay item list of Players 
	 */
	private void refreshPlayerOverlay() {
		mOponentList.clear();
		
		OverlayItem i;
		for (Iterator<user_state_list> iterator = usl.iterator(); iterator.hasNext(); ) {
			user_state_list a = iterator.next();
			if (a.id.equals(mMyPlayerId)) //vynecha moji polohu TODO debug
				continue;
			GeoPoint p = new GeoPoint(a.p_lat, a.p_lon);
			i = new OverlayItem(a.name, "", p);
			i.setMarker(getResources().getDrawable(R.drawable.person_pink));
			
			mOponentList.add(i);	//prida oponenta k zobrazeni
		}
		
	}
	
	/**
	 * Pøijímaè broadcastu od GPSService
	 */
	private BroadcastReceiver GPointReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extra = intent.getExtras();
						
			if (!extra.containsKey("cur_loc")) 
				return;
			
			// nová poloha
			GeoPoint loc = new  GeoPoint((Location) extra.get("cur_loc"));
				
			
			if (mGame == GAME_MULTI) {
				JSONObject jo = new JSONObject();
				try {
					jo.put("p_lat", loc.getLatitudeE6());
					jo.put("p_lon", loc.getLongitudeE6());
					jo.put("p_pass", mReachedPoints.size());
					//odesle novou hracovu pozici
					gc.send("pos", jo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			//novy dosazeny bod
			if (extra.getBoolean("ReachPoint")) {
				long pid = extra.getLong("rp_Id");
				Log.i("Game_Act", "predany bod: "+pid);
				mReachedPoints.add(pid);	//pridá do seznamu
				refreshPointsOverlay();
				if (mIsEnableQue) {
					Cursor c = mDbHelper.fetchPoint(pid);
					Question = c.getString(c.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_QUESTION));
					Answer = c.getString(c.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_ANSWER));
					if (Question != null) {
						while (!CorrectAns)
							showDialog(dial_que); //zobrazi dialog s otázkou
					}
					Question = Answer = null;
					CorrectAns = false;
				}
			}
			//pri typu hry "Naslepo"
			if (mGmType == GAME_TYPE_BLI && extra.getBoolean("NearestPoint")) {
				float np_bearing = extra.getFloat("np_bearing");
				mDirectionOverlay.setLocation(loc);
				Log.i("Game_Act", "bearing:"+np_bearing);
				mDirectionOverlay.setBearing(np_bearing);	//zobrazí smìr k nejbližšímu bodu	
			}
			
			mTrLength = extra.getDouble("tr_length");
			
			mLocationOverlay.setLocation(loc);
			mOsmViewController.animateTo(loc);
			
			//pokud je hra dokoncena
			if (extra.getBoolean("Win_Game")) {
				if (mGame == GAME_SINGLE)
					showDialog(dial_win);	//zobrazi dialog vyhry
				else {
					try {
						gc.send("win", null);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				mTimer.cancel();	//ukonèí poèitadlo èasu
				doUnbindService();	//zastaví GPSService
			}
		}
	};
	
	/**
	 * Task for display current Game time
	 * @author Petr
	 *
	 */
	private class myTimerTask extends TimerTask {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				public void run() {
					if (!mPPState) //hra je pozastavena
						return;
					if (mGmType == GAME_TYPE_TIM) {
						mTimeView.setText(new MyTime(mCurrTime).toString());
						mCurrTime -= 1000;
						if (mCurrTime == 0) {
							showDialog(dial_win);
							mTimer.cancel();
							doUnbindService();
						}
					} else {
						mTimeView.setText(new MyTime(mCurrTime).toString()); 
						mCurrTime += 1000;
					}
					//Log.i(TAG, "Timiiii:" + mCurrTime);
				}
			});
			
		}
	}
	
	
	/**
	 * Handler for Connect
	 */
	ConnectCallback connectCallback = new ConnectCallback() {
		public void handle() {
			runOnUiThread(new Runnable() {
				public void run() {
					if (mRole == ROLE_SER) {
						//mRoom_Text.setText(""+gc.getSessId());
					}
				
					try {
						gc.send("user_name", mPlayerName);
						if (mRole == ROLE_CLI) 	//client konci, pouze server posila track
							return;
						
						myJSONTrack data = new myJSONTrack(mGmType, mIsEnableQue, mEndTim, mTraId);
						gc.send("track_data", data);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					//Toast.makeText(Game_Act.this, R.string.waitGPS, Toast.LENGTH_SHORT).show();
					
					showDialog(dial_mult);
					dialshown = true;
				}
			});			
		}
	};
	
	/**
	 * Handler for Your Id message
	 */
	MessageCallback youridMessage = new MessageCallback() {
		public void handle(Object data) {
			JSONObject jo = (JSONObject)data;
			try {
				mMyPlayerId = jo.getString("id");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};
	
	/**
	 * Handler for UserList message
	 */
	MessageCallback userlistMessage = new MessageCallback() {
		public void handle(Object data) {
			Log.i(TAG, "prisel userlist:"+data);
			JSONObject jo = (JSONObject)data;
			//usl = new ArrayList<Game_Act.user_state_list>();
			usl.clear();
			for (Iterator it = jo.keys(); it.hasNext(); ) {
				String i_id = (String) it.next();
				JSONObject item;
				user_state_list usl_item = null;
				try {
					item = (JSONObject) jo.get(i_id);
					usl_item = new user_state_list(i_id, item.getString("name"), item.getBoolean("stat"), item.optInt("p_lat"), item.optInt("p_lon"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				usl.add(usl_item);
			}
			runOnUiThread(new Runnable() {
				public void run() {
					if (dialshown) {
						UserListAdapter adapter = new UserListAdapter(Game_Act.this, R.layout.userlist_item, usl);
						mDial_ulview.setAdapter(adapter);
					}
					
					refreshPlayerOverlay();
				}
			});
		}
	};
	
	/**
	 * Handler for Track message
	 */
	MessageCallback trackMessage = new MessageCallback() {
		public void handle(Object data) {			
			if (mRole == ROLE_CLI) {
				Log.i(TAG, "prišli track data: "+data.toString());
				JSONObject jo = (JSONObject)data;
				
				try {
					mGmType = jo.getInt("gm_type");
					mIsEnableQue = jo.getBoolean("is_que");
					mEndTim = jo.getLong("gm_tim");
					mTraId = createTrack(jo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				refreshPointsOverlay();
			}
		}
	};

	/**
	 * Handler for Ready message
	 */
	MessageCallback readyMessage = new MessageCallback() {
		public void handle(Object arg0) {
			runOnUiThread(new Runnable() {
				public void run() {
					mDial_stbtn.setEnabled(true);
				}
			});
		}
	};
	
	/**
	 * Handler for Start message
	 */
	MessageCallback startMessage = new MessageCallback() {
		public void handle(Object arg0) {
			dismissDialog(dial_mult);
			doBindService();
		}
	};
	
	/**
	 * Handler for User Position message
	 */
	MessageCallback posMessage = new MessageCallback() {
		public void handle(Object data) {
			JSONObject jo = (JSONObject)data;
			String rec_id = null;
			int rec_lat = 0, rec_lon = 0, rec_pass = 0;
			try {
				rec_id = jo.getString("id");
				rec_lat = jo.getInt("p_lat");
				rec_lon = jo.getInt("p_lon");
				rec_pass = jo.getInt("p_pass");
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.i(TAG, "prijata pozice od:"+rec_id);
			for (user_state_list a : usl) {
				if (a.id.equals(rec_id)) {
					a.p_lat = rec_lat; 
					a.p_lon = rec_lon;
					a.p_pass = rec_pass;
					break;
				}
			}
			
			refreshPlayerOverlay();
		}
	};
	
	/**
	 * Handler for You Win message
	 */
	MessageCallback youwinMessage = new MessageCallback() {
		public void handle(Object data) {
			JSONObject jo = (JSONObject)data;
			try {
				mUrsWinOrd = jo.getInt("ord");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			showDialog(dial_win);
		}
	};
	
	/**
	 * Handler for User Win message
	 */
	MessageCallback usrwinMessage = new MessageCallback() {
		public void handle(Object data) {
			JSONObject jo = (JSONObject)data;
			String usr_name = "";
			int usr_ord = 0;
			try {
				usr_name = jo.getString("name");
				usr_ord = jo.getInt("ord");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String text = getString(R.string.gm_toast_ursname) + usr_name
					+getString(R.string.gm_toast_ursord) + usr_ord;
			Toast.makeText(Game_Act.this, text, Toast.LENGTH_SHORT).show();
		}
	};
	
	/**
	 * Reprezentace odesílaných TrackData
	 * @author Petr
	 *
	 */
	public class myJSONTrack extends JSONObject {
		
		public myJSONTrack(int gt, boolean iq, Long tm, long tId) throws JSONException {
			this.accumulate(KEY_GMTP, gt);
			this.accumulate(KEY_ISQU, iq);
			this.put(KEY_GMTI, gt);
			
			Cursor t = mDbHelper.fetchTrack(tId);
			this.put(KEY_TRNM, t.getString(t.getColumnIndex(TrackDBAdapter.T_KEY_NAME)));
			
			JSONObject p;
			JSONArray pois = new JSONArray();
			
			Cursor tp = mDbHelper.fetchTrackPoints(tId);
			for (tp.moveToFirst(); !tp.isAfterLast(); tp.moveToNext()) {
				p = new JSONObject();
				p.put(KEY_P_NM, tp.getString(tp.getColumnIndex(TrackDBAdapter.P_KEY_NAME)));
				p.put(KEY_P_LA, tp.getInt(tp.getColumnIndex(TrackDBAdapter.P_KEY_GPSLA)));
				p.put(KEY_P_LO, tp.getInt(tp.getColumnIndex(TrackDBAdapter.P_KEY_GPSLO)));
				p.put(KEY_P_OR, tp.getInt(tp.getColumnIndex(TrackDBAdapter.P_KEY_ORDER)));
				p.put(KEY_P_QE, tp.getString(tp.getColumnIndex(TrackDBAdapter.P_KEY_QUESTION))); 
				p.put(KEY_P_AN, tp.getString(tp.getColumnIndex(TrackDBAdapter.P_KEY_ANSWER)));
				
				pois.put(p);
			}
			
			this.put(KEY_POIS, pois);
		}
	}
	
	/**
	 * On client side create Track in database
	 * @param jo JSONObject representing Track data
	 * @return database Track Id
	 * @throws JSONException
	 */
	public long createTrack(JSONObject jo) throws JSONException {
		long tid = mDbHelper.createTrack(jo.getString(KEY_TRNM));
		JSONArray pa = jo.getJSONArray(KEY_POIS);			
		for (int i = 0; i < pa.length(); i++) {
			JSONObject p = pa.getJSONObject(i);
			mDbHelper.createPoint(p.getString(KEY_P_NM), 
					p.getInt(KEY_P_LA), 
					p.getInt(KEY_P_LO), 
					p.getInt(KEY_P_OR), 
					p.optString(KEY_P_QE), 
					p.optString(KEY_P_AN), 
					tid);
		}
		return tid;
	}
	
	/**
	 * Tøída reprezentující jednoho uživatele
	 * @author Petr
	 *
	 */
	public class user_state_list {
		String id;
		String name;
		boolean state;
		Integer p_lat;
		Integer p_lon;
		Integer p_pass;
		Integer ord;
		public user_state_list(String id, String nm, boolean st, Integer la, Integer lo) {
			this.id = id;
			this.name = nm;
			this.state = st;
			this.p_lat = la;
			this.p_lon = lo;
			this.p_pass = 0;
			this.ord = 0;
		}
	}
	
	/**
	 * Tøída pro zjištìní poèáteèní pozice pro hru
	 * @author Petr
	 *
	 */
	public class GeoUpdateHandler implements LocationListener {

		public void onLocationChanged(Location location) {
			loc = location;
			//if (loc != null)
				
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {	
		}
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	//reakce na stisk tlaèítka zpìt
			doUnbindService();
			mTimer.cancel();
			mTimer.purge();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch (id) {
		case dial_que:		//dialog pro zobrazeni otazky
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.quedialog_title)
				.setMessage(Question)
				.setCancelable(false);
			final EditText ans_text = new EditText(getBaseContext());
			builder.setView(ans_text);
			builder.setPositiveButton(R.string.positive_btn_text, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					if (Answer.compareTo(ans_text.getText().toString()) != 0){
						CorrectAns = false;
					} else {
						CorrectAns = true;
					}
				}
			});
			dialog = builder.create();
			break;
		case dial_win:		//dialog vyhry 
			builder = new AlertDialog.Builder(this);
			String msg = "";
			if (mGame == GAME_MULTI) {
				msg = msg + getString(R.string.windialog_msgord) + mUrsWinOrd + "\n";
			}
			if (mGmType == GAME_TYPE_TIM && mCurrTime == 0)
				msg = msg + getString(R.string.windialog_msgpois) + mReachedPoints.size();
			else
				msg = msg + getString(R.string.windialog_msgtime) + new MyTime(mCurrTime).toString();
			msg = msg +"\n" + getString(R.string.windialog_msgtrlen) + String.format("%.2f", mTrLength) + getString(R.string.windialog_msgtrlen2);
			builder.setTitle(R.string.windialog_title)
				.setMessage(msg)
				.setCancelable(false)
				.setPositiveButton(R.string.windialog_statbtn, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent(Game_Act.this, Tabhost_Act.class);
						i.putExtra("traid", mTraId);
						startActivityForResult(i, RESULT_STAT);
					}
				})
				.setNegativeButton(R.string.windialog_exit, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
			saveStat();
			dialog = builder.create(); 
			break;
		case dial_wait:		//dialog cekani na GPS pozici u single player
			dialog = ProgressDialog.show(Game_Act.this, 
					getResources().getString(R.string.waitDialog_title), 	//set title
					getResources().getString(R.string.waitDialog_message),  //set message
					true, true, 											//set indeterminate and cancelable
					new DialogInterface.OnCancelListener() {				//handle cancel
						public void onCancel(DialogInterface dialog) {
							stopService(mIntentGPSService);
							locationManager.removeUpdates(loclis);
							finish();
						}
					});
			break;
		case dial_mult:		//uvodni dialog pro multiplayer, zobrazeni oponenty a gps stav
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.multigame_dialog);
			dialog.setTitle(R.string.multigameDialog_title);
			
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					stopService(mIntentGPSService);
					locationManager.removeUpdates(loclis);
					gc.disconnect();
					finish();
				}
			});
			
			EditText sessidtext = (EditText)dialog.findViewById(R.id.mgDial_gameidtext);
			sessidtext.setText(""+gc.getSessId());
			
			mDial_ulview = (ListView)dialog.findViewById(R.id.mgDial_list);
			UserListAdapter adapter = new UserListAdapter(Game_Act.this, R.layout.userlist_item, usl);
			mDial_ulview.setAdapter(adapter);
			
			mDial_stbtn = (Button)dialog.findViewById(R.id.mgDial_startbtn);
			mDial_stbtn.setEnabled(false);
			mDial_stbtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismissDialog(dial_mult);
					doBindService();
					try {
						gc.send("start", null);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			break;
		case dial_user:		//dialog informací o oponentovi
			builder = new AlertDialog.Builder(this);
			user_state_list usi = usl.get(us_ind);
			String user_msg = "";
			user_msg = getString(R.string.usrdialog_msgpois) + usi.p_pass + "\n" 
					+ getString(R.string.usrdialog_msgord) + usi.ord + "\n";
			builder.setTitle(usi.name)
				.setMessage(user_msg)
				.setPositiveButton(R.string.positive_btn_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(dial_user);
					}
				});
			dialog = builder.create();
			break;
		default:
			dialog = super.onCreateDialog(id); 
			
		}
		return dialog;
	}


	/**
	 * Save statistic for track
	 */
	private void saveStat() {
		Runnable runSaveStat = new Runnable() {
			public void run() {
				Cursor c = mDbHelper.fetchTrackStat(mTraId);
				c.moveToFirst();
				long newtime = mCurrTime;
				double newlen = mTrLength;
				
				mDbHelper.updateStat(c.getLong(c.getColumnIndex(TrackDBAdapter.S_KEY_ID)),
						newtime, 
						newlen);
			}
		};
		Thread t = new Thread(runSaveStat);
		t.start();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RESULT_STAT)
			finish();
	}
	
	

}
