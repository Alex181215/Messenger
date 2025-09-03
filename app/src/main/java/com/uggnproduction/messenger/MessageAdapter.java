package com.uggnproduction.messenger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_INCOMING = 0;
    private static final int TYPE_OUTGOING = 1;

    private final List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        return msg.isSentByMe() ? TYPE_OUTGOING : TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_OUTGOING) {
            View v = inflater.inflate(R.layout.item_message_outgoing, parent, false);
            return new OutgoingViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_message_incoming, parent, false);
            return new IncomingViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof OutgoingViewHolder) {
            ((OutgoingViewHolder) holder).bind(message);
        } else if (holder instanceof IncomingViewHolder) {
            ((IncomingViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    // ViewHolder для исходящих сообщений (использует tv_text, tv_time)
    static class OutgoingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvText;
        private final TextView tvTime;

        OutgoingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_text);   // из твоего item_message_outgoing.xml
            tvTime = itemView.findViewById(R.id.tv_time);   // из твоего item_message_outgoing.xml
        }

        void bind(Message message) {
            tvText.setText(message.getText());
            tvTime.setText(formatTime(message.getTimestamp()));
        }
    }

    // ViewHolder для входящих сообщений (использует text_message, text_time)
    static class IncomingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvText;
        private final TextView tvTime;

        IncomingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_text); // из твоего item_message_incoming.xml
            tvTime = itemView.findViewById(R.id.tv_time);    // из твоего item_message_incoming.xml
        }

        void bind(Message message) {
            tvText.setText(message.getText());
            tvTime.setText(formatTime(message.getTimestamp()));
        }
    }

    private static String formatTime(long timestamp) {
        try {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
        } catch (Exception e) {
            return "";
        }
    }
}
