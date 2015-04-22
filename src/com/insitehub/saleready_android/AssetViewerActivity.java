package com.insitehub.saleready_android;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.insitehub.saleready_android.config.OpenTokConfig;
import com.joanzapata.pdfview.PDFView;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.PublisherKit.PublisherKitVideoType;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

public class AssetViewerActivity extends Activity implements
		Session.SessionListener, Publisher.PublisherListener,
		Subscriber.VideoListener, Subscriber.SubscriberListener {
	private static final String LOGTAG = AssetViewerActivity.class
			.getSimpleName();
	
	private static final String PUBLISHER = "publisher";
	
	
	private Session mSession;
	private Publisher mPublisher;
	private Subscriber mSubscriber;
	private ArrayList<Stream> mStreams;
	private Handler mHandler = new Handler();

	private RelativeLayout mSubscriberViewContainer;
	// Spinning wheel for loading subscriber view
	private ProgressBar mLoadingSub;
	private View sharedView;

	private boolean resumeHasRun = false;

	private PDFView mPDFViewer;
	private WebView mWebViewer;

	private String role; //publisher: the user publish his/her document screen
	private String type; // document type
	
	private String chatSessionToken;
	private String chatSessionID;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		setContentView(R.layout.activity_assetviewer);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		Intent intent = getIntent();
		role = intent.getStringExtra(PartyChatActivity.ROLE);
	    type = intent.getStringExtra("type");
	    chatSessionToken = intent.getStringExtra(PartyChatActivity.TOKEN);
	    chatSessionID = intent.getStringExtra(PartyChatActivity.SESSIONID);
		mPDFViewer = (PDFView) findViewById(R.id.pdfView);
		mWebViewer = (WebView) findViewById(R.id.webView);

		mSubscriberViewContainer = (RelativeLayout) findViewById(R.id.subscriberview);
		mLoadingSub = (ProgressBar) findViewById(R.id.loadingSpinner);
		mStreams = new ArrayList<Stream>();

		
		if(role!=null){
			sessionConnect();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pdfeditor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();

		if(role == null || (role!=null && role.equals("publisher"))){
			if (type.equals("pdf")) {
				mWebViewer.setVisibility(View.GONE);
				String path = intent.getStringExtra("path");
				mPDFViewer = (PDFView) findViewById(R.id.pdfView);

				mPDFViewer.fromFile(new File(path)) // you can also load from File
													// using pdfView.fromFile()
						.defaultPage(0).load();
				mPDFViewer.enableSwipe(true);
				sharedView = mPDFViewer;

			} else if (type.equals("link")) {
				mPDFViewer.setVisibility(View.GONE);
				mWebViewer.setWebViewClient(new WebViewClient());
				mWebViewer.loadUrl(intent.getStringExtra("hyperlink"));
				sharedView= mWebViewer;

			}
			setTitle(getIntent().getStringExtra("title"));
		}
		else{
			mSubscriberViewContainer.setVisibility(View.VISIBLE);
			mLoadingSub.setVisibility(View.VISIBLE);
			mPDFViewer.setVisibility(View.GONE);
			mWebViewer.setVisibility(View.GONE);
		}
		

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
	protected void onPause() {
		super.onPause();

		if (mSession != null) {
			mSession.onPause();

			if (mSubscriber != null) {
				mSubscriberViewContainer.removeView(mSubscriber.getView());
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (isFinishing()) {
			if (mSession != null) {
				mSession.disconnect();
			}
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

	private void reloadInterface() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mSubscriber != null) {
					attachSubscriberView(mSubscriber);
				}
			}
		}, 500);
	}

	private void sessionConnect() {
		if (mSession == null) {
			mSession = new Session(AssetViewerActivity.this,
					OpenTokConfig.API_KEY,chatSessionID);
			mSession.setSessionListener(this);
			mSession.connect(chatSessionToken);
		}
	}

	@Override
	public void onConnected(Session session) {
		Log.i(LOGTAG, "Connected to the chatSessionID.");

		// Start screensharing
		if (mPublisher == null && role.equals("publisher")) {
			mPublisher = new Publisher(AssetViewerActivity.this, "publisher");
			mPublisher.setPublisherListener(this);
			mPublisher
					.setPublisherVideoType(PublisherKitVideoType.PublisherKitVideoTypeScreen);
			mPublisher.setAudioFallbackEnabled(false);
			ScreensharingCapturer screenCapturer = new ScreensharingCapturer(
					this, sharedView);
			mPublisher.setCapturer(screenCapturer);
			mPublisher.setPublishAudio(false);
			mSession.publish(mPublisher);
		}

	}

	@Override
	public void onDisconnected(Session session) {
		Log.i(LOGTAG, "Disconnected from the chatSessionID.");
		if (mSubscriber != null) {
			mSubscriberViewContainer.removeView(mSubscriber.getView());
		}

		mPublisher = null;
		mSubscriber = null;
		mStreams.clear();
		mSession = null;
	}

	private void subscribeToStream(Stream stream) {
		mSubscriber = new Subscriber(AssetViewerActivity.this, stream);
		mSubscriber.setVideoListener(this);
		mSubscriber.setSubscriberListener(this);
		mSubscriber.setSubscribeToAudio(false);
		mSession.subscribe(mSubscriber);
		mSubscriberViewContainer.setVisibility(View.VISIBLE);
		if (mSubscriber.getSubscribeToVideo()) {
			// start loading spinning
			mLoadingSub.setVisibility(View.VISIBLE);
		}
	}

	private void unsubscribeFromStream(Stream stream) {
		mStreams.remove(stream);
		if (mSubscriber.getStream().equals(stream)) {
			mSubscriberViewContainer.removeView(mSubscriber.getView());
			mSubscriberViewContainer.setVisibility(View.GONE);
			mSubscriber = null;
			if (!mStreams.isEmpty()) {
				subscribeToStream(mStreams.get(0));
			}
		}
	}

	private void attachSubscriberView(Subscriber subscriber) {
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				getResources().getDisplayMetrics().widthPixels, getResources()
						.getDisplayMetrics().heightPixels);
		mSubscriberViewContainer.removeView(mSubscriber.getView());
		mSubscriberViewContainer.addView(mSubscriber.getView(), layoutParams);
	}

	@Override
	public void onError(Session session, OpentokError exception) {
		Log.i(LOGTAG, "Session exception: " + exception.getMessage());
	}

	@Override
	public void onStreamReceived(Session session, Stream stream) {
		if (!OpenTokConfig.SUBSCRIBE_TO_SELF && !role.equals(PUBLISHER)) {
			mStreams.add(stream);
			if (mSubscriber == null) {
				subscribeToStream(stream);
			}
		}
	}

	@Override
	public void onStreamDropped(Session session, Stream stream) {
		if (!OpenTokConfig.SUBSCRIBE_TO_SELF && !role.equals(PUBLISHER) ) {
			if (mSubscriber != null) {
				unsubscribeFromStream(stream);
			}
		}
	}

	@Override
	public void onStreamCreated(PublisherKit publisher, Stream stream) {
		if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
			mStreams.add(stream);
			if (mSubscriber == null) {
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
	public void onVideoDataReceived(SubscriberKit subscriber) {
		Log.i(LOGTAG, "First frame received");

		// stop loading spinning
		mLoadingSub.setVisibility(View.GONE);
		attachSubscriberView(mSubscriber);
	}

	/**
	 * Converts dp to real pixels, according to the screen density.
	 *
	 * @param dp
	 *            A number of density-independent pixels.
	 * @return The equivalent number of real pixels.
	 */
//	private int dpToPx(int dp) {
//		double screenDensity = this.getResources().getDisplayMetrics().density;
//		return (int) (screenDensity * (double) dp);
//	}

	@Override
	public void onVideoDisabled(SubscriberKit subscriber, String reason) {
		Log.i(LOGTAG, "Video disabled:" + reason);
	}

	@Override
	public void onVideoEnabled(SubscriberKit subscriber, String reason) {
		Log.i(LOGTAG, "Video enabled:" + reason);
	}

	@Override
	public void onVideoDisableWarning(SubscriberKit subscriber) {
		Log.i(LOGTAG,
				"Video may be disabled soon due to network quality degradation. Add UI handling here.");
	}

	@Override
	public void onVideoDisableWarningLifted(SubscriberKit subscriber) {
		Log.i(LOGTAG,
				"Video may no longer be disabled as stream quality improved. Add UI handling here.");
	}

	@Override
	public void onConnected(SubscriberKit subscriber) {
		Log.i(LOGTAG, "Subscriber is connected: ");

	}

	@Override
	public void onDisconnected(SubscriberKit subscriber) {
		Log.i(LOGTAG, "Subscriber is disconnected: ");

	}

	@Override
	public void onError(SubscriberKit subscriber, OpentokError exception) {
		Log.i(LOGTAG, "Subscriber exception: " + exception.getMessage());
	}

}
