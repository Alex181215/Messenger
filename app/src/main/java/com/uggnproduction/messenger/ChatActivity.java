package com.uggnproduction.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatActivity — безопасная версия с динамическим расширением панели ввода,
 * автопрокруткой EditText при превышении лимита строк и корректировкой
 * paddingBottom у RecyclerView.
 */
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

    // Поведение composer
    private View composer;
    private int lastComposerHeight = 0;
    private final int MAX_VISIBLE_LINES = 6; // показываем до 6 полных строк
    private final float HALF_LINE = 0.5f;    // дополнительная "половинка" сверху (6.5 визуально)

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
                getSupportActionBar().setTitle("");
            }
            toolbar.setNavigationOnClickListener(v -> finish());

            // Кастомные view внутри Toolbar — используем реальные id из xml
            imgAvatar = toolbar.findViewById(R.id.img_avatar_small);
            tvName = toolbar.findViewById(R.id.tv_title);
            tvStatus = toolbar.findViewById(R.id.tv_subtitle);

            Log.d(TAG, "Toolbar views: imgAvatar=" + imgAvatar + ", tvName=" + tvName + ", tvStatus=" + tvStatus);

            if (tvName != null) tvName.setText(chatName != null ? chatName : "Имя контакта");
            if (tvStatus != null) tvStatus.setText("в сети");
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
            rvMessages.setClipToPadding(false); // важно, чтобы последний элемент был виден над composer
            Log.d(TAG, "RecyclerView инициализирован");
        } else {
            Log.e(TAG, "RecyclerView не найден!");
        }

        // Панель ввода
        composer = findViewById(R.id.composer);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);

        // Защита
        if (etMessage == null) Log.e(TAG, "etMessage == null");
        if (composer == null) Log.e(TAG, "composer == null");

        // Инициализация поведения поля ввода:
        // - авто-расширение до MAX_VISIBLE_LINES + HALF_LINE
        // - после этого включается прокрутка внутри EditText
        if (etMessage != null) {
            // базовые настройки
            etMessage.setHorizontallyScrolling(false);
            etMessage.setMaxLines(12); // абсолютный предел, чтобы не раздувать слишком сильно

            // Отслеживаем изменения текста, чтобы регулировать высоту/скролл
            etMessage.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { /* не нужно */ }

                @Override
                public void afterTextChanged(Editable s) {
                    // post чтобы гарантировать, что layout уже обновился и getLineCount даст корректное значение
                    etMessage.post(() -> adjustEditTextHeightAndScrolling());
                }
            });

            // Когда пользователь скроллит внутри EditText, чтобы не переключался scroll на RecyclerView
            etMessage.setOnTouchListener((v, event) -> {
                // Если текст больше видимой области — не даём родителю перехватывать касание
                if (etMessage.getLineCount() > MAX_VISIBLE_LINES) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    // при ACTION_UP/_CANCEL отдаём обратно
                    if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                return false; // позволяем EditText обрабатывать прикосновение (включая прокрутку)
            });
        }

        // Кнопка отправки
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> sendMessage());
        } else {
            Log.e(TAG, "Кнопка отправки не найдена!");
        }

        // Слушаем изменение layout, чтобы синхронизировать padding RecyclerView с высотой composer
        if (composer != null && rvMessages != null) {
            composer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int h = composer.getHeight();
                    if (h != lastComposerHeight) {
                        lastComposerHeight = h;
                        // резервируем небольшой дополнительный отступ (8dp)
                        final int extraPx = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
                        rvMessages.setPadding(rvMessages.getPaddingLeft(),
                                rvMessages.getPaddingTop(),
                                rvMessages.getPaddingRight(),
                                h + extraPx);
                    }
                }
            });
        }
    }

    /**
     * Корректирует высоту EditText / включает внутреннюю прокрутку при превышении лимита.
     * Логика:
     *  - если строк <= MAX_VISIBLE_LINES: EditText растёт естественно (wrap_content)
     *  - если строк > MAX_VISIBLE_LINES: устанавливаем maxHeight = lineHeight * (MAX_VISIBLE_LINES + HALF_LINE) и включаем прокрутку
     */
    private void adjustEditTextHeightAndScrolling() {
        if (etMessage == null) return;

        int lineCount = etMessage.getLineCount();
        int lineHeight = etMessage.getLineHeight(); // px
        Log.d(TAG, "adjustEditTextHeightAndScrolling: lineCount=" + lineCount + " lineHeight=" + lineHeight);

        // вычисляем высоту в пикселях для MAX_VISIBLE_LINES + HALF_LINE
        float visibleLines = MAX_VISIBLE_LINES + HALF_LINE; // 6.5
        int maxVisibleHeight = (int) (visibleLines * lineHeight);

        if (lineCount <= MAX_VISIBLE_LINES) {
            // даём EditText занимать высоту по содержимому
            etMessage.setMaxHeight(Integer.MAX_VALUE);
            etMessage.setScroller(null);
            etMessage.setVerticalScrollBarEnabled(false);
            etMessage.setMovementMethod(null);
            // высоту оставим wrap_content — поэтому не трогаем layout params
        } else {
            // ограничиваем по высоте и включаем прокрутку внутри EditText
            etMessage.setMaxHeight(maxVisibleHeight);
            etMessage.setScroller(new Scroller(this));
            etMessage.setVerticalScrollBarEnabled(true);
            // enable scrolling movement to allow programmatic scroll if needed
            etMessage.setMovementMethod(new ScrollingMovementMethod());
        }

        // Обновим padding у RecyclerView (иногда composer изменился)
        if (composer != null && rvMessages != null) {
            int h = composer.getHeight();
            final int extraPx = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
            rvMessages.setPadding(rvMessages.getPaddingLeft(),
                    rvMessages.getPaddingTop(),
                    rvMessages.getPaddingRight(),
                    h + extraPx);
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

        // Очищаем поле ввода и вызываем корректировку (в т.ч. чтобы composer мог уменьшиться)
        etMessage.setText("");
        etMessage.post(this::adjustEditTextHeightAndScrolling);
        Log.d(TAG, "Отправлено сообщение: " + text);
    }
}
