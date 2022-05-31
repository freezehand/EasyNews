package com.example.easynews;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.qweather.sdk.bean.air.AirNowBean;
import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.base.Unit;
import com.qweather.sdk.bean.geo.GeoBean;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.HeConfig;
import com.qweather.sdk.view.QWeather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private  androidx.appcompat.widget.Toolbar toolbar;
    private androidx.drawerlayout.widget.DrawerLayout mDrawerLayout;
    private com.google.android.material.navigation.NavigationView navigationView;
    private com.google.android.material.tabs.TabLayout tabLayout;
    private androidx.viewpager.widget.ViewPager viewPager;
    private List<String> list;

    //天气
    TextView tv_tianqi,tv_kongqi,tv_airqlty;
    ImageView img_weather;
    public AMapLocationClient mLocationClient=null;
    //声明定位回调监听器
    public AMapLocationClientOption mLocationOption=null;
    GeoBean.LocationBean location_bean;
    private  String CityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            initMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //username,key要替换成你自己的哦
        HeConfig.init("HE2205260009191403","63a947006a80404ca4367b79e2051fb9");
        HeConfig.switchToDevService();

        tv_tianqi =(TextView) findViewById(R.id.tv_tianqi);
        tv_kongqi =(TextView) findViewById(R.id.tv_kongqi);
        tv_airqlty =(TextView) findViewById(R.id.tv_airqlty);
        img_weather=(ImageView) findViewById(R.id.img_weather);

        toolbar =  findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout); //获取抽屉布局
        navigationView = (NavigationView) findViewById(R.id.nav_design);//获取菜单控件实例
        View v = navigationView.getHeaderView(0);
        CircleImageView circleImageView =(CircleImageView) v.findViewById(R.id.icon_image);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        list = new ArrayList<>();
    }


    private void initMap() throws Exception {

        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this,true);

        //初始化定位
        mLocationClient=new AMapLocationClient(MainActivity.this);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        mLocationOption = new AMapLocationClientOption();
//设置定位模式为高精度模式，AMapLocationMode.Battery_Saving为低功耗模式，AMapLocationMode.Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(false);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(15000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setOnceLocation(false);//可选，是否设置单次定位默认为false即持续定位
        mLocationOption.setOnceLocationLatest(false); //可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        mLocationOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mLocationOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
