package cn.edu.gdmec.android.mobileguard.m9advancedtools.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.edu.gdmec.android.mobileguard.App;
import cn.edu.gdmec.android.mobileguard.m4appmanager.entity.AppInfo;
import cn.edu.gdmec.android.mobileguard.m9advancedtools.adapter.AppLockAdapter;
import cn.edu.gdmec.android.mobileguard.m9advancedtools.db.dao.AppLockDao;

public class AppLockFragment extends Fragment {
    private Context context;
    private TextView mLockTV;
    private ListView mLockLV;
    private AppLockDao dao;
    List<AppInfo> mLockApps = new ArrayList<AppInfo>();
    private AppLockAdapter adapter;
    private Uri uri = Uri.parse(App.APPLOCK_CONTENT_URI);
    private Handler mHandler = new Handler(){
        public void handlerMessage(android.os.Message msg){
            switch (msg.what){
                case 10:
                    mLockApps.clear();
                    mLockApps.addAll((List<AppInfo>)msg.obj);
                    if(adapter == null){
                        adapter = new AppLockAdapter(mLockApps,getActivity());
                        mLockLV.setAdapter(adapter);
                    }else{
                        adapter.notifyDataSetChanged();
                    }
                    mLockTV.setText("加载应用"+mLockApps.size()+"个");
                    break;
            }
        }
    };
}
