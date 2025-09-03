package com.uggnproduction.messenger;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ChatViewHolder extends RecyclerView.ViewHolder {

    ImageView imgAvatar;
    TextView tvTitle, tvSubtitle, tvTime, badgeUnread;

    public ChatViewHolder(View itemView) {
        super(itemView);
        imgAvatar = itemView.findViewById(R.id.img_avatar);
        tvTitle = itemView.findViewById(R.id.tv_title);
        tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
        tvTime = itemView.findViewById(R.id.tv_time);
        badgeUnread = itemView.findViewById(R.id.badge_unread);
    }
}
