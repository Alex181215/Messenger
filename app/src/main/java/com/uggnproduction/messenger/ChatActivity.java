package com.uggnproduction.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    // Заголовок Toolbar
    private ShapeableImageView imgAvatar;
    private TextView tvName, tvStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Log.d(TAG, "onCreate: старт Activity");

        // Получаем Intent-параметры
        Intent intent = getIntent();
        String chatId = intent.getStringExtra("chatId");
        String chatName = intent.getStringExtra("chatName");
        String chatAvatarUrl = intent.getStringExtra("chatAvatar");

        Log.d(TAG, "Intent: chatId=" + chatId + ", chatName=" + chatName + ", chatAvatarUrl=" + chatAvatarUrl);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            Log.e(TAG, "Toolbar не найден!");
        } else {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(""); // используем кастомный заголовок
            }

            toolbar.setNavigationOnClickListener(v -> finish());

            // Кастомные view внутри Toolbar
            imgAvatar = toolbar.findViewById(R.id.img_avatar_small);
            tvName = toolbar.findViewById(R.id.tv_title);
            tvStatus = toolbar.findViewById(R.id.tv_subtitle);

            Log.d(TAG, "Toolbar views: imgAvatar=" + imgAvatar + ", tvName=" + tvName + ", tvStatus=" + tvStatus);

            if (tvName != null) tvName.setText(chatName != null ? chatName : "Имя контакта");
            if (tvStatus != null) tvStatus.setText("в сети"); // заглушка
            if (imgAvatar != null) {
                if (chatAvatarUrl != null && !chatAvatarUrl.isEmpty()) {
                    Glide.with(this)
                            .load(chatAvatarUrl)
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .error(R.drawable.ic_avatar_placeholder)
                            .circleCrop()
                            .into(imgAvatar);
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                }
                imgAvatar.setOnClickListener(v -> Log.d(TAG, "Нажат аватар"));
            }
        }

        // RecyclerView
        rvMessages = findViewById(R.id.rv_messages);
        if (rvMessages != null) {
            rvMessages.setLayoutManager(new LinearLayoutManager(this));
            messageList = new ArrayList<>();
            messageAdapter = new MessageAdapter(messageList);
            rvMessages.setAdapter(messageAdapter);
            Log.d(TAG, "RecyclerView инициализирован");
        } else {
            Log.e(TAG, "RecyclerView не найден!");
        }

        // Панель ввода
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> sendMessage());
        } else {
            Log.e(TAG, "Кнопка отправки не найдена!");
        }
    }

    private void sendMessage() {
        if (etMessage == null || messageList == null || messageAdapter == null || rvMessages == null) {
            Log.e(TAG, "Невозможно отправить сообщение, не все элементы инициализированы");
            return;
        }

        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        // Создаем новое сообщение
        Message newMessage = new Message(text, true, System.currentTimeMillis());
        messageList.add(newMessage);

        // Обновляем список
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);

        // Очищаем поле ввода
        etMessage.setText("");

        Log.d(TAG, "Отправлено сообщение: " + text);
    }
}
