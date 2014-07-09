package pb.ibp.DefeatMap;

import pb.ibp.DefeatMap.stat_act.Tabhost_Act;
import pb.ibp.DefeatMap.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Aktivita výchozí obrazovky
 * @author Petr
 *
 */
public class Main_Act extends Activity {
		
	private static final int dial_multi_pl = 0x01;
	private static final int dial_gps = 0x02;
	
	private SharedPreferences prefs;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m);
        
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
      /*  ConnectivityManager connec = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        if ( connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED || connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED )
        	finish();
       */ 
        //Dialog a = new Dialog(null);
        
        
        checkGPSProvider();       
        
        Button traBtn = (Button) findViewById(R.id.main_editorbtn);
        Button plaBtn = (Button) findViewById(R.id.main_playbtn);
        Button mplaBtn = (Button) findViewById(R.id.main_mutliplaybtn);
        
        traBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(Main_Act.this, TraList_Act.class);
				startActivity(i);
			}
		});
        
        plaBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Main_Act.this, GameSett_Act.class);
				startActivity(i);
			}
		});
        mplaBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(dial_multi_pl);
				
			}
		});
    }

    /**
     * Metoda zjištìní zaptuné GPS
     */
    private void checkGPSProvider() {
		LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// GPS isn't enabled. Offer user to go enable it
			if (prefs.getBoolean("gps_checkstartup", true)) {
				showDialog(dial_gps);
			} else {
	        	final Intent intent = new Intent();
	            intent.setClassName("com.android.settings",
	                                "com.android.settings.widget.SettingsAppWidgetProvider");
	            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
	            intent.setData(Uri.parse("custom:3"));
	            this.getApplicationContext().sendBroadcast(intent);
	        } 
		}
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
    	/*case R.id.menu_exit:
    		this.finish();
    		return true; */
    	}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch (id) {
		case dial_multi_pl:		//dialog výbìru role multiplayeru
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.multiplDialog_title)
				//.setMessage(R.string.multiplDialog_message)
				.setPositiveButton(R.string.multiplDialog_serverbtn, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent(Main_Act.this, MultiGameSett_Act.class);
						i.putExtra("role", MultiGameSett_Act.ROLE_SER);
						startActivity(i); 
					}
				})
				.setNegativeButton(R.string.multiplDialog_clientbtn, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent(Main_Act.this, MultiGameSett_Act.class);
						i.putExtra("role", MultiGameSett_Act.ROLE_CLI);
						startActivity(i);
					}
				});
			dialog = builder.create();
			break;
		case dial_gps:			//dialog zapnutí GPS
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.gpsdialog_disabled)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(getResources().getString(R.string.gpsdialog_disabled_hint))
				.setCancelable(true)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						finish();//dialog.cancel();
					}
				});
			dialog = builder.create();
			break;
		default:
			dialog = super.onCreateDialog(id); 		
		}
		
		return dialog;
	}
    
    

}