package com.insitehub.saleready_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

public class DragandDrop {
	public static class DragShadow extends View.DragShadowBuilder{
		private Context mContext;
		public DragShadow(Context context){
			mContext = context;
		}
		
		@Override
		public void onDrawShadow(Canvas canvas) {
			// TODO Auto-generated method stub
			super.onDrawShadow(canvas);
			Drawable d = mContext.getResources().getDrawable(R.drawable.woman1);
			d.setBounds(0, 0, 75, 75);
			d.draw(canvas);

		}
		
		
	}
	
	
	
}
