package com.gohelping;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.content.Intent;

public class QuestBoardActivity extends AppCompatActivity {

    private EditText etQuestTitle, etQuestDesc, etReward, etLocation; // Tambahkan etLocation
    private Button btnPostQuest;
    private FirebaseFirestore db;

    // Di dalam class, tambahkan variabel tombol
    private Button btnViewMyQuests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // HANYA gunakan layout milik QuestBoard
        setContentView(R.layout.activity_quest_board);

        db = FirebaseFirestore.getInstance();

        // Inisialisasi View
        etQuestTitle = findViewById(R.id.etQuestTitle);
        etQuestDesc = findViewById(R.id.etQuestDesc);
        etReward = findViewById(R.id.etReward);
        etLocation = findViewById(R.id.etLocation);
        btnPostQuest = findViewById(R.id.btnPostQuest);
        btnViewMyQuests = findViewById(R.id.btnViewMyQuests);


        // Panggil fungsi saat tombol diklik
        btnViewMyQuests.setOnClickListener(v -> {
            fetchMyQuests();
        });

        // Pastikan btnPostQuest tidak null sebelum pasang listener
        if (btnPostQuest != null) {
            btnPostQuest.setOnClickListener(v -> postQuestToFirebase());
        } else {
            // Log ini akan membantu jika kamu lupa memasukkan ID di XML
            android.util.Log.e("ERROR", "Tombol btnPostQuest tidak ditemukan di XML!");
        }
    }

    private void fetchMyQuests() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("quests")
                .whereEqualTo("creatorId", currentUserId) // KUNCI UTAMA: Memfilter data milik sendiri
                .get() // Mengambil data
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        Toast.makeText(
                                this,
                                "Berhasil memuat " + queryDocumentSnapshots.size() + " quest kamu",
                                Toast.LENGTH_SHORT
                        ).show();

                        // Pindah ke halaman MyQuestActivity
                        Intent intent = new Intent(
                                QuestBoardActivity.this,
                                MyQuestActivity.class
                        );
                        startActivity(intent);

                    } else {
                        Toast.makeText(
                                this,
                                "Kamu belum membuat quest apapun.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal mengambil data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void postQuestToFirebase() {
        String title = etQuestTitle.getText().toString().trim();
        String desc = etQuestDesc.getText().toString().trim();
        String rewardStr = etReward.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        // 1. Validasi Input (Cegah Crash)
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(rewardStr) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Judul, Lokasi, dan Reward harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Ambil User ID
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Kamu harus login terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }



        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 3. Persiapkan Data
        Map<String, Object> quest = new HashMap<>();
        quest.put("title", title);
        quest.put("description", desc);
        quest.put("location", location); // Tambahkan field ini agar muncul di RecyclerView
        quest.put("creatorId", userId);
        quest.put("status", "Open");
        quest.put("reward", Integer.parseInt(rewardStr));

        // Disable tombol agar tidak klik dua kali (Double Post)
        btnPostQuest.setEnabled(false);

        // 4. Kirim ke Firestore
        db.collection("quests").add(quest)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(QuestBoardActivity.this, "Quest Berhasil Diposting!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnPostQuest.setEnabled(true);
                    Toast.makeText(QuestBoardActivity.this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}