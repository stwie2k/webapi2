package com.example.alias.webapi;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alias.webapi.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<User> users;
    private LayoutInflater mInflater;
    Context context;

    Handler handler;


    public interface OnItemClickLitener {
        void onItemClick(View view, int position, User item);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public MyAdapter(Context _context, List<User> items) {
        super();
        users = items;
        mInflater = LayoutInflater.from(_context);
        context=_context;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.userlist, viewGroup, false);
        ViewHolder holder = new ViewHolder(view) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
        holder.Cover=view.findViewById(R.id.cover);
        holder.Create = (TextView) view.findViewById(R.id.create);
        holder.Title = (TextView) view.findViewById(R.id.title);
        holder.Content = (TextView) view.findViewById(R.id.content);
       holder.pb=view.findViewById(R.id.pb);
        holder.Play = (TextView) view.findViewById(R.id.play);
        holder.Comment = (TextView) view.findViewById(R.id.comment);
        holder.Duration = (TextView) view.findViewById(R.id.duration);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        viewHolder.Create.setText("创建时间: "+users.get(i).data.create);
        viewHolder.Title.setText(users.get(i).data.title);
        viewHolder.Content.setText(users.get(i).data.content);
        viewHolder.Play.setText("播放： "+users.get(i).data.play);
        viewHolder.Comment.setText("评论： "+users.get(i).data.video_review);
        viewHolder.Duration.setText("时长： "+users.get(i).data.duration);

//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                String urlpath = users.get(i).data.cover;
//                final Bitmap bm = getInternetPicture(urlpath);
//
//
//                handler.post(new Runnable() {
//                    public void run() {
//
//                        viewHolder.Cover.setImageBitmap(bm);//新线程更新界面，需要使用handler
//                    }
//                });
//            }
//        }).start();



        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {

                String urlpath = users.get(i).data.cover;
                final Bitmap bm = getInternetPicture(urlpath);
                emitter.onNext(bm);

                emitter.onComplete();
            }
        }).observeOn(AndroidSchedulers.mainThread())//回调在主线程
                .subscribeOn(Schedulers.io())//执行在io线程
                .subscribe(new io.reactivex.Observer<Bitmap>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Bitmap bm) {
                        viewHolder.Cover.setImageBitmap(bm);


                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        viewHolder.pb.setVisibility(View.INVISIBLE);

                    }
                });






        if (mOnItemClickLitener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickLitener.onItemClick(viewHolder.itemView, i, users.get(i));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
        ImageView Cover;
        TextView Create;
        TextView Title;
        TextView Content;
        ProgressBar pb;
        TextView Play;
        TextView Comment;
        TextView Duration;
    }
    public Bitmap getInternetPicture(String UrlPath) {
        Bitmap bm = null;
        // 1、确定网址
        // http://pic39.nipic.com/20140226/18071023_164300608000_2.jpg
        String urlpath = UrlPath;
        // 2、获取Uri
        try {
            URL uri = new URL(urlpath);

            // 3、获取连接对象、此时还没有建立连接
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            // 4、初始化连接对象
            // 设置请求的方法，注意大写
            connection.setRequestMethod("GET");
            // 读取超时
            connection.setReadTimeout(5000);
            // 设置连接超时
            connection.setConnectTimeout(5000);
            // 5、建立连接
            connection.connect();

            // 6、获取成功判断,获取响应码
            if (connection.getResponseCode() == 200) {
                // 7、拿到服务器返回的流，客户端请求的数据，就保存在流当中
                InputStream is = connection.getInputStream();
                // 8、开启文件输出流，把读取到的字节写到本地缓存文件
                File file = new File(context.getCacheDir(), getFileName(urlpath));
                FileOutputStream fos = new FileOutputStream(file);
                int len = 0;
                byte[] b = new byte[1024];
                while ((len = is.read(b)) != -1) {
                    fos.write(b, 0, len);
                }
                fos.close();
                is.close();
                //9、 通过图片绝对路径，创建Bitmap对象

                bm = BitmapFactory.decodeFile(file.getAbsolutePath());

                Log.i("", "网络请求成功");

            } else {
                Log.v("tag", "网络请求失败");
                bm = null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;

    }

    public String getFileName(String path) {
        int index = path.lastIndexOf("/");
        return path.substring(index + 1);
    }
}






