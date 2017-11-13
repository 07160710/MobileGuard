package cn.edu.gdmec.android.mobileguard.m4appmanager.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m4appmanager.entity.AppInfo;
import cn.edu.gdmec.android.mobileguard.m4appmanager.utils.DensityUtil;
import cn.edu.gdmec.android.mobileguard.m4appmanager.utils.EngineUtils;

/**
 * Created by Administrator on 2017/11/7.
 */

public class AppManagerAdapter extends BaseAdapter {
    private List<AppInfo> UserAppInfos;
    private List<AppInfo> SystemAppInfos;
    private Context context;

    public AppManagerAdapter(List<AppInfo> userAppInfos,List<AppInfo> systemAppInfos,Context context){
        super();
        UserAppInfos = userAppInfos;
        SystemAppInfos = systemAppInfos;
        this.context = context;
    }
    @Override
    public int getCount() {
        return UserAppInfos.size() + SystemAppInfos.size() + 2;
    }

    @Override
    public Object getItem(int i) {
        if(i == 0) {
            return null;
        }else if(i == (UserAppInfos.size() + 1)){
            return null;
        }
        AppInfo appInfo;
        if(i < (UserAppInfos.size() +1)){
            appInfo = UserAppInfos.get(i-1);
        }else{
            int location = i - UserAppInfos.size() - 2;
            appInfo = SystemAppInfos.get(location);
        }
        return appInfo;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(i == 0){
            TextView tv = getTextView();
            tv.setText("用户程序：" + UserAppInfos.size() + "个");
            return tv;
        }else if(i == (UserAppInfos.size() +1)){
            TextView tv = getTextView();
            tv.setText("系统程序：" + SystemAppInfos.size() + "个");
            return tv;
        }
        AppInfo appInfo;
        if(i < (UserAppInfos.size() + 1)){
            appInfo = UserAppInfos.get(i-1);
        }else{
            appInfo = SystemAppInfos.get(i - UserAppInfos.size() - 2);
        }
        ViewHolder viewHolder = null;
        if(view != null & view instanceof LinearLayout){
            viewHolder = (ViewHolder) view.getTag();
        }else{
            viewHolder = new ViewHolder();
            view = View.inflate(context, R.layout.item_appmanager_list,null);
            viewHolder.mAppIconImgv = (ImageView) view.findViewById(R.id.imgv_appicon);
            viewHolder.mAppLocationTV = (TextView) view.findViewById(R.id.tv_appisroom);
            viewHolder.mAppSizeTV = (TextView) view.findViewById(R.id.tv_appsize);
            viewHolder.mAppNameTV = (TextView) view.findViewById(R.id.tv_appname);
            viewHolder.mLuanchAppTV = (TextView) view.findViewById(R.id.tv_launch_app);
            viewHolder.mSettingAppTV = (TextView) view.findViewById(R.id.tv_setting_app);
            viewHolder.mShareAppTV = (TextView) view.findViewById(R.id.tv_share_app);
            viewHolder.mUninstallTV = (TextView) view.findViewById(R.id.tv_uninstall_app);
            viewHolder.mAppOptionLL = (LinearLayout) view.findViewById(R.id.ll_option_app);
            viewHolder.mAboutBTN = (TextView) view.findViewById(R.id.tv_aboutapp);
            viewHolder.mActivityTV = (TextView) view.findViewById(R.id.tv_activityapp);
            view.setTag(viewHolder);
        }
        if(appInfo != null){
            viewHolder.mAppLocationTV.setText(appInfo.getAppLocation(appInfo.isInRoom));
            viewHolder.mAppIconImgv.setImageDrawable(appInfo.icon);
            viewHolder.mAppSizeTV.setText(Formatter.formatFileSize(context,appInfo.appSize));
            viewHolder.mAppNameTV.setText(appInfo.appName);
            if(appInfo.isSelected){
                viewHolder.mAppOptionLL.setVisibility(View.VISIBLE);
            }else{
                viewHolder.mAppOptionLL.setVisibility(View.GONE);
            }
        }
        MyClickListener listener = new MyClickListener(appInfo);
        viewHolder.mLuanchAppTV.setOnClickListener(listener);
        viewHolder.mSettingAppTV.setOnClickListener(listener);
        viewHolder.mShareAppTV.setOnClickListener(listener);
        viewHolder.mUninstallTV.setOnClickListener(listener);
        viewHolder.mAboutBTN.setOnClickListener(listener);
        viewHolder.mActivityTV.setOnClickListener(listener);

        return view;
    }
    private TextView getTextView(){
        TextView tv = new TextView(context);
        tv.setBackgroundColor(ContextCompat.getColor(context,R.color.graye5));
        tv.setPadding(DensityUtil.dip2px(context,5),DensityUtil.dip2px(context,5),DensityUtil.dip2px(context,5),DensityUtil.dip2px(context,5));
        tv.setTextColor(ContextCompat.getColor(context,R.color.black));
        return tv;
    }
    static class ViewHolder{
        TextView mLuanchAppTV;
        TextView mUninstallTV;
        TextView mShareAppTV;
        TextView mSettingAppTV;
        ImageView mAppIconImgv;
        TextView mAppLocationTV;
        TextView mAppSizeTV;
        TextView mAppNameTV;
        LinearLayout mAppOptionLL;
        TextView mAboutBTN;
        TextView mActivityTV;
    }
    class MyClickListener implements View.OnClickListener{
        private AppInfo appInfo;
        public MyClickListener(AppInfo appInfo){
            super();
            this.appInfo = appInfo;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv_launch_app:
                    EngineUtils.startApplication(context,appInfo);
                    break;
                case R.id.tv_share_app:
                    EngineUtils.shareApplication(context,appInfo);
                    break;
                case R.id.tv_setting_app:
                    EngineUtils.SettingAppDetail(context,appInfo);
                    break;
                case R.id.tv_uninstall_app:
                    if(appInfo.packageName.equals(context.getPackageName())){
                        Toast.makeText(context,"您没有权限卸载此应用！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    EngineUtils.uninstallApplication(context,appInfo);
                    break;
                case R.id.tv_aboutapp:
                    EngineUtils.AboutSign(context,appInfo);
                    break;
                case R.id.tv_activityapp:
                    EngineUtils.getActivity(context,appInfo);
                    break;
            }
        }
    }
}
