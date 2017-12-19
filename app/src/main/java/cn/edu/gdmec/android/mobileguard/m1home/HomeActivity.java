package cn.edu.gdmec.android.mobileguard.m1home;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m1home.adapter.HomeAdapter;
import cn.edu.gdmec.android.mobileguard.m2theftguard.LostFindActivity;
import cn.edu.gdmec.android.mobileguard.m2theftguard.dialog.InterPasswordDialog;
import cn.edu.gdmec.android.mobileguard.m2theftguard.dialog.SetupPasswordDialog;
import cn.edu.gdmec.android.mobileguard.m2theftguard.receiver.MyDeviceAdminReceiver;
import cn.edu.gdmec.android.mobileguard.m2theftguard.utils.MD5Utils;
import cn.edu.gdmec.android.mobileguard.m3communicationguard.SecurityPhoneActivity;
import cn.edu.gdmec.android.mobileguard.m4appmanager.AppManagerActivity;
import cn.edu.gdmec.android.mobileguard.m5virusscan.VirusScanActivity;
import cn.edu.gdmec.android.mobileguard.m6cleancache.CacheClearListActivity;
import cn.edu.gdmec.android.mobileguard.m8trafficmonitor.TrafficMonitoringActivity;
import cn.edu.gdmec.android.mobileguard.m9advancedtools.AdvancedToolsActivity;

public class HomeActivity extends AppCompatActivity {

    private GridView gv_home;
    private long mExitTime;
    /** 存储手机防盗密码的sp */
    private SharedPreferences msharedPreferences;
    /** 设备管理员 */
    private DevicePolicyManager policyManager;
    /** 申请权限 */
    private ComponentName componentName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置布局
        setContentView(R.layout.activity_home);
        //隐藏标题栏
        getSupportActionBar().hide();
        //获取共享参考对象
        msharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        //获取gridview实例
        gv_home = (GridView) findViewById(R.id.gv_home);
        //为gridview设置是适配器
        gv_home.setAdapter(new HomeAdapter(HomeActivity.this));
        //为gridview设置item的点击监听
        gv_home.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.print(i);
                switch( i ){
                    case 0: // 点击手机防盗
                        if (isSetUpPassword()) {
                            // 弹出输入密码对话框
                            showInterPswdDialog();
                        } else {
                            // 弹出设置密码对话框
                            showSetUpPswdDialog();
                        }
                        break;
                    case 1:
                        Intent intent = new Intent(HomeActivity.this, SecurityPhoneActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        startActivity(AppManagerActivity.class);
                        break;
                    case 3:
                        startActivity(VirusScanActivity.class);
                        break;
                    case 4:
                        startActivity(CacheClearListActivity.class);
                        break;
                    case 6:
                        startActivity(TrafficMonitoringActivity.class);
                        break;
                    case 7:
                        startActivity(AdvancedToolsActivity.class);
                }
            }
        });
        // 1.获取设备管理员
        policyManager=(DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        //本行代码需要 "手机防盗模块"完成后才能启用
        // 2.申请权限, MyDeviceAdminReciever继承自DeviceAdminReceiver
        componentName=new ComponentName(this, MyDeviceAdminReceiver.class);
        // 3.判断,如果没有权限则申请权限
        boolean active=policyManager.isAdminActive(componentName);
        if(!active) {
            //没有管理员的权限，则获取管理员的权限
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "获取超级管理员权限，用于远程锁屏和清除数据");
            startActivity(intent);
        }

    }
    public void startActivity(Class<?> cls){
        Intent intent = new Intent(HomeActivity.this,cls);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if((System.currentTimeMillis()-mExitTime)<2000){
                System.exit(0);
            }else{
                Toast.makeText(this,"再按一次退出程序",Toast.LENGTH_LONG).show();
                mExitTime = System.currentTimeMillis();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /***
     * 弹出设置密码对话框  本方法需要完成"手机防盗模块"之后才能启用
     */
    private void showSetUpPswdDialog() {
        final SetupPasswordDialog setUpPasswordDialog = new SetupPasswordDialog(
                HomeActivity.this);
        setUpPasswordDialog
                .setCallBack(new SetupPasswordDialog.MyCallBack() {
                    @Override
                    public void ok() {
                        String firstPwsd = setUpPasswordDialog.mFirstPWDET
                                .getText().toString().trim();
                        String affirmPwsd = setUpPasswordDialog.mAffirmET
                                .getText().toString().trim();
                        if (!TextUtils.isEmpty(firstPwsd)
                                && !TextUtils.isEmpty(affirmPwsd)) {
                            if (firstPwsd.equals(affirmPwsd)) {
                                // 两次密码一致,存储密码
                                savePswd(affirmPwsd);
                                setUpPasswordDialog.dismiss();
                                // 显示输入密码对话框
                                showInterPswdDialog();
                            } else {
                                Toast.makeText(HomeActivity.this, "两次密码不一致！", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, "密码不能为空！", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void cancel() {
                        setUpPasswordDialog.dismiss();
                    }
                });
        setUpPasswordDialog.setCancelable(true);
        setUpPasswordDialog.show();
    }

    /**
     * 弹出输入密码对话框   本方法需要完成"手机防盗模块"之后才能启用
     */
    private void showInterPswdDialog() {
        final String password = getPassword();
        final InterPasswordDialog mInPswdDialog = new InterPasswordDialog(
                HomeActivity.this);
        mInPswdDialog.setCallBack(new InterPasswordDialog.MyCallBack() {
            @Override
            public void confirm() {
                if (TextUtils.isEmpty(mInPswdDialog.getPassword())) {
                    Toast.makeText(HomeActivity.this, "密码不能为空！", Toast.LENGTH_LONG).show();
                } else if (password.equals(MD5Utils.encode(mInPswdDialog
                        .getPassword()))) {
                    // 进入防盗主界面
                    mInPswdDialog.dismiss();
                    startActivity(LostFindActivity.class);
                    Toast.makeText(HomeActivity.this,"可以进入手机防盗模块",Toast.LENGTH_LONG).show();
                } else {
                    // 对话框消失，弹出土司
                    mInPswdDialog.dismiss();
                    Toast.makeText(HomeActivity.this, "密码有误，请重新输入！", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void cancle() {
                mInPswdDialog.dismiss();
            }
        });
        mInPswdDialog.setCancelable(true);
        // 让对话框显示
        mInPswdDialog.show();
    }

    /***
     * 保存密码   本方法需要完成"手机防盗模块"之后才能启用
     *
     * @param affirmPwsd
     */
    private void savePswd(String affirmPwsd) {
        SharedPreferences.Editor edit = msharedPreferences.edit();
        // 为了防止用户隐私被泄露，因此需要加密密码
        edit.putString("PhoneAntiTheftPWD", MD5Utils.encode(affirmPwsd));
        edit.commit();
    }

    /***
     * 获取密码
     *
     * @return sp存储的密码
     */
    private String getPassword() {
        String password = msharedPreferences.getString("PhoneAntiTheftPWD",
                null);
        if (TextUtils.isEmpty(password)) {
            return "";
        }
        return password;
    }

    /** 判断用户是否设置过手机防盗密码 */
    private boolean isSetUpPassword() {
        String password = msharedPreferences.getString("PhoneAntiTheftPWD",
                null);
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        return true;
    }
}
