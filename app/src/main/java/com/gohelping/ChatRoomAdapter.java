package com.gohelping;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.RoomViewHolder> {

    private List<ChatRoom> chatRooms;
    private String currentUserId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());

    public ChatRoomAdapter(List<ChatRoom> chatRooms) {
        this.chatRooms = chatRooms;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_room, parent, false);

        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        ChatRoom room = chatRooms.get(position);

        if (room == null) return;

        // Tentukan nama yang ditampilkan
        String displayName = "Unknown";
        String otherUserId = null;

        if (currentUserId != null && currentUserId.equals(room.getSenderId())) {
            // User adalah sender, tampilkan nama receiver
            if (room.getReceiverName() != null && !room.getReceiverName().isEmpty()) {
                displayName = room.getReceiverName();
            }
            otherUserId = room.getReceiverId();
        } else {
            // User adalah receiver, tampilkan nama sender
            if (room.getSenderName() != null && !room.getSenderName().isEmpty()) {
                displayName = room.getSenderName();
            }
            otherUserId = room.getSenderId();
        }

        holder.tvName.setText(displayName);

        // Tampilkan pesan terakhir
        String lastMsg = (room.getLastMessage() != null && !room.getLastMessage().isEmpty())
                ? room.getLastMessage()
                : "Belum ada pesan";
        holder.tvLastMessage.setText(lastMsg);

        // Tampilkan waktu
        if (room.getLastTimestamp() != null) {
            Date date = room.getLastTimestamp().toDate();
            holder.tvTime.setText(dateFormat.format(date));
        } else {
            holder.tvTime.setText("");
        }

        // 🔥 TAMBAHKAN CLICK LISTENER - Buka ChatActivity saat item diklik
        String finalOtherUserId = otherUserId;
        String finalDisplayName = displayName;
        holder.itemView.setOnClickListener(v -> {
            // Ambil questId dari chat room
            String questId = room.getQuestId();
            String chatId = room.getRoomId();

            if (chatId != null && questId != null) {
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                intent.putExtra("QUEST_ID", questId);
                intent.putExtra("CHAT_ID", chatId);
                intent.putExtra("RECEIVER_ID", finalOtherUserId);
                intent.putExtra("RECEIVER_NAME", finalDisplayName);
                v.getContext().startActivity(intent);
            }
        });

        // Optional: Tampilkan badge/unread indicator
        // holder.unreadBadge.setVisibility(room.hasUnread() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return (chatRooms != null) ? chatRooms.size() : 0;
    }

    public void updateData(List<ChatRoom> newChatRooms) {
        this.chatRooms = newChatRooms;
        notifyDataSetChanged();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}