package com.gohelping;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserBio, tvUserLevel, tvUserPoints, tvUserQuests;
    private Button btnChatUser;
    private FirebaseFirestore db;
    private String userId, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userId = getIntent().getStringExtra("USER_ID");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvUserName = findViewById(R.id.tvUserName);
        tvUserBio = findViewById(R.id.tvUserBio);
        tvUserLevel = findViewById(R.id.tvUserLevel);
        tvUserPoints = findViewById(R.id.tvUserPoints);
        tvUserQuests = findViewById(R.id.tvUserQuests);
        btnChatUser = findViewById(R.id.btnChatUser);

        db = FirebaseFirestore.getInstance();

        loadUserProfile();

        if (btnChatUser != null) {
            btnChatUser.setOnClickListener(v -> openChat());
        }
    }

    private void loadUserProfile() {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String bio = doc.getString("bio");
                        Long points = doc.getLong("points");
                        Long completed = doc.getLong("questsComplete");
                        Long level = doc.getLong("level");
                        Boolean isPrivate = doc.getBoolean("isPrivate");

                        tvUserName.setText(name != null ? name : "User");
                        tvUserBio.setText(bio != null ? bio : "Belum ada bio");
                        tvUserLevel.setText("Level " + (level != null ? level : 1) + " ⭐");

                        if (isPrivate != null && isPrivate) {
                            tvUserPoints.setText("🔒 Diprivat");
                            tvUserQuests.setText("🔒 Diprivat");
                        } else {
                            tvUserPoints.setText("💰 " + (points != null ? points : 0) + " GD");
                            tvUserQuests.setText("✅ " + (completed != null ? completed : 0) + " Quest");
                        }
                    } else {
                        Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat profil", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void openChat() {
        String chatId = userId + "_" + currentUserId;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        intent.putExtra("RECEIVER_ID", userId);
        intent.putExtra("RECEIVER_NAME", tvUserName.getText().toString());
        startActivity(intent);
    }
}