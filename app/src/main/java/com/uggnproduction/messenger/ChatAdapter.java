package com.uggnproduction.messenger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    private Context context;
    private List<Chat> chatList;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(Context context, List<Chat> chatList, OnChatClickListener listener) {
        this.context = context;
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        holder.tvTitle.setText(chat.getName());
        holder.tvSubtitle.setText(chat.getLastMessage());
        holder.tvTime.setText(chat.getTime());

        // Загрузка аватарки через Glide
        Glide.with(context)
                .load(chat.getAvatarUrl())
                .placeholder(R.drawable.ic_avatar_placeholder) // обязательно добавить в res/drawable
                .error(R.drawable.ic_avatar_placeholder)
                .circleCrop()
                .into(holder.imgAvatar);

        // Badge непрочитанных сообщений
        if (chat.getUnreadCount() > 0) {
            holder.badgeUnread.setVisibility(View.VISIBLE);
            holder.badgeUnread.setText(String.valueOf(chat.getUnreadCount()));
        } else {
            holder.badgeUnread.setVisibility(View.GONE);
        }

        // Клик по элементу
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList != null ? chatList.size() : 0;
    }
}
