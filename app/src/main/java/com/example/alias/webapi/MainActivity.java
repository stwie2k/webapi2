package com.example.alias.webapi;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    MyAdapter adapter;
    List<User> users = new ArrayList<>();
    String qq="{\"status\":true,\"data\":{\"aid\":349,\"state\":0,\"cover\":\"http:\\/\\/i2.hdslb.com\\/bfs\\/archive\\/8d15f47650e6e95e11ad7a3d6ae06ea52231169a.jpg\",\"title\":\"[\\u4eba\\u751f\\u7684\\u5bfc\\u5e08\\u677e\\u5188\\u4fee\\u9020]\\u6771\\u65b9\\u4fee\\u5922\\u9020\",\"content\":\"sm5159640\",\"play\":185998,\"duration\":\"01:23\",\"video_review\":936,\"create\":\"2009-09-08 17:00:16\",\"rec\":\"e'm'm'm'm'm\",\"count\":1}}";
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String jsonString = "{\"status\":true,\"data\":{\"aid\":439237,\"state\":0,\"cover\":\"http:\\/\\/i1.hdslb.com\\/bfs\\/archive\\/2285cd1e6681b1f9748fd003d467fbe672db0024.jpg\",\"title\":\"\\u6211\\u4eec\\u90fd\\u660e\\u767d\\u771f\\u6b63\\u7684\\u540e\\u671f[\\u66f4\\u65b0\\u4e2d]\",\"content\":\"\\u559c\\u95fb\\u4e50\\u89c1\\u3002\",\"play\":582811,\"duration\":\"02:19\",\"video_review\":4123,\"create\":\"2013-01-13 00:07:36\",\"rec\":\"\",\"count\":1}}";

        User user = new Gson().fromJson((String) jsonString, User.class);
         users.add(user);


        RecyclerView rv = findViewById(R.id.recyclerview);
        adapter = new MyAdapter(this, users);
        rv.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);

        editText = findViewById(R.id.ed);
        Button search = findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String id=editText.getText().toString();
                if(id.equals("")){
                    Toast.makeText(MainActivity.this, "请输入用户id", Toast.LENGTH_SHORT).show();
                    return;
                }
                int i;
                try{
                   i=Integer.parseInt(id);
                }catch(NumberFormatException e){
                    Toast.makeText(MainActivity.this, "需要整数类型数据", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(i<=0){
                    Toast.makeText(MainActivity.this, "需要大于0的user_id", Toast.LENGTH_SHORT).show();
                    return;
                }

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if ((networkInfo == null) || !networkInfo.isConnected()) {
                    Toast.makeText(MainActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    return;
                }


                final String u="https://space.bilibili.com/ajax/top/showTop?mid="+id;


                Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        String message="";
                        try {
                            URL url=new URL(u);
                            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(5*1000);
                            connection.connect();
                            InputStream inputStream=connection.getInputStream();
                            byte[] data=new byte[1024];
                            StringBuffer sb=new StringBuffer();
                            int length=0;
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(connection.getInputStream()));
                            String line = null;

                            while ((line = reader.readLine()) != null) { // 循环从流中读取
                                message += line ;
                            }
                            reader.close(); // 关闭流





                            inputStream.close();
                            connection.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        char index = message.charAt(10);
                        if(index=='f'){
//                            Toast.makeText(MainActivity.this, "数据库中不存在记录", Toast.LENGTH_SHORT).show();
                            emitter.onNext("quit");
                        }
                        else{
                            emitter.onNext(message);
                        }



                        emitter.onComplete();
                    }
                }).observeOn(AndroidSchedulers.mainThread())//回调在主线程
                        .subscribeOn(Schedulers.io())//执行在io线程
                        .subscribe(new io.reactivex.Observer<String>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(String s) {

                                if(s.equals("quit")){
                                    Toast.makeText(MainActivity.this, "数据库中不存在记录", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                User user = new Gson().fromJson((String) s, User.class);
                                users.add(user);
                                adapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });






            }

        });
    }

}