package com.insitehub.saleready_android;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.insitehub.saleready_android.DataModels.SaveFormInJson;
import com.insitehub.saleready_android.DataModels.FormStructures.Form;
import com.insitehub.saleready_android.DataModels.FormStructures.PreloadedForms;
import com.parse.ParseUser;

public class CoachingCornerActivity extends Activity {

	public static final String LOG_TAG = CoachingCornerActivity.class
			.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		setContentView(R.layout.activity_coaching_corner);
		if (savedInstanceState == null) {
			getFragmentManager()
					.beginTransaction()
					.add(R.id.coaching_corner_container,
							new CoachinGCornerFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.coaching_corner, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class CoachinGCornerFragment extends Fragment {

		private static final String PRECOACHING_FORM = "PreCoachingForm";
		private static final String FIELD_COACHING_FORM = "ActiveForm";
		private static final String POST_COACHING_FORM = "PostCoachingForm";
		public static final String URL_SAVE_FORM = "http://admin.mobilesfe.com/save_form";

		private TextView mPreCoach_textview;
		private TextView mfieldCoach_textview;
		private TextView mPostCoach_textview;
		private ListView usersView;
		private ScrollView formContainer;
		private Button mbtnSave;
		private ProgressBar formLoadingBar;

		private HashMap<String, View> formViews = new LinkedHashMap<String, View>();

		private ParseUser selectedUser;
		private Form selectedForm;

		public CoachinGCornerFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View rootView = inflater.inflate(R.layout.fragment_coaching_corner,
					container, false);
			usersView = (ListView) rootView.findViewById(R.id.listview_users);
			usersView.setAdapter(UserGroup.getUsersAdapter(getActivity()
					.getApplicationContext()));
			mbtnSave = (Button) rootView.findViewById(R.id.btnSaveForm);
			formLoadingBar = (ProgressBar) rootView
					.findViewById(R.id.formLoadingSpinner);
			mPreCoach_textview = (TextView) rootView
					.findViewById(R.id.preCoach_textview);
			mPostCoach_textview = (TextView) rootView
					.findViewById(R.id.postCoach_textview);
			mfieldCoach_textview = (TextView) rootView
					.findViewById(R.id.fieldCoach_textview);
			formContainer = (ScrollView) rootView
					.findViewById(R.id.formContainer);

			addHanlderForViews();

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			usersView.performItemClick(null, 0,
					usersView.getFirstVisiblePosition());
			mPreCoach_textview.performClick();
		}

		private void addHanlderForViews() {

			mbtnSave.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new AsyncTask<Void, Void, Void>() {

						@Override
						protected Void doInBackground(Void... params) {
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							selectedForm.setUserID(selectedUser.getObjectId());
							SaveFormInJson.saveFormInJson(out, selectedForm);
							final String jsonStr = "formdata="
									+ new String(out.toByteArray());
							Utility.HTTP_POST(URL_SAVE_FORM, jsonStr);
							return null;
						}

						protected void onPostExecute(Void result) {
							Toast.makeText(getActivity(), "Form Saved",
									Toast.LENGTH_SHORT).show();
						};

					}.execute();

				}
			});

			usersView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> adapter, View view,
						int arg2, long arg3) {

					if (view == null) {
						selectedUser = (ParseUser) adapter.getAdapter()
								.getItem(0);
					} else {
						
						
						selectedUser = ((UserGroup.ViewHolder)view.getTag()).getParseUser();
					}

				}
			});

			mPreCoach_textview.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					formClickedHandler(PRECOACHING_FORM,
							PreloadedForms.preCoachingForm);
				}
			});

			mfieldCoach_textview.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					formClickedHandler(FIELD_COACHING_FORM,
							PreloadedForms.fieldCoachingForm);
				}
			});

			mPostCoach_textview.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					formClickedHandler(POST_COACHING_FORM,
							PreloadedForms.postCoachingForm);
				}
			});

		}

		private void formClickedHandler(final String formName, Form form) {

			formContainer.removeAllViews();
			if (formViews.get(formName) != null) {
				if (formName.equals(PRECOACHING_FORM)) {
					selectedForm = PreloadedForms.preCoachingForm;
				} else if (formName.equals(FIELD_COACHING_FORM)) {
					selectedForm = PreloadedForms.fieldCoachingForm;
				} else if (formName.equals(POST_COACHING_FORM)) {
					selectedForm = PreloadedForms.postCoachingForm;
				}
				formContainer.addView(formViews.get(formName));
			} else {

				new AsyncTask<Form, Void, Form>() {

					@Override
					protected Form doInBackground(Form... params) {
						Form form = params[0];
						synchronized (form) {
							try {
								if (formName.equals(PRECOACHING_FORM)) {
									while (!PreloadedForms
											.isPreCoachingFormLoaded()) {
										form.wait();
									}
								} else if (formName.equals(FIELD_COACHING_FORM)) {
									while (!PreloadedForms
											.isFieldCoachingFormLoaded()) {
										form.wait();
									}
								} else if (formName.equals(POST_COACHING_FORM)) {
									while (!PreloadedForms
											.isPostCoachingFormLoaded()) {
										form.wait();
									}
								}
								selectedForm = form;
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

						}
						return form;
					}

					protected void onPostExecute(Form form) {
						View view = Utility.buildDynamicFormUI(getActivity(),
								form, form.isWritable());
						formViews.put(formName, view);
						formLoadingBar.setVisibility(View.GONE);
						mbtnSave.setVisibility(View.VISIBLE);
						formContainer.addView(view);
					}

				}.execute(form);

			}
		}
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
