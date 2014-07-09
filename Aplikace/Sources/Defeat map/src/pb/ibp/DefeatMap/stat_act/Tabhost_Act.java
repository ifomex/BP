package pb.ibp.DefeatMap.stat_act;

import pb.ibp.DefeatMap.R;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

/**
 * Aktivita pro obsluhu záložek
 * @author Petr Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class Tabhost_Act extends TabActivity {

	private TabHost mTabHost;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics);
		
		mTabHost = getTabHost();  // The activity TabHost
	    
		Intent intent;  // Reusable Intent for each tab

		//Bundle extras = getIntent().getExtras();
	    Long tid = null;//extras.getLong("traid");
		
		intent = new Intent().setClass(this, Summary_Act.class);
	    setupTab(new TextView(this), getResources().getString(R.string.tab_summary), intent);
	    intent = new Intent().setClass(this, ForTrack_Act.class);
	    intent.putExtra("tid", tid);
	    setupTab(new TextView(this), getResources().getString(R.string.tab_forTrack), intent);
	    if (tid != null) {
	    	setDefaultTab(getResources().getString(R.string.tab_forTrack));
	    }
	}
	    	
	private void setupTab(final View view, final String tag, Intent i) {
	    View tabview = createTabView(mTabHost.getContext(), tag);
	    TabSpec setContent = mTabHost.newTabSpec(tag)
	    		.setIndicator(tabview)
	    		.setContent(i);
	    mTabHost.addTab(setContent);
	}


	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tab_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}

}
