package com.easy.wtool.sdk.demo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easy.wtool.sdk.MessageEvent;
import com.easy.wtool.sdk.OnMessageListener;
import com.easy.wtool.sdk.WToolSDK;

import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static String LOG_TAG = "javahook";
    Context mContext;
    // Used to load the 'native-lib' library on application startup.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        final WToolSDK wToolSDK = new WToolSDK();
        this.setTitle(this.getTitle()+" - V"+wToolSDK.getVersion());



        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());
        Button buttonInit = (Button)findViewById(R.id.buttonInit);
        Button buttonText = (Button)findViewById(R.id.buttonText);
        Button buttonImage = (Button)findViewById(R.id.buttonImage);
        Button buttonFriends = (Button)findViewById(R.id.buttonFriends);
        Button buttonChatrooms = (Button)findViewById(R.id.buttonChatrooms);
        final Button buttonStartMessage = (Button)findViewById(R.id.buttonStartMessage);
        final EditText editAuthCode = (EditText)findViewById(R.id.editAuthCode);
        final EditText editWxId = (EditText)findViewById(R.id.editWxId);
        final EditText editText = (EditText)findViewById(R.id.editText);
        final EditText editImage = (EditText)findViewById(R.id.editImage);
        final TextView editContent = (TextView)findViewById(R.id.editContent);


        editContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        //处理消息 回调的Handler
        final Handler messageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {


                MessageEvent event = (MessageEvent) msg.obj;

                editContent.append("message: "+event.getTalker()+","+event.getContent()+"\n");
                super.handleMessage(msg);
            }
        };
        wToolSDK.setOnMessageListener(new OnMessageListener() {
            @Override
            public void messageEvent(MessageEvent event) {
                Log.d(LOG_TAG,"on message: "+event.getTalker()+","+event.getContent());

                //editContent.setText("message: "+event.getTalker()+","+event.getContent());
                //由于该回调是在线程中，因些如果是有UI更新，需要使用Handler
                messageHandler.obtainMessage(0, event).sendToTarget();

            }
        });
        buttonInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editAuthCode.getText().toString().equals(""))
                {
                    Toast.makeText(mContext, "授权码不能为空！", Toast.LENGTH_LONG).show();
                    return;
                }
                //初始化
                parseResult(wToolSDK.init(editAuthCode.getText().toString()));
            }
        });
        buttonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editWxId.getText().toString().equals(""))
                {
                    Toast.makeText(mContext, "接收人不能为空！", Toast.LENGTH_LONG).show();
                    return;
                }
                //发送文本
                parseResult(wToolSDK.sendText(editWxId.getText().toString(),editText.getText().toString()));
            }
        });
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editWxId.getText().toString().equals(""))
                {
                    Toast.makeText(mContext, "接收人不能为空！", Toast.LENGTH_LONG).show();
                    return;
                }
                //发送图片
                parseResult(wToolSDK.sendImage(editWxId.getText().toString(),editImage.getText().toString()));
            }
        });
        buttonFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取好友列表
                String content = wToolSDK.getFriends(0,0);
                editContent.setText(content);
                parseResult(content);
            }
        });
        buttonChatrooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取群列表
                String content = wToolSDK.getChatrooms(0,0);
                editContent.setText(content);
                parseResult(content);
            }
        });
        buttonStartMessage.setTag(0);
        buttonStartMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonStartMessage.getTag().equals(0)) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        JSONArray jsonArray = new JSONArray();
                        jsonArray.put(1);
                        jsonArray.put(2);
                        jsonObject.put("talkertypes", jsonArray);
                        jsonObject.put("froms", new JSONArray());
                        jsonArray = new JSONArray();
                        jsonArray.put(1);
                        jsonObject.put("msgtypes", jsonArray);
                        jsonObject.put("msgfilters", new JSONArray());
                        String result = wToolSDK.startMessageListener(jsonObject.toString());
                        jsonObject = new JSONObject(result);
                        if(jsonObject.getInt("result")==0) {
                            buttonStartMessage.setTag(1);
                            buttonStartMessage.setText("停止监听消息");
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "err", e);
                    }
                }
                else
                {
                    wToolSDK.stopMessageListener();
                    buttonStartMessage.setTag(0);
                    buttonStartMessage.setText("监听消息");
                }
            }
        });
    }

    private void parseResult(String result)
    {
        String text = "";
        try {
            JSONObject jsonObject = new JSONObject(result);
            if(jsonObject.getInt("result")==0)
            {
                text = "操作成功";
            }
            else
            {
                text = jsonObject.getString("errmsg");
            }
        }
        catch (Exception e)
        {
            text = "解析结果失败";
        }
        Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
    }


}
