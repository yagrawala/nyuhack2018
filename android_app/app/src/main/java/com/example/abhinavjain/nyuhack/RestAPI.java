package com.example.abhinavjain.nyuhack;

import android.util.Log;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by aman on 3/24/18.
 */

public interface RestAPI {

    @Multipart
    @POST("/analyze")
    Call<EmotionModel> uploadAudio(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part audioFile);
}
