package com.test.ashish.clientsideapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private Context context;
    private static final int GET_RANDOM_NO_FLAG = 0;
    private boolean mIsBound;
    private int randomNoValue;
    Messenger randomNoRequestMesseneger, randomNoReceiveMessenger;
    private Intent serviceIntent;

    private TextView textViewServiceRandomNo;
    private Button buttonBindService, buttonUnBindService, buttonGetRandomNoGenerator;


    class ReceiverRandomNoHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            randomNoValue = 0;
            switch (msg.what){
                case GET_RANDOM_NO_FLAG:
                    randomNoValue = msg.arg1;
                    textViewServiceRandomNo.setText("Random No+"+String.valueOf(randomNoValue));
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            randomNoRequestMesseneger = new Messenger(service);
            randomNoReceiveMessenger = new Messenger(new ReceiverRandomNoHandler());
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            randomNoRequestMesseneger = null;
            randomNoReceiveMessenger = null;
            mIsBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        textViewServiceRandomNo = findViewById(R.id.randomNo);

        buttonBindService = findViewById(R.id.bindService);
        buttonUnBindService = findViewById(R.id.unBindService);
        buttonGetRandomNoGenerator = findViewById(R.id.getRandomNo);

        buttonBindService.setOnClickListener(this);
        buttonUnBindService.setOnClickListener(this);
        buttonGetRandomNoGenerator.setOnClickListener(this);

        serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName("com.test.ashish.androidpractice", "com.test.ashish.androidpractice.MyService"));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.bindService:
                bindToRemoteService();
                break;
            case R.id.unBindService:
                unBindToRemoteService();
                break;
            case R.id.getRandomNo:
                getRandomNo();
                break;
            default:
                break;
        }
    }

    public void bindToRemoteService(){
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        Toast.makeText(this, "Bounded To Remote Service", Toast.LENGTH_SHORT).show();
        mIsBound = true;
    }

    public void unBindToRemoteService(){
        unbindService(serviceConnection);
        mIsBound = false;
        Toast.makeText(this, "UnBounded To Remote Service", Toast.LENGTH_SHORT).show();
    }

    public void getRandomNo(){
        if(mIsBound == true){
            Message requestMessage = Message.obtain(null, GET_RANDOM_NO_FLAG);
            requestMessage.replyTo = randomNoReceiveMessenger;
            try {
                randomNoRequestMesseneger.send(requestMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "Service Unbound, can't get random no", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        serviceConnection = null;
        super.onDestroy();
    }
}
