package pb.ibp.DefeatMap;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

/**
 * Aktivita Nastavení
 * @author Petr  Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class Preferences extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		/*
		final CheckBoxPreference pref_en_gps = (CheckBoxPreference)findPreference("gps_checkstartup");
		pref_en_gps.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (pref_en_gps.isChecked())
					;
				return false;
			}
		});
		*/
		Preference pref;		
		pref = findPreference("about");
		pref.setOnPreferenceClickListener(new  OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(getBaseContext(), About_Act.class));
				return true;
			}
		});
	}
	

}
