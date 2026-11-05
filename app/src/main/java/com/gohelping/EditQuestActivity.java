package com.gohelping;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditQuestActivity extends AppCompatActivity {

    private EditText etQuestTitle, etQuestDesc, etReward, etLocation;
    private Button btnUpdateQuest, btnCancel;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String questId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quest);

        db = FirebaseFirestore.getInstance();

        // Ambil data dari intent
        questId = getIntent().getStringExtra("QUEST_ID");
        String title = getIntent().getStringExtra("TITLE");
        String description = getIntent().getStringExtra("DESCRIPTION");
        String location = getIntent().getStringExtra("LOCATION");
        int reward = getIntent().getIntExtra("REWARD", 0);

        // Inisialisasi views
        etQuestTitle = findViewById(R.id.etQuestTitle);
        etQuestDesc = findViewById(R.id.etQuestDesc);
        etReward = findViewById(R.id.etReward);
        etLocation = findViewById(R.id.etLocation);
        btnUpdateQuest = findViewById(R.id.btnUpdateQuest);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);

        // Isi form dengan data lama
        etQuestTitle.setText(title);
        etQuestDesc.setText(description);
        etLocation.setText(location);
        etReward.setText(String.valueOf(reward));

        // Tombol Update
        btnUpdateQuest.setOnClickListener(v -> updateQuest());

        // Tombol Cancel
        btnCancel.setOnClickListener(v -> finish());
    }

    private void updateQuest() {
        String title = etQuestTitle.getText().toString().trim();
        String desc = etQuestDesc.getText().toString().trim();
        String rewardStr = etReward.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        // Validasi
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(rewardStr) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Judul, Lokasi, dan Reward harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading
        progressBar.setVisibility(View.VISIBLE);
        btnUpdateQuest.setEnabled(false);

        // Update data di Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("description", desc);
        updates.put("location", location);
        updates.put("reward", Integer.parseInt(rewardStr));

        db.collection("quests")
                .document(questId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Quest berhasil diupdate!", Toast.LENGTH_SHORT).show();
                    finish(); // Kembali ke activity sebelumnya
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdateQuest.setEnabled(true);
                    Toast.makeText(this, "Gagal update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}