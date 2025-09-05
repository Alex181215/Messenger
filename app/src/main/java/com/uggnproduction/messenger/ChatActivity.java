package com.uggnproduction.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Scroller;
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
import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private ShapeableImageView imgAvatar;
    private TextView tvName, tvStatus;

    private View composer;
    private int lastComposerHeight = 0;
    private final int MAX_VISIBLE_LINES = 6;
    private final float HALF_LINE = 0.5f;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        imgAvatar = toolbar.findViewById(R.id.img_avatar_small);
        tvName = toolbar.findViewById(R.id.tv_title);
        tvStatus = toolbar.findViewById(R.id.tv_subtitle);

        Intent intent = getIntent();
        String chatName = intent.getStringExtra("chatName");
        String chatAvatar = intent.getStringExtra("chatAvatar");

        tvName.setText(chatName != null ? chatName : "Имя контакта");
        tvStatus.setText("в сети");

        if (chatAvatar != null && !chatAvatar.isEmpty()) {
            Glide.with(this)
                    .load(chatAvatar)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }

        // RecyclerView и Composer
        rvMessages = findViewById(R.id.rv_messages);
        composer = findViewById(R.id.composer);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);

        // Настройка LayoutManager
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setClipToPadding(false);

        // Данные и адаптер
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        rvMessages.setAdapter(messageAdapter);

        // Настройка EditText
        etMessage.setHorizontallyScrolling(false);
        etMessage.setMaxLines(12);
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                etMessage.post(ChatActivity.this::adjustEditTextHeight);
            }
        });

        etMessage.setOnTouchListener((v, event) -> {
            if (etMessage.getLineCount() > MAX_VISIBLE_LINES) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                    v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });

        btnSend.setOnClickListener(v -> sendMessage());

        // Обновляем padding RecyclerView и скроллим к последнему сообщению только при изменении высоты composer
        composer.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int h = composer.getHeight();
            if (h != lastComposerHeight) {
                lastComposerHeight = h;
                int offsetPx = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                rvMessages.setPadding(rvMessages.getPaddingLeft(),
                        rvMessages.getPaddingTop(),
                        rvMessages.getPaddingRight(),
                        h + offsetPx);
                rvMessages.post(() -> scrollToBottom(false));
            }
        });

        // Скролл к последнему сообщению при старте
        rvMessages.post(() -> scrollToBottom(false));

        // Обработка клавиатуры
        final View rootView = findViewById(R.id.coordinator);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int screenHeight = rootView.getRootView().getHeight();
            int[] location = new int[2];
            rootView.getLocationOnScreen(location);
            int keypadHeight = screenHeight - (location[1] + rootView.getHeight());
            int offsetPx = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);

            if (keypadHeight > 150) { // клавиатура открыта
                rvMessages.setPadding(rvMessages.getPaddingLeft(),
                        rvMessages.getPaddingTop(),
                        rvMessages.getPaddingRight(),
                        lastComposerHeight + offsetPx + keypadHeight);
            } else { // клавиатура закрыта
                rvMessages.setPadding(rvMessages.getPaddingLeft(),
                        rvMessages.getPaddingTop(),
                        rvMessages.getPaddingRight(),
                        lastComposerHeight + offsetPx);
            }
        });
    }

    // Корректная настройка высоты EditText без дерганья RecyclerView
    private void adjustEditTextHeight() {
        int lineCount = etMessage.getLineCount();
        int lineHeight = etMessage.getLineHeight();
        int maxVisibleHeight = (int)((MAX_VISIBLE_LINES + HALF_LINE) * lineHeight);

        if (lineCount <= MAX_VISIBLE_LINES) {
            etMessage.setMaxHeight(Integer.MAX_VALUE);
            etMessage.setScroller(null);
            etMessage.setVerticalScrollBarEnabled(false);
            etMessage.setMovementMethod(null);
        } else {
            etMessage.setMaxHeight(maxVisibleHeight);
            etMessage.setScroller(new Scroller(this));
            etMessage.setVerticalScrollBarEnabled(true);
            etMessage.setMovementMethod(new ScrollingMovementMethod());
        }
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        Message newMessage = new Message(text, true, System.currentTimeMillis());
        messageList.add(newMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);

        etMessage.setText("");
        etMessage.post(this::adjustEditTextHeight);
        rvMessages.post(() -> scrollToBottom(false));

        // Ответ-бот
        rvMessages.postDelayed(() -> {
            String[] replies = {
                    "Привет 👋", "Как дела? 🤔", "Хорошо, спасибо! 😄","л",
                    "Интересно 🤔", "Да, согласен 👍", "Сейчас занят, отпишусь позже ⏳"
            };
            Random random = new Random();
            Message reply = new Message(replies[random.nextInt(replies.length)], false, System.currentTimeMillis());
            messageList.add(reply);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            rvMessages.post(() -> scrollToBottom(false));
        }, 1000);
    }

    // Плавный скролл к последнему сообщению
    private void scrollToBottom(boolean smooth) {
        if (messageList == null || messageList.isEmpty()) return;
        int lastIndex = messageList.size() - 1;
        if (smooth) {
            rvMessages.smoothScrollToPosition(lastIndex);
        } else {
            layoutManager.scrollToPositionWithOffset(lastIndex, 0);
        }
    }
}
