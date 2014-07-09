package pb.ibp.DefeatMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import pb.ibp.DefeatMap.stat_act.Tabhost_Act;
import pb.ibp.DefeatMap.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Aktivita obrazovky pro editaci tratì
 * @author Petr Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class TraEdit_Act extends Activity{
	
	private static final int MENU_MY_LOC = Menu.FIRST;
	
	public static final String TRACK_ID = "t_id";
	public static final String P_LAT = "p_lat";
	public static final String P_LON = "p_lon";
	
	protected LocationManager mLocationManager;
	
	private MapView osmView;
	private MapController osmViewController;
	private MyLocationOverlay myLocationOverlay;
	private ItemizedOverlayWithFocus<OverlayItem> mItemPointOverlay;
	private List<OverlayItem> mPointsList  = new ArrayList<OverlayItem>();
	
	private TrackDBAdapter mDbHelper;
	
	private EditText mEditTrackName;
	private Long mRowId;
	
	private boolean mAddBtnClick = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDbHelper = new TrackDBAdapter(this);
		mDbHelper.open();
		
		setContentView(R.layout.track_edit);
		
		mEditTrackName = (EditText)findViewById(R.id.edtra_edittraname);
		
		mRowId = (savedInstanceState == null) ? null :
			(Long) savedInstanceState.getSerializable(TrackDBAdapter.T_KEY_ID);
		if (mRowId == null) {
        	Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong(TrackDBAdapter.T_KEY_ID) 
            		: null;
        }
		populateFields();
		
		saveState();
		
		osmView = (MapView) findViewById(R.id.edtra_mapview);
		osmView.setTileSource(TileSourceFactory.MAPNIK);
		//osmView.setBuiltInZoomControls(true);
		osmView.setMultiTouchControls(true);
		osmViewController = osmView.getController();
		
		this.applymaptouchlistener();
		
		createOverlays();
		
		osmViewController.setZoom(4);
		
		//currentPosition = myLocationOverlay.getMyLocation();
		osmViewController.setCenter(new GeoPoint(47146000, 11777344));	//vycentruje nad evropu
		
		/** Talèítko ruèního pøidávání bodù **/
		Button add_point_btn = (Button)findViewById(R.id.edtra_addbtn);
		add_point_btn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				TraEdit_Act.this.mAddBtnClick = true;
				Toast.makeText(TraEdit_Act.this, R.string.edtra_addbtn_toast, Toast.LENGTH_LONG).show();
			}
		});
		
		/** Tlaèítko generování bodù **/
		Button gen_point_btn = (Button)findViewById(R.id.edtra_genbtn);
		gen_point_btn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				IGeoPoint center = osmView.getMapCenter();
				int latSpan = osmView.getLatitudeSpan();
				int lonSpan = osmView.getLongitudeSpan() / 2;
				
				Random gen = new Random();
				int newlat =  gen.nextInt(latSpan) + (center.getLatitudeE6()-latSpan/2);
				int newlon =  gen.nextInt(lonSpan) + (center.getLongitudeE6()-lonSpan/2);

				Log.i("TraEdit_Act", "Vytvoren bod: " + newlat + ":" + newlon);
				long a = -1;
				Intent i = new Intent(TraEdit_Act.this, PointEdit_Act.class);
				i.putExtra(TrackDBAdapter.P_KEY_TRA_ID, mRowId);
				i.putExtra(TrackDBAdapter.P_KEY_GPSLA, newlat);
				i.putExtra(TrackDBAdapter.P_KEY_GPSLO, newlon);
				i.putExtra(TrackDBAdapter.P_KEY_ID, a);
				startActivity(i);
			}
		});
		
		
		Button save_btn = (Button)findViewById(R.id.edtra_savebtn);
		save_btn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				saveState();
				finish();
			}
		});
		
	}
	
	private void populateFields() {
		if(mRowId != null) {
			Cursor track = mDbHelper.fetchTrack(mRowId);
			startManagingCursor(track);
			mEditTrackName.setText(track.getString(
					track.getColumnIndexOrThrow(TrackDBAdapter.T_KEY_NAME)));
			track.close();
		}
	}
	
	
	@Override
	protected void onPause() {
		this.myLocationOverlay.disableMyLocation();
		mDbHelper.close();
		super.onPause();
		//saveState(); 
	}



	@Override
	protected void onResume() {
		super.onResume();
		mDbHelper.open();
		populateFields();
		
		refreshPointsOverlay();
	}
	

	@Override
	protected void onStop() {
		//mDbHelper.close();
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//saveState();
		outState.putSerializable(TrackDBAdapter.T_KEY_ID, mRowId);
	}

	private void saveState() {
		String name = mEditTrackName.getText().toString();
		
		if (mRowId == null) {
			long id = mDbHelper.createTrack(name);
			if (id > 0)
				mRowId = id;
		}
		mDbHelper.updateTrack(mRowId, name);
	}
	
	/**
	 * Metoda vytvoøení pøekrytí mapy
	 */
	private void createOverlays() {
		myLocationOverlay = new MyLocationOverlay(TraEdit_Act.this, this.osmView);
		osmView.getOverlays().add(myLocationOverlay);
		
		mItemPointOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this.getBaseContext(), mPointsList, 				 
				new ItemizedOverlay.OnItemGestureListener<OverlayItem>(){

					public boolean onItemLongPress(int arg0,
							OverlayItem arg1) {
						return false;
					}

					public boolean onItemSingleTapUp(final int arg0, final OverlayItem arg1) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(TraEdit_Act.this);
						dialog.setTitle(arg1.getTitle());
						final CharSequence[] items = {getString(R.string.edtra_cm_edit),
								getString(R.string.edtra_cm_del)};
						dialog.setItems(items, new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								Cursor c = mDbHelper.fetchPoint(arg1.mTitle, mRowId);
								long pId = c.getInt(c.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_ID));
								c.close();
								switch (which) {
								case 0: //edit
									Intent i = new Intent(TraEdit_Act.this, PointEdit_Act.class);
									i.putExtra(TrackDBAdapter.P_KEY_ID, pId);
									startActivity(i);
									break;
								case 1: //delete
									mDbHelper.deletePoint(pId);
									refreshPointsOverlay();
									osmView.invalidate();
									break;
								} 
								
							}
						}).create().show();
						
						return true;
					}

				});

		osmView.getOverlays().add(mItemPointOverlay);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		pMenu.add(0, MENU_MY_LOC, Menu.NONE, R.string.menu_myloc).setIcon(
						android.R.drawable.ic_menu_mylocation);

		MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.mainmenu, pMenu);

		return true;
	}
	
	public boolean onMenuItemSelected(final int futureId, final MenuItem item){
		switch(item.getItemId()){
		case MENU_MY_LOC:
			this.myLocationOverlay.enableMyLocation();
			final Location lastFix = this.myLocationOverlay.getLastFix();
			if (lastFix != null)
				this.osmViewController.setCenter(new GeoPoint(lastFix));
			return true;
		case R.id.menu_sett:
    		// Start settings activity
    		startActivity(new Intent(this, Preferences.class));
    		break;
    	case R.id.menu_stat:
    		startActivity(new Intent(this, Tabhost_Act.class));
    		break;
		}
		return false;
	}
	
	/**
	 * Metoda pro zajíštìní reakce stisku bodu na mapì
	 */
	private void applymaptouchlistener(){
		final GestureDetector gd = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				if (mAddBtnClick) {  
					mAddBtnClick = false;
					final Projection pj = TraEdit_Act.this.osmView.getProjection();
					IGeoPoint clickpoint = pj.fromPixels((int)e.getX(), (int)e.getY());							
					
					Log.i("TraEdit_Act", "Vytvoren bod: " + clickpoint.toString());
					long a = -1;
					Intent i = new Intent(TraEdit_Act.this, PointEdit_Act.class);
					i.putExtra(TrackDBAdapter.P_KEY_TRA_ID, mRowId);
					i.putExtra(TrackDBAdapter.P_KEY_GPSLA, clickpoint.getLatitudeE6());
					i.putExtra(TrackDBAdapter.P_KEY_GPSLO, clickpoint.getLongitudeE6());
					i.putExtra(TrackDBAdapter.P_KEY_ID, a);
					startActivity(i);
				}
				return super.onSingleTapUp(e);
			}
			
		});
		this.osmView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gd.onTouchEvent(event);
			}
		});
	}

	/**
	 * Metoda pro obnovu bodù tratì
	 */
	private void refreshPointsOverlay() {
		//if (mRowId != null) {
			mPointsList.clear();
			
			Cursor c = mDbHelper.fetchTrackPoints(mRowId);
			startManagingCursor(c);
			
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				String p_name = c.getString(c.getColumnIndex(TrackDBAdapter.P_KEY_NAME));
				
				GeoPoint p = new GeoPoint(c.getInt(c.getColumnIndex(TrackDBAdapter.P_KEY_GPSLA)),
						c.getInt(c.getColumnIndex(TrackDBAdapter.P_KEY_GPSLO)));
				
				OverlayItem i = new OverlayItem(p_name, "", p);
				i.setMarker(getResources().getDrawable(R.drawable.star_yellow));
				mPointsList.add(i);
			}
			c.close();
		//}
	}
	
}
