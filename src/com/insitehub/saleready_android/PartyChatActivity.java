package com.insitehub.saleready_android;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.RelativeLayout;

import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.PartySession;
import com.insitehub.saleready_android.Messaging.MessagingDataForPartyChat;
import com.insitehub.saleready_android.Messaging.NewMessagingListner;
import com.insitehub.saleready_android.config.OpenTokConfig;
import com.insitehub.saleready_android.group_chat.MySession;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class PartyChatActivity extends Activity implements IOCallback,
		NewMessagingListner {
	public static final String SESSIONID = "party session";
	public static final String TOKEN = "party token";
	public static final String ROLE = "party role";

	public static final String TAG = PartyChatActivity.class.getSimpleName();

	private MySession mSession;
	private RelativeLayout mPreview;
	private LinearLayout mGridview;
	private ViewGroup mVideoContainer;

	private String chatSession;
	private String chatSessionToken;

	private ListView chatHistory;
	private Button btnSendMessage;
	private EditText messageToSend;
	private LinearLayout groupChatMessagingView;
	private boolean isGroupChatMessagingViewVisible = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		setContentView(R.layout.activity_party_chat);
		mVideoContainer = (ViewGroup) findViewById(R.id.video_container);
		mPreview = (RelativeLayout) findViewById(R.id.preview);
		mGridview = (LinearLayout) findViewById(R.id.gridview_participants);
		groupChatMessagingView = (LinearLayout) findViewById(R.id.group_chat_messaging_view);

		chatHistory = (ListView) findViewById(R.id.messageHistory);
		btnSendMessage = (Button) findViewById(R.id.sendMessage);
		messageToSend = (EditText) findViewById(R.id.chatMessage);

		chatHistory.setAdapter(MessagingDataForPartyChat
				.getPartyMessagingAdapter(this));
		MessagingDataForPartyChat.addNewMessagesListner(this);

		setupDragandDrop();
		addHandlers();
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mSession == null) {
			participateGroupChat();
		}
		if (mSession != null) {
			mSession.onResume();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mSession != null) {
			// mSession.onPause();
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

	public void onBackPressed() {
		if (mSession != null) {
			mSession.disconnect();
		}
		super.onBackPressed();
	}

	public void participateGroupChat() {
		ParseQuery<PartySession> query = ParseQuery
				.getQuery(PartySession.class);
		query.whereEqualTo("tenant",
				ParseUser.getCurrentUser().getParseObject("tenant"));
		query.findInBackground(new FindCallback<PartySession>() {

			@Override
			public void done(List<PartySession> objects, ParseException e) {
				if (objects.size() > 0) {
					PartySession party = objects.get(0);
					joinSession(party.getString("partySession"));
				} else {

				}
			}
		});
	}

	public void joinSession(String sessionID) {
		String url = "http://admin.mobilesfe.com/get_token?session="
				+ sessionID + "&user="
				+ ParseUser.getCurrentUser().getUsername();
		new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... params) {
				return Utility.HTTPGET(params[0]);
			}

			@Override
			protected void onPostExecute(String result) {
				try {
					JSONObject sessionInfo = new JSONObject(result);
					String sessionID = sessionInfo
							.getString(OpenTokConfig.SESSION_ID);
					String token = sessionInfo.getString(OpenTokConfig.TOKEN);
					sessionConnect(sessionID, token);
				} catch (JSONException e) {
					Log.e("JSON Exception", "fail to get token for party chat");
				}
			}

		}.execute(url);
	}

	public void createSession() {
		String url = "http://admin.mobilesfe.com/get_session?user="
				+ ParseUser.getCurrentUser().getUsername();
		new AsyncTask<String, Void, String>() {

			@Override
			protected String doInBackground(String... params) {
				return Utility.HTTPGET(params[0]);
			}

			@Override
			protected void onPostExecute(String result) {
				try {
					JSONObject sessionInfo = new JSONObject(result);
					String sessionID = sessionInfo
							.getString(OpenTokConfig.SESSION_ID);
					String token = sessionInfo.getString(OpenTokConfig.TOKEN);
					sessionConnect(sessionID, token);

					PartySession partySession = new PartySession();
					partySession.put("tenant", ParseUser.getCurrentUser()
							.getParseObject("tenant"));
					partySession.put("partySession", sessionID);
					partySession.saveInBackground();
				} catch (JSONException e) {
					Log.e("JSON Exception", "fail to create party chat session");
				}
			}

		}.execute(url);
	}

	private void sessionConnect(String sessionID, String token) {
		if (mSession == null) {

			chatSession = sessionID;
			SocketHandlerForPartyChat.startSocketConnection(this);
			chatSessionToken = token;
			mSession = new MySession(this, OpenTokConfig.API_KEY, sessionID);
			mSession.setPreviewView(mPreview);
			mSession.setParticipantsViewContainer(mGridview);
			mSession.connect(token);
		}
	}

	private void setupDragandDrop() {

		mPreview.setOnTouchListener(new PreviewOnTouchListener());
		mVideoContainer.setOnDragListener(new PreviewOnDragListener(mPreview));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.party_chat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
		case R.id.document:
			Intent documentIntent = new Intent(this, AssetsActivity.class);
			documentIntent.putExtra(SESSIONID, chatSession);
			documentIntent.putExtra(TOKEN, chatSessionToken);
			documentIntent.putExtra(ROLE, "publisher");
			startActivity(documentIntent);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
		case R.id.document:
			Intent intent = new Intent(this, AssetsActivity.class);
			intent.putExtra(SESSIONID, chatSession);
			intent.putExtra(TOKEN, chatSessionToken);
			intent.putExtra(ROLE, "publisher");
			startActivity(intent);
			break;
		case R.id.message:
			turnPartyMessagingOnOff();
			break;
		case R.id.video:
			mSession.getPublisher().setPublishVideo(
					!mSession.getPublisher().getPublishVideo());
			break;
		case R.id.audio:
			mSession.getPublisher().setPublishAudio(
					!mSession.getPublisher().getPublishAudio());
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void on(String event, IOAcknowledge ack, Object... objects) {
		Log.d("Received message", objects[0].toString());
		String content = objects[0].toString();
		if (event.equals("CHAT:CHANNEL")) {

			try {
				JSONObject chatMessage = new JSONObject(content);

				chatMessage.put("timestamp",
						DateFormat.getTimeFormat(PartyChatActivity.this)
								.format(Calendar.getInstance().getTime()));
				MessagingDataForPartyChat.addChatMessage(chatMessage);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public void onConnect() {

		SocketHandlerForPartyChat.connectToSession(chatSession);

	}

	@Override
	public void onDisconnect() {

	}

	@Override
	public void onError(SocketIOException arg0) {

	}

	@Override
	public void onMessage(String arg0, IOAcknowledge arg1) {

	}

	@Override
	public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
		Log.d("on message", arg0.toString());
	}

	@Override
	public void HandleNewMessage() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				MessagingDataForPartyChat.getPartyMessagingAdapter(
						PartyChatActivity.this).notifyDataSetChanged();
				int position = MessagingDataForPartyChat
						.getPartyMessagingAdapter(PartyChatActivity.this)
						.getCount() - 1;
				if (position >= 0) {
					chatHistory.setSelection(position);
				}

			}
		});

	}

	private void addHandlers() {
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
							DateFormat.getTimeFormat(PartyChatActivity.this)
									.format(Calendar.getInstance().getTime()));
					messageToSend.setText("");
					SocketHandlerForPartyChat.sendChatMessage(chatMessage
							.toString());

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private void turnPartyMessagingOnOff() {
		if (isGroupChatMessagingViewVisible) {
			groupChatMessagingView.setVisibility(View.GONE);
			isGroupChatMessagingViewVisible = false;
		} else {
			groupChatMessagingView.setVisibility(View.VISIBLE);
			isGroupChatMessagingViewVisible = true;
		}

	}

}
