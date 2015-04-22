package com.insitehub.saleready_android;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.PeerSession;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ViewReps extends Activity {
	public final static String USER_NAMES = "User_Name";
	public final static String TARGET_USER = "target";
	public final static String DIRECTION = "direction";
	public final static String INCOMING = "incoming";
	public final static String OUTGOING = "outgoing";
	public final static String SESSION_ID = "sessionId";
	public static final String SHARE_TO = "share to";
	public final static long INTERVAL = 10000;
	public static String TAG = ViewReps.class.getSimpleName();

	private Handler mhandler = new Handler();
	private Runnable mCheckingIncomingCallTask;
	private boolean isIncomingCallRequestShown = false;
	private boolean isViewRepsRunning = true;
	private String shareTo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		setContentView(R.layout.activity_view_reps);
		ListView listView = (ListView) findViewById(R.id.listView_rep);
		listView.setAdapter(UserGroup.getUsersAdapter(getApplicationContext()));
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				startInCallActivity(OUTGOING, ((UserGroup.ViewHolder) view
						.getTag()).getParseUser().getUsername(), null);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_reps, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void checkingIncomingCallTask() {
		mCheckingIncomingCallTask = new Runnable() {
			@Override
			public void run() {
				if (isIncomingCallRequestShown) {
					return;
				}
				Log.d("mCheckingIncomingCallTask", "running");
				ParseQuery<PeerSession> query = ParseQuery
						.getQuery("PeerSession");
				query.whereEqualTo("requestTo", ParseUser.getCurrentUser());
				query.include("sessionOwner");
				query.addDescendingOrder("createdAt");
				query.findInBackground(new FindCallback<PeerSession>() {

					@Override
					public void done(List<PeerSession> peerSessions,
							ParseException e) {
						boolean first = true;
						for (PeerSession peerSession : peerSessions) {
							long lastUpdatedAtMs = peerSession.getUpdatedAt()
									.getTime();
							long diffInMs = System.currentTimeMillis()
									- lastUpdatedAtMs;
							long diffInSec = TimeUnit.MILLISECONDS
									.toSeconds(diffInMs);
							if (diffInSec > 240) {
								peerSession.deleteInBackground();
								continue;
							}

							if (first) {
								first = false;
								isIncomingCallRequestShown = true;
								String caller = peerSession.getParseUser(
										"sessionOwner").getUsername();
								shareTo = peerSession.getParseUser(
										"sessionOwner").getUsername();
								mhandler.removeCallbacksAndMessages(null);
								buildAlertDialog(caller,
										peerSession.getString(SESSION_ID));

								peerSession.deleteInBackground();
							} else {
								peerSession.deleteInBackground();
							}
						}
					}
				});

				if (!isIncomingCallRequestShown && isViewRepsRunning) {
					mhandler.postDelayed(mCheckingIncomingCallTask, INTERVAL);
				}
			}
		};
	}

	private void startInCallActivity(String direction, String target,
			String sessionID) {
		shareTo = target;
		Intent intent = new Intent(ViewReps.this, InCallActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(DIRECTION, direction);
		intent.putExtra(SHARE_TO, shareTo);
		if (direction.equals(OUTGOING)) {
			intent.putExtra(TARGET_USER, target);
		} else {
			intent.putExtra(SESSION_ID, sessionID);
			// intent.putextra
		}

		intent.putExtra(InCallActivity.PURPOSE,
				getIntent().getStringExtra(InCallActivity.PURPOSE));
		startActivity(intent);
	}

	private void buildAlertDialog(final String caller, final String sessionID) {
		mhandler.removeCallbacksAndMessages(null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(caller + " wants to connect, " + "Do you accept?")
				.setTitle("Incoming call");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				isIncomingCallRequestShown = false;
				startInCallActivity(INCOMING, caller, sessionID);
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						isIncomingCallRequestShown = false;
						mhandler.postDelayed(mCheckingIncomingCallTask,
								INTERVAL);
					}
				});
		AlertDialog dialog = builder.create();
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				isIncomingCallRequestShown = false;
				mhandler.postDelayed(mCheckingIncomingCallTask, INTERVAL);
			}
		});
		dialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isViewRepsRunning = true;
		if (mCheckingIncomingCallTask == null) {
			checkingIncomingCallTask();
		}
		Log.d("isIncomingCallRequestShown",
				String.valueOf(isIncomingCallRequestShown));
		isIncomingCallRequestShown = false;
		mhandler.postDelayed(mCheckingIncomingCallTask, INTERVAL);

	}

	@Override
	protected void onPause() {
		super.onPause();
		isIncomingCallRequestShown = true;

	}

	
	@Override
	protected void onStop() {
		super.onStop();
		isViewRepsRunning = false;
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
		default:
			break;
		}
		return true;
	}
}
