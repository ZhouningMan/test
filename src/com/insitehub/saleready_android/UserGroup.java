package com.insitehub.saleready_android;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.insitehub.saleready_android.Messaging.MessagingHelper;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class UserGroup {
	private static BaseAdapter usersAdapter = null;
	private static List<ParseUser> users = null;
	private static List<ViewHolder> viewHolders = new LinkedList<UserGroup.ViewHolder>();
	// Need synchronization?
	private static HashMap<String, Bitmap> availableUserAvatars = new LinkedHashMap<String, Bitmap>();

	private static boolean isInMainActivityChatMode = false;

	public synchronized static void loadUsers(final Context context) {
		ParseUser user = ParseUser.getCurrentUser();
		ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
		query.whereEqualTo("tenant", user.getParseObject("tenant"));
		query.whereNotEqualTo("objectId", user.getObjectId());
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> objects, ParseException e) {
				if (e == null) {
					users = objects;
					byte[] bmp = null;
					try {
						bmp = ParseUser.getCurrentUser().getParseFile("image")
								.getData();
						Bitmap bitmap = BitmapFactory.decodeByteArray(bmp, 0,
								bmp.length);
						if (bitmap != null) {
							availableUserAvatars.put(ParseUser.getCurrentUser()
									.getUsername(), Utility.getRoundedCornerBitmap(bitmap, 7));
						}
					} catch (ParseException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					MessagingHelper.setUsers(objects);
					UserGroup.getUsersAdapter(context);
				}

			}
		});
	}

	public synchronized static BaseAdapter getUsersAdapter(final Context context) {
		if (usersAdapter == null) {
			usersAdapter = new BaseAdapter() {

				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {

					ViewHolder viewHolder;
					View userView = convertView;
					if (userView == null) {
						LayoutInflater inflater = (LayoutInflater) context
								.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

						userView = inflater.inflate(R.layout.item_user, parent,
								false);
						viewHolder = new ViewHolder(userView);
					} else {
						viewHolder = (ViewHolder) userView.getTag();
					}

					viewHolder.userName.setTextColor(context.getResources()
							.getColor(R.color.black));
					ParseUser user = users.get(position);
					String userName = user.getUsername();
					viewHolder.userName.setText(userName);
					ParseFile userImage = user.getParseFile("image");

					if (isInMainActivityChatMode) {
						viewHolder.newMessageCount.setVisibility(View.VISIBLE);
						viewHolder.userType.setVisibility(View.INVISIBLE);
						int count = MessagingHelper
								.getNewMessageCountForUser(userName);
						String message;
						
						if(count>0){
							message=count==1?(count + " new message"): (count + " new messages");
							viewHolder.newMessageCount.setText(message);
							viewHolder.newMessageCount.setTextColor(Color.BLUE);
						}else{
							message= (count + " new message");
							viewHolder.newMessageCount.setText(message);
							viewHolder.newMessageCount.setTextColor(Color.BLACK);
						}
					} else {
						viewHolder.newMessageCount
								.setVisibility(View.INVISIBLE);
						viewHolder.userType.setVisibility(View.VISIBLE);
						viewHolder.userType.setText(user.getString("type"));
					}

					if (userImage == null) {
						Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
					            R.drawable.woman1);
						viewHolder.userAvatar
								.setImageBitmap(Utility.getRoundedCornerBitmap(bitmap, 7));
					} else {
						Bitmap bitmap = availableUserAvatars.get(userName);
						if (bitmap == null) {
							byte[] bmp = null;
							try {
								bmp = userImage.getData();
								bitmap = BitmapFactory.decodeByteArray(bmp, 0,
										bmp.length);
								availableUserAvatars.put(userName, Utility.getRoundedCornerBitmap(bitmap, 7));
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						viewHolder.userAvatar.setImageBitmap(bitmap);
					}
					viewHolder.setParseUser(user);
					userView.setTag(viewHolder);
					if (!viewHolders.contains(viewHolder)) {
						viewHolders.add(viewHolder);
					}
					return userView;
				}

				@Override
				public long getItemId(int arg0) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public Object getItem(int position) {
					if (users == null) {
						return null;
					} else {
						return users.get(position);
					}

				}

				@Override
				public int getCount() {
					// TODO Auto-generated method stub
					if (users == null) {
						return 0;
					} else {
						return users.size();
					}

				}
			};
		}
		return usersAdapter;
	}

	public static HashMap<String, Bitmap> getAvailableUserAvatars() {
		return availableUserAvatars;
	}

	public static class ViewHolder {

		private ImageView userAvatar;
		private TextView userName;
		private TextView userType;
		private TextView newMessageCount;

		private ParseUser parseUser;

		public ViewHolder(View userView) {
			userAvatar = (ImageView) userView.findViewById(R.id.userAvatar);
			userName = (TextView) userView.findViewById(R.id.userName);
			userType = (TextView) userView.findViewById(R.id.userType);
			newMessageCount = (TextView) userView
					.findViewById(R.id.newMessageCount);
		}

		public ParseUser getParseUser() {
			return parseUser;
		}

		public void setParseUser(ParseUser parseUser) {
			this.parseUser = parseUser;
		}

		public void showNewMessageCountView(String numOfNewMessages) {
			userType.setVisibility(View.INVISIBLE);
			newMessageCount.setVisibility(View.VISIBLE);
			newMessageCount.setText(numOfNewMessages);
		}

		public void hideNewMessageCountView() {
			newMessageCount.setVisibility(View.INVISIBLE);
			userType.setVisibility(View.VISIBLE);
		}

	}

	public static List<ViewHolder> getViewHolders() {
		return viewHolders;
	}

	public static void toggleIsInMainActivityChatMode() {
		isInMainActivityChatMode = !isInMainActivityChatMode;
		if(usersAdapter!=null){
			usersAdapter.notifyDataSetChanged();
		}
	}
	
	public static void updateUserView(){
		if(usersAdapter!=null){
			usersAdapter.notifyDataSetChanged();
		}
	}

}