//给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
//启动定位
        mLocationClient.startLocation();
    }

    public AMapLocationListener mLocationListener=new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    aMapLocation.getAccuracy();//获取精度信息
                    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //获取经纬度
                    double  LongitudeId = aMapLocation.getLongitude();
                    double LatitudeId = aMapLocation.getLatitude();
                    //获取定位城市定位的ID
                    requestCityInfo(LongitudeId,LatitudeId);
                    Toast.makeText(MainActivity.this,"所在城市："+aMapLocation.getProvince()+aMapLocation.getCity(),Toast.LENGTH_SHORT).show();
                    mLocationClient.stopLocation();//停止定位
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("info", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };

    public void  requestCityInfo(double longitude,double latitude){
        //这里的key是webapi key
        String cityUrl = "https://geoapi.qweather.com/v2/city/lookup?location="+longitude+","+latitude+"&key=41df96ba354340b8a05f56c59f859b0e";
        sendRequestWithOkHttp(cityUrl);
    }

    private void sendRequestWithOkHttp(String cityUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().url(cityUrl).build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    //返回城市列表json数据
                    String responseData = response.body().string();
                    System.out.println("变成json数据的格式："+responseData);
                    JSONObject jsonWeather = null;
                    try {
                        jsonWeather = new JSONObject(responseData);
                   /*     JSONArray jsonArray = jsonWeather.getJSONArray(1);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);*/
                        String jsonStatus = jsonWeather.getString("code");
                        System.out.println("解析以后的内容："+jsonStatus);
                        if (jsonStatus.equals("200")){

                            JSONArray jsonBasic = jsonWeather.getJSONArray("location");
                            JSONObject jsonCityId = jsonBasic.getJSONObject(0);
                            CityId = jsonCityId.getString("id");
                            getWether();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
     /*   toolbar.setLogo(R.drawable.nav_icon);//设置图片logo,你可以添加自己的图片*/
        toolbar.setTitle("简易新闻");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar !=null){
            //通过HomeAsUp来让导航按钮显示出来
            actionBar.setDisplayHomeAsUpEnabled(true);
            //设置Indicator来添加一个点击图标
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        }
        navigationView.setCheckedItem(R.id.nav_call);//设置第一个默认选中
        navigationView.setNavigationItemSelectedListener(new  NavigationView.OnNavigationItemSelectedListener() {
            //设置菜单项的监听事件
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_call:
                        //每个菜单项的点击事件，通过Intent实现点击item简单实现活动页面的跳转。
                        /*Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                        //第二个Main2Activity.class需要你自己new一个 Activity来做出其他功能页面
                        startActivity(intent);*/
                        break;
                    case R.id.nav_friends:
                        Toast.makeText(MainActivity.this, "你点击了好友", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_location:
                        Toast.makeText(MainActivity.this, "你点击了发布新闻，下步实现", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_favorite:
                        Toast.makeText(MainActivity.this, "你点击了个人收藏，下步实现", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_settings:
                        Toast.makeText(MainActivity.this,"需要做出登出功能，可扩展夜间模式，离线模式等,检查更新",Toast.LENGTH_LONG).show();
                        break;
                    case R.id.nav_exit:

                        break;
                    default:
                }
                return true;
            }
        });
        list.add("头条");
        list.add("社会");
        list.add("国内");
        list.add("国际");
        list.add("娱乐");
        list.add("体育");
        list.add("军事");
        list.add("科技");
        list.add("财经");
        /* viewPager.setOffscreenPageLimit(1);*/
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            //得到当前页的标题，也就是设置当前页面显示的标题是tabLayout对应标题

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return list.get(position);
            }
            @Override
            public Fragment getItem(int position) {
                NewsFragment newsFragment = new NewsFragment();
                //判断所选的标题，进行传值显示
                Bundle bundle = new Bundle();
                if (list.get(position).equals("头条")){
                    bundle.putString("name","top");
                }else if (list.get(position).equals("社会")){
                    bundle.putString("name","shehui");
                }else if (list.get(position).equals("国内")){
                    bundle.putString("name","guonei");
                }else if (list.get(position).equals("国际")){
                    bundle.putString("name","guoji");
                }else if (list.get(position).equals("娱乐")){
                    bundle.putString("name","yule");
                }else if (list.get(position).equals("体育")){
                    bundle.putString("name","tiyu");
                }else if (list.get(position).equals("军事")){
                    bundle.putString("name","junshi");
                }else if (list.get(position).equals("科技")){
                    bundle.putString("name","keji");
                }else if (list.get(position).equals("财经")){
                    bundle.putString("name","caijing");
                }else if (list.get(position).equals("时尚")){
                    bundle.putString("name","shishang");
                }
                newsFragment.setArguments(bundle);
                return newsFragment;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                NewsFragment newsFragment = (NewsFragment)  super.instantiateItem(container, position);

                return newsFragment;
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return FragmentStatePagerAdapter.POSITION_NONE;
            }

            @Override
            public int getCount() {
                return list.size();
            }
        });
        //TabLayout要与ViewPAger关联显示
        tabLayout.setupWithViewPager(viewPager);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //获取toolbar菜单项
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //R.id.home修改导航按钮的点击事件为打开侧滑栏
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);  //打开侧滑栏
                break;
            case R.id.userFeedback:
                final EditText ed =new EditText(MainActivity.this);
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("用户反馈");
                dialog.setView(ed);
                dialog.setCancelable(false);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //添加点击事件
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();
                break;
            case R.id.userExit:
                Toast.makeText(this,"ni click 退出",Toast.LENGTH_SHORT).show();
                break;
            default:

        }
        return true;
    }

    private void getWether() {
        /**
         * 实况天气
         * 实况天气即为当前时间点的天气状况以及温湿风压等气象指数，具体包含的数据：体感温度、
         * 实测温度、天气状况、风力、风速、风向、相对湿度、大气压强、降水量、能见度等。
         * @param context  上下文
         * @param location 地址详解
         * @param lang       多语言，默认为简体中文
         * @param unit        单位选择，公制（m）或英制（i），默认为公制单位
         * @param listener  网络访问回调接口
         */
        QWeather.getWeatherNow(MainActivity.this, CityId,  Lang.ZH_HANS , Unit.METRIC , new QWeather.OnResultWeatherNowListener() {
            public static final String TAG="he_feng_now";
            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "onError: ", e);
                System.out.println("Weather Now Error:"+new Gson());
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(WeatherNowBean weatherBean) {
                Log.i(TAG, " Weather Now onSuccess: " + new Gson().toJson(weatherBean));
                String jsonData = new Gson().toJson(weatherBean);
                String tianqi = null,wendu = null, tianqicode = null;
                String code=weatherBean.getCode().toString();
                if (Code.OK==weatherBean.getCode()){

                    WeatherNowBean.NowBaseBean now = weatherBean.getNow();

                    String JsonNow = new Gson().toJson(weatherBean.getNow());
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(JsonNow);
                        tianqi = now.getText();
                        wendu = now.getTemp();
                        tianqicode=now.getIcon();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(MainActivity.this,"有错误",Toast.LENGTH_SHORT).show();
                    return;
                }
                String wendu2 = wendu +"℃";
                tv_tianqi.setText(tianqi);
                tv_kongqi.setText(wendu2);
                String url="https://cdn.heweather.com/cond_icon/"+tianqicode+".png";

            }
        });
        QWeather.getAirNow(MainActivity.this, CityId, Lang.ZH_HANS,  new QWeather.OnResultAirNowListener () {
            public static final String TAG2="he_feng_air";
            @Override
            public void onError(Throwable throwable) {
                Log.i(TAG2,"ERROR IS:",throwable);
            }
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(AirNowBean airNowBean) {
                Log.i(TAG2,"Air Now onSuccess:"+new Gson().toJson(airNowBean));

                if (Code.OK==airNowBean.getCode()){

                    AirNowBean.NowBean nowBean=airNowBean.getNow();


                    String aqi = null,qlty = null;
                    JSONObject objectAir = null;


                    aqi = nowBean.getAqi();
                    qlty = nowBean.getCategory();
                    tv_airqlty.setText(qlty+"("+aqi+")");

                }else {
                    Toast.makeText(MainActivity.this,"有错误",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
