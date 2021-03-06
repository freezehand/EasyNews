package com.example.easynews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.easynews.TabAdpater.MyTabAdapter;
import com.example.easynews.json.NewsBean;
import com.example.easynews.tools.DBOpenHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.qweather.sdk.view.HeContext;

/*
import static interfaces.heweather.com.interfacesmodule.view.HeContext.context;
*/

public class NewsFragment extends Fragment {

    private FloatingActionButton fab;
    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<NewsBean.ResultBean.DataBean> list;
    private static final int UPNEWS_INSERT = 0;
    private int page =0,row =10;
    private static final int SELECT_REFLSH = 1;

    String responseDate;

    private static final int NO_NETWORK = 1001;

    String dataChina ="头条" ;

    @SuppressLint("HandlerLeak")
    private Handler newsHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String uniquekey,title,ddate, category,author_name,url,thumbnail_pic_s,thumbnail_pic_s02,thumbnail_pic_s03;
            switch (msg.what){
                case UPNEWS_INSERT:
                    list = ((NewsBean) msg.obj).getResult().getData();
                    MyTabAdapter adapter = new MyTabAdapter(getActivity(),list);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
                case SELECT_REFLSH:
                    list =((NewsBean) msg.obj).getResult().getData();
                    MyTabAdapter myTabAdapter = new MyTabAdapter(getActivity(),list);
                    listView.setAdapter(myTabAdapter);
                    myTabAdapter.notifyDataSetChanged();
                    if (swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);//设置不刷新
                    }
                    break;
                case NO_NETWORK:
                    Toast.makeText(HeContext.context,"请检查手机网络连接", Toast.LENGTH_SHORT).show();
//                    lay_empty.setVisibility(View.VISIBLE);
//                    listView.setEmptyView(lay_empty);
                    if (swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);//设置不刷新
                    }
                    break;
                default:
            }
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_item,container,false);
        listView = (ListView) view.findViewById(R.id.listView);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        return view;

    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onAttach(getActivity());
        //获取传递的值
        Bundle bundle = getArguments();
        final String data =bundle.getString("name","top");
        //置顶功能
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.smoothScrollToPosition(0);
            }
        });
        //下拉刷新
        swipeRefreshLayout.setColorSchemeResources(R.color.colorRed);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                page++;
                //通过获取传值的bundle值进行判断转换
                if (data.equals("guoji")) {
                    dataChina = "国际";
                } else if (data.equals("tiyu")) {
                    dataChina = "体育";
                } else if (data.equals("yule")) {
                    dataChina = "娱乐";
                } else if (data.equals("caijing")) {
                    dataChina = "财经";
                } else if (data.equals("keji")) {
                    dataChina = "科技";
                } else if (data.equals("guonei")) {
                    dataChina = "国内";
                } else if (data.equals("shehui")) {
                    dataChina = "社会";
                } else if (data.equals("junshi")) {
                    dataChina = "军事";
                }

       /*         new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);*/

                // 下一步实现从数据库中读取数据刷新到listview适配器中
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        NewsBean newsBean = new NewsBean();
                        List<NewsBean.ResultBean.DataBean> dataBeanList = new ArrayList<>();
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        int pages = (page - 1) * row;
                         String sql = "select *from news_info where category='"+dataChina+"' limit "+pages+","+row;
                     /*   String sql = "select *from news_info limit " + pages + "," + row;*/
                        PreparedStatement pst;
                        try {
                            pst = (PreparedStatement) conn.prepareStatement(sql);
                            ResultSet rs = pst.executeQuery();
                            while (rs.next()) {
                                NewsBean.ResultBean.DataBean dataBean = new NewsBean.ResultBean.DataBean();
                                dataBean.setUniquekey(rs.getString(1));
                                dataBean.setTitle(rs.getString(2));
                                dataBean.setDate(rs.getString(3));
                                dataBean.setCategory(rs.getString(4));
                                dataBean.setAuthor_name(rs.getString(5));
                                dataBean.setUrl(rs.getString(6));
                                dataBean.setThumbnail_pic_s(rs.getString(7));
                                dataBean.setThumbnail_pic_s02(rs.getString(8));
                                dataBean.setThumbnail_pic_s03(rs.getString(9));
                            }
                            newsBean.setResult(new NewsBean.ResultBean());
                            newsBean.getResult().setData(dataBeanList);
                            pst.close();
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        Message msg = newsHandler.obtainMessage();
                        msg.what = SELECT_REFLSH;
                        msg.obj = newsBean;
                        newsHandler.sendMessage(msg);
                    }
                }).start();
            }
       /*      },1000);
        }*/
        });
        //异步加载数据
        getDataFromNet(data);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获取点击条目的路径，传值显示webview页面
                String url = list.get(position).getUrl();
                String uniquekey = list.get(position).getUniquekey();
  /*              final NewsBean.ResultBean.DataBean dataBean = (NewsBean.ResultBean.DataBean) list.get(position);
                //这里是在listview子item的点击事件中添加一个插入新闻的具体json数据到news_info表中
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        System.out.print(conn);
                        String sql = "insert into news_info(uniquekey,title,date,category,author_name,url,thumbnail_pic_s,thumbnail_pic_s02,thumbnail_pic_s03) values(?,?,?,?,?,?,?,?,?)";
                        int i = 0;
                        PreparedStatement pstmt;
                        try {
                            pstmt = (PreparedStatement) conn.prepareStatement(sql);
                            pstmt.setString(1,dataBean.getUniquekey());
                            pstmt.setString(2,dataBean.getTitle());
                            pstmt.setString(3,dataBean.getDate());
                            pstmt.setString(4,dataBean.getCategory());
                            pstmt.setString(5,dataBean.getAuthor_name());
                            pstmt.setString(6,dataBean.getUrl());
                            pstmt.setString(7,dataBean.getThumbnail_pic_s());
                            pstmt.setString(8,dataBean.getThumbnail_pic_s02());
                            pstmt.setString(9,dataBean.getThumbnail_pic_s03());
                            i = pstmt.executeUpdate();
                            pstmt.close();
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
*/
                Intent intent = new Intent(getActivity(),WebActivity.class);
                intent.putExtra("url",url);
                intent.putExtra("uniquekey",uniquekey);
                startActivity(intent);

            }
        });
    }

    private void getDataFromNet(final String data){
/*        @SuppressLint("StaticFieldLeak") AsyncTask<Void,Void,String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {*/
        final String path = "http://v.juhe.cn/toutiao/index?type="+data+"&key=c81224b312efe42bbf3c72b1741074af";

                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(path)
                                .build();

                            try {
                                Response response = okHttpClient.newCall(request).execute();
                                assert response.body() != null;
                                 responseDate = response.body().string();
                                NewsBean newsBean = new Gson().fromJson(responseDate, NewsBean.class);
                                System.out.println("获取到的json数据新闻内容："+responseDate);
                                System.out.println("获取到的json数据新闻解析内容："+newsBean.getError_code());

                                //如果每天免费次数用完后，根据错误code加载数据库新闻数据
                                if ("10012".equals("" + newsBean.getError_code())) {
                                    List<NewsBean.ResultBean.DataBean> listDataBean = new ArrayList<>();

                                    if (data.equals("guoji")){
                                        dataChina ="国际";
                                    }else if (data.equals("tiyu")){
                                        dataChina ="体育";
                                    }else if (data.equals("yule")){
                                        dataChina ="娱乐";
                                    }else if (data.equals("caijing")){
                                        dataChina ="财经";
                                    }else if (data.equals("keji")){
                                        dataChina ="科技";
                                    }else if (data.equals("guonei")){
                                        dataChina ="国内";
                                    }else if (data.equals("shehui")){
                                        dataChina ="社会";
                                    }else if (data.equals("junshi")){
                                        dataChina ="军事";
                                    }
                                    Connection conn=null;
                                    conn = (Connection) DBOpenHelper.getConn();
                                    /*String sql = "select * from news_info ";*/
                                    String sql = "select * from news_info where category='"+dataChina+"'";
                                    System.out.println("当没有次数的时候获取到的标题内容:"+data);
                                    System.out.println(sql);

                                    if (conn ==null){
                                        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                                        System.out.println(""+conn);
                                    }

                                    PreparedStatement pstmt = conn.prepareStatement(sql);
                                    ResultSet rs = pstmt.executeQuery();
                                    while (rs.next()) {
                                        NewsBean.ResultBean.DataBean dataBean = new NewsBean.ResultBean.DataBean();
                                        dataBean.setUniquekey(rs.getString(1));
                                        dataBean.setTitle(rs.getString(2));
                                        dataBean.setDate(rs.getString(3));
                                        dataBean.setCategory(rs.getString(4));
                                        dataBean.setAuthor_name(rs.getString(5));
                                        dataBean.setUrl(rs.getString(6));
                                        dataBean.setThumbnail_pic_s(rs.getString(7));
                                        dataBean.setThumbnail_pic_s02(rs.getString(8));
                                        dataBean.setThumbnail_pic_s03(rs.getString(9));
                                        listDataBean.add(dataBean);
                                    }
                                    newsBean.setResult(new NewsBean.ResultBean());
                                    newsBean.getResult().setData(listDataBean);
                                    pstmt.close();
                                    conn.close();
                                }
                                //解析得到的请求到的对应标题的新闻数据json
                                JSONObject jsonObject =null;
                                try {
                                    jsonObject = new JSONObject(responseDate);
                                    JSONObject jsonObject1 = jsonObject.getJSONObject("result");
                                    JSONArray jsonArray = jsonObject1.getJSONArray("data");
                                    for (int i=0;i<jsonArray.length();i++){
                                        JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                        //这里是在新闻类别的点击事件中添加一个插入新闻的具体json数据到news_info表中
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Connection conn = null;
                                                conn = (Connection) DBOpenHelper.getConn();
                                                System.out.print(conn);
                                                String sql = "insert into news_info(uniquekey,title,date,category,author_name,url,thumbnail_pic_s,thumbnail_pic_s02,thumbnail_pic_s03) values(?,?,?,?,?,?,?,?,?)";
                                                int i = 0;
                                                PreparedStatement pstmt;
                                                try {
                                                    pstmt = (PreparedStatement) conn.prepareStatement(sql);
                                                    pstmt.setString(1,jsonObject2.getString("uniquekey"));
                                                    pstmt.setString(2,jsonObject2.getString("title"));
                                                    pstmt.setString(3,jsonObject2.getString("date"));
                                                    pstmt.setString(4,jsonObject2.getString("category"));
                                                    pstmt.setString(5,jsonObject2.getString("author_name"));
                                                    pstmt.setString(6,jsonObject2.getString("url"));
                                                    pstmt.setString(7,jsonObject2.getString("thumbnail_pic_s"));
                                                    pstmt.setString(8,jsonObject2.getString("thumbnail_pic_s02"));
                                                    pstmt.setString(9,jsonObject2.getString("thumbnail_pic_s03"));
                                                    i = pstmt.executeUpdate();
                                                    pstmt.close();
                                                    conn.close();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Message msg = newsHandler.obtainMessage();
                                msg.what = UPNEWS_INSERT;
                                msg.obj = newsBean;
                                newsHandler.sendMessage(msg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                        }

                    }).start();

                }
                /*URL url = null;
                try {
                    url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(5000);
                    connection.setConnectTimeout(5000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200){
                        InputStream inputStream = connection.getInputStream();
                        String json = streamToString(inputStream,"utf-8");
                        return json;
                    } else {
                        System.out.println(responseCode);
                        return "已达到今日访问次数上限";
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
            }

            protected void onPostExecute(final String result){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        NewsBean newsBean = new Gson().fromJson(result,NewsBean.class);
                        System.out.println(newsBean.getError_code());
                        //打印输出看一下当达到每日访问次数上限时出现的错误code，发现为10012
                        if ("10012".equals(""+newsBean.getError_code())){
                            //这里通过判断error_code来决定是否查询new_info表中的数据来填充到listview的适配器中
                            List<NewsBean.ResultBean.DataBean> listDataBean = new ArrayList<>();
                            Connection conn = null;
                            conn = (Connection) DBOpenHelper.getConn();
                            String sql = "select * from news_info ";
                            PreparedStatement pstmt;
                            try {
                                pstmt = (PreparedStatement) conn.prepareStatement(sql);
                                ResultSet rs = pstmt.executeQuery();
                                while (rs.next()){
                                    NewsBean.ResultBean.DataBean dataBean = new NewsBean.ResultBean.DataBean();
                                    dataBean.setUniquekey(rs.getString(1));
                                    dataBean.setTitle(rs.getString(2));
                                    dataBean.setDate(rs.getString(3));
                                    dataBean.setCategory(rs.getString(4));
                                    dataBean.setAuthor_name(rs.getString(5));
                                    dataBean.setUrl(rs.getString(6));
                                    dataBean.setThumbnail_pic_s(rs.getString(7));
                                    dataBean.setThumbnail_pic_s02(rs.getString(8));
                                    dataBean.setThumbnail_pic_s03(rs.getString(9));
                                    listDataBean.add(dataBean);


                                }
                                newsBean.setResult(new NewsBean.ResultBean());
                                newsBean.getResult().setData(listDataBean);
                                pstmt.close();
                                conn.close();
                                System.out.println(newsBean.getResult().getData());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        Message msg=newsHandler.obtainMessage();
                        msg.what=UPNEWS_INSERT;
                        msg.obj = newsBean;
                        newsHandler.sendMessage(msg);
                    }
                }).start();
            }
            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }
        };
        task.execute();*/





    private String streamToString(InputStream inputStream, String charset){
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,charset);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s = null;
            StringBuilder builder = new StringBuilder();
            while ((s = bufferedReader.readLine()) != null){
                builder.append(s);
            }
            bufferedReader.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

}
