package pb.ibp.DefeatMap;

import pb.ibp.DefeatMap.stat_act.Tabhost_Act;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;

/**
 * Aktivita pro manažer tratí
 * @author Petr Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class TraList_Act extends ListActivity {

	private static final int ACT_EDIT = 0;
	
	private static final int CM_EDIT = Menu.FIRST;
	private static final int CM_DEL = Menu.FIRST+1;
	
	private TrackDBAdapter mDBHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.track_list);
		
		mDBHelper = new TrackDBAdapter(this);
		mDBHelper.open();
		fill_data();
		
		Button newTraBtn = (Button) findViewById(R.id.TrLst_NewBtn);
		newTraBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(TraList_Act.this, TraEdit_Act.class);
				startActivityForResult(i, ACT_EDIT);
			}
		});
	
		registerForContextMenu(getListView());
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		mDBHelper.open();
		fill_data();
	}


	@Override
	protected void onStop() {
		mDBHelper.close();
		super.onStop();
	}


	private void fill_data() {
		Cursor c = mDBHelper.fetchAllTracks();
		startManagingCursor(c);
		
		String[] from = new String[]{TrackDBAdapter.T_KEY_NAME};
		
		int[] to = new int[]{R.id.TraListItem_Name}; 
		/*		
		SimpleCursorAdapter listAd = 
				new SimpleCursorAdapter(this, R.layout.track_list_item, c, from, to);*/
		TraListAdapter listAd = new TraListAdapter(this, R.layout.track_list_item, c, from, to);
		
		setListAdapter(listAd);
	}

	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		this.openContextMenu(v);
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CM_EDIT, 0, R.string.tralst_cm_edit);
		menu.add(0, CM_DEL, 0, R.string.tralst_cm_del);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info =  (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case CM_EDIT:
				Intent i = new Intent(TraList_Act.this, TraEdit_Act.class);
				i.putExtra(TrackDBAdapter.T_KEY_ID, info.id);
				startActivity(i);
				return true;
			case CM_DEL:
				mDBHelper.deleteTrack(info.id);
				fill_data();
				return true;
		}
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.mainmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
	
	
}
