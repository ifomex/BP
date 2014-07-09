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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Aktivita nastavení hry pro více hráèù
 * @author Petr  Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class MultiGameSett_Act extends Activity {
	
	public static final String TAG = "ibp.MultiSettGame_Act";

	private static final int dial_tim = 0x01;
	
	/** Database Helper **/
	TrackDBAdapter mDBAdapter;
	/** GUI fields **/
	Spinner mGmType_Spinn, mTra_Spinn; 
	CheckBox mQueCheck;
	EditText mPlName_Text, mRoom_Text;
	Button start_btn;
	
	
	/** Time for "In Time" game type **/
	Long tim = null;


	
	private int mRole;
	public static final int ROLE_SER = 0x01;
	public static final int ROLE_CLI = 0x02;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		runOnUiThread(new Runnable() {
			public void run() {
				setContentView(R.layout.set_mutligame);
				mGmType_Spinn = (Spinner) findViewById(R.id.setmgm_gmTpSpinner);
				mTra_Spinn = (Spinner) findViewById(R.id.setmgm_trackSpinner);
				mQueCheck = (CheckBox)findViewById(R.id.setmgm_EnableQue);
				mPlName_Text = (EditText)findViewById(R.id.setmgm_playerName_Text);
				mRoom_Text = (EditText)findViewById(R.id.setmgm_roomName);
			}
		});
		mDBAdapter = new TrackDBAdapter(this);
		mDBAdapter.open();
		
		mRole = getIntent().getExtras().getInt("role");
		
		populateFields();
		
		mDBAdapter.close();
		
		start_btn = (Button)findViewById(R.id.setmgm_startBnt);
		start_btn.setOnClickListener(startBtnListener);

	}

	
	@Override
	protected void onStart() {
		super.onStart();
		mDBAdapter.open();
		populateFields();
		mDBAdapter.close();
	}


	private void populateFields() {
		runOnUiThread(new Runnable() {
			public void run() {
				mGmType_Spinn.setAdapter(null);
				ArrayAdapter<CharSequence> gmtp_Adapt = ArrayAdapter.createFromResource(MultiGameSett_Act.this,
						R.array.gameTypeArr, 
						android.R.layout.simple_spinner_item);
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
				SimpleCursorAdapter ca = new SimpleCursorAdapter(MultiGameSett_Act.this, android.R.layout.simple_spinner_item, c, from, to);
				ca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				mTra_Spinn.setAdapter(ca);
						
				if (mRole == ROLE_CLI) {
					mGmType_Spinn.setEnabled(false);
					mTra_Spinn.setEnabled(false);
					mQueCheck.setEnabled(false);
				} else {
					mRoom_Text.setEnabled(false);
				}
			}
		});
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
			final EditText timtext = new EditText(MultiGameSett_Act.this); 
			timtext.setText("5");
			timtext.setInputType(InputType.TYPE_CLASS_NUMBER);
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
	
	View.OnClickListener startBtnListener = new View.OnClickListener() {
		public void onClick(View v) {
			String sess_name = mRoom_Text.getText().toString();
			String user_name = mPlName_Text.getText().toString();
			if (mRole == ROLE_CLI && sess_name.length()<=0) {
				Toast.makeText(MultiGameSett_Act.this, R.string.roname_warn, Toast.LENGTH_SHORT).show();
				return;
			}
			if (user_name.length()<=0) {
				Toast.makeText(MultiGameSett_Act.this, R.string.plname_warn, Toast.LENGTH_SHORT).show();
				return;
			}
			
			Intent i = new Intent(MultiGameSett_Act.this, Game_Act.class);
			i.putExtra("Game", Game_Act.GAME_MULTI);
			if (mRole == ROLE_SER) {
				i.putExtra("GameType", mGmType_Spinn.getSelectedItemPosition());
				i.putExtra("Game_Time", tim);
				i.putExtra(TrackDBAdapter.T_KEY_ID, mTra_Spinn.getSelectedItemId());
				i.putExtra("QueEnable", mQueCheck.isChecked());
			}
			i.putExtra("Player_Name", mPlName_Text.getText().toString());
			i.putExtra("Role", mRole);
			if (mRole == ROLE_CLI)
				i.putExtra("Sess_Id", Integer.parseInt(sess_name));
			startActivity(i);
			
		}
	};
	
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
