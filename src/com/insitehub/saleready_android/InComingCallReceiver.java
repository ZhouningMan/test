package com.insitehub.saleready_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InComingCallReceiver extends BroadcastReceiver  {

	@Override
	public void onReceive(Context context, Intent arg1) {
//		ParseQuery<PeerSession> query = ParseQuery.getQuery("PeerSession");    
//    	query.whereEqualTo("requestTo", ParseUser.getCurrentUser());
//    	query.include("sessionOwner");
//    	query.addDescendingOrder("createdAt");
//    	final Context receivedContext= context;
//        query.findInBackground(new FindCallback<PeerSession>() {
//			
//			@Override
//			public void done(List<PeerSession> peerSessions, ParseException e) {
//				boolean first = true;
//				for (PeerSession peerSession : peerSessions) {
//					long lastUpdatedAtMs  = peerSession.getUpdatedAt().getTime();
//					long diffInMs = System.currentTimeMillis() - lastUpdatedAtMs;
//					long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
//					if(diffInSec > 240){
//						peerSession.deleteInBackground();
//						continue;
//					}
//					
//					if(first){
//						first = false;
//						String caller = peerSession.getParseUser("sessionOwner").getUsername();
//						Utility.buildAlertDialog(receivedContext, caller);
//					}
//					else{
//						peerSession.deleteInBackground();
//					}	
//				}
//
//			}
//		});
		
		
//    	final Context receivedContext= context;
//		Utility.buildAlertDialog(context, "Linfeng");
//		Toast.makeText(context, "received alarm", Toast.LENGTH_SHORT).show();;
//		
	}

	

}