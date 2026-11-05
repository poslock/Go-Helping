package com.gohelping;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {

    private List<Quest> questList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;
    private OnQuestActionListener listener;

    // Interface dengan default methods
    public interface OnQuestActionListener {
        void onQuestTaken(int position);
        default void onQuestDeleted(int position) {}
        default void onChatOpened(String questId) {}
        default void onQuestEdited(String questId) {}
    }

    // Constructor
    public QuestAdapter(List<Quest> questList, OnQuestActionListener listener) {
        this.questList = questList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_quest, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        Quest quest = questList.get(position);
        if (quest == null) return;

        // Set data
        holder.tvTitle.setText(quest.getTitle() != null ? quest.getTitle() : "No Title");
        holder.tvLocation.setText(quest.getLocation() != null ? quest.getLocation() : "No Location");
        holder.tvReward.setText(quest.getReward() + " GD");
        if (holder.tvStatus != null) {
            holder.tvStatus.setText("Status: " + (quest.getStatus() != null ? quest.getStatus() : "Unknown"));
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reset buttons
        holder.btnAccept.setVisibility(View.VISIBLE);
        holder.btnAccept.setEnabled(true);
        if (holder.btnEdit != null) holder.btnEdit.setVisibility(View.GONE);
        if (holder.btnReportQuest != null) holder.btnReportQuest.setVisibility(View.GONE);

        // Load creator name
        loadCreatorName(quest.getCreatorId(), holder.tvCreatorName);

        // Klik creator untuk lihat profil
        if (holder.layoutCreatorInfo != null) {
            holder.layoutCreatorInfo.setOnClickListener(v -> {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("USER_ID", quest.getCreatorId());
                context.startActivity(intent);
            });
        }

        // Tombol Lapor (hanya untuk user yang BUKAN creator)
        if (holder.btnReportQuest != null && !currentUserId.equals(quest.getCreatorId())) {
            holder.btnReportQuest.setVisibility(View.VISIBLE);
            holder.btnReportQuest.setOnClickListener(v -> showReportDialog(quest));
        }

        // Logika tombol
        if (quest.getCreatorId() != null && quest.getCreatorId().equals(currentUserId)) {
            if ("Open".equals(quest.getStatus())) {
                holder.btnAccept.setText("🗑️ Hapus");
                holder.btnAccept.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
                holder.btnAccept.setOnClickListener(v -> deleteQuest(quest.getQuestId(), position));
                if (holder.btnEdit != null) {
                    holder.btnEdit.setVisibility(View.VISIBLE);
                    holder.btnEdit.setText("✏️ Edit");
                    holder.btnEdit.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
                    holder.btnEdit.setOnClickListener(v -> openEditQuest(quest));
                }
            } else if ("Taken".equals(quest.getStatus())) {
                holder.btnAccept.setText("💬 Chat");
                holder.btnAccept.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
                holder.btnAccept.setOnClickListener(v -> openChat(quest));
            } else if ("Completed".equals(quest.getStatus())) {
                holder.btnAccept.setText("✅ Selesai");
                holder.btnAccept.setEnabled(false);
                holder.btnAccept.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        } else if (quest.getHelperId() != null && quest.getHelperId().equals(currentUserId)) {
            if ("Taken".equals(quest.getStatus())) {
                holder.btnAccept.setText("💬 Chat Helper");
                holder.btnAccept.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
                holder.btnAccept.setOnClickListener(v -> openChat(quest));
            } else if ("Completed".equals(quest.getStatus())) {
                holder.btnAccept.setText("✅ Selesai");
                holder.btnAccept.setEnabled(false);
                holder.btnAccept.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        } else if ("Taken".equals(quest.getStatus())) {
            holder.btnAccept.setText("🔒 Diambil");
            holder.btnAccept.setEnabled(false);
            holder.btnAccept.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
        } else if ("Open".equals(quest.getStatus())) {
            holder.btnAccept.setText("✅ Ambil");
            holder.btnAccept.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
            holder.btnAccept.setOnClickListener(v -> takeQuest(quest, position));
        }
    }

    private void loadCreatorName(String creatorId, TextView tvCreatorName) {
        if (creatorId == null || tvCreatorName == null) return;
        db.collection("users").document(creatorId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        tvCreatorName.setText(name != null ? name : "Unknown");
                    }
                });
    }

    private void showReportDialog(Quest quest) {
        String[] reasons = {"Konten tidak pantas", "Penipuan", "Spam", "Kekerasan", "Lainnya"};
        new AlertDialog.Builder(context)
                .setTitle("Laporkan Quest")
                .setItems(reasons, (dialog, which) -> {
                    submitReport(quest, reasons[which]);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void submitReport(Quest quest, String reason) {
        String reporterId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> report = new HashMap<>();
        report.put("questId", quest.getQuestId());
        report.put("questTitle", quest.getTitle());
        report.put("reporterId", reporterId);
        report.put("reportedUserId", quest.getCreatorId());
        report.put("reason", reason);
        report.put("status", "pending");
        report.put("timestamp", Timestamp.now());

        db.collection("reports").add(report)
                .addOnSuccessListener(doc ->
                        Toast.makeText(context, "✅ Laporan terkirim", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "❌ Gagal melapor", Toast.LENGTH_SHORT).show());
    }

    private void takeQuest(Quest quest, int position) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String chatId = quest.getQuestId() + "_" + currentUserId;
        Toast.makeText(context, "Mengambil quest...", Toast.LENGTH_SHORT).show();

        final String finalQuestId = quest.getQuestId();
        final String finalCreatorId = quest.getCreatorId();
        final int finalPosition = position;

        db.collection("users").document(finalCreatorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String creatorName = documentSnapshot.getString("name");
                    if (creatorName == null || creatorName.isEmpty()) creatorName = "Unknown";
                    final String finalCreatorName = creatorName;

                    db.collection("quests").document(finalQuestId)
                            .update("status", "Taken", "helperId", currentUserId, "chatId", chatId, "completed", false)
                            .addOnSuccessListener(aVoid -> {
                                quest.setStatus("Taken");
                                quest.setHelperId(currentUserId);
                                quest.setChatId(chatId);
                                notifyItemChanged(finalPosition);
                                if (listener != null) listener.onQuestTaken(finalPosition);

                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtra("QUEST_ID", finalQuestId);
                                intent.putExtra("CHAT_ID", chatId);
                                intent.putExtra("RECEIVER_ID", finalCreatorId);
                                intent.putExtra("RECEIVER_NAME", finalCreatorName);
                                context.startActivity(intent);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "❌ Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    private void openChat(Quest quest) {
        if (quest.getChatId() == null) return;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String receiverId = currentUserId.equals(quest.getCreatorId()) ? quest.getHelperId() : quest.getCreatorId();
        final String finalReceiverId = receiverId;
        final String finalQuestId = quest.getQuestId();
        final String finalChatId = quest.getChatId();

        db.collection("users").document(finalReceiverId).get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    if (name == null) name = "Unknown";
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("QUEST_ID", finalQuestId);
                    intent.putExtra("CHAT_ID", finalChatId);
                    intent.putExtra("RECEIVER_ID", finalReceiverId);
                    intent.putExtra("RECEIVER_NAME", name);
                    context.startActivity(intent);
                    if (listener != null) listener.onChatOpened(finalQuestId);
                });
    }

    private void deleteQuest(String questId, int position) {
        if (questId == null) return;
        db.collection("quests").document(questId).delete()
                .addOnSuccessListener(aVoid -> {
                    questList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, questList.size());
                    Toast.makeText(context, "🗑️ Quest dihapus", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onQuestDeleted(position);
                });
    }

    private void openEditQuest(Quest quest) {
        Intent intent = new Intent(context, EditQuestActivity.class);
        intent.putExtra("QUEST_ID", quest.getQuestId());
        intent.putExtra("TITLE", quest.getTitle());
        intent.putExtra("DESCRIPTION", quest.getDescription());
        intent.putExtra("LOCATION", quest.getLocation());
        intent.putExtra("REWARD", quest.getReward());
        context.startActivity(intent);
        if (listener != null) listener.onQuestEdited(quest.getQuestId());
    }

    @Override
    public int getItemCount() {
        return questList != null ? questList.size() : 0;
    }

    // ViewHolder
    public static class QuestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvReward, tvStatus, tvCreatorName, btnReportQuest;
        Button btnAccept, btnEdit;
        LinearLayout layoutCreatorInfo;

        public QuestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvQuestTitle);
            tvLocation = itemView.findViewById(R.id.tvQuestLocation);
            tvReward = itemView.findViewById(R.id.tvQuestReward);
            tvStatus = itemView.findViewById(R.id.tvQuestStatus);
            tvCreatorName = itemView.findViewById(R.id.tvCreatorName);
            btnReportQuest = itemView.findViewById(R.id.btnReportQuest);
            btnAccept = itemView.findViewById(R.id.btnAcceptQuest);
            btnEdit = itemView.findViewById(R.id.btnEditQuest);
            layoutCreatorInfo = itemView.findViewById(R.id.layoutCreatorInfo);
        }
    }
}