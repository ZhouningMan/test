package com.insitehub.saleready_android.Messaging;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.insitehub.saleready_android.R;
import com.insitehub.saleready_android.UserGroup;
import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.Message;
import com.parse.ParseUser;

public class MessagingHelper {
	private static List<ParseUser> users = null;
	private static List<Message> messages = null;
	private static HashMap<String, List<Message>> messagesWithEachUserList = new HashMap<String, List<Message>>();
	private static HashMap<String, Integer> newMessageCounts = new HashMap<String, Integer>();
	private static ChatmessageAdapter adapterForEachUser = null;
	private static List<Message> messagesWithEachUser = null;
	private static Context context;
	
	
	public static List<ParseUser> getUsers() {
		return users;
	}

	public static void clear(){
		if(messages!=null){
			messages.clear();
		}
		messagesWithEachUserList.clear();
	}
	
	public static void setUsers(List<ParseUser> users) {
		MessagingHelper.users = users;
	}

	public static void setMessages(List<Message> messages) {
		MessagingHelper.messages = messages;
	}

	private static void setNewMessageCountForUser(String userName, Integer count){
		newMessageCounts.put(userName, count);
	}
	
	public static int getNewMessageCountForUser(String userName){
		Integer count  = newMessageCounts.get(userName);
		if(count==null){
			return 0;
		}else{
			return count;
		}
	}
	
	private static  void printNewMessageCount(){
		Set<String> keys = newMessageCounts.keySet();
		for (String key : keys) {
			Log.d(key, String.valueOf(newMessageCounts.get(key)));
		}
	}
	
	
	private static void incrementNewMessageCountForUser(String userName){
		setNewMessageCountForUser(userName, getNewMessageCountForUser(userName)+1);
	}
	
	
	public static void updateMessageStatusForUserAsSeen(String userName){
		if(messagesWithEachUser!=null)
		for (Message message : messagesWithEachUser) {
			
			if(!message.getBoolean("hasBeenSeen")){
				message.put("hasBeenSeen", true);
				message.saveInBackground();
			}
		}
		setNewMessageCountForUser(userName, 0);
	}
	
	
	public static HashMap<String, List<Message>> sortMessages() {
		messagesWithEachUserList.clear();
		newMessageCounts.clear();
		boolean sentToMe=false;
		for (Message message : messages) {
			String key;
			if (message.getParseUser("Sender").getUsername()
					.equals(ParseUser.getCurrentUser().getUsername())) {
				key = message.getParseUser("Recipient").getUsername();
				sentToMe = false;
			} else {
				sentToMe = true;
				key = message.getParseUser("Sender").getUsername();
			}
			
			
	//		Log.d("Message", key +", " + message.getString("Message") + ", sentToMe = "+ String.valueOf(sentToMe) + ", hasbeenSeen = " + String.valueOf(message.getBoolean("hasBeenSeen")) );
			if(sentToMe&& !message.getBoolean("hasBeenSeen")){
				incrementNewMessageCountForUser(key);
			}
			List<Message> messagesWithEachUser = messagesWithEachUserList
					.get(key);
			if (messagesWithEachUser == null) {
				messagesWithEachUser = new LinkedList<Message>();
				messagesWithEachUserList.put(key, messagesWithEachUser);
			}
			messagesWithEachUser.add(message);
		}
		if(adapterForEachUser!=null){
			adapterForEachUser.notifyDataSetChanged();
		}
		printNewMessageCount();
		return messagesWithEachUserList;
	}

	public static ChatmessageAdapter getAdapterForuser(final String userName,
			final Context context) {
		 messagesWithEachUser = messagesWithEachUserList
				.get(userName);
		 MessagingHelper.context = context;
		if (adapterForEachUser == null) {
			adapterForEachUser = new ChatmessageAdapter(userName);
		}else{
			adapterForEachUser.notifyDataSetChanged();
		}
		return adapterForEachUser;
	}
	
	
	public static class ChatmessageAdapter extends BaseAdapter{

		private DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(MessagingHelper.context);
		private DateFormat timeFormat = DateFormat.getTimeInstance();
				
		public ChatmessageAdapter(String userName){
			TimeZone tz = Calendar.getInstance().getTimeZone();
			dateFormat.setTimeZone(tz);
			timeFormat.setTimeZone(tz);
		}
		@Override
		public View getView(int position, View convertView,
				ViewGroup parent) {
			Message message = messagesWithEachUser.get(position);
			int viewType;
			if (message.getParseUser("Sender").getUsername()
					.equals(ParseUser.getCurrentUser().getUsername())) {
				viewType = R.layout.item_chat_sent_by_me;

			} else {
				viewType = R.layout.item_chat_received_by_me;

			}
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//TODO use viewholder pattern
			View chatView = inflater.inflate(viewType, parent, false);

			ImageView userAvatar = (ImageView) chatView
					.findViewById(R.id.userAvatar);
			TextView chatMessage = (TextView) chatView
					.findViewById(R.id.chatMessage);
			TextView timeStamp = (TextView) chatView
					.findViewById(R.id.timeStamp);
			chatMessage.setText(message.getString("Message"));
			Date date = message.getCreatedAt();
			timeStamp.setText(dateFormat.format(date)+" "+timeFormat.format(date));
			Bitmap bitmap = UserGroup.getAvailableUserAvatars().get(
					message.getParseUser("Sender").getUsername());

			if (bitmap != null) {
				userAvatar.setImageBitmap(bitmap);
			} else {
				userAvatar.setImageResource(R.drawable.woman1);
			}
			
			
			
			return chatView;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return messagesWithEachUser.get(position);
		}

		@Override
		public int getCount() {
			if (messagesWithEachUser != null) {
				return messagesWithEachUser.size();
			} else {
				return 0;
			}

		}
		
		public void addMessage(ParseUser user, Message message){
			if(messagesWithEachUser==null){
				messagesWithEachUser = new LinkedList<Message>();
				messagesWithEachUserList.put(user.getUsername(), messagesWithEachUser);
			}
			messagesWithEachUser.add(message);
			adapterForEachUser.notifyDataSetChanged();
		}
		
	}
	
}
