package com.example.abhinavjain.nyuhack;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by aman on 3/25/18.
 */

public class FirebaseToken extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log.d("qwe-", "Refreshed token: " + refreshedToken);
        //new RetrofitService().sendRegistrationToServer(refreshedToken);
    }
}
