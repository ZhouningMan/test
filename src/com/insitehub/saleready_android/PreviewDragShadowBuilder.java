package com.insitehub.saleready_android;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public class PreviewDragShadowBuilder extends View.DragShadowBuilder{
	// The drag shadow image, defined as a drawable thing
			private Drawable shadow;
			private View mView;

			// Defines the constructor for myDragShadowBuilder
			public PreviewDragShadowBuilder(View view) {

				// Stores the View parameter passed to myDragShadowBuilder.
				super(view);

				// Creates a draggable image that will fill the Canvas provided by
				// the system.
				shadow = new ColorDrawable(Color.LTGRAY);
				this.mView=view;
			}

			// Defines a callback that sends the drag shadow dimensions and touch
			// point back to the
			// system.
			@Override
			public void onProvideShadowMetrics(Point size, Point touch) {
				// Defines local variables
				int width, height;

				// Sets the width of the shadow to half the width of the original
				// View

				width = mView.getWidth();
				// Sets the height of the shadow to half the height of the original
				// View
				height = mView.getHeight();

				// The drag shadow is a ColorDrawable. This sets its dimensions to
				// be the same as the
				// Canvas that the system will provide. As a result, the drag shadow
				// will fill the
				// Canvas.
				shadow.setBounds(0, 0, width, height);

				// Sets the size parameter's width and height values. These get back
				// to the system
				// through the size parameter.
				size.set(width, height);

				// Sets the touch point's position to be in the middle of the drag
				// shadow
				touch.set(width / 2, height / 2);
			}

			// Defines a callback that draws the drag shadow in a Canvas that the
			// system constructs
			// from the dimensions passed in onProvideShadowMetrics().
			@Override
			public void onDrawShadow(Canvas canvas) {

				// Draws the ColorDrawable in the Canvas passed in from the system.
				shadow.draw(canvas);
			}
}
