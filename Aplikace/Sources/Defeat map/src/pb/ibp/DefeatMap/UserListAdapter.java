package pb.ibp.DefeatMap;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Adaptér pro zobrazení položky v seznamu hráèù
 * @author Petr Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class UserListAdapter extends ArrayAdapter<Game_Act.user_state_list>{

	int resource;
	Context context;
	
	public UserListAdapter(Context context, int res,
			List<Game_Act.user_state_list> objects) {
		super(context, res, objects);
		this.resource =res;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LinearLayout userView;
        //Get the current alert object
        Game_Act.user_state_list al = getItem(position);
 
        //Inflate the view
        if(convertView==null)
        {
            userView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)getContext().getSystemService(inflater);
            vi.inflate(resource, userView, true);
        }
        else
        {
            userView = (LinearLayout) convertView;
        }
        //Get the text boxes from the listitem.xml file
        TextView nameText =(TextView)userView.findViewById(R.id.usl_nametext);
        ImageView gpssImg =(ImageView)userView.findViewById(R.id.usl_gpsstate);
 
        //Assign the appropriate data from our alert object above
        nameText.setText(al.name);
        if (al.state)
        	gpssImg.setImageResource(R.drawable.gps_state_green);
        else
        	gpssImg.setImageResource(R.drawable.gps_state_red);
 
        return userView;
	}

	
}
