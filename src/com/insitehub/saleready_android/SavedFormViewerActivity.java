package com.insitehub.saleready_android;

import java.util.LinkedHashSet;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.insitehub.saleready_android.DataModels.FormStructures.ActionItem;
import com.insitehub.saleready_android.DataModels.FormStructures.CompetencyListItem;
import com.insitehub.saleready_android.DataModels.FormStructures.Form;
import com.insitehub.saleready_android.DataModels.FormStructures.FormMetaData;
import com.insitehub.saleready_android.DataModels.FormStructures.Option;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class SavedFormViewerActivity extends Activity {

	private FrameLayout savedFormContainer;
	private LinearLayout formSetController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		setContentView(R.layout.activity_saved_form_viewer);
		savedFormContainer = (FrameLayout) findViewById(R.id.savedFormContainer);
		formSetController = (LinearLayout) findViewById(R.id.formSetController);
		Intent intent = getIntent();
		FormMetaData formMetaData = (FormMetaData) intent
				.getSerializableExtra("SavedForm");
		LinkedHashSet<FormMetaData> formset = (LinkedHashSet<FormMetaData>) intent
				.getSerializableExtra("SavedFormSet");

		if (formset != null) {
			for (FormMetaData metaData : formset) {
				Button btnform = new Button(this);
				btnform.setText(metaData.getFormClass());
				formSetController.addView(btnform);
				btnform.setTag(metaData);
				btnform.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						savedFormContainer.removeAllViews();
						savedFormContainer
								.addView(formSelectionButtonClicked(v));
					}
				});
			}
			((Button) formSetController.getChildAt(0)).performClick();
		} else if (formMetaData != null) {
			Form form = loadSavedForm(formMetaData);
			savedFormContainer.removeAllViews();
			savedFormContainer.addView(Utility.buildDynamicFormUI(this, form, form.isWritable()));
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.saved_form_viewer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		return super.onOptionsItemSelected(item);
	}

	private View formSelectionButtonClicked(View v) {
		FormMetaData formMetaData = (FormMetaData) v.getTag();
		Form form = loadSavedForm(formMetaData);
		return Utility.buildDynamicFormUI(this, form, form.isWritable());

	}

	private Form loadSavedForm(FormMetaData formMetaData) {
		ParseObject formParse = ParseObject.createWithoutData("SavedForm",
				formMetaData.getObjectId());
		ParseQuery<ParseObject> compQuery = formParse.getRelation(
				"savedCompetencies").getQuery();
		Form form = new Form();
		compQuery.orderByAscending("sortOrder");
		form.setFormClass(formMetaData.getFormClass());
		try {
			List<ParseObject> competencies = compQuery.find();
			for (ParseObject competency : competencies) {
				// Log.d(LOG_TAG, competency.toString());
				CompetencyListItem competencyListItem = new CompetencyListItem();
				competencyListItem.setName(competency.getString("name"));

				ParseQuery<ParseObject> actionQuery = competency.getRelation(
						"savedActions").getQuery();
				actionQuery.orderByAscending("sortOrder");
				List<ParseObject> actions = actionQuery.find();
				for (ParseObject action : actions) {
					ActionItem actionItem = new ActionItem();
					actionItem.setName(action.getString("name"));
					actionItem.setType(action.getString("type"));
					if (action.getString("type").equals("select")) {
						ParseQuery<ParseObject> optionQuery = action
								.getRelation("savedActionOptions").getQuery();
						optionQuery.orderByAscending("sortOrder");
						List<ParseObject> options = optionQuery.find();
						for (ParseObject option : options) {
							Option optionItem = new Option();
							optionItem.setName(option.getString("name"));
							optionItem.setSelected(option
									.getBoolean("selected"));
							actionItem.addOption(optionItem);
						}
					}
					else{
						actionItem.setValue(action.getString("value"));
					}
					competencyListItem.addAction(actionItem);
				}
				form.addCompetencyListItem(competencyListItem);
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		form.setWritable(false);
		return form;

	}
	
	

}
