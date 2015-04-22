package com.insitehub.saleready_android;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.Asset;
import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.PeerSession;
import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.PeerShare;
import com.insitehub.saleready_android.Messaging.MessagingDataForP2P;
import com.insitehub.saleready_android.Messaging.NewMessagingListner;
import com.insitehub.saleready_android.config.OpenTokConfig;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class InCallActivity extends Activity implements
		Session.SessionListener, Publisher.PublisherListener,
		Subscriber.VideoListener, IOCallback, NewMessagingListner {

	public static final String PURPOSE = "purpose";
	public static final String PURPOSE_PITCH_PRACTICE = "pitch practice";
	public static final String PURPOSE_CERTIFICATION = "certification center";

	private static final String SESSION_SERVICE_URL = "http://admin.mobilesfe.com/get_session?user=";
	private static final String TOKEN_SERVICE_URL = "http://admin.mobilesfe.com/get_token?session=";
	public static final String SESSION_ID = "Peer Share Session ID";

	private static final String TAG = InCallActivity.class.getSimpleName();

	private static final long TIMEOUT = 18000;
	private static final long INTERVAL = 3000;
	
	private static final String LOGTAG = "InCall";
	private Session mSession;
	private Publisher mPublisher;
	private Subscriber mSubscriber;
	private ArrayList<Stream> mStreams;
	private ProgressBar mPubSpinner;
	private ProgressBar mSubSpinner;

	

	private RelativeLayout mPreview;
	private RelativeLayout mSubscriberViewContainer;

	private boolean resumeHasRun = false;

	private String sessionID="";
	private String token;
	// private String targetUser;

	private Intent mIntent;
	private String direction;

	private String shareTo;
	private String purpose;
	
	private Handler mHandler = new Handler();
	private Runnable checkingDocumentSharingTask;
	private ParseUser targetUser;
	
	
	//The following variables are related to p2p messaging
	private ListView chatHistory;
	private Button btnSendMessage;
	private EditText messageToSend;
	private LinearLayout p2pChatMessagingView;
	private boolean isP2PChatMessagingViewVisible = true;
	private boolean isIncallActivityRunning = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(LOGTAG, "ONCREATE");
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);

		setContentView(R.layout.activity_incall);

		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		mPreview = (RelativeLayout) findViewById(R.id.publisherview);
	
		mSubscriberViewContainer = (RelativeLayout) findViewById(R.id.subscriberview);
		mPubSpinner = (ProgressBar) findViewById(R.id.pub_loadingSpinner);
		mSubSpinner = (ProgressBar) findViewById(R.id.sub_loadingSpinner);

	    purpose = getIntent().getStringExtra(PURPOSE);

		mStreams = new ArrayList<Stream>();

		mIntent = getIntent();
		direction = mIntent.getStringExtra(ViewReps.DIRECTION);
		shareTo = mIntent.getStringExtra(ViewReps.SHARE_TO);
		
		p2pChatMessagingView = (LinearLayout)findViewById(R.id.p2p_chat_messaging_view);
		chatHistory = (ListView) findViewById(R.id.messageHistory);
		btnSendMessage = (Button) findViewById(R.id.sendMessage);
		messageToSend = (EditText) findViewById(R.id.chatMessage);
		chatHistory.setAdapter(MessagingDataForP2P
				.getPartyMessagingAdapter(this));
		MessagingDataForP2P.addNewMessagesListner(this);
		
		findTheOtherUser();
		setupDragandDrop();
		addHandlers();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Remove publisher & subscriber views because we want to reuse them
		if (mSubscriber != null) {
			mSubscriberViewContainer.removeView(mSubscriber.getView());
		}
		reloadInterface();
	}

	@Override
	public void onPause() {
		super.onPause();



	}

	@Override
	public void onResume() {
		super.onResume();
		isIncallActivityRunning = true;
		if (!resumeHasRun) {
			resumeHasRun = true;
			return;
		} else {
			if (mSession != null) {
				mSession.onResume();
			}
		}

		reloadInterface();

	}

	@Override
	public void onStop() {
		super.onStop();
		isIncallActivityRunning = false;

		if (mSession != null) {
			mSession.onPause();

			if (mSubscriber != null) {
				mSubscriberViewContainer.removeView(mSubscriber.getView());
			}
		}
		
		if (isFinishing()) {

			if (mSession != null) {
				mSession.disconnect();
			}
		}
		
		mHandler.removeCallbacksAndMessages(null);
		
		MessagingDataForP2P.getMessageHistory().clear();
		if(!sessionID.equals("")){
			SocketHandlerForP2P.disconnectFromSession(sessionID);
		}

	}

	@Override
	public void onDestroy() {
		if (mSession != null) {
			mSession.disconnect();
		}
		super.onDestroy();
		finish();
	}

	@Override
	public void onBackPressed() {
		if (mSession != null) {
			mSession.disconnect();
		}
		super.onBackPressed();
	}

	public void reloadInterface() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mSubscriber != null) {
					attachSubscriberView(mSubscriber);
				}
			}
		}, 500);
	}

	// Create a session with session ID and the associated token
	private void sessionConnect() {
		if (mSession == null) {
			mSession = new Session(InCallActivity.this, OpenTokConfig.API_KEY,
					sessionID);
			mSession.setSessionListener(this);
			mSession.connect(token);
			SocketHandlerForP2P.startSocketConnection(this,sessionID);
		}
	}


	private void createSession() {
		sessionConnect();
		PeerSession peerSession = new PeerSession();
		peerSession.put("sessionOwner", ParseUser.getCurrentUser());
		peerSession.put("requestTo", targetUser);
		peerSession.put("sessionId", sessionID);
		peerSession.saveInBackground();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				removeMyRequests();
			}
		}, TIMEOUT);
	}

	private void removeMyRequests() {
		ParseQuery<PeerSession> query = ParseQuery.getQuery("PeerSession");
		query.whereEqualTo("sessionOwner", ParseUser.getCurrentUser());
		query.findInBackground(new FindCallback<PeerSession>() {

			@Override
			public void done(List<PeerSession> objects, ParseException e) {
				for (PeerSession peerSession : objects) {
					peerSession.deleteInBackground();
				}

			}
		});
	}

	// invoked when the publisher connects to the OpenTok session.
	@Override
	public void onConnected(Session session) {
		Log.i(LOGTAG, "Connected to the session.");
		if (mPublisher == null) {
			// publisher will represent a audio-video stream
			mPublisher = new Publisher(InCallActivity.this, "publisher");
			mPublisher.setPublisherListener(this);
			attachPublisherView(mPublisher);
			mPublisher.setPublishAudio(false);
			// Starts a Publisher streaming to the session.
			mSession.publish(mPublisher);
		}
	}

	// Invoked when the publisher is no longer connected to the OpenTok session.
	@Override
	public void onDisconnected(Session session) {
		Log.i(LOGTAG, "Disconnected from the session.");
		if (mPublisher != null) {
			mPreview.removeView(mPublisher.getView());
		}

		if (mSubscriber != null) {
			mSubscriberViewContainer.removeView(mSubscriber.getView());
		}

		mPublisher = null;
		mSubscriber = null;
		mStreams.clear();
		mSession = null;
	}

	private void subscribeToStream(Stream stream) {
		// Used to consume an audio-video stream in the OpenTok session.
		// Subscribers are created by passing a valid Stream instance into the
		// Subscriber constructor.
		mSubscriber = new Subscriber(InCallActivity.this, stream);
		mSubscriber.setVideoListener(this);
		mSubscriber.setSubscribeToAudio(true);
		// Start receiving and rendering audio-video stream data for the
		// specified subscriber.
		mSession.subscribe(mSubscriber);
		// start loading spinning
		mSubSpinner.setVisibility(View.VISIBLE);
	}

	private void unsubscribeFromStream(Stream stream) {
		mStreams.remove(stream);
		if (mSubscriber.getStream().getStreamId().equals(stream.getStreamId())) {
			mSubscriberViewContainer.removeView(mSubscriber.getView());
			mSubscriber = null;
			if (!mStreams.isEmpty()) {
				subscribeToStream(mStreams.get(0));
			}
		}
	}

	private void attachSubscriberView(Subscriber subscriber) {
		// RelativeLayout.LayoutParams layoutParams = new
		// RelativeLayout.LayoutParams(
		// getResources().getDisplayMetrics().widthPixels, getResources()
		// .getDisplayMetrics().heightPixels);

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		mSubscriberViewContainer.removeAllViews();
		mSubscriberViewContainer.addView(mSubscriber.getView(), layoutParams);
		subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
				BaseVideoRenderer.STYLE_VIDEO_FILL);

	}

	private void attachPublisherView(Publisher publisher) {
		mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
				BaseVideoRenderer.STYLE_VIDEO_FILL);
		mPreview.removeAllViews();
		mPreview.addView(mPublisher.getView());

	}

	@Override
	public void onError(Session session, OpentokError exception) {
		Log.i(LOGTAG, "Session exception: " + exception.getMessage());
	}

	// invoked when a stream published by another client is created in a
	// session.
	@Override
	public void onStreamReceived(Session session, Stream stream) {

		Log.d("Stream", "Received");
		if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
			mStreams.add(stream);
			if (mSubscriber == null) {
				subscribeToStream(stream);
			}
		}
	}

	@Override
	public void onStreamDropped(Session session, Stream stream) {
		if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
			if (mSubscriber != null) {
				unsubscribeFromStream(stream);
			}
		}
	}

	// invoked when the publisher starting streaming
	@Override
	public void onStreamCreated(PublisherKit publisher, Stream stream) {
		if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
			mStreams.add(stream);
			if (mSubscriber == null) {
				mPubSpinner.setVisibility(View.GONE);
				subscribeToStream(stream);
			}
		}
	}

	@Override
	public void onStreamDestroyed(PublisherKit publisher, Stream stream) {
	}

	@Override
	public void onError(PublisherKit publisher, OpentokError exception) {
		Log.i(LOGTAG, "Publisher exception: " + exception.getMessage());
	}

	@Override
	public void onVideoDisabled(SubscriberKit subscriber, String arg) {
		Log.i(LOGTAG,
				"Video quality changed. It is disabled for the subscriber.");
	}

	@Override
	public void onVideoDataReceived(SubscriberKit subscriber) {
		Log.i(LOGTAG, "First frame received");

		// stop loading spinning
		mSubSpinner.setVisibility(View.GONE);
		attachSubscriberView(mSubscriber);
	}


	@Override
	public void onVideoDisableWarning(SubscriberKit arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoDisableWarningLifted(SubscriberKit arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoEnabled(SubscriberKit arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	private class GetSessionIDAndTokenTask extends
			AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			return Utility.HTTPGET(params[0]);

		}

		@Override
		protected void onPostExecute(String result) {
			try {
				JSONObject sessionInfo = new JSONObject(result);
				sessionID = sessionInfo.getString(OpenTokConfig.SESSION_ID);
				token = sessionInfo.getString(OpenTokConfig.TOKEN);
				if (direction.equals(ViewReps.OUTGOING)) {
					createSession();
				} else {
					sessionConnect();
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void findTheOtherUser() {
		ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
		query.whereEqualTo("username", shareTo);
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> objects, ParseException e) {
				if (e == null && objects.size() > 0) {
					targetUser = objects.get(0);

					if (direction.equals(ViewReps.OUTGOING)) {
						new GetSessionIDAndTokenTask()
								.execute(SESSION_SERVICE_URL
										+ ParseUser.getCurrentUser()
												.getUsername());
					} else {
						String sessionID = mIntent
								.getStringExtra(ViewReps.SESSION_ID);
						new GetSessionIDAndTokenTask()
								.execute(TOKEN_SERVICE_URL
										+ sessionID
										+ "&user="
										+ ParseUser.getCurrentUser()
												.getUsername());
					}

					checkForSharedAssetsFire();

				} else {
					Log.d(TAG, "failed to find the target1");
				}
			}
		});

	}

	private void checkForSharedAssetsFire() {

		checkingDocumentSharingTask = new Runnable() {
			@Override
			public void run() {
				ParseQuery<PeerShare> query = ParseQuery
						.getQuery(PeerShare.class);
				query.whereEqualTo("shareTo", ParseUser.getCurrentUser());
				query.whereEqualTo("shareOwner", targetUser);
				query.include("shareAsset");
				query.findInBackground(new FindCallback<PeerShare>() {

					@Override
					public void done(List<PeerShare> objects, ParseException e) {
						Log.d(TAG, "Checking");
						for (PeerShare peerShare : objects) {
							Utility.openRemoteAsset(InCallActivity.this,
									(Asset) peerShare
											.getParseObject("shareAsset"),
									null, null, null);
							peerShare.deleteInBackground();
						}
						if(isIncallActivityRunning){
							mHandler.postDelayed(checkingDocumentSharingTask,
									INTERVAL);
						}
						
					}
				});

			}
		};
	
		checkingDocumentSharingTask.run();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.in_call, menu);
		return true;
	}
	
	
	
	
	/**
	 * This function allows for the use of the custom slide animation on the
	 * up/back button in the action bar. This function must be present in an
	 * activity to retain this functionality. A copy of this function is
	 * commented out in the utility class.
	 */
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			super.onMenuItemSelected(featureId, item);
			this.finish();
			overridePendingTransition(R.anim.slide_holder,
					R.anim.slide_out_right);
			break;
		case R.id.turn_video_on_off:
			mPublisher.setPublishVideo(!mPublisher.getPublishVideo());
			break;
		case R.id.turn_audio_on_off:
			mPublisher.setPublishAudio(!mPublisher.getPublishAudio());
			break;
		case R.id.btnShareDocument:
			shareDocument();
			break;
		case R.id.message:
			turnP2PMessagingOnOff();
			break;
		case R.id.btnAction:
			if (purpose.equals(PURPOSE_CERTIFICATION)) {
				item.setTitle("Certification");
			} else if (purpose.equals(PURPOSE_PITCH_PRACTICE)) {
				item.setTitle("Assessment");
			}
			btnActionitemClicked();
			break;
		default:
			break;
		}
		return true;
	}

	private void btnActionitemClicked() {
		Intent intent = new Intent(InCallActivity.this,
				IncallFormActivity.class);
		intent.putExtra(PURPOSE, getIntent().getStringExtra(PURPOSE));
		intent.putExtra(IncallFormActivity.TARGET_ID, targetUser.getObjectId());
		startActivity(intent);
	}

	private void shareDocument() {
		Intent intent = new Intent(InCallActivity.this, AssetsActivity.class);
		intent.putExtra(ViewReps.SHARE_TO, shareTo);
		intent.putExtra(SESSION_ID, sessionID);
		startActivity(intent);
	}

	
	
	
	@Override
	public void on(String event, IOAcknowledge arg1, Object... objects) {
		Log.d("Received message", objects[0].toString());
		String content = objects[0].toString();
		if (event.equals("CHAT:CHANNEL")) {
			
			try {
				JSONObject chatMessage = new JSONObject(content);

				chatMessage.put(
						"timestamp",
						DateFormat.getTimeFormat(
								InCallActivity.this).format(
								Calendar.getInstance().getTime()));
				MessagingDataForP2P.addChatMessage(chatMessage);	
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		
	}

	@Override
	public void onConnect() {
		SocketHandlerForP2P.connectToSession(sessionID);
		
	}

	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(SocketIOException arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(String arg0, IOAcknowledge arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
		// TODO Auto-generated method stub
		
	}
	

	private void setupDragandDrop(){
		
		mPreview.setOnTouchListener(new PreviewOnTouchListener());
	    ViewGroup mainlayout =(ViewGroup)findViewById(R.id.mainlayout);
	    mainlayout.setOnDragListener(new PreviewOnDragListener(mPreview));
	}

	@Override
	public void HandleNewMessage() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				MessagingDataForP2P.getPartyMessagingAdapter(
						InCallActivity.this).notifyDataSetChanged();
				int position = MessagingDataForP2P
						.getPartyMessagingAdapter(InCallActivity.this)
						.getCount() - 1;
				if (position >= 0) {
					chatHistory.setSelection(position);
				}
			}
		});
		
	}
	
	private void addHandlers(){
		btnSendMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					JSONObject chatMessage = new JSONObject();
					chatMessage.put("author", ParseUser.getCurrentUser()
							.getUsername());
					chatMessage.put("type", "chatmessage");
					chatMessage.put("text", messageToSend.getText().toString());

					chatMessage.put("timestamp",
							DateFormat.getTimeFormat(InCallActivity.this)
									.format(Calendar.getInstance().getTime()));
					messageToSend.setText("");
					SocketHandlerForP2P.sendChatMessage(chatMessage
							.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	private void turnP2PMessagingOnOff(){
		if (isP2PChatMessagingViewVisible) {
			p2pChatMessagingView.setVisibility(View.GONE);
			isP2PChatMessagingViewVisible = false;
		} else {
			p2pChatMessagingView.setVisibility(View.VISIBLE);
			isP2PChatMessagingViewVisible = true;
		}

	}
	
}
