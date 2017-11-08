package cn.edu.gdmec.android.mobileguard.m4appmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m4appmanager.adapter.AppManagerAdapter;
import cn.edu.gdmec.android.mobileguard.m4appmanager.entity.AppInfo;
import cn.edu.gdmec.android.mobileguard.m4appmanager.utils.AppInfoParser;

public class AppManagerActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView mPhoneMemoryTV;
    private TextView mSDMemoryTV;
    private ListView mListView;
    private List<AppInfo> appInfos;
    private List<AppInfo> userAppInfos = new ArrayList<AppInfo>();
    private List<AppInfo> systemAppInfos = new ArrayList<AppInfo>();
    private AppManagerAdapter adapter;
    private TextView mAppNumTV;
    private TextView mAboutBTN;
    private UninstallRececiver receciver;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 10:
                    if(adapter == null){
                        adapter = new AppManagerAdapter(userAppInfos,systemAppInfos,AppManagerActivity.this);
                    }
                    mListView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
                case 15:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private void initData(){
        appInfos = new ArrayList<AppInfo>();
        new Thread(){
            public void run(){
                appInfos.clear();
                userAppInfos.clear();
                systemAppInfos.clear();
                appInfos.addAll(AppInfoParser.getAppInfos(AppManagerActivity.this));
                for(AppInfo appInfo : appInfos){
                    if(appInfo.isUserApp){
                        userAppInfos.add(appInfo);
                    }else{
                        systemAppInfos.add(appInfo);
                    }
                }
                mHandler.sendEmptyMessage(10);
            };
        }.start();
    }
    class UninstallRececiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            initData();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        receciver = new UninstallRececiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        registerReceiver(receciver,intentFilter);
        initView();
    }
    private void initView(){
        findViewById(R.id.rl_titlebar).setBackgroundColor(getResources().getColor(R.color.bright_yellow));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        ((TextView) findViewById(R.id.tv_title)).setText("软件管家");
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        mPhoneMemoryTV = (TextView) findViewById(R.id.tv_phonememory_appmanager);
        mSDMemoryTV = (TextView) findViewById(R.id.tv_sdmemory_appmanager);
        mAppNumTV = (TextView) findViewById(R.id.tv_appnumber);
        mListView = (ListView) findViewById(R.id.lv_appmanager);
        mAboutBTN = (TextView) findViewById(R.id.tv_aboutapp);
        getMemoryFromPhone();
        initData();
        initListener();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgv_leftbtn:
                finish();
                break;
        }
    }
    private void getMemoryFromPhone(){
        long avail_sd = Environment.getExternalStorageDirectory().getFreeSpace();
        long avail_rom = Environment.getDataDirectory().getFreeSpace();
        String str_avail_sd = Formatter.formatFileSize(this,avail_sd);
        String str_avail_rom = Formatter.formatFileSize(this,avail_rom);
        mPhoneMemoryTV.setText("剩余手机内存：" + str_avail_rom);
        mSDMemoryTV.setText("剩余SD卡内存："+str_avail_sd);
    }
    private void initListener(){
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,final int i, long l) {
                if(adapter != null){
                    new Thread(){
                        public void run(){
                            AppInfo mappInfo = (AppInfo) adapter.getItem(i);
                            boolean flag = mappInfo.isSelected;
                            for(AppInfo appInfo : userAppInfos){
                                appInfo.isSelected = false;
                            }
                            for(AppInfo appInfo : systemAppInfos){
                                appInfo.isSelected = false;
                            }
                            if(mappInfo != null){
                                if(flag){
                                    mappInfo.isSelected = false;
                                }else{
                                    mappInfo.isSelected = true;
                                }
                                mHandler.sendEmptyMessage(15);
                            }
                        };
                    }.start();
                }
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if(i >= userAppInfos.size() + 1){
                    mAppNumTV.setText("系统应用："+systemAppInfos.size()+"个");
                }else{
                    mAppNumTV.setText("用户应用："+userAppInfos.size()+"个");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receciver);
        receciver = null;
        super.onDestroy();
    }
}
