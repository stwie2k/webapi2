package com.example.alias.webapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.reactivestreams.Subscriber;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Repoissues extends AppCompatActivity{
    EditText title;
    EditText body;
     SimpleAdapter simpleAdapter;
    List<Map<String,String>> data = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.repository);


        Intent intent=getIntent();
        Bundle bundle =intent.getExtras();
        String name=bundle.getString("username");

       final String reponame=bundle.getString("reponame");



        OkHttpClient build = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build();

       final  String baseURL="https://api.github.com";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)

                // 本次实验不需要自定义Gson
                .addConverterFactory(GsonConverterFactory.create())

                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

                // build 即为okhttp声明的变量，下文会讲
                .client(build)

                .build();


        GitHubService service = retrofit.create(GitHubService.class);


        service.getIssueObservable(name,reponame)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<List<Issues>>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Issues> l) {
                        List<Issues> iss=l;

                        for( int i = 0 ; i < iss.size() ; i++) {

                                Map<String,String>temp = new LinkedHashMap<>();
                                temp.put("title",iss.get(i).title);
                                temp.put("state", iss.get(i).state);
                                temp.put("body",iss.get(i).body);


                                temp.put("created_at", iss.get(i).created_at);
                                data.add(temp);

                        }
                        simpleAdapter.notifyDataSetChanged();









                    }
                });

        ListView listview = (ListView)findViewById(R.id.lv1);

        simpleAdapter = new SimpleAdapter(this,data,R.layout.repolist,
                new String[] {"title","created_at","state","body"},new int[]{R.id.name,R.id.id,R.id.problem,R.id.description});


        listview.setAdapter(simpleAdapter);


        Button button=findViewById(R.id.bu);

        title=findViewById(R.id.title);
        body=findViewById(R.id.body);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Comment Comment = new Comment();
                Comment.body = title.getText().toString();
                Comment.title = body.getText().toString();

                OkHttpClient build = new OkHttpClient.Builder()
                        .connectTimeout(2, TimeUnit.SECONDS)
                        .readTimeout(2, TimeUnit.SECONDS)
                        .writeTimeout(2, TimeUnit.SECONDS)
                        .build();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(baseURL)
                        // 本次实验不需要自定义Gson
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .client(build)
                        .build();
                GitHubService githubService = retrofit.create(GitHubService.class);
                Call<Comment> commentCall = githubService.addIssue("PeanutADi",reponame,"0c7df72fc026123d4c3249cf5d8e5020662dffb1",Comment);
                commentCall.enqueue(new Callback<Comment>() {
                    @Override
                    public void onResponse(Call<Comment> call, Response<Comment> response) {



                    }

                    @Override
                    public void onFailure(Call<Comment> call, Throwable t) {

                    }
                });

//                OkHttpClient build2 = new OkHttpClient.Builder()
//                        .connectTimeout(2, TimeUnit.SECONDS)
//                        .readTimeout(2, TimeUnit.SECONDS)
//                        .writeTimeout(2, TimeUnit.SECONDS)
//                        .build();
//
//                Retrofit retrofit2 = new Retrofit.Builder()
//                        .baseUrl(baseUrl)
//                        // 本次实验不需要自定义Gson
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                        .client(build2)
//                        .build();

//                GitHubService myService2 = retrofit2.create(GithubService.class);
//
//
//
//                myService2.getIssueObservable(user,repo.name)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Subscriber<List<Issues>>() {
//                            @Override
//                            public void onCompleted() {
//
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//
//                            }
//
//                            @Override
//                            public void onNext(List<Issues> issuesList) {
//                                for(int i = 0;i<issuesList.size();i++){
//                                    issues.add(issuesList.get(i));
//                                }
//                                issueAdapter.notifyDataSetChanged();
//                            }
//                        });
            }
        });



    }

}
