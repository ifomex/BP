package pb.ibp.DefeatMap;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Adaptér pro zobrazení položky v seznamu tratí
 * @author Petr Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class TraListAdapter extends SimpleCursorAdapter {

	private int layout;
	private Cursor c;
	
	public TraListAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to) {
		super(context, layout, c, from, to);
		this.layout = layout;
		this.c = c;
	}

	@Override
	public void bindView(View v, Context context, Cursor cursor) {
		String name = c.getString(c.getColumnIndex(TrackDBAdapter.T_KEY_NAME));
		
		TextView t_name = (TextView)v.findViewById(R.id.TraListItem_Name);
		if (t_name != null)
			t_name.setText(name);
		
		long tid = c.getLong(c.getColumnIndex(TrackDBAdapter.T_KEY_ID));
		TrackDBAdapter dbHelper;
		dbHelper = new TrackDBAdapter(context);
		dbHelper.open();
		
		Cursor ccc = dbHelper.fetchTrackPoints(tid);
		TextView t_desc = (TextView)v.findViewById(R.id.TraListItem_Desc);
		if (t_desc != null)
			t_desc.setText(""+ccc.getCount());
		
	}


	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(layout, parent, false);
		
		String name = c.getString(c.getColumnIndex(TrackDBAdapter.T_KEY_NAME));
		
		TextView t_name = (TextView)v.findViewById(R.id.TraListItem_Name);
		if (t_name != null)
			t_name.setText(name);
		
		long tid = c.getLong(c.getColumnIndex(TrackDBAdapter.T_KEY_ID));
		TrackDBAdapter dbHelper;
		dbHelper = new TrackDBAdapter(context);
		dbHelper.open();
		
		Cursor ccc = dbHelper.fetchTrackPoints(tid);
		TextView t_desc = (TextView)v.findViewById(R.id.TraListItem_Desc);
		if (t_desc != null)
			t_desc.setText(""+ccc.getCount());
				
		ccc.close();
		return v;					//super.newView(context, cursor, parent);
	}
	
}
