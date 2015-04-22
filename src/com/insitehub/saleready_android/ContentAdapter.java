package com.insitehub.saleready_android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContentAdapter extends BaseAdapter {

	private final String[] context_text = new String[] { "Pitch Practice",
			"Coaches Corner", "Certification Center", "Knowledge Center",
			"Performance/Reporting", "Group Chat" };

	private final int[] imageIDs = new int[] { R.drawable.btnpitchpractice,
			R.drawable.btncoachescorner, R.drawable.btncertificationcenter,
			R.drawable.btnknowledgecenter, R.drawable.btnperformance,
			R.drawable.btngroupchat };

	private Context mContext;

	public ContentAdapter(Context context) {
		mContext = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return imageIDs.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// ImageView imageView;
		// if (convertView == null) { // if it's not recycled, initialize some
		// attributes
		// imageView = new ImageView(mContext);
		// imageView.setLayoutParams(new
		// GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
		// ViewGroup.LayoutParams.WRAP_CONTENT));
		// imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		// imageView.setPadding(8, 8, 8, 8);
		// } else {
		// imageView = (ImageView) convertView;
		// }
		//
		// imageView.setImageResource(imageIDs[position]);
		// return imageView;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rootView = new View(mContext);

		// get layout from mobile.xml
		rootView = inflater.inflate(R.layout.item_content, parent, false);

		// set value into textview
		TextView textView = (TextView) rootView
				.findViewById(R.id.item_content_textview);
		textView.setText(context_text[position]);

		// set image based on selected text
		ImageView imageView = (ImageView) rootView
				.findViewById(R.id.item_context_image);

		imageView.setImageResource(imageIDs[position]);

		return rootView;

	}

}
