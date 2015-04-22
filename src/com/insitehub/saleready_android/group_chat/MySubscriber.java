package com.insitehub.saleready_android.group_chat;

import android.content.Context;

import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

public class MySubscriber extends Subscriber {

    private String userId;
    private String name;
        
    public MySubscriber(Context context, Stream stream) {
        super(context, stream);
 
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String name) {
        this.userId = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    

}
