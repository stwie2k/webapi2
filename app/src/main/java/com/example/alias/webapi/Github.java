package com.example.alias.webapi;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;


public class Github extends AppCompatActivity {
    String username;
    EditText tv;
    SimpleAdapter simpleAdapter;
    List<Map<String,String>> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.github);

        Button se=findViewById(R.id.se);

        tv=findViewById(R.id.et);

        se.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.clear();
                 username=tv.getText().toString();

                OkHttpClient build = new OkHttpClient.Builder()
                        .connectTimeout(2, TimeUnit.SECONDS)
                        .readTimeout(2, TimeUnit.SECONDS)
                        .writeTimeout(2, TimeUnit.SECONDS)
                        .build();

                String baseURL="https://api.github.com";

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(baseURL)

                        // 本次实验不需要自定义Gson
                        .addConverterFactory(GsonConverterFactory.create())

                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

                        // build 即为okhttp声明的变量，下文会讲
                        .client(build)

                        .build();


                GitHubService service = retrofit.create(GitHubService.class);


                service.getRepo(username)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableObserver<List<Repo>>() {
                            @Override
                            public void onComplete() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(List<Repo> l) {
                                List<Repo> repoes=l;
                                for( int i = 0 ; i < repoes.size() ; i++) {
                                    if(repoes.get(i).has_issues){
                                        Map<String,String>temp = new LinkedHashMap<>();
                                        temp.put("name",repoes.get(i).name);
                                        temp.put("id",String.valueOf( repoes.get(i).id));
                                        if(repoes.get(i).description==null){
                                            temp.put("description","null");
                                        }
                                        else {
                                            temp.put("description",repoes.get(i).description);
                                        }

                                        temp.put("issues",String.valueOf( repoes.get(i).open_issues));
                                        data.add(temp);
                                    }
                                }

                                simpleAdapter.notifyDataSetChanged();


                            }
                        });



            }
        });





        ListView listview = (ListView)findViewById(R.id.lv);

        simpleAdapter = new SimpleAdapter(this,data,R.layout.repolist,
                new String[] {"name","id","issues","description"},new int[]{R.id.name,R.id.id,R.id.problem,R.id.description});


        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 处理单击事件
                Intent intent=new Intent(Github.this,Repoissues.class);

                Bundle bundle = new Bundle();
                bundle.putString("username",username);
                bundle.putString("reponame",data.get(i).get("name"));
                intent.putExtras(bundle);

                startActivityForResult(intent,1);



            }
        });




    }
}

