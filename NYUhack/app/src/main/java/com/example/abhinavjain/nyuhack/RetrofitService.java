package com.example.abhinavjain.nyuhack;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by aman on 3/24/18.
 */

public class RetrofitService {

    RestAPI api = null;

    public static RetrofitService getInstance() {
        RetrofitService retrofitService = new RetrofitService();
        retrofitService.setRetrofit();
        return retrofitService;
    }

    public void setRetrofit() {
        new RetrofitService();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.196:5000") //192.168.43.196:5000 35.224.23.142:5000
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
            .build();
        api = retrofit.create(RestAPI.class);
    }


    public EmotionModel getEmotion() {

        try {
            Call<EmotionModel> call = api.getEmotion(new EmotionModel());
            return call.execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendRegistrationToServer(String token) {
        //api.sendTokenToServer(token);
    }

    //35.224.23.142:5000
    public void uploadFile(String path, Uri uri, String text, Context context, Callback<EmotionModel> callback) {


        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = new File(path);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(context.getContentResolver().getType(uri)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("audio/wav", file.getName(), requestFile);

        // add another part within the multipart request
        //String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, text);

        // finally, execute the request
        Call<EmotionModel> call = api.uploadAudio(description, body);
        call.enqueue(callback);
        //return
    }
}
