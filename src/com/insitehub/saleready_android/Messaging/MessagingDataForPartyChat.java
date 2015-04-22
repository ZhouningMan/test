package com.insitehub.saleready_android.Messaging;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.BaseAdapter;

public class MessagingDataForPartyChat {
	private static List<JSONObject> messageHistory = new LinkedList<JSONObject>();
	private static List<NewMessagingListner> listeners = new LinkedList<NewMessagingListner>();

	private static BaseAdapter partyMessagingAdapter = null;

	public static List<JSONObject> getMessageHistory() {
		return messageHistory;
	}

	public static void setMessageHistory(List<JSONObject> messageHistory) {
		MessagingDataForPartyChat.messageHistory = messageHistory;
	}

	public static BaseAdapter getPartyMessagingAdapter(Context context) {
		if (partyMessagingAdapter == null) {
			partyMessagingAdapter = new MessagingAdapter(context,messageHistory);
		}
		return partyMessagingAdapter;
	}

	
	public static void addChatMessage(JSONObject message){
		messageHistory.add(message);
		
		Handler handler = new Handler(Looper.getMainLooper()); 
		handler.post(new Runnable() {

			@Override
			public void run() {
				for (NewMessagingListner newMessagingListner : listeners) {
					newMessagingListner.HandleNewMessage();
				}
			}
		});
	}
	
	public static void addNewMessagesListner(NewMessagingListner listener){
		listeners.add(listener);
	}
	
	
	public static void clearMessagingData(){
		messageHistory.clear();
		listeners.clear();
	}
}
