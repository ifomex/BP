package pb.ibp.DefeatMap;

import pb.ibp.DefeatMap.stat_act.Tabhost_Act;
import pb.ibp.DefeatMap.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

/**
 * Aktivita nastavení hry
 * @author Petr  Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class GameSett_Act extends Activity{

	/** Database Helper **/
	TrackDBAdapter mDBAdapter;
	/** GUI fields **/
	Spinner mGmType_Spinn, mTra_Spinn; 
	/** Time for "In Time" game type **/
	Long tim = null;
	
	private static final int dial_tim = 0x01;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_game);
		mGmType_Spinn = (Spinner) findViewById(R.id.setgm_gmTpSpinner);
		mTra_Spinn = (Spinner) findViewById(R.id.setgm_trackSpinner);
		final CheckBox queCheck = (CheckBox)findViewById(R.id.setgm_EnableQue);
		
		mDBAdapter = new TrackDBAdapter(this);
		mDBAdapter.open();
		
		populateFields();
		
		mDBAdapter.close();
		
		Button start_btn = (Button)findViewById(R.id.setgm_startbtn);
		start_btn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(GameSett_Act.this, Game_Act.class);
				i.putExtra("Game", Game_Act.GAME_SINGLE);
				i.putExtra("GameType", mGmType_Spinn.getSelectedItemPosition());
				
				//if (mGmType_Spinn.getSelectedItemId() == Game_Act.GAME_TYPE_TIM) {
					i.putExtra("Game_Time", tim);
				//}
				i.putExtra(TrackDBAdapter.T_KEY_ID, mTra_Spinn.getSelectedItemId());
				i.putExtra("QueEnable", queCheck.isChecked());
				startActivity(i);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		mDBAdapter.open();
		populateFields();
		mDBAdapter.close();
	}
	
	private void populateFields() {
		ArrayAdapter<CharSequence> gmtp_Adapt = ArrayAdapter.createFromResource(this, R.array.gameTypeArr, android.R.layout.simple_spinner_item);
		gmtp_Adapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mGmType_Spinn.setAdapter(gmtp_Adapt);
		
		mGmType_Spinn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (arg3 == Game_Act.GAME_TYPE_TIM)
					showDialog(dial_tim);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
	
		});
		
		Cursor c = mDBAdapter.fetchAllTracks();
		startManagingCursor(c);
		
		String[] from = new String[] {TrackDBAdapter.T_KEY_NAME};
		int[] to = new int[] {android.R.id.text1};
		SimpleCursorAdapter ca = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, from, to);
		ca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTra_Spinn.setAdapter(ca);
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch (id) {
		case dial_tim:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.timedialog_title)
				.setMessage(R.string.timedialog_message)
				.setCancelable(false);
			final EditText timtext = new EditText(GameSett_Act.this);
			timtext.setInputType(InputType.TYPE_CLASS_NUMBER);
			timtext.setText("5");
			builder.setView(timtext)
				.setPositiveButton(R.string.positive_btn_text,  new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							tim = Long.parseLong(timtext.getText().toString()) * 1000 * 60; //prevod z min -> millis
						}
				});
			
			dialog = builder.create();
			break;
		default:
			dialog = super.onCreateDialog(id); 
		}
		
		return dialog;
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.mainmenu, menu);
    	return true;
    }
    
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    	case R.id.menu_sett:
    		// Start settings activity
    		startActivity(new Intent(this, Preferences.class));
    		break;
    	case R.id.menu_stat:
    		startActivity(new Intent(this, Tabhost_Act.class));
    		break;
    	}
		return super.onOptionsItemSelected(item);
	}
}
