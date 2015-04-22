package com.insitehub.saleready_android;

import io.socket.IOCallback;
import io.socket.SocketIO;

import java.net.MalformedURLException;

import com.insitehub.saleready_android.Messaging.MessagingDataForP2P;

public class SocketHandlerForP2P {
	private static SocketIO socket = new SocketIO();
	

	public static void startSocketConnection(IOCallback callbacks, String sessionID) {
		MessagingDataForP2P.getMessageHistory().clear();
		try {
			if (!socket.isConnected()) {
				socket = new SocketIO();
				socket.connect("http://admin.mobilesfe.com:8080", callbacks);
			}else{
				//connect to a new session

				SocketHandlerForP2P.connectToSession(sessionID);
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void connectToSession(String sessionID) {
		socket.emit("JOIN:CHANNEL", sessionID);
		// socket.emit("CHAT:CHANNEL",
		// "{\"y\":214.5,\"author\":\"FROM Android\",\"type\":\"scroll\",\"x\":0}");
	}

	public static void disconnectSocketConnection() {
		if(socket!=null && socket.isConnected()){
			socket.disconnect();
		}
	}

	public static void sendChatMessage(String chatmessage) {
		socket.emit("CHAT:CHANNEL", chatmessage);
	}
	
	public static void disconnectFromSession(String sessionID){
		socket.emit("PART:CHANNEL", sessionID);
	}
	
}
