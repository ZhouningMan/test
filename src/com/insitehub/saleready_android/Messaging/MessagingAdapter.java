package com.insitehub.saleready_android.Messaging;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.insitehub.saleready_android.R;
import com.insitehub.saleready_android.UserGroup;
import com.parse.ParseUser;

public class MessagingAdapter extends BaseAdapter {

		private Context mContext;
		private List<JSONObject> messageHistory; 

		public MessagingAdapter(Context context, List<JSONObject> messageHistory) {
			mContext = context;
			this.messageHistory = messageHistory;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return messageHistory.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			try {
				JSONObject message = messageHistory.get(position);
				int viewType;
				if (message.getString("author").equals(
						ParseUser.getCurrentUser().getUsername())) {
					viewType = R.layout.item_chat_sent_by_me;
				} else {
					viewType = R.layout.item_chat_received_by_me;
				}
				LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				// TODO use Viewholder pattern here
				View chatView = inflater.inflate(viewType, parent, false);

				ImageView userAvatar = (ImageView) chatView
						.findViewById(R.id.userAvatar);
				TextView chatMessage = (TextView) chatView
						.findViewById(R.id.chatMessage);
				TextView timeStamp = (TextView) chatView
						.findViewById(R.id.timeStamp);
				chatMessage.setText(message.getString("text"));
				chatMessage.setTextColor(mContext.getResources().getColor(
						R.color.orange));
				timeStamp.setText(message.getString("timestamp"));
				timeStamp.setTextColor(mContext.getResources().getColor(
						R.color.orange));
				Bitmap bitmap = UserGroup.getAvailableUserAvatars().get(
						message.getString("author"));

				if (bitmap != null) {
					userAvatar.setImageBitmap(bitmap);
				} else {
					userAvatar.setImageResource(R.drawable.woman1);
				}
				return chatView;
			} catch (JSONException e) {
			}
			return null;

		}
		
		
	}