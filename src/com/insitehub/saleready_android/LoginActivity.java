package com.insitehub.saleready_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.insitehub.saleready_android.Utility.Tinter;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends Activity {

	private RelativeLayout rootView;
	private EditText usernameField;
	private EditText passwordField;
	private Button loginButton;
	private Animation fadeIn;
	private Animation fadeOut;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();

		Intent startIntent = getIntent();
		if (startIntent.hasExtra("fromMain")) {
			setContentView(R.layout.white_screen);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					setContentView(R.layout.activity_login);
					setup();
				}
			}, 500);
		} else {
			setContentView(R.layout.activity_login);
			setup();
		}

	}

	public void setup() {
		// Get references for views
		this.usernameField = (EditText) findViewById(R.id.username);
		this.passwordField = (EditText) findViewById(R.id.password);
		this.loginButton = (Button) findViewById(R.id.login_button);
		this.rootView = (RelativeLayout) findViewById(R.id.root_login);
		this.fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		this.fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

		Tinter tinter = new Tinter();
		loginButton.setOnTouchListener(tinter);

		for (int i = 0; i < rootView.getChildCount(); i++) {
			rootView.getChildAt(i).startAnimation(fadeIn);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}

	public void login(View v) {
		String username = usernameField.getText().toString();
		String password = passwordField.getText().toString();

		ParseUser.logInInBackground(username, password, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException e) {
				if (user != null) {
					Log.d("Login Success: ",
							user.getUsername() + " " + user.getEmail());
					for (int i = 0; i < rootView.getChildCount(); i++) {
						rootView.getChildAt(i).startAnimation(fadeOut);
					}
					// TODO make the handler its own class
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							setContentView(R.layout.white_screen);
							Intent mainIntent = new Intent(LoginActivity.this,
									MainActivity.class);
							LoginActivity.this.startActivity(mainIntent);
							LoginActivity.this.finish();
							overridePendingTransition(R.anim.fade_in,
									R.anim.fade_holder);
						}
					}, 500);
				} else {
					Log.d("Login failed: ", e.toString());
					showLoginErrorToast();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void showLoginErrorToast() {
		Toast errorToast = Toast.makeText(this,
				"Login Failed: Please Try Again", Toast.LENGTH_SHORT);
		errorToast.setGravity(Gravity.CENTER, 0, 0);
		errorToast.show();
	}

}
