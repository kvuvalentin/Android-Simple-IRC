package irc.simple.valentinshelkov.simpleircclient.server;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import irc.simple.valentinshelkov.simpleircclient.data.ClientInfo;
import irc.simple.valentinshelkov.simpleircclient.listener.ClientListener;
import irc.simple.valentinshelkov.simpleircclient.utils.ClientSender;
import irc.simple.valentinshelkov.simpleircclient.utils.ServerDispatcher;

public class SimpleChatServer extends Service {
    private static final String TAG = SimpleChatServer.class.getSimpleName();
    public static final int START_SERVER = 1;
    public static final int STOP_SERVER = 2;
    public static final int LISTENING_PORT = 2002;
    private final ScheduledExecutorService serverThreadPool = Executors.newSingleThreadScheduledExecutor();
    private final Messenger messenger = new Messenger(new ServerHandler(this));

    public void startServer() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = Executors.newFixedThreadPool(1).submit(new Callable<ServerSocket>() {
                @Override
                public ServerSocket call() throws Exception {
                    ServerSocket serverSocket = null;
                    try {
                        serverSocket = new ServerSocket(LISTENING_PORT, 50, Inet4Address.getLocalHost());
                    } catch (IOException se) {
                        return null;
                    }
                    return serverSocket;
                }
            }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        ServerDispatcher serverDispatcher = new ServerDispatcher();
        serverDispatcher.start();
        serverThreadPool.scheduleAtFixedRate(new ServerTask(serverSocket, serverDispatcher), 1, 1, TimeUnit.MILLISECONDS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private static class ServerHandler extends Handler {
        private final SimpleChatServer service;

        private ServerHandler(SimpleChatServer service) {
            this.service = service;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_SERVER:
                    Log.w(TAG, "START_SERVER");
                    service.startServer();
                    if (msg.replyTo != null) {
                        Message m = Message.obtain();
                        m.what = START_SERVER;
                        try {
                            msg.replyTo.send(m);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case STOP_SERVER:
                    service.serverThreadPool.shutdownNow();
                    if (msg.replyTo != null) {
                        Message m = Message.obtain();
                        m.what = STOP_SERVER;
                        try {
                            msg.replyTo.send(m);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    private static class ServerTask implements Runnable {
        private static final String TAG = ServerTask.class.getSimpleName();
        private ServerSocket serverSocket;
        private ServerDispatcher serverDispatcher;

        public ServerTask(ServerSocket serverSocket, ServerDispatcher serverDispatcher) {
            this.serverSocket = serverSocket;
            this.serverDispatcher = serverDispatcher;
        }

        public void run() {
            try {
                Socket socket = serverSocket.accept();
                ClientInfo clientInfo = new ClientInfo();
                clientInfo.setSocket(socket);
                ClientListener clientListener = new ClientListener(clientInfo, serverDispatcher);
                clientListener.init();
                ClientSender clientSender = new ClientSender(clientInfo, serverDispatcher);
                clientSender.init();
                clientInfo.setClientListener(clientListener);
                clientInfo.setClientSender(clientSender);
                clientListener.start();
                clientSender.start();
                serverDispatcher.addClient(clientInfo);
            } catch (IOException e) {
                Log.e(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}