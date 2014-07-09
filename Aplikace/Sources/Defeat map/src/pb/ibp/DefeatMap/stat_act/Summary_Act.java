package pb.ibp.DefeatMap.stat_act;

import pb.ibp.DefeatMap.MyTime;
import pb.ibp.DefeatMap.TrackDBAdapter;
import pb.ibp.DefeatMap.R;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Aktivita celkových statistik
 * @author Petr Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class Summary_Act extends Activity {

	private TrackDBAdapter mDbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stat_summ);
		
		mDbHelper = new TrackDBAdapter(this);
		mDbHelper.open();
	}

	
	
	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}



	private void populateFields() {
		Cursor c = mDbHelper.fetchStat(1);
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
		pc_t = (TextView)findViewById(R.id.sums_plcnt);
		pc_t.setText(pc);
		st_t = (TextView)findViewById(R.id.sums_smtime);
		st_t.setText(st);
		bt_t = (TextView)findViewById(R.id.sums_betime);
		bt_t.setText(bt);
		at_t = (TextView)findViewById(R.id.sums_avtime);
		at_t.setText(at);
		sl_t = (TextView)findViewById(R.id.sums_sulen);
		sl_t.setText(sl);
		bl_t = (TextView)findViewById(R.id.sums_belen);
		bl_t.setText(bl);
		al_t = (TextView)findViewById(R.id.sums_avlen);
		al_t.setText(al);
	}
	
}
