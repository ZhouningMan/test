package com.insitehub.saleready_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.insitehub.saleready_android.Utility.Tinter;
import com.insitehub.saleready_android.DataModels.FormStructures.PreloadedForms;
import com.insitehub.saleready_android.Messaging.MessagingDataForP2P;
import com.insitehub.saleready_android.Messaging.MessagingDataForPartyChat;
import com.parse.ParseUser;

public class MainActivity extends Activity {

	public static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utility.setOrientationPerDevice(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setOnTouchListeners();
		getActionBar().setDisplayHomeAsUpEnabled(false);
		preloadForms();
		loadUsers();

		// updateMessages();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		if (item.getItemId() == R.id.messaging) {
			Intent intent = new Intent(this, MainActivityMessagesActivity.class);
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}

	public void navigate(View button) {
		Intent intent;
		switch (button.getId()) {

		case R.id.pitch_practice_button:
			Intent viewRepIntent = new Intent(MainActivity.this, ViewReps.class);
			viewRepIntent.putExtra(InCallActivity.PURPOSE,
					InCallActivity.PURPOSE_PITCH_PRACTICE);
			startActivity(viewRepIntent);
			break;

		case R.id.coaching_corner_button:
			Intent coachingCornerIntent = new Intent(MainActivity.this,
					CoachingCornerActivity.class);
			startActivity(coachingCornerIntent);
			break;

		case R.id.certification_center_button:
			intent = new Intent(MainActivity.this, ViewReps.class);
			intent.putExtra(InCallActivity.PURPOSE,
					InCallActivity.PURPOSE_CERTIFICATION);
			startActivity(intent);
			break;

		case R.id.knowledge_center_button:
			Intent KnowledgeCenterIntent = new Intent(this,
					KnowledgeCenterActivity.class);
			startActivity(KnowledgeCenterIntent);
			break;

		case R.id.performance_button:
			Intent PerformanceReportingIntent = new Intent(this,
					PerformanceReportingActivity.class);
			startActivity(PerformanceReportingIntent);
			break;

		case R.id.group_chat_button:
			Intent groupChatIntent = new Intent(MainActivity.this,
					PartyChatActivity.class);
			startActivity(groupChatIntent);
			break;

		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void setOnTouchListeners() {
		Tinter tinter = new Tinter();
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
		for (int i = 0; i < relativeLayout.getChildCount(); i++) {
			if (relativeLayout.getChildAt(i) instanceof ImageView) {
				relativeLayout.getChildAt(i).setOnTouchListener(tinter);
			}
		}
	}

	public void logoutFromActionBar(MenuItem item) {
		logout();
	}

	public void logout() {
		SocketHandlerForPartyChat.disconnectSocketConnection();
		SocketHandlerForP2P.disconnectSocketConnection();
		ParseUser.logOut();
		MessagingDataForP2P.clearMessagingData();
		MessagingDataForPartyChat.clearMessagingData();
		Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
		logoutIntent.putExtra("fromMain", true);
		logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(logoutIntent);
		finish();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_holder);
	}

	public void onBackPressed() {
		moveTaskToBack(true);
	}

	private void loadUsers() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				UserGroup.loadUsers(MainActivity.this);

			}
		}).start();

	}

	private void preloadForms() {
		if (!PreloadedForms.isAssessmentFormLoaded()) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					synchronized (PreloadedForms.assessmentForm) {
						ParseUser user = ParseUser.getCurrentUser();
						String tenantId = user.getString("tenantId");
						String url = "http://admin.mobilesfe.com/get_form_structure?id="
								+ tenantId + "&type=" + "AssessmentForm";

						String result = Utility.HTTPGET(url);
						// Log.d("JSON Str", result);
						Utility.buildDynamicForm(result,
								PreloadedForms.assessmentForm);
						PreloadedForms.setAssessmentFormLoaded(true);
						PreloadedForms.assessmentForm.notify();
					}
				}
			}).start();
		}

		if (!PreloadedForms.isCertificationFormLoaded()) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					synchronized (PreloadedForms.certificationForm) {
						ParseUser user = ParseUser.getCurrentUser();
						String tenantId = user.getString("tenantId");
						String url = "http://admin.mobilesfe.com/get_form_structure?id="
								+ tenantId + "&type=" + "CertificationForm";
						String result = Utility.HTTPGET(url);
						// Log.d("JSON Str", result);
						Utility.buildDynamicForm(result,
								PreloadedForms.certificationForm);
						PreloadedForms.setCertificationFormLoaded(true);
						PreloadedForms.certificationForm.notify();
					}
				}
			}).start();
		}

		if (!PreloadedForms.isPreCoachingFormLoaded()) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					synchronized (PreloadedForms.preCoachingForm) {
						ParseUser user = ParseUser.getCurrentUser();
						String tenantId = user.getString("tenantId");
						String url = "http://admin.mobilesfe.com/get_form_structure?id="
								+ tenantId + "&type=" + "PreCoachingForm";
						String result = Utility.HTTPGET(url);
						Utility.buildDynamicForm(result,
								PreloadedForms.preCoachingForm);
						PreloadedForms.setPreCoachingFormLoaded(true);
						PreloadedForms.preCoachingForm.notify();
					}

				}
			}).start();
		}

		if (!PreloadedForms.isFieldCoachingFormLoaded()) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					synchronized (PreloadedForms.fieldCoachingForm) {
						ParseUser user = ParseUser.getCurrentUser();
						String tenantId = user.getString("tenantId");
						String url = "http://admin.mobilesfe.com/get_form_structure?id="
								+ tenantId + "&type=" + "ActiveForm";
						String result = Utility.HTTPGET(url);
						Utility.buildDynamicForm(result,
								PreloadedForms.fieldCoachingForm);
						PreloadedForms.setFieldCoachingFormLoaded(true);
						PreloadedForms.fieldCoachingForm.notify();
					}
				}
			}).start();
		}

		if (!PreloadedForms.isPostCoachingFormLoaded()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					synchronized (PreloadedForms.postCoachingForm) {
						ParseUser user = ParseUser.getCurrentUser();
						String tenantId = user.getString("tenantId");
						String url = "http://admin.mobilesfe.com/get_form_structure?id="
								+ tenantId + "&type=" + "PostCoachingForm";
						String result = Utility.HTTPGET(url);
						Utility.buildDynamicForm(result,
								PreloadedForms.postCoachingForm);
						PreloadedForms.setPostCoachingFormLoaded(true);
						PreloadedForms.postCoachingForm.notify();
					}
				}
			}).start();
		}
	}

}
