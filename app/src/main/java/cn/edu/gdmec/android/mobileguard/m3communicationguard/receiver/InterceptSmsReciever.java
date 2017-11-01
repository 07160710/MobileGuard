package cn.edu.gdmec.android.mobileguard.m3communicationguard.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsMessage;
import android.util.Log;

import cn.edu.gdmec.android.mobileguard.m3communicationguard.db.dao.BlackNumberDao;

/**
 * Created by Administrator on 2017/11/1 0001.
 */

public class InterceptSmsReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences mSP = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        boolean BlackNumStaus = mSP.getBoolean("BlackNumStaus",true);
        if(!BlackNumStaus){
            return;
        }
        BlackNumberDao dao = new BlackNumberDao(context);
        Object[] objs = (Object[]) intent.getExtras().get("pdus");
        for(Object obj : objs){
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
            String sender = smsMessage.getOriginatingAddress();
            String body = smsMessage.getMessageBody();
            if(sender.startsWith("+86")){
                sender = sender.substring(3,sender.length());
            }
            int mode = dao.getBlackContactMode(sender);
            Log.d("-------","onReceive:"+mode);
            if(mode == 2 || mode == 3){
                abortBroadcast();
            }
        }
    }
}
