package com.uggnproduction.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = "ChatListActivity";

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private RecyclerView rvChats;
    private FloatingActionButton fabNewChat;
    private NavigationView navView;
    private ActionBarDrawerToggle toggle;

    private ChatAdapter chatAdapter;
    private List<Chat> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: старт Activity");

        setContentView(R.layout.activity_chat_list);
        Log.d(TAG, "setContentView выполнен");

        // Привязка View
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        rvChats = findViewById(R.id.rv_chats);
        fabNewChat = findViewById(R.id.fab_new_chat);
        navView = findViewById(R.id.nav_view);

        Log.d(TAG, "Views найдены: drawer=" + (drawerLayout != null) +
                ", toolbar=" + (toolbar != null) +
                ", rvChats=" + (rvChats != null) +
                ", fab=" + (fabNewChat != null) +
                ", navView=" + (navView != null));

        // Настройка Toolbar и Drawer
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        Log.d(TAG, "Toolbar и Drawer настроены");

        // ---- Заполнение шапки меню ----
        View headerView = navView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.tv_name);
        TextView tvUsername = headerView.findViewById(R.id.tv_username);
        ImageView imgAvatar = headerView.findViewById(R.id.img_avatar);

        // Заглушка (потом подтянешь реальные данные)
        String userName = "Александр Иванов";
        String userUsername = "@alex";

        tvName.setText(userName);
        tvUsername.setText(userUsername);

        imgAvatar.setOnClickListener(v ->
                showToast("Открыть профиль")
        );


        // ---- Обработка кликов по пунктам меню ----
        navView.setNavigationItemSelectedListener(item -> {
            String message;
            if (item.getItemId() == R.id.nav_contacts) message = "Контакты";
            else if (item.getItemId() == R.id.nav_calls) message = "Звонки";
            else if (item.getItemId() == R.id.nav_favorites) message = "Избранное";
            else if (item.getItemId() == R.id.nav_settings) message = "Настройки";
            else message = "Неизвестный пункт";

            showToast(message);
            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        });

        // Отключаем выделение пунктов меню
        navView.setItemBackground(null);

        // FAB
        fabNewChat.setOnClickListener(v -> showToast("Создать новый чат"));

        // Моковые данные
        chatList = getMockChats();
        Log.d(TAG, "Mock data создано, size=" + chatList.size());

        // RecyclerView
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, chatList, chat -> {
            Log.d(TAG, "Чат кликнут: " + chat.getName());
            // Запуск ChatActivity
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("chatId", chat.getId());
            intent.putExtra("chatName", chat.getName());
            intent.putExtra("chatAvatar", chat.getAvatarUrl());
            startActivity(intent);
        });
        rvChats.setAdapter(chatAdapter);

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showToast(String text) {
        Log.d(TAG, "showToast: " + text);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private List<Chat> getMockChats() {
        List<Chat> chats = new ArrayList<>();
        chats.add(new Chat(1, "Алиса", "Привет! Как дела?", "12:45", "https://example.com/avatar1.png", 2));
        chats.add(new Chat(2, "Работа", "Не забудь про митинг", "11:20", "https://example.com/avatar2.png", 0));
        chats.add(new Chat(3, "Мама", "Позвони вечером", "Вчера", "https://example.com/avatar3.png", 1));
        chats.add(new Chat(4, "Друзья", "Сегодня в 7 вечера!", "Пн", "https://example.com/avatar4.png", 0));
        chats.add(new Chat(5, "Telegram", "Код подтверждения: 12345", "Сб", "https://example.com/avatar5.png", 0));
        return chats;
    }
}
