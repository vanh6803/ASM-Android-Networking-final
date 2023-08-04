package com.example.asmsocketserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.asmsocketserver.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String IP = "192.168.137.229";
    private static final int PORT = 8080;
    private ServerThread serverThread;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final int PERMISSION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showRequestPermissions();

        initUI();

    }

    private void showRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        }
    }

    private void initUI() {
        binding.tvIp.setText(IP);
        binding.tvPort.setText(PORT + "");
        binding.btnOpen.setOnClickListener(view -> {
            serverThread = new ServerThread();
            serverThread.startServer();
        });
    }

    //show notification
    private void showNotification(String message) {
        Notification notification = new NotificationCompat.Builder(this, Notify.CHANNEL_ID)
                .setContentTitle("A hava message from client")
                .setContentText("The number of images pushed to My Server is:" + message)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.i8))
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(getNotificationId(), notification);

    }

    private int getNotificationId(){
        return (int) new Date().getTime();
    }

    public class ServerThread extends Thread {

        private ServerSocket serverSocket;

        public void startServer() {
            start();
        }

        private List<ClientThread> clients = new ArrayList<>();

        @Override
        public void run() {
            super.run();
            try {
                serverSocket = new ServerSocket(PORT);
                handler.post(() -> {
                    binding.tvNotification.setText("Waiting for Clients");
                    binding.tvNotification.setTextColor(getColor(R.color.yellow));
                });

                Socket socket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(socket);
                clientThread.start();
                clients.add(clientThread);
                handler.post(() -> {
                    binding.tvNotification.setText("Connected to: " + socket.getInetAddress() + " : " + socket.getLocalPort() +" success!");
                    binding.tvNotification.setTextColor(getColor(R.color.green));
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public class ClientThread extends Thread {

        private final Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;

        private ClientThread(Socket socket) {
            this.clientSocket = socket;
            try {
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                output = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            super.run();
            try {
                String messageFormClient;
                while ((messageFormClient = input.readLine()) != null) {
                    String finalMessage = messageFormClient;
                    handler.post(() -> {
                        binding.tvMessage.setText("Number image push to my server " + finalMessage);
                        showNotification(finalMessage);
                    });

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}