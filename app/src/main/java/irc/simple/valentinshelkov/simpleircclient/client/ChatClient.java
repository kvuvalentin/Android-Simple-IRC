package irc.simple.valentinshelkov.simpleircclient.client;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import irc.simple.valentinshelkov.simpleircclient.data.MessageData;
import irc.simple.valentinshelkov.simpleircclient.server.SimpleChatServer;

public class ChatClient {
    public static final int INCOMING_MESSAGE = 1;
    private static final String TAG = ChatClient.class.getSimpleName();
    private final ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<String>();
    private final ScheduledExecutorService senderThreadPool = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService receiverThreadPool = Executors.newSingleThreadScheduledExecutor();
    private final Handler handler;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;

    public ChatClient(Handler handler) {
        this.handler = handler;
    }

    public void postMessage(String message) {
        messages.add(message);
    }

    public void init() {
        ExecutorService tmp = Executors.newCachedThreadPool();
        Future<Socket> sf = tmp.submit(new Callable<Socket>() {
            @Override
            public Socket call() throws Exception {
                return new Socket(InetAddress.getLocalHost(), SimpleChatServer.LISTENING_PORT);
            }
        });
        socket = null;
        try {
            // Connect to Chat Server
            socket = sf.get();
            in = new BufferedReader(new InputStreamReader(new BufferedInputStream(socket.getInputStream(), 1024)));
            out = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream(), 1024)));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        }
        if (socket == null) return;
        // Create and start Sender thread
        Sender sender = new Sender(out, messages);
        senderThreadPool.scheduleAtFixedRate(sender, 0, 100, TimeUnit.MILLISECONDS);
        receiverThreadPool.scheduleAtFixedRate(new Receiver(in, handler, socket), 0, 100, TimeUnit.MILLISECONDS);
    }

    public void release() {
        senderThreadPool.shutdownNow();
        receiverThreadPool.shutdownNow();
    }

    private static class Receiver implements Runnable {
        private final BufferedReader in;
        private final Handler handler;
        private final Socket socket;

        private Receiver(BufferedReader in, Handler handler, Socket socket) {
            this.in = in;
            this.handler = handler;
            this.socket = socket;
        }

        @Override
        public void run() {
            // Read messages from the server and print them
            try {
                String message = in.readLine();
                String[] tmp = message.split(":");
                int port = Integer.valueOf(tmp[1]);
                int color = port == socket.getLocalPort() ? Color.GREEN : Color.BLACK;
                int gravity = Gravity.LEFT;
                if (message != null && handler != null) {
                    Message m = Message.obtain();
                    m.what = INCOMING_MESSAGE;
                    MessageData messageData = new MessageData(message, gravity, color);
                    m.obj = messageData;
                    handler.sendMessage(m);
                }
            } catch (IOException e) {
                return;
            }

        }
    }

    private static class Sender implements Runnable {
        private final PrintWriter printWriter;
        private final ConcurrentLinkedQueue<String> messages;

        public Sender(PrintWriter aOut, ConcurrentLinkedQueue<String> messages) {
            printWriter = aOut;
            this.messages = messages;
        }

        public void run() {
            String message = messages.poll();
            if (message != null) {
                Log.w(TAG, String.valueOf(message));
                printWriter.println(message);
                printWriter.flush();
            }
        }

    }
}