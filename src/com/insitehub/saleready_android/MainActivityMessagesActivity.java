package com.insitehub.saleready_android;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.insitehub.saleready_android.R;
import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.Message;
import com.insitehub.saleready_android.Messaging.MessagingHelper;
import com.insitehub.saleready_android.Messaging.MessagingHelper.ChatmessageAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MainActivityMessagesActivity extends Activity {
	public static final String TAG = MainActivityMessagesActivity.class
			.getSimpleName();

	private static final int PERIOD = 4000;
	private ListView usersView;
	private ListView chatMessages;
	private EditText chatMessage;
	private Button btnSendMessage;
	private ParseUser talkedTo;
	private Handler mHanlder = new Handler();
	private Runnable checkforMessageUpdateTask;
	private int individualMessageListCount=0;
	private Date oldDate = new Date(0);


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.setOrientationPerDevice(this);
		setContentView(R.layout.activity_main_messaging);
		usersView = (ListView) findViewById(R.id.listview_contacts);
		chatMessages = (ListView) findViewById(R.id.listview_chat_messages);
		chatMessage = (EditText) findViewById(R.id.chatMessage);
		btnSendMessage = (Button) findViewById(R.id.sendMessage);
		usersView.setAdapter(UserGroup.getUsersAdapter(this));
		addHandlers();
		periodicallyCheckForMessageUpdate();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		UserGroup.toggleIsInMainActivityChatMode();
		checkforMessageUpdateTask.run();
		usersView.performItemClick(null, 0, usersView.getFirstVisiblePosition());
	
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		mHanlder.removeCallbacksAndMessages(null);
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
		mHanlder.removeCallbacksAndMessages(null);
		UserGroup.toggleIsInMainActivityChatMode();
		MessagingHelper.clear();
	}

	private void addHandlers() {
		usersView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int arg2,
					long arg3) {
				if (view == null) {
					talkedTo = (ParseUser) adapterView.getAdapter().getItem(0);
				} else {
					talkedTo = ((UserGroup.ViewHolder)view.getTag()).getParseUser();
				}
				String key = talkedTo.getUsername();
				BaseAdapter adapter = MessagingHelper.getAdapterForuser(key,
						MainActivityMessagesActivity.this);
				chatMessages.setAdapter(adapter);
				individualMessageListCount = adapter.getCount();
				int position = adapter.getCount() - 1;
				if (position > 0) {
					chatMessages.setSelection(position);
				}
				MessagingHelper.updateMessageStatusForUserAsSeen(key);
				UserGroup.updateUserView();
			}
		});

		btnSendMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String text = chatMessage.getText().toString().trim();
				if (!text.equals("") && talkedTo != null) {
					Message message = new Message();
					message.put("Sender", ParseUser.getCurrentUser());
					message.put("Recipient", talkedTo);
					message.put("Message", text);
					message.put("hasBeenSeen", false);
					ChatmessageAdapter adapter = MessagingHelper
							.getAdapterForuser(talkedTo.getUsername(),
									MainActivityMessagesActivity.this);
					adapter.addMessage(talkedTo, message);
					chatMessage.setText("");
					updateChatListView();
					try {
						message.save();
					} catch (ParseException e) {
						e.printStackTrace();
					}

				}

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.messages, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.messaging) {
			Intent messageIntent = new Intent(this,
					MainActivityMessagesActivity.class);
			startActivity(messageIntent);
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateChatListView(){
		if(talkedTo!=null){
			ChatmessageAdapter adapter = MessagingHelper
					.getAdapterForuser(talkedTo.getUsername(),
							MainActivityMessagesActivity.this);
			int count = adapter.getCount();
			if(count-individualMessageListCount>0){
				individualMessageListCount = count;
				chatMessages.setSelection(count-1);
			}
		}	
	}
	
	private void periodicallyCheckForMessageUpdate() {
		List<ParseQuery<Message>> queries = new LinkedList<ParseQuery<Message>>();
		ParseQuery<Message> recipientMatchQuery = ParseQuery
				.getQuery(Message.class);
		recipientMatchQuery.whereEqualTo("Recipient",
				ParseUser.getCurrentUser());
		queries.add(recipientMatchQuery);
		ParseQuery<Message> senderMatchQuery = ParseQuery
				.getQuery(Message.class);
		senderMatchQuery.whereEqualTo("Sender", ParseUser.getCurrentUser());
		queries.add(senderMatchQuery);
		final ParseQuery<Message> query = ParseQuery.or(queries);
		query.orderByDescending("createdAt");
		query.include("Sender");
		query.include("Recipient");
		checkforMessageUpdateTask = new Runnable() {
			@Override
			public void run() {
				query.findInBackground(new FindCallback<Message>() {
					@Override
					public void done(List<Message> objects, ParseException e) {
						
						Log.d(TAG+", size", String.valueOf(objects.size()));
						Date newDate = objects.get(0).getCreatedAt();
						if (isNewerThanOldDate(oldDate, newDate) && e == null) {
							oldDate = newDate;
							Collections.reverse(objects);
							MessagingHelper.setMessages(objects);
							MessagingHelper.sortMessages();
							updateChatListView();
							UserGroup.updateUserView();
						} 
						mHanlder.postDelayed(checkforMessageUpdateTask,
								PERIOD);
					}
				});

			}
		};
	}
	
	private boolean isNewerThanOldDate(Date oldDate, Date newDate){
		Calendar cal1 = Calendar.getInstance();
    	Calendar cal2 = Calendar.getInstance();
    	cal1.setTime(oldDate);
    	cal2.setTime(newDate);

    	if(cal2.after(cal1)){
    		return true;
    	}else{
    		return false;
    	}
	}
	

}
