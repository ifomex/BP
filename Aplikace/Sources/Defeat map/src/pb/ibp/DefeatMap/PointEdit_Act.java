package pb.ibp.DefeatMap;

import pb.ibp.DefeatMap.R;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Aktivita editace informací o bodu
 * @author Petr  Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class PointEdit_Act extends Activity{

	
	/**
	 * Access to database
	 */
	private TrackDBAdapter mDbHelper;
	
	private Long mPoiId, mPoiTraid;
	private Integer mPoiLat, mPoiLon, mPoiOrd;
	private String mPoiName, mPoiQue, mPoiAns;
	
	/**
	 * GUI fields
	 */
	TextView lat_text, lon_text; 
	EditText NameText, OrdText, QueText, AnsText;
	CheckBox EQue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.point_edit);
		
		mDbHelper = new TrackDBAdapter(this);
		mDbHelper.open();
		
		NameText = (EditText)findViewById(R.id.poied_nametext);
		OrdText = (EditText)findViewById(R.id.poied_ordtext);
		QueText = (EditText)findViewById(R.id.poied_quetext);
		AnsText = (EditText)findViewById(R.id.poied_anstext);
		EQue = (CheckBox)findViewById(R.id.poied_eque);
		lat_text = (TextView)findViewById(R.id.poied_lattext);
		lon_text = (TextView)findViewById(R.id.poied_lontext);
				
		Bundle extras = getIntent().getExtras();
		if (savedInstanceState != null){
			mPoiId = (Long) savedInstanceState.getSerializable(TrackDBAdapter.P_KEY_ID);
			mPoiLat = (Integer)savedInstanceState.getSerializable(TrackDBAdapter.P_KEY_GPSLA);
			mPoiLon = (Integer)savedInstanceState.getSerializable(TrackDBAdapter.P_KEY_GPSLO);
			mPoiTraid = (Long)savedInstanceState.getSerializable(TrackDBAdapter.P_KEY_TRA_ID);
		} else  if (extras != null) {
			Long x = extras.getLong(TrackDBAdapter.P_KEY_ID);
			mPoiId = (x < 0) ? null : x;
			mPoiLat = extras.getInt(TrackDBAdapter.P_KEY_GPSLA);
			mPoiLon = extras.getInt(TrackDBAdapter.P_KEY_GPSLO);
			mPoiTraid = extras.getLong(TrackDBAdapter.P_KEY_TRA_ID);
		} else {
			mPoiId = null;
			mPoiLat = null;
			mPoiLon = null;
			mPoiTraid = null;
		}
		

		populateFields();
		
		EQue.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				QueText.setEnabled(isChecked);
				AnsText.setEnabled(isChecked);
			}
		});
		
		Button save_btn = (Button)findViewById(R.id.poied_savebtn);
		save_btn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				save_state();
				mDbHelper.close();
				finish();
			}
		});

	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(TrackDBAdapter.T_KEY_ID, mPoiId);
		outState.putSerializable(TrackDBAdapter.P_KEY_GPSLA, mPoiLat);
		outState.putSerializable(TrackDBAdapter.P_KEY_GPSLO, mPoiLon);
		outState.putSerializable(TrackDBAdapter.P_KEY_TRA_ID, mPoiTraid);
	}

	/**
	 * Fill GUI texts fields 
	 */
	private void populateFields() {
		if (mPoiId != null) {
			Cursor point = mDbHelper.fetchPoint(mPoiId);
			startManagingCursor(point);
			lat_text.setText(point.getString(
					point.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_GPSLA)));
			lon_text.setText(point.getString(
					point.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_GPSLO)));
			NameText.setText(point.getString(
					point.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_NAME)));
			OrdText.setText(point.getString(
					point.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_ORDER)));
			if (point.getString(point.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_QUESTION)) == null) {
				EQue.setChecked(false);
			} else {
				EQue.setChecked(true);
				QueText.setText(point.getString(
						point.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_QUESTION)));
				AnsText.setText(point.getString(
						point.getColumnIndexOrThrow(TrackDBAdapter.P_KEY_ANSWER)));
			}
		} else {
			lat_text.setText(mPoiLat + "\t");
			lon_text.setText(mPoiLon + "\t");
			
			OrdText.setText(Integer.toString(mDbHelper.fetchTrackPoints(mPoiTraid).getCount()));
		}
	}
	/**
	 * Save Point to database
	 */
	private void save_state() {
		mPoiName = NameText.getText().toString();
		String ord = OrdText.getText().toString();
		if (ord != "")
			mPoiOrd = Integer.parseInt(ord);
		else
			mPoiOrd = null;
		if(EQue.isChecked()){
			mPoiQue = QueText.getText().toString();
			mPoiAns = AnsText.getText().toString();
		} else {
			mPoiQue = null;
			mPoiAns = null;
		}
		
		if (mPoiId == null){
			long id = mDbHelper.createPoint(mPoiName, mPoiLat, mPoiLon, 
					mPoiOrd, mPoiQue, mPoiAns, mPoiTraid);
			if (id > 0)
				mPoiId = id;
		} else
			mDbHelper.updatePoint(mPoiId, mPoiName, mPoiOrd, mPoiQue, mPoiAns, mPoiTraid);
		Log.i("PoiEdit_Act","Ukladam bod:"+mPoiId);
	}
	
}
