package com.insitehub.saleready_android;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.insitehub.saleready_android.DataModels.FormStructures.FormMetaData;
import com.parse.ParseUser;

public class SavedFormsActivity extends Activity {

	public final static String LOG_TAG = SavedFormsActivity.class
			.getSimpleName();

	private ListView usersView;
	private ListView assessmentsView;
	private ListView coachingSetsView;
	private ListView certificationsView;

	private List<FormMetaData> assessmentForms = new LinkedList<FormMetaData>();
	private List<FormMetaData> certificationForms = new LinkedList<FormMetaData>();
	private List<Set<FormMetaData>> coachingForms = new LinkedList<Set<FormMetaData>>();

	private BaseAdapter coachingFormsAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout coachingFormView = new LinearLayout(
					SavedFormsActivity.this);
			// certificationView.setLayoutParams(new
			// LayoutParams(LayoutParams.MATCH_PARENT,
			// LayoutParams.MATCH_PARENT));
			coachingFormView.setOrientation(LinearLayout.VERTICAL);

			Set<FormMetaData> coachingSet = coachingForms.get(position);
			for (FormMetaData formMetaData : coachingSet) {
				TextView name = new TextView(SavedFormsActivity.this);
				TextView dateOfCreation = new TextView(SavedFormsActivity.this);
				name.setTextAppearance(SavedFormsActivity.this,
						android.R.style.TextAppearance_DeviceDefault_Medium);
				name.setText(formMetaData.getName());
				dateOfCreation.setTextAppearance(SavedFormsActivity.this,
						android.R.style.TextAppearance_DeviceDefault_Small);
				dateOfCreation.setText(formMetaData.getDateOfCreation());
				coachingFormView.addView(name);
				coachingFormView.addView(dateOfCreation);
			}
			coachingFormView.setTag(coachingSet);
			coachingFormView.setPadding(5, 5, 5, 5);
			return coachingFormView;

		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return coachingForms.size();
		}
	};

	private BaseAdapter certificationsAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout certificationView = new LinearLayout(
					SavedFormsActivity.this);
			// certificationView.setLayoutParams(new
			// LayoutParams(LayoutParams.MATCH_PARENT,
			// LayoutParams.MATCH_PARENT));
			certificationView.setOrientation(LinearLayout.VERTICAL);
			TextView name = new TextView(SavedFormsActivity.this);
			TextView dateOfCreation = new TextView(SavedFormsActivity.this);
			FormMetaData certification = certificationForms.get(position);

			name.setTextAppearance(SavedFormsActivity.this,
					android.R.style.TextAppearance_DeviceDefault_Medium);
			name.setText(certification.getName());
			dateOfCreation.setTextAppearance(SavedFormsActivity.this,
					android.R.style.TextAppearance_DeviceDefault_Small);
			dateOfCreation.setText(certification.getDateOfCreation());

			certificationView.addView(name);
			certificationView.setTag(certification);
			certificationView.addView(dateOfCreation);
			certificationView.setPadding(5, 5, 5, 5);
			return certificationView;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return certificationForms.size();
		}
	};

	private BaseAdapter assessmentsAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout assessmentView = new LinearLayout(
					SavedFormsActivity.this);
			// assessmentView.setLayoutParams(new
			// LayoutParams(LayoutParams.MATCH_PARENT,
			// LayoutParams.MATCH_PARENT));
			assessmentView.setOrientation(LinearLayout.VERTICAL);
			TextView name = new TextView(SavedFormsActivity.this);
			TextView dateOfCreation = new TextView(SavedFormsActivity.this);
			FormMetaData formMetadata = assessmentForms.get(position);
			name.setTextAppearance(SavedFormsActivity.this,
					android.R.style.TextAppearance_DeviceDefault_Medium);
			name.setText(formMetadata.getName());
			dateOfCreation.setTextAppearance(SavedFormsActivity.this,
					android.R.style.TextAppearance_DeviceDefault_Small);
			dateOfCreation.setText(formMetadata.getDateOfCreation());

			assessmentView.addView(name);
			assessmentView.addView(dateOfCreation);

			assessmentView.setTag(formMetadata);
			assessmentView.setPadding(5, 5, 5, 5);
			return assessmentView;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getCount() {
			return assessmentForms.size();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		setContentView(R.layout.activity_saved_forms);
		usersView = (ListView) findViewById(R.id.listview_users);
		assessmentsView = (ListView) findViewById(R.id.listview_assessments);
		coachingSetsView = (ListView) findViewById(R.id.listview_coachingsets);
		certificationsView = (ListView) findViewById(R.id.listview_certifications);
		usersView
				.setAdapter(UserGroup.getUsersAdapter(getApplicationContext()));
		assessmentsView.setAdapter(assessmentsAdapter);
		certificationsView.setAdapter(certificationsAdapter);
		coachingSetsView.setAdapter(coachingFormsAdapter);
		addListViewsClickedHandler();

	}

	@Override
	protected void onResume() {
		super.onResume();
		usersView
				.performItemClick(null, 0, usersView.getFirstVisiblePosition());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.saved_forms, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void createFormSetStructures(String formStr) {

		assessmentForms.clear();
		certificationForms.clear();
		coachingForms.clear();
		try {
			JSONObject formSets = new JSONObject(formStr)
					.getJSONObject(FormMetaData.FORM_SETS);
			JSONArray assessments = formSets
					.getJSONArray(FormMetaData.ASSESSMENT_FORMS);
			JSONArray certifications = formSets
					.getJSONArray(FormMetaData.CERTIFICATION_FORMS);
			JSONArray coachings = formSets
					.getJSONArray(FormMetaData.COACHING_FORMS);

			for (int i = 0; i < assessments.length(); i++) {
				FormMetaData assessment = new FormMetaData();
				JSONObject mainForm = assessments.getJSONObject(i)
						.getJSONObject(FormMetaData.MAIN_FORM);
				JSONObject savedForm = assessments.getJSONObject(i)
						.getJSONObject(FormMetaData.SAVED_FORM);
				assessment.setDateOfCreation(savedForm.getString(
						FormMetaData.CREATED_AT).substring(0, 10));
				assessment.setFormClass(mainForm
						.getString(FormMetaData.FROM_CLASS));
				assessment.setName(mainForm.getString(FormMetaData.NAME));
				assessment.setObjectId(savedForm
						.getString(FormMetaData.OBJECT_ID));
				assessmentForms.add(assessment);
			}

			for (int i = 0; i < certifications.length(); i++) {
				FormMetaData certification = new FormMetaData();
				JSONObject mainForm = certifications.getJSONObject(i)
						.getJSONObject(FormMetaData.MAIN_FORM);
				JSONObject savedForm = certifications.getJSONObject(i)
						.getJSONObject(FormMetaData.SAVED_FORM);
				certification.setDateOfCreation(savedForm.getString(
						FormMetaData.CREATED_AT).substring(0, 10));
				certification.setFormClass(mainForm
						.getString(FormMetaData.FROM_CLASS));
				certification.setName(mainForm.getString(FormMetaData.NAME));
				certification.setObjectId(savedForm
						.getString(FormMetaData.OBJECT_ID));
				certificationForms.add(certification);
			}

			for (int i = 0; i < coachings.length(); i++) {

				LinkedHashSet<FormMetaData> coachingSet = new LinkedHashSet<FormMetaData>();
				JSONArray coachingSetJSON = coachings.getJSONObject(i)
						.getJSONArray(FormMetaData.COACHING_SET);
				for (int j = 0; j < coachingSetJSON.length(); j++) {
					FormMetaData coachingForm = new FormMetaData();
					JSONObject mainForm = coachingSetJSON.getJSONObject(j)
							.getJSONObject(FormMetaData.MAIN_FORM);
					JSONObject savedForm = coachingSetJSON.getJSONObject(j)
							.getJSONObject(FormMetaData.SAVED_FORM);
					coachingForm.setDateOfCreation(savedForm.getString(
							FormMetaData.CREATED_AT).substring(0, 10));
					coachingForm.setFormClass(mainForm
							.getString(FormMetaData.FROM_CLASS));
					coachingForm.setName(mainForm.getString(FormMetaData.NAME));
					coachingForm.setObjectId(savedForm
							.getString(FormMetaData.OBJECT_ID));
					coachingSet.add(coachingForm);
				}
				coachingForms.add(coachingSet);

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addListViewsClickedHandler() {
		usersView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int arg2, long arg3) {
				ParseUser user;
				if (view == null) {
					user = (ParseUser) adapter.getAdapter().getItem(0);
				} else {
					user = ((UserGroup.ViewHolder)view.getTag()).getParseUser();
				}

				String URL = "http://admin.mobilesfe.com/get_form_set_groups?user="
						+ user.getObjectId()
						+ "&role="
						+ ParseUser.getCurrentUser().getString("type");
				new GetFormSetsTask().execute(URL);

			}
		});

		assessmentsView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				FormMetaData formMetaData = (FormMetaData) view.getTag();
				Intent intent = new Intent(SavedFormsActivity.this,
						SavedFormViewerActivity.class);
				intent.putExtra("SavedForm", formMetaData);
				startActivity(intent);

			}
		});

		certificationsView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				FormMetaData formMetaData = (FormMetaData) view.getTag();
				Intent intent = new Intent(SavedFormsActivity.this,
						SavedFormViewerActivity.class);
				intent.putExtra("SavedForm", formMetaData);
				startActivity(intent);
			}
		});

		coachingSetsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				LinkedHashSet<FormMetaData> formSet = (LinkedHashSet<FormMetaData>) view
						.getTag();
				Intent intent = new Intent(SavedFormsActivity.this,
						SavedFormViewerActivity.class);
				intent.putExtra("SavedFormSet", formSet);
				startActivity(intent);

			}
		});
	}

	private class GetFormSetsTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			return Utility.HTTPGET(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			// Log.d(LOG_TAG, result);
			createFormSetStructures(result);
			assessmentsAdapter.notifyDataSetChanged();
			certificationsAdapter.notifyDataSetChanged();
			coachingFormsAdapter.notifyDataSetChanged();
		}

	}

}
