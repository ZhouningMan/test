package com.insitehub.saleready_android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.insitehub.saleready_android.Utility.Tinter;
import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.Asset;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class KnowledgeCenterActivity extends Activity {

	public final static String LOG_TAG = AssetsActivity.class.getSimpleName();
	public final static String LIBRARY = "library";

	private List<Asset> assignedAssets = new ArrayList<Asset>();
	private GridView assignedAssetView;
	// adapter for the assignedAssets
	private BaseAdapter assignedAssetsAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View assignedAssetView;
			//TODO use viewholder pattern
			assignedAssetView = inflater.inflate(R.layout.item_assigned_asset,
					parent, false);
			ImageView icon = (ImageView) assignedAssetView
					.findViewById(R.id.assignedAssetTypeIcon);
			TextView title = (TextView) assignedAssetView
					.findViewById(R.id.assignedAssetTitle);
			TextView detail = (TextView) assignedAssetView
					.findViewById(R.id.assignedAssetDetail);
			ParseObject assignedAsset = assignedAssets.get(position);
			String type = assignedAsset.getString("type");
			if (type.equals("pdf")) {
				icon.setImageResource(R.drawable.icon_pdf);
			} else if (type.equals("video")) {
				icon.setImageResource(R.drawable.icon_film);
			} else if (type.equals("link")) {
				icon.setImageResource(R.drawable.icon_link);
			} else {
				icon.setImageResource(R.drawable.icon_pdf);
			}

			title.setText(assignedAsset.getString("name"));
			detail.setText(assignedAsset.getString("description"));
			assignedAssetView.setTag(assignedAsset);
			return assignedAssetView;
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
			return assignedAssets.size();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		setContentView(R.layout.activity_knowledge_center);
		assignedAssetView = (GridView) findViewById(R.id.gridview_assigned_asset);
		assignedAssetView.setAdapter(assignedAssetsAdapter);
		assignedAssetView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Utility.openRemoteAsset(KnowledgeCenterActivity.this,
						(Asset) view.getTag(), null, null, null);
			}
		});

//		approvedMaterials = (ImageView) findViewById(R.id.approved_materials_button);
//		coachingForms = (ImageView) findViewById(R.id.coaching_forms_button);
//		library = (ImageView) findViewById(R.id.library_button);
		loadLearningModules();
		//addHandlers();
		setOnTouchListeners();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.knowledge_center, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void loadLearningModules() {
		ParseUser user = ParseUser.getCurrentUser();
		ParseObject userData = user.getParseObject("userData");
		try {
			userData.fetchIfNeeded();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		ParseRelation<Asset> relation = userData.getRelation("assignedAssets");
		relation.getQuery().findInBackground(new FindCallback<Asset>() {

			@Override
			public void done(List<Asset> objects, ParseException e) {
				if (e == null) {
					assignedAssets = objects;
					assignedAssetsAdapter.notifyDataSetChanged();
				} else {
					Toast.makeText(KnowledgeCenterActivity.this,
							"Error loading asssinged assets",
							Toast.LENGTH_SHORT).show();
				}

			}
		});

	}

	public void navigate(View v) {
		Intent intent;
		switch (v.getId()) {

		case R.id.approved_materials_button:
			break;

		case R.id.coaching_forms_button:
			intent = new Intent(KnowledgeCenterActivity.this,
					SavedFormsActivity.class);
			startActivity(intent);
			break;

		case R.id.library_button:
			intent = new Intent(KnowledgeCenterActivity.this,
					AssetsActivity.class);
			intent.putExtra(LIBRARY, LIBRARY);
			startActivity(intent);
			break;

		default:
			break;

		}

	}
	
	public void setOnTouchListeners() {
		Tinter tinter = new Tinter();
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.knowledge_center_main_wrapper);
		for (int i = 0; i < relativeLayout.getChildCount(); i++) {
			if (relativeLayout.getChildAt(i) instanceof ImageView) {
				relativeLayout.getChildAt(i).setOnTouchListener(tinter);
			}
		}
	}


//	 Afraid to delete in case the new implementation is unstable, but it should be fine. It's here just in case.   
//
//	 private void addHandlers(){
//	 approvedMaterials.setOnClickListener(new OnClickListener() {
//	
//	 @Override
//	 public void onClick(View v) {
//	 // TODO Auto-generated method stub
//	
//	 }
//	 });
//	
//	 coachingForms.setOnClickListener(new OnClickListener() {
//	
//	 @Override
//	 public void onClick(View v) {
//	 Intent intent = new Intent(KnowledgeCenterActivity.this,
//	 SavedFormsActivity.class);
//	 startActivity(intent);
//	
//	 }
//	 });
//	
//	
//	 library.setOnClickListener(new OnClickListener() {
//	
//	 @Override
//	 public void onClick(View v) {
//	 Intent intent = new Intent(KnowledgeCenterActivity.this,
//	 AssetsActivity.class);
//	 intent.putExtra(LIBRARY, LIBRARY);
//	 startActivity(intent);
//	
//	 }
//	 });
//	 }

}
