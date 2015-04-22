package com.insitehub.saleready_android;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.insitehub.saleready_android.CoachingCornerActivity.CoachinGCornerFragment;
import com.insitehub.saleready_android.DataModels.SaveFormInJson;
import com.insitehub.saleready_android.DataModels.FormStructures.Form;
import com.insitehub.saleready_android.DataModels.FormStructures.PreloadedForms;

public class IncallFormActivity extends Activity {
	public static final String TARGET_ID = "targetID";
	

	private ScrollView mFormViewContainer;
	private Button mBtnSave;
	private String mPurpose;
	private Form mSelectedForm;
	private String mTargetID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		setContentView(R.layout.activity_incall_form);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mFormViewContainer = (ScrollView) findViewById(R.id.formContainer);
		mBtnSave =(Button)findViewById(R.id.btnSave);
		Intent intent  = getIntent();
		mPurpose = intent.getStringExtra(InCallActivity.PURPOSE);
		mTargetID = intent.getStringExtra(TARGET_ID);
		loadFormView();
		addHandler();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.incall_form, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void loadFormView() {
		if (mPurpose.equals(InCallActivity.PURPOSE_CERTIFICATION)) {
			synchronized (PreloadedForms.certificationForm) {
				try {
					while (!PreloadedForms.isCertificationFormLoaded()) {
						PreloadedForms.certificationForm.wait();
					}
					mSelectedForm = PreloadedForms.certificationForm;
					mFormViewContainer.addView(Utility.buildDynamicFormUI(this, PreloadedForms.certificationForm, true));
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		} else if (mPurpose.equals(InCallActivity.PURPOSE_PITCH_PRACTICE)) {
			synchronized (PreloadedForms.assessmentForm) {
				try {
					while (!PreloadedForms.isAssessmentFormLoaded()) {
						PreloadedForms.assessmentForm.wait();
					}
					mSelectedForm = PreloadedForms.assessmentForm;
					mFormViewContainer.addView(Utility.buildDynamicFormUI(this, PreloadedForms.assessmentForm, true));
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	private void addHandler(){
		mBtnSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AsyncTask<Void, Void, Void>(){

					@Override
					protected Void doInBackground(Void... params) {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						mSelectedForm.setUserID(mTargetID);
						SaveFormInJson.saveFormInJson(out, mSelectedForm);
						final String jsonStr = "formdata=" + new String(out.toByteArray());
						Utility.HTTP_POST(CoachinGCornerFragment.URL_SAVE_FORM, jsonStr);
						return null;
					}
					protected void onPostExecute(Void result) {
						Toast.makeText(IncallFormActivity.this, "Form Saved", Toast.LENGTH_SHORT).show();
						IncallFormActivity.this.finish();
					};
				}.execute();
				
			}
		});
	}
	
}
