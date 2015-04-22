package com.insitehub.saleready_android.group_chat;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.insitehub.saleready_android.config.OpenTokConfig;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Publisher;
import com.opentok.android.Session;
import com.opentok.android.Stream;

public class MySession extends Session {

	private Context mContext;

	// Interface
	private LinearLayout mGridviewParticipants;
	private ViewGroup mPreview;

	// Players status
	private ArrayList<MySubscriber> mSubscribers = new ArrayList<MySubscriber>();
	private HashMap<Stream, MySubscriber> mSubscriberStream = new HashMap<Stream, MySubscriber>();
	private HashMap<String, MySubscriber> mSubscriberConnection = new HashMap<String, MySubscriber>();

	private Publisher publiser;

	private int activityWidth;

	public MySession(Context context, String key, String sessionID) {
		super(context, key, sessionID);
		this.mContext = context;

	}

	// public methods
	public void setParticipantsViewContainer(LinearLayout container) {
		this.mGridviewParticipants = container;
	}

	public void setPreviewView(ViewGroup preview) {
		this.mPreview = preview;
	}

	public void connect() {
		this.connect(OpenTokConfig.TOKEN);
	}

	// callbacks
	@Override
	protected void onConnected() {

		publiser = new Publisher(mContext, "Me");
		publiser.setPublishAudio(false);
		publish(publiser);

		mPreview.addView(publiser.getView());
		publiser.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
				BaseVideoRenderer.STYLE_VIDEO_FILL);

	}

	@Override
	protected void onStreamReceived(Stream stream) {

		MySubscriber p = new MySubscriber(mContext, stream);
		p.setSubscribeToAudio(false);
		// we can use connection data to obtain each user id

		p.setUserId(stream.getConnection().getData());

		this.subscribe(p);
		// p.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
		// BaseVideoRenderer.STYLE_VIDEO_FILL);
		p.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
				BaseVideoRenderer.STYLE_VIDEO_FILL);

		mSubscribers.add(p);
		mSubscriberStream.put(stream, p);
		mSubscriberConnection.put(stream.getConnection().getConnectionId(), p);

		//p.getView().setOnClickListener(new SubscriberOnTouchlistener());
		p.getView().setOnTouchListener(new SubscriberOnTouchlistener());
		resizeGroupchatParticipants();

	}

	@Override
	protected void onStreamDropped(Stream stream) {
		MySubscriber p = mSubscriberStream.get(stream);
		if (p != null) {
			mGridviewParticipants.removeView(p.getView());
			mSubscribers.remove(p);
			mSubscriberStream.remove(stream);
			mSubscriberConnection.remove(stream.getConnection()
					.getConnectionId());
			resizeGroupchatParticipants();
		}
	}

	public Publisher getPublisher() {
		return publiser;
	}

	public void resizeGroupchatParticipants() {
		Display display = ((Activity) mContext).getWindowManager()
				.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		activityWidth = width;
		int numOfSubscribers = mSubscribers.size();
		LinearLayout.LayoutParams lp;
		if (numOfSubscribers >0 && numOfSubscribers <= 4) {
			lp = new LinearLayout.LayoutParams(width / numOfSubscribers,
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0);
		} else {
			lp = new LinearLayout.LayoutParams(width / 4,
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0);
		}

		mGridviewParticipants.removeAllViews();
		for (MySubscriber mySubscriber : mSubscribers) {

			mGridviewParticipants.addView(mySubscriber.getView(), lp);
		}

		if (numOfSubscribers > 4) {
			Toast.makeText(mContext,
					"Swipe left to see more users!",
					Toast.LENGTH_SHORT).show();
		}
	}
	private class SubscriberOnTouchlistener implements View.OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Log.d("Subscriber","Touched");
			if(event.getAction()==MotionEvent.ACTION_UP){
				
				LinearLayout parent = (LinearLayout) v.getParent();
				int index = 0;
				for (int i = 0; i < parent.getChildCount(); i++) {
					if (v == parent.getChildAt(i)) {
						index = i;
						parent.removeViewAt(i);
						break;
					}
				}

				LinearLayout.LayoutParams lp;
				if (v.getWidth() != activityWidth) {
					Point points = new Point(v.getWidth(), v.getHeight());
					v.setTag(points);
					lp = new LinearLayout.LayoutParams(activityWidth,
							android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
							0);
					parent.addView(v, index, lp);
				} else {
					Point dim = (Point) v.getTag();
					if (dim != null) {
						lp = new LinearLayout.LayoutParams(
								dim.x,
								android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
								0);
						parent.addView(v, index, lp);
					} else {
						parent.addView(v);
					}
				}
			}
			
			return true;
		}

	}
}
