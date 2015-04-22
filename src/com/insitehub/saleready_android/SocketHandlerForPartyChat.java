package com.insitehub.saleready_android;

import io.socket.IOCallback;
import io.socket.SocketIO;

import java.net.MalformedURLException;

public class SocketHandlerForPartyChat {

	private static SocketIO socket = new SocketIO();
	
	
	public static void startSocketConnection(IOCallback callbacks){
		try {
			if(!socket.isConnected()){
				socket = new SocketIO();
				socket.connect("http://admin.mobilesfe.com:8080",callbacks);
				
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public static void connectToSession(String sessionID){
		socket.emit("JOIN:CHANNEL", sessionID);
		//socket.emit("CHAT:CHANNEL", "{\"y\":214.5,\"author\":\"FROM Android\",\"type\":\"scroll\",\"x\":0}");
	}
	
	public static void disconnectSocketConnection(){
			if(socket!=null && socket.isConnected()){
				socket.disconnect();
			}
			
	}
	
	public static void sendChatMessage(String chatmessage){
		socket.emit("CHAT:CHANNEL",chatmessage);
	}
	
}
