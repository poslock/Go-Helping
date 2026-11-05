package com.gohelping;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class MyQuestAdapter extends RecyclerView.Adapter<MyQuestAdapter.ViewHolder> {

    private List<Quest> list;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MyQuestAdapter(List<Quest> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Panggil layout item_my_quest yang sudah kamu buat
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_quest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Quest quest = list.get(position);
        holder.tvTitle.setText(quest.getTitle());

        // Logika Hapus
        holder.btnDelete.setOnClickListener(v -> {
            db.collection("quests").document(quest.getQuestId()).delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Berhasil dihapus", Toast.LENGTH_SHORT).show());
        });

        // Logika Edit (Opsional - Kirim data ke QuestBoardActivity untuk diedit)
        holder.btnEdit.setOnClickListener(v -> {
            // Kamu bisa membuat EditQuestActivity atau kirim balik ke QuestBoard
            Toast.makeText(context, "Fitur Edit sedang dikembangkan", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        Button btnEdit, btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvQuestTitle);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}