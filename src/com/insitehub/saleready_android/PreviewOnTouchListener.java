package com.insitehub.saleready_android;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;


public class PreviewOnTouchListener implements OnTouchListener {

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			view.setVisibility(View.GONE);
			DragShadowBuilder shadowBuilder = new PreviewDragShadowBuilder(view);
			view.startDrag(null, shadowBuilder, view, 0);
		}
		return false;
	}

}
