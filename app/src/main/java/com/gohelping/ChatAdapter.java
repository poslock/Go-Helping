package com.gohelping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages;
    private String currentUserId;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);

        // Pesan sistem
        if ("system".equals(message.getType())) {
            return 2;
        }

        // Pesan dari kita
        if (currentUserId != null &&
                message.getSenderId() != null &&
                message.getSenderId().equals(currentUserId)) {
            return 1;
        }

        // Pesan dari orang lain
        return 0;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout;
        if (viewType == 2) {
            layout = R.layout.item_chat_system; // Pesan sistem
        } else if (viewType == 1) {
            layout = R.layout.item_chat_out; // Kanan (kita)
        } else {
            layout = R.layout.item_chat_in; // Kiri (orang lain)
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        if (holder.tvMessage != null) {
            holder.tvMessage.setText(message.getMessage());
        }

        if (holder.tvTime != null && message.getTimestamp() != null) {
            Date date = message.getTimestamp().toDate();
            holder.tvTime.setText(timeFormat.format(date));
        }

        if (holder.tvSenderName != null && message.getSenderName() != null) {
            holder.tvSenderName.setText(message.getSenderName());
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages != null ? chatMessages.size() : 0;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvSenderName;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvChatMessage);
            tvTime = itemView.findViewById(R.id.tvChatTime);
            tvSenderName = itemView.findViewById(R.id.tvChatSenderName);
        }
    }
}