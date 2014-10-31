package irc.simple.valentinshelkov.simpleircclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.commons.lang.RandomStringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import irc.simple.valentinshelkov.simpleircclient.adapter.ChatAdapter;
import irc.simple.valentinshelkov.simpleircclient.client.ChatClient;
import irc.simple.valentinshelkov.simpleircclient.data.MessageData;
import irc.simple.valentinshelkov.simpleircclient.db.ChatDBHelper;
import irc.simple.valentinshelkov.simpleircclient.db.DictionaryHelper;
import irc.simple.valentinshelkov.simpleircclient.server.SimpleChatServer;


public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int RANDOM_MESSAGE_SEND_INTERVAL_SECONDS = 3;
    private final ScheduledExecutorService randomMessageSender = Executors.newSingleThreadScheduledExecutor();
    private final ServiceConnection serverConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serverMessenger = new Messenger(service);
            Message message = Message.obtain();
            message.what = SimpleChatServer.START_SERVER;
            message.replyTo = new Messenger(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case SimpleChatServer.START_SERVER:
                            serverStartCallback();
                            break;
                    }
                }
            });
            try {
                serverMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serverMessenger = null;
        }
    };
    private DictionaryHelper dictDBHelper;

    private void serverStartCallback() {
        realClient = new ChatClient(handler);
        realClient.init();
        botClient = new ChatClient(null);
        botClient.init();
        randomMessageSender.scheduleAtFixedRate(new SendRandomMessage(this), 0, RANDOM_MESSAGE_SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private Intent serverBindIntent;
    private Messenger serverMessenger;
    private ChatDBHelper chatDBHelper;
    private ChatAdapter adapter;
    private ListView messagesList;
    private Button buttonSend;
    private EditText inputField;
    private ChatClient realClient, botClient;
    private int currentPosition;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ChatClient.INCOMING_MESSAGE:
                    boolean scroll = currentPosition == adapter.getCount() - 1;
                    chatDBHelper.saveMessage((MessageData) msg.obj);
                    adapter.add((MessageData) msg.obj);
                    adapter.notifyDataSetChanged();
                    if (scroll) {
                        currentPosition = adapter.getCount() - 1;
                        messagesList.setSelection(currentPosition);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(TAG, "DIR " + getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath());
        serverBindIntent = new Intent(this, SimpleChatServer.class);
        bindService(serverBindIntent, serverConnection, BIND_ABOVE_CLIENT);
        startService(serverBindIntent);
        chatDBHelper = new ChatDBHelper(this);
        dictDBHelper = new DictionaryHelper(this);
        dictDBHelper.init();
        initUI();
    }

    private void initUI() {
        List<MessageData> messages = chatDBHelper.getMessages();
        adapter = new ChatAdapter(this, messages);
        messagesList = (ListView) findViewById(R.id.messagesList);
        messagesList.setAdapter(adapter);
        currentPosition = adapter.getCount() - 1;
        messagesList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    currentPosition = view.getLastVisiblePosition();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //Ignored
            }
        });
        messagesList.setSelection(currentPosition);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        inputField = (EditText) findViewById(R.id.inputField);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputField.getText().length() > 0) {
                    getRealClient().postMessage(inputField.getText().toString());
                    inputField.getText().clear();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        try {
            if (serverMessenger != null) {
                Message m = Message.obtain();
                m.what = SimpleChatServer.STOP_SERVER;
                serverMessenger.send(m);
            }
            unbindService(serverConnection);
            stopService(serverBindIntent);
            realClient.release();
            botClient.release();
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        } finally {
            super.onDestroy();
        }
    }

    public ChatClient getRealClient() {
        return realClient;
    }

    public ChatClient getBotClient() {
        return botClient;
    }

    private static class SendRandomMessage implements Runnable {
        private final MainActivity activity;

        private SendRandomMessage(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        public void run() {
            Log.w(TAG, "loop");
            Random random = new Random();
            //Get random words count from 1 to 25
            int wordsCount = 1 + random.nextInt(24);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < wordsCount; i++) {
                int rInt = random.nextInt(50);
                // Random paste #awesome_app hash tag
                switch (rInt) {
                    case 8:
                        builder.append(ChatAdapter.AWESOME_APP);
                        break;
                    case 44:
                        builder.append(ChatAdapter.AWESOME_APP);
                        break;
                    case 35:
                        builder.append(ChatAdapter.AWESOME_APP);
                        break;
                    case 17:
                        builder.append(ChatAdapter.AWESOME_APP);
                        break;
                    case 26:
                        builder.append(ChatAdapter.AWESOME_APP);
                        break;
                    default:
                        builder.append(" ");
                        builder.append("#");
                        builder.append(RandomStringUtils.randomAlphanumeric(1+rInt/3));
                        builder.append(" ");
                        break;
                }
                // Get random id
                long id = Math.abs(random.nextLong() % activity.dictDBHelper.getMaxID());
                Log.w(TAG, "id = " + id);
                String word = activity.dictDBHelper.getWord(id);
                builder.append(word);
                builder.append(" ");
                Log.w(TAG, "loop");
            }
            activity.getBotClient().postMessage(builder.toString());
        }
    }
}
