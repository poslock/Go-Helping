package com.gohelping;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView rvChatRoom;
    private ChatRoomAdapter adapter;
    private List<ChatRoom> chatRooms = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;
    private LinearLayout emptyStateLayout;
    private TextView tvEmptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        rvChatRoom = findViewById(R.id.rvChatRoom);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvEmptyText = findViewById(R.id.tvEmptyText);

        if (rvChatRoom == null) {
            Log.e("ChatRoomActivity", "RecyclerView not found!");
            return;
        }

        rvChatRoom.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Log.e("ChatRoomActivity", "User not logged in");
            return;
        }

        adapter = new ChatRoomAdapter(chatRooms);
        rvChatRoom.setAdapter(adapter);

        loadChatRooms();
    }

    private void loadChatRooms() {
        if (currentUserId == null) return;

        db.collection("chat_rooms")
                .orderBy("lastTimestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ChatRoomActivity", "Error: " + error.getMessage());
                        return;
                    }

                    if (value == null) return;

                    chatRooms.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            ChatRoom room = doc.toObject(ChatRoom.class);

                            if (room.getSenderId() != null && room.getReceiverId() != null) {
                                if (room.getSenderId().equals(currentUserId)
                                        || room.getReceiverId().equals(currentUserId)) {
                                    chatRooms.add(room);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("ChatRoomActivity", "Error parsing: " + e.getMessage());
                        }
                    }

                    // Update UI
                    if (chatRooms.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        rvChatRoom.setVisibility(View.GONE);
                    } else {
                        emptyStateLayout.setVisibility(View.GONE);
                        rvChatRoom.setVisibility(View.VISIBLE);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data saat kembali ke activity ini
        if (adapter != null) {
            loadChatRooms();
        }
    }
}