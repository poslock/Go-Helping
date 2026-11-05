package com.gohelping;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private Button btnBack, btnCompleteQuest;
    private TextView tvChatTitle, tvQuestInfo;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUserId;
    private String currentUserName;
    private String questId;
    private String chatId;
    private String receiverId;
    private String receiverName;
    private String questStatus;
    private String creatorId;
    private String helperId;
    private int questReward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Ambil data dari intent
        questId = getIntent().getStringExtra("QUEST_ID");
        chatId = getIntent().getStringExtra("CHAT_ID");
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        receiverName = getIntent().getStringExtra("RECEIVER_NAME");

        // Inisialisasi views
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        btnCompleteQuest = findViewById(R.id.btnCompleteQuest);
        tvChatTitle = findViewById(R.id.tvChatTitle);
        tvQuestInfo = findViewById(R.id.tvQuestInfo);

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            currentUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            if (currentUserName == null) currentUserName = "User";
        } else {
            Toast.makeText(this, "Anda harus login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set title
        if (tvChatTitle != null) {
            tvChatTitle.setText(receiverName != null ? receiverName : "Chat");
        }

        chatAdapter = new ChatAdapter(chatMessages);
        rvChat.setAdapter(chatAdapter);

        // Load quest info dan setup UI
        loadQuestInfo();

        // Setup chat room jika belum ada
        setupChatRoom();

        // Load messages
        loadMessages();

        // Send button
        btnSend.setOnClickListener(v -> sendMessage());

        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Complete quest button
        if (btnCompleteQuest != null) {
            btnCompleteQuest.setOnClickListener(v -> showCompleteQuestDialog());
        }
    }

    private void loadQuestInfo() {
        if (questId == null) return;

        db.collection("quests")
                .document(questId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null || documentSnapshot == null || !documentSnapshot.exists()) {
                        return;
                    }

                    creatorId = documentSnapshot.getString("creatorId");
                    helperId = documentSnapshot.getString("helperId");
                    questStatus = documentSnapshot.getString("status");
                    Long reward = documentSnapshot.getLong("reward");
                    questReward = reward != null ? reward.intValue() : 0;
                    String questTitle = documentSnapshot.getString("title");

                    // Update UI berdasarkan status
                    updateQuestUI(questTitle);

                    // Update info quest
                    if (tvQuestInfo != null) {
                        String info = "Quest: " + (questTitle != null ? questTitle : "Unknown") +
                                " | Reward: " + questReward + " GD";
                        tvQuestInfo.setText(info);
                    }
                });
    }

    private void updateQuestUI(String questTitle) {
        if (btnCompleteQuest == null) return;

        // Hanya creator yang bisa menyelesaikan quest
        boolean isCreator = currentUserId.equals(creatorId);
        boolean isTaken = "Taken".equals(questStatus);
        boolean isCompleted = "Completed".equals(questStatus);

        if (isCompleted) {
            btnCompleteQuest.setVisibility(View.GONE);
            etMessage.setEnabled(false);
            btnSend.setEnabled(false);
            etMessage.setHint("Quest sudah selesai");
        } else if (isCreator && isTaken) {
            btnCompleteQuest.setVisibility(View.VISIBLE);
            btnCompleteQuest.setText("✅ Selesaikan Quest");
            btnCompleteQuest.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            btnCompleteQuest.setVisibility(View.GONE);
        }
    }

    private void setupChatRoom() {
        if (chatId == null || questId == null) {
            Toast.makeText(this, "Data chat tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cek apakah chat room sudah ada
        db.collection("chat_rooms")
                .document(chatId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Buat chat room baru
                        createChatRoom();
                    }
                });
    }

    private void createChatRoom() {
        Map<String, Object> chatRoom = new HashMap<>();
        chatRoom.put("roomId", chatId);
        chatRoom.put("questId", questId);
        chatRoom.put("senderId", currentUserId);
        chatRoom.put("receiverId", receiverId);
        chatRoom.put("senderName", currentUserName);
        chatRoom.put("receiverName", receiverName != null ? receiverName : "Unknown");
        chatRoom.put("lastMessage", "");
        chatRoom.put("lastTimestamp", Timestamp.now());
        chatRoom.put("createdAt", Timestamp.now());

        db.collection("chat_rooms")
                .document(chatId)
                .set(chatRoom)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Chat", "Chat room created");
                    // Kirim pesan otomatis
                    sendSystemMessage("Chat dimulai. Silakan diskusikan detail quest.");
                })
                .addOnFailureListener(e -> Log.e("Chat", "Error creating chat room", e));
    }

    private void sendSystemMessage(String messageText) {
        Map<String, Object> message = new HashMap<>();
        message.put("message", messageText);
        message.put("senderId", "system");
        message.put("senderName", "System");
        message.put("timestamp", Timestamp.now());
        message.put("type", "system");

        db.collection("chat_rooms")
                .document(chatId)
                .collection("messages")
                .add(message);
    }

    private void loadMessages() {
        if (chatId == null) return;

        db.collection("chat_rooms")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    chatMessages.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        chatMessages.add(message);
                    }
                    chatAdapter.notifyDataSetChanged();

                    // Scroll ke bawah
                    if (!chatMessages.isEmpty()) {
                        rvChat.smoothScrollToPosition(chatMessages.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty() || chatId == null) return;

        if ("Completed".equals(questStatus)) {
            Toast.makeText(this, "Quest sudah selesai, tidak bisa mengirim pesan lagi", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("message", messageText);
        message.put("senderId", currentUserId);
        message.put("senderName", currentUserName);
        message.put("timestamp", Timestamp.now());
        message.put("type", "message");

        db.collection("chat_rooms")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    etMessage.setText("");

                    // Update last message di chat room
                    db.collection("chat_rooms")
                            .document(chatId)
                            .update(
                                    "lastMessage", messageText,
                                    "lastTimestamp", Timestamp.now()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal mengirim pesan", Toast.LENGTH_SHORT).show());
    }

    // ============ FITUR PENYELESAIAN QUEST ============

    private void showCompleteQuestDialog() {
        // Cek lagi apakah user adalah creator
        if (!currentUserId.equals(creatorId)) {
            Toast.makeText(this, "Hanya pembuat quest yang bisa menyelesaikan", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Selesaikan Quest")
                .setMessage("Apakah kamu yakin quest sudah selesai?\n\n" +
                        "Helper akan menerima " + questReward + " GD\n" +
                        "Status quest akan berubah menjadi Completed")
                .setPositiveButton("Ya, Selesaikan", (dialog, which) -> {
                    completeQuest();
                })
                .setNegativeButton("Batal", null)
                .setCancelable(false)
                .show();
    }

    private void completeQuest() {
        // Tampilkan loading
        btnCompleteQuest.setEnabled(false);
        btnCompleteQuest.setText("Memproses...");

        // Update quest status
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Completed");
        updates.put("completed", true);
        updates.put("completedAt", Timestamp.now());

        db.collection("quests")
                .document(questId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Berikan poin ke helper
                    giveRewardToHelper();
                })
                .addOnFailureListener(e -> {
                    btnCompleteQuest.setEnabled(true);
                    btnCompleteQuest.setText("✅ Selesaikan Quest");
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void giveRewardToHelper() {
        if (helperId == null) {
            sendCompletionMessage();
            return;
        }

        // Ambil poin helper saat ini
        db.collection("users")
                .document(helperId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    long currentPoints = 0;
                    if (documentSnapshot.exists() && documentSnapshot.contains("points")) {
                        Long points = documentSnapshot.getLong("points");
                        currentPoints = points != null ? points : 0;
                    }

                    long newPoints = currentPoints + questReward;

                    // Update poin helper
                    Map<String, Object> pointUpdate = new HashMap<>();
                    pointUpdate.put("points", newPoints);
                    pointUpdate.put("totalEarned", newPoints);

                    db.collection("users")
                            .document(helperId)
                            .update(pointUpdate)
                            .addOnSuccessListener(aVoid -> {
                                // Tambahkan ke history transaksi
                                addTransactionHistory(helperId, questReward);

                                // Kirim pesan sistem
                                sendSystemMessage("🎉 Quest selesai! Helper menerima " + questReward + " GD");

                                Toast.makeText(ChatActivity.this,
                                        "✅ Quest selesai! Helper menerima " + questReward + " GD",
                                        Toast.LENGTH_LONG).show();

                                // Update UI
                                questStatus = "Completed";
                                updateQuestUI(null);
                                etMessage.setEnabled(false);
                                btnSend.setEnabled(false);
                                etMessage.setHint("Quest sudah selesai");

                                // Kembali setelah 3 detik
                                new android.os.Handler().postDelayed(() -> finish(), 3000);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Gagal memberikan poin: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                btnCompleteQuest.setEnabled(true);
                                btnCompleteQuest.setText("✅ Selesaikan Quest");
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat data user", Toast.LENGTH_SHORT).show();
                    btnCompleteQuest.setEnabled(true);
                    btnCompleteQuest.setText("✅ Selesaikan Quest");
                });
    }

    private void addTransactionHistory(String userId, int amount) {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("userId", userId);
        transaction.put("amount", amount);
        transaction.put("type", "Quest Reward");
        transaction.put("questId", questId);
        transaction.put("timestamp", Timestamp.now());
        transaction.put("description", "Pembayaran quest: " + questId);

        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(documentReference ->
                        Log.d("Transaction", "Transaction recorded: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.e("Transaction", "Error recording transaction", e));
    }

    private void sendCompletionMessage() {
        // Kirim pesan sistem
        sendSystemMessage("🎉 Quest telah diselesaikan!");

        // Update UI
        questStatus = "Completed";
        updateQuestUI(null);
        etMessage.setEnabled(false);
        btnSend.setEnabled(false);
        etMessage.setHint("Quest sudah selesai");
        btnCompleteQuest.setVisibility(View.GONE);

        Toast.makeText(this, "✅ Quest selesai!", Toast.LENGTH_LONG).show();

        new android.os.Handler().postDelayed(() -> finish(), 3000);
    }
}