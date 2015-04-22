package com.insitehub.saleready_android;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.insitehub.saleready_android.DataModels.AssessmentRecord;
import com.parse.ParseUser;

public class PerformanceReportingActivity extends Activity {

	private ListView latestAssementView;
	private ListView groupAverageView;
	private ListView usersView;

	private List<AssessmentRecord> latestAssements = new LinkedList<AssessmentRecord>();
	private List<AssessmentRecord> groupAverageAssements = new LinkedList<AssessmentRecord>();

	private BaseAdapter latestAssementsAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View assessmentView;
			//TODO use viewholder pattern
			assessmentView = inflater.inflate(R.layout.item_assessment, parent,
					false);

			TextView name = (TextView) assessmentView.findViewById(R.id.name);
			TextView score = (TextView) assessmentView.findViewById(R.id.score);
			name.setText(latestAssements.get(position).getName());
			score.setText(latestAssements.get(position).getAverage());
		
			return assessmentView;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public int getCount() {
			return latestAssements.size();
		}
	};

	private BaseAdapter groupAverageAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View assessmentView;
			//use viewholder pattern
			assessmentView = inflater.inflate(R.layout.item_assessment, parent,
					false);

			TextView name = (TextView) assessmentView.findViewById(R.id.name);
			TextView score = (TextView) assessmentView.findViewById(R.id.score);
			name.setText(groupAverageAssements.get(position).getName());
			score.setText(groupAverageAssements.get(position).getAverage());
			return assessmentView;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			return groupAverageAssements.size();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		setContentView(R.layout.activity_performance_reporting);
		usersView = (ListView) findViewById(R.id.listview_users);
		latestAssementView = (ListView) findViewById(R.id.listview_lastest_assessment);
		groupAverageView = (ListView) findViewById(R.id.listview_group_average);

		usersView
				.setAdapter(UserGroup.getUsersAdapter(getApplicationContext()));
		latestAssementView.setAdapter(latestAssementsAdapter);
		groupAverageView.setAdapter(groupAverageAdapter);

//		getLatestAssessmentForUser(ParseUser.getCurrentUser());
//		getGroupAverageAssessmentForUser(ParseUser.getCurrentUser());
		
		
		usersView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int arg2,
					long arg3) {
				ParseUser user;
				if (view == null) {
					user = (ParseUser) adapterView.getAdapter().getItem(0);
				} else {
					user = ((UserGroup.ViewHolder)view.getTag()).getParseUser();

				}
				getLatestAssessmentForUser(user);
				getGroupAverageAssessmentForUser(user);
				
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		usersView.performItemClick(null, 0, usersView.getFirstVisiblePosition());
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.performance_reporting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void getLatestAssessmentForUser(ParseUser user) {
		String URL = "http://admin.mobilesfe.com/get_latest_assessment?id="
				+ user.getParseObject("tenant").getObjectId() + "&user="
				+ user.getObjectId();
		new AsyncTask<String, Void, String>() {

			@Override
			protected String doInBackground(String... params) {
				return Utility.HTTPGET(params[0]);
			}

			protected void onPostExecute(String result) {
				createAssementRecords(result, latestAssements);
				latestAssementsAdapter.notifyDataSetChanged();

			};

		}.execute(URL);
	}

	private void getGroupAverageAssessmentForUser(ParseUser user) {
		String URL = "http://admin.mobilesfe.com/get_group_assessment?id="
				+ user.getParseObject("tenant").getObjectId();
		new AsyncTask<String, Void, String>() {

			@Override
			protected String doInBackground(String... params) {
				return Utility.HTTPGET(params[0]);
			}

			protected void onPostExecute(String result) {
				createAssementRecords(result, groupAverageAssements);
				groupAverageAdapter.notifyDataSetChanged();

			};

		}.execute(URL);
	}

	private void createAssementRecords(String assessmentStr,
			List<AssessmentRecord> assessments) {
		assessments.clear();
		try {
			JSONArray assessmentsJSON = new JSONObject(assessmentStr)
					.getJSONArray(AssessmentRecord.COMPETENCIES);
			for (int i = 0; i < assessmentsJSON.length(); i++) {
				AssessmentRecord assessment = new AssessmentRecord();
				JSONObject assessmentJSON = assessmentsJSON.getJSONObject(i);
				assessment.setName(assessmentJSON
						.getString(AssessmentRecord.NAME));
				JSONObject actionJSON = assessmentJSON
						.getJSONObject(AssessmentRecord.ACTIONS);
				assessment.setAverage(actionJSON
						.getString(AssessmentRecord.AVERAGE));
				assessment.setCount(actionJSON
						.getString(AssessmentRecord.COUNT));
				assessment.setSum(actionJSON.getString(AssessmentRecord.SUM));
				assessments.add(assessment);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	


}
