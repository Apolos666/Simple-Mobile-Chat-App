package com.example.simplechatapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private HubConnection hubConnection;
    private EditText messageEditText;
    private Button sendButton;
    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        messageRecyclerView = findViewById(R.id.messageRecyclerView);

        chatMessages = new ArrayList<>();
        messageAdapter = new MessageAdapter(chatMessages);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageRecyclerView.setAdapter(messageAdapter);

        hubConnection = HubConnectionBuilder.create("http://10.0.2.2:5271/chathub").build();

        hubConnection.on("ReceiveMessage", (user, message) -> {
            runOnUiThread(() -> {
                chatMessages.add(new ChatMessage(user, message));
                messageAdapter.notifyItemInserted(chatMessages.size() - 1);
                messageRecyclerView.scrollToPosition(chatMessages.size() - 1);
            });
        }, String.class, String.class);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getText().toString().trim();
                if (!message.isEmpty()) {
                    hubConnection.send("SendMessage", "User", message);
                    messageEditText.setText("");
                }
            }
        });

        hubConnection.start().blockingAwait();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hubConnection.stop();
    }
}