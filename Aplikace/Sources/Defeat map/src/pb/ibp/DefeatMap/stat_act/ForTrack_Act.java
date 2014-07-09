package pb.ibp.DefeatMap.stat_act;

import pb.ibp.DefeatMap.MyTime;
import pb.ibp.DefeatMap.TrackDBAdapter;
import pb.ibp.DefeatMap.R;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Aktivita statistik pro traù 
 * @author Petr Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class ForTrack_Act extends Activity {

	private TrackDBAdapter mDbHelper;
	
	private Spinner traSpinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stat_ftra);
		
		mDbHelper = new TrackDBAdapter(this);
		mDbHelper.open();
		
		traSpinner = (Spinner) findViewById(R.id.ftrs_traspin);
		Cursor ct = mDbHelper.fetchAllTracks();
		startManagingCursor(ct);
		
		String[] from = new String[] {TrackDBAdapter.T_KEY_NAME};
		int[] to = new int[] {android.R.id.text1};
		SimpleCursorAdapter ca = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, ct, from, to);
		ca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		traSpinner.setAdapter(ca);
		
		traSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				populateFields();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		});
		Long tid = getIntent().getExtras().getLong("tid");
		if (tid != null) {
			traSpinner.setSelection((int)(tid+0));
		}
		//populateFields();
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}



	private void populateFields() {
		Long tId = traSpinner.getSelectedItemId();
		Log.i("fTraStat", "sipn trackID: "+tId);
		Cursor c;
		if (tId == null)
			return;
		else
			c = mDbHelper.fetchTrackStat(tId);
		
		startManagingCursor(c);
		c.moveToFirst();
		int play_count = c.getInt(c.getColumnIndex(TrackDBAdapter.S_KEY_PLAY_COUNT)); 
		String pc, st, bt, at, sl, bl, al;
		pc = Integer.toString(play_count);
		if (play_count > 0) {
			st =  new MyTime(c.getLong(c.getColumnIndex(TrackDBAdapter.S_KEY_SUM_TIME))).toString();
			bt =  new MyTime(c.getLong(c.getColumnIndex(TrackDBAdapter.S_KEY_BEST_TIME))).toString();
			at =  new MyTime(c.getLong(c.getColumnIndex(TrackDBAdapter.S_KEY_AVG_TIME))).toString();
			
			sl = String.format("%.2f",c.getDouble(c.getColumnIndex(TrackDBAdapter.S_KEY_SUM_LEN)));
			bl = String.format("%.2f",c.getDouble(c.getColumnIndex(TrackDBAdapter.S_KEY_BEST_LEN)));
			al = String.format("%.2f",c.getDouble(c.getColumnIndex(TrackDBAdapter.S_KEY_AVG_LEN)));
		} else {
			String q = "---";
			st = bt = at = q;
			sl = bl = al = q;
		}
		TextView pc_t, st_t, bt_t, at_t, sl_t, bl_t, al_t;
		pc_t = (TextView)findViewById(R.id.ftrs_plcnt);
		pc_t.setText(pc);
		st_t = (TextView)findViewById(R.id.ftrs_smtime);
		st_t.setText(st);
		bt_t = (TextView)findViewById(R.id.ftrs_betime);
		bt_t.setText(bt);
		at_t = (TextView)findViewById(R.id.ftrs_avtime);
		at_t.setText(at);
		sl_t = (TextView)findViewById(R.id.ftrs_sulen);
		sl_t.setText(sl);
		bl_t = (TextView)findViewById(R.id.ftrs_belen);
		bl_t.setText(bl);
		al_t = (TextView)findViewById(R.id.ftrs_avlen);
		al_t.setText(al);
	}
	
}
