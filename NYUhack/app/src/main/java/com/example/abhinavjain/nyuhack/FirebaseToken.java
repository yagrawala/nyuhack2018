package com.example.abhinavjain.nyuhack;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by abhinavjain on 3/25/18.
 */

public class FirebaseToken extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("qwe-", "Refreshed token: " + refreshedToken);
        System.out.println("THIS IS TOKEN" + refreshedToken);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        new RetrofitService().sendRegistrationToServer(refreshedToken);
    }
}
