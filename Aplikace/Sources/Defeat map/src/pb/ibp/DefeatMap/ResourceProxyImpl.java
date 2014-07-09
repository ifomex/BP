package pb.ibp.DefeatMap;

import org.osmdroid.DefaultResourceProxyImpl;

import pb.ibp.DefeatMap.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

/**
 * Tøída pro definování zmìn bitmap pro OSM overlay 
 * @author Petr Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class ResourceProxyImpl extends DefaultResourceProxyImpl {

    private final Context mContext;
	
	public ResourceProxyImpl(Context pContext) {
		super(pContext);
		mContext = pContext;
	}

	@Override
	public Bitmap getBitmap(bitmap pResId) {
		switch (pResId) {
		case direction_arrow:
			return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.direction_arrow);
		}
		return super.getBitmap(pResId);
	}

	@Override
	public Drawable getDrawable(bitmap pResId) {
		switch (pResId) {
		case direction_arrow:
			return mContext.getResources().getDrawable(R.drawable.direction_arrow);
		}
		return super.getDrawable(pResId);
	}
	
	

}
