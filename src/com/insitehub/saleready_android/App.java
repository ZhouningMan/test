package com.insitehub.saleready_android;

import android.app.Application;

public class App extends Application{

	
	@Override
	public void onCreate() {
		super.onCreate();
		Utility.initializeParse(this);
	}
	
}
