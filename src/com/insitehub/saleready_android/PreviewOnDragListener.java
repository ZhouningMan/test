package com.insitehub.saleready_android;

import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.FrameLayout;

public class PreviewOnDragListener implements OnDragListener{
	private FrameLayout.LayoutParams layoutParams;
	private View mPreview;
	
	public PreviewOnDragListener(View userPreview) {
		mPreview = userPreview;
	}
	
	
	@Override
	public boolean onDrag(View v, DragEvent event) {
		final int action = event.getAction();
		switch (action) {
		case DragEvent.ACTION_DRAG_STARTED:
			v.invalidate();
			return true;
		case DragEvent.ACTION_DRAG_ENTERED:
			v.invalidate();
			return true;
		case DragEvent.ACTION_DRAG_EXITED:

			break;
		case DragEvent.ACTION_DRAG_LOCATION:
			return true;

		case DragEvent.ACTION_DROP:
			int x_cord = (int) event.getX();
			int y_cord = (int) event.getY();
			int width = mPreview.getWidth();
			int height = mPreview.getHeight();
			layoutParams = (FrameLayout.LayoutParams) mPreview
					.getLayoutParams();
			layoutParams.leftMargin = x_cord - width / 2;
			layoutParams.topMargin = y_cord - height / 2;
			mPreview.invalidate();
			mPreview.setVisibility(View.VISIBLE);
			return true;
		case DragEvent.ACTION_DRAG_ENDED:
			
			return true;
		default:
			break;
		}
		return true;
	}

}
