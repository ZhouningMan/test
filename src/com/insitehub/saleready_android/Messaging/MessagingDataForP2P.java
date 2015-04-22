package com.insitehub.saleready_android.Messaging;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.insitehub.saleready_android.R;
import com.insitehub.saleready_android.UserGroup;
import com.parse.ParseUser;

//TODO Create an abstract class for MessagingDataForP2P  and MessaginDataForPartyChat.java
public class MessagingDataForP2P {
	private static List<JSONObject> messageHistory = new LinkedList<JSONObject>();
	private static List<NewMessagingListner> listeners = new LinkedList<NewMessagingListner>();

	private static BaseAdapter partyMessagingAdapter = null;

	public static List<JSONObject> getMessageHistory() {
		return messageHistory;
	}

	public static void setMessageHistory(List<JSONObject> messageHistory) {
		MessagingDataForP2P.messageHistory = messageHistory;
	}

	public static BaseAdapter getPartyMessagingAdapter(Context context) {
		if (partyMessagingAdapter == null) {
			partyMessagingAdapter = new MessagingAdapter(context,messageHistory);
		}
		return partyMessagingAdapter;
	}

	public static void addChatMessage(JSONObject message) {
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

	public static void addNewMessagesListner(NewMessagingListner listener) {
		listeners.add(listener);
	}

	public static void clearMessagingData() {
		messageHistory.clear();
		listeners.clear();
	}
}
