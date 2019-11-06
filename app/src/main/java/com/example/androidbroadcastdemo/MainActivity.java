package com.example.androidbroadcastdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private IntentFilter mIntentFilter;
    private NetworkChangeReceiver mNetworkChangeReceiver;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mNetworkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(mNetworkChangeReceiver,mIntentFilter);//注册广播

        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.my.broadcast");//android 8和以上版本需要指向具体的广播接收器
//                intent.setComponent(new ComponentName("com.example.androidbroadcastdemo","com.example.androidbroadcastdemo.MyBroadcastReceiver"));//new ComponentName(广播接收器的包名,广播接收器的路径)
//                sendBroadcast(intent);//发送标准广播
                intent.addFlags(0x01000000);//强行突破隐式广播限制。也可以多条定向广播，分多次发送
//                intent.setComponent(new ComponentName("com.example.ziyuanjiandie","com.example.ziyuanjiandie.MyReceiver"));
//                sendBroadcast(intent);
                sendOrderedBroadcast(intent,null);//有序广播，第一个参数是Intent，第二个参数是与权限相关的字符串
                sendOrderedBroadcast(intent, null, mNetworkChangeReceiver, null, Activity.RESULT_OK, null, null);//设置最终收到的广播接收器
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkChangeReceiver);
    }

    class NetworkChangeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(MainActivity.this, "网络状态发生变化", Toast.LENGTH_SHORT).show();
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isAvailable()){//是否有网络
                Toast.makeText(MainActivity.this, "网络已连接", Toast.LENGTH_SHORT).show();
                String typeName = "";
                if(networkInfo.getType()==ConnectivityManager.TYPE_WIFI){
                    typeName = networkInfo.getTypeName();
                    Toast.makeText(MainActivity.this, "网络类型为"+typeName+"网络", Toast.LENGTH_SHORT).show();
                }else if(networkInfo.getType()==ConnectivityManager.TYPE_MOBILE){
                    typeName = networkInfo.getTypeName();
                    Toast.makeText(MainActivity.this, "网络类型为"+typeName+"移到数据网络", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(MainActivity.this, "无网络连接", Toast.LENGTH_SHORT).show();
            }
            // Android 7 行为变更上明确说明
            // Android 7 移除了三项隐式广播，因为隐式广播会在后台频繁启动已注册侦听这些广播的应用。删除这些广播可以显著提升设备性能和用户体验。
            // 为缓解这些问题，Android 7.0 应用了以下优化措施：
            // 面向 Android 7.0 开发的应用不会收到 CONNECTIVITY_ACTION 广播，即使它们已有清单条目来请求接受这些事件的通知。
            // 在前台运行的应用如果使用 BroadcastReceiver 请求接收通知，则仍可以在主线程中侦听 CONNECTIVITY_CHANGE。
            // 应用无法发送或接收 ACTION_NEW_PICTURE 或 ACTION_NEW_VIDEO 广播。此项优化会影响所有应用，而不仅仅是面向 Android 7.0 的应用。
            // Android文档中描述，通过在AndroidManifest.xml中注册方式(静态注册广播)，App在前后台都无法接收到广播。
            // 通过register的注册方式（动态注册广播），当App在运行时，是可以接收到广播的。
        }
    }
    
}
