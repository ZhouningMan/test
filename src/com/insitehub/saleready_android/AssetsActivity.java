package com.insitehub.saleready_android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.Asset;
import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.PeerShare;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class AssetsActivity extends Activity {

	private final static String LOG_TAG = AssetsActivity.class.getSimpleName();

	private ListView assetListView;
	private List<Asset> assets = new ArrayList<Asset>();

	private String shareTo;
	private boolean isForLibrary = false;
	
	
	//These variables are useful only when the assetactivity is called in
	//PartyChatActivity
	private String chatSession;
	private String chatSessionToken;
	private String role;
	private boolean isForPartyChat = false;

	// adapter for the assets 
	private BaseAdapter assetsAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View assetView;

			//TODO use viewholder pattern
			assetView = inflater
					.inflate(R.layout.item_asset, parent, false);
			TextView txtTitle = (TextView) assetView
					.findViewById(R.id.txtTitle);
			TextView txtType = (TextView) assetView
					.findViewById(R.id.txtType);
			TextView txtDate = (TextView) assetView
					.findViewById(R.id.txtDate);
			Button btnAction = (Button) assetView
					.findViewById(R.id.btnAction);
			Intent intent = getIntent();
			if(intent.getStringExtra(KnowledgeCenterActivity.LIBRARY)!=null){
				isForLibrary = true;
			}
			

			Asset asset = assets.get(position);
			btnAction.setTag(asset);
			if(isForLibrary){
				btnAction.setText("View");
			}
			txtTitle.setText(asset.getString("name"));
			txtType.setText(asset.getString("type"));
			txtDate.setText(SimpleDateFormat.getDateInstance().format(
					asset.getCreatedAt()));

			return assetView;
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
			return assets.size();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		setContentView(R.layout.activity_assets);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		Intent intent = getIntent();
		shareTo = intent.getStringExtra(ViewReps.SHARE_TO);
		assetListView = (ListView) findViewById(R.id.listView_assets);
		assetListView.setAdapter(assetsAdapter);

		chatSession = intent.getStringExtra(PartyChatActivity.SESSIONID);
		chatSessionToken = intent.getStringExtra(PartyChatActivity.TOKEN);
		role = intent.getStringExtra(PartyChatActivity.ROLE);
		if(chatSession!=null){
			isForPartyChat = true;
		}
		
		
		loadLibraryAssets();
	}

	

	private void createPeerShareRecord(final Asset asset) {
		ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
		query.whereEqualTo("username", shareTo);
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> objects, ParseException e) {
				if (e == null && objects.size() > 0) {
					PeerShare peerShare = new PeerShare();
					peerShare.put("shareOwner", ParseUser.getCurrentUser());
					peerShare.put("shareTo", objects.get(0));
					peerShare.put("shareAsset", asset);
					peerShare.saveInBackground();
					
				} else {
					Log.d(LOG_TAG,
							"failed to find the target user to share the document");
				}
			}
		});
	}

	public void openAsset(View view) {
		Asset asset = (Asset) view.getTag();
		if(!isForLibrary && !isForPartyChat){
			createPeerShareRecord(asset);
		}
		Utility.openRemoteAsset(this,asset, role,  chatSession,  chatSessionToken);
	}

	
	private void loadLibraryAssets() {
		ParseQuery<Asset> query = ParseQuery.getQuery(Asset.class);
		query.orderByDescending("createdAt");
		query.findInBackground(new FindCallback<Asset>() {

			@Override
			public void done(List<Asset> objects, ParseException e) {
				if (e == null) {
					assets = objects;
					assetsAdapter.notifyDataSetChanged();
				}

			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.assets, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}
