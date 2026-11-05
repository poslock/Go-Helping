package com.gohelping;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileInitial, tvLevel, tvGdBalance, tvBio;
    private TextView tvQuestSelesai, tvGdEarned, tvRating;
    private Button btnLogout, btnPrivacy, btnAdminPanel; // ⬅️ TAMBAHKAN btnAdminPanel
    private ImageButton btnBack, btnSettings;
    private LinearLayout layoutTransactionHistory;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();

        // Inisialisasi Views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        tvLevel = findViewById(R.id.tvLevel);
        tvGdBalance = findViewById(R.id.tvGdBalance);
        tvBio = findViewById(R.id.tvBio); // ⬅️ SEKARANG ADA
        tvQuestSelesai = findViewById(R.id.tvQuestSelesai);
        tvGdEarned = findViewById(R.id.tvGdEarned);
        tvRating = findViewById(R.id.tvRating);
        btnLogout = findViewById(R.id.btnLogout);
        btnPrivacy = findViewById(R.id.btnPrivacy); // ⬅️ SEKARANG ADA
        btnAdminPanel = findViewById(R.id.btnAdminPanel); // ⬅️ SEKARANG ADA
        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        layoutTransactionHistory = findViewById(R.id.layoutTransactionHistory);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnSettings != null) btnSettings.setOnClickListener(v -> openSettings());

        // Logout
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Apakah kamu yakin ingin logout?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            mAuth.signOut();
                            startActivity(new Intent(ProfileActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        })
                        .setNegativeButton("Batal", null).show();
            });
        }

        // Privacy button
        if (btnPrivacy != null) {
            btnPrivacy.setOnClickListener(v -> showPrivacyDialog());
        }

        // Admin Panel button (hanya admin)
        if (btnAdminPanel != null) {
            btnAdminPanel.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, AdminActivity.class));
            });
        }

        loadUserData();
        loadTransactionHistory();
        checkIfAdmin(); // ⬅️ Cek role admin
    }

    private void loadUserData() {
        db.collection("users").document(currentUserId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) {
                        createUserDocument(currentUserId);
                        return;
                    }

                    String name = snapshot.getString("name");
                    String bio = snapshot.getString("bio");
                    Boolean isPrivate = snapshot.getBoolean("isPrivate");
                    Long points = snapshot.getLong("points");
                    Long completed = snapshot.getLong("questsComplete");
                    Long earned = snapshot.getLong("totalGdEarned");
                    String rating = snapshot.getString("rating");
                    Long level = snapshot.getLong("level");

                    if (name != null && !name.isEmpty()) {
                        tvProfileInitial.setText(name.substring(0, 1).toUpperCase());
                    } else {
                        tvProfileInitial.setText("?");
                    }

                    tvProfileName.setText(name != null ? name : "User");

                    if (tvBio != null) {
                        tvBio.setText(bio != null && !bio.isEmpty() ? bio : "Ketuk untuk tambah bio");
                        tvBio.setOnClickListener(v -> showEditBioDialog(bio));
                    }

                    tvGdBalance.setText("★ " + (points != null ? points : 0) + " GD");
                    tvQuestSelesai.setText(String.valueOf(completed != null ? completed : 0));
                    tvGdEarned.setText(String.valueOf(earned != null ? earned : 0));
                    tvRating.setText((rating != null ? rating : "0.0") + "★");
                    tvLevel.setText("Level " + (level != null ? level : 1) + " ⭐");

                    if (btnPrivacy != null) {
                        btnPrivacy.setText(isPrivate != null && isPrivate ? "🔒 Akun Privat" : "🌍 Akun Publik");
                    }
                });
    }

    private void checkIfAdmin() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    String role = doc.getString("role");
                    if ("admin".equals(role) && btnAdminPanel != null) {
                        btnAdminPanel.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showEditBioDialog(String currentBio) {
        EditText input = new EditText(this);
        input.setText(currentBio);
        input.setTextColor(getResources().getColor(R.color.text_primary));
        input.setHint("Tulis bio kamu...");
        input.setHintTextColor(getResources().getColor(R.color.text_dim));
        input.setBackgroundResource(R.drawable.bg_neon_border);
        input.setPadding(24, 16, 24, 16);
        input.setSingleLine(false);
        input.setMinLines(2);

        new AlertDialog.Builder(this)
                .setTitle("Edit Bio")
                .setView(input)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String newBio = input.getText().toString().trim();
                    db.collection("users").document(currentUserId).update("bio", newBio);
                    tvBio.setText(newBio.isEmpty() ? "Ketuk untuk tambah bio" : newBio);
                    Toast.makeText(this, "Bio berhasil diupdate!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Privasi Akun")
                .setMessage("Sembunyikan pencapaian dari user lain?")
                .setPositiveButton("Ya, Privat", (d, w) -> {
                    db.collection("users").document(currentUserId).update("isPrivate", true);
                    if (btnPrivacy != null) btnPrivacy.setText("🔒 Akun Privat");
                    Toast.makeText(this, "Akun diprivat", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Tidak, Publik", (d, w) -> {
                    db.collection("users").document(currentUserId).update("isPrivate", false);
                    if (btnPrivacy != null) btnPrivacy.setText("🌍 Akun Publik");
                    Toast.makeText(this, "Akun publik", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void openSettings() {
        Toast.makeText(this, "Pengaturan: Edit Profil, Privasi, Notifikasi", Toast.LENGTH_LONG).show();
    }

    private void createUserDocument(String userId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", mAuth.getCurrentUser().getEmail());
        userData.put("bio", "");
        userData.put("isPrivate", false);
        userData.put("isBlocked", false);
        userData.put("role", "user");
        userData.put("points", 0);
        userData.put("questsComplete", 0);
        userData.put("totalGdEarned", 0);
        userData.put("rating", "0.0");
        userData.put("level", 1);
        userData.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> loadUserData())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal membuat profil", Toast.LENGTH_SHORT).show());
    }

    // ... loadTransactionHistory() dan addTransactionItem() tetap sama ...

    private void loadTransactionHistory() {
        db.collection("transactions")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    layoutTransactionHistory.removeAllViews();
                    if (queryDocumentSnapshots.isEmpty()) {
                        TextView tvEmpty = new TextView(this);
                        tvEmpty.setText("📭 Belum ada transaksi");
                        tvEmpty.setTextColor(getResources().getColor(R.color.text_dim));
                        tvEmpty.setTextSize(14);
                        tvEmpty.setGravity(android.view.Gravity.CENTER);
                        tvEmpty.setPadding(0, 24, 0, 24);
                        layoutTransactionHistory.addView(tvEmpty);
                        return;
                    }
                    boolean first = true;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (!first) {
                            View divider = new View(this);
                            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
                            dp.setMargins(0, 8, 0, 8);
                            divider.setLayoutParams(dp);
                            divider.setBackgroundColor(getResources().getColor(R.color.dark_input));
                            layoutTransactionHistory.addView(divider);
                        }
                        first = false;
                        addTransactionItem(doc);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show());
    }

    private void addTransactionItem(QueryDocumentSnapshot doc) {
        RelativeLayout itemLayout = new RelativeLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        itemLayout.setLayoutParams(params);

        String desc = doc.getString("description");
        String type = doc.getString("type");
        String title = (desc != null) ? desc : (type != null ? type : "Transaksi");

        TextView tvTitle = new TextView(this);
        RelativeLayout.LayoutParams tp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tp.addRule(RelativeLayout.ALIGN_PARENT_START);
        tp.setMarginStart(20);
        tvTitle.setLayoutParams(tp);
        tvTitle.setText(title);
        tvTitle.setTextColor(getResources().getColor(R.color.text_primary));
        tvTitle.setTextSize(13);
        itemLayout.addView(tvTitle);

        Long amount = doc.getLong("amount");
        String amountText = (amount != null) ? ((amount >= 0 ? "+" : "") + amount + " GD") : "0 GD";

        TextView tvAmount = new TextView(this);
        RelativeLayout.LayoutParams ap = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ap.addRule(RelativeLayout.ALIGN_PARENT_END);
        tvAmount.setLayoutParams(ap);
        tvAmount.setText(amountText);
        tvAmount.setTextColor(getResources().getColor(
                amount != null && amount >= 0 ? R.color.neon_green : R.color.red));
        tvAmount.setTextSize(13);
        itemLayout.addView(tvAmount);

        layoutTransactionHistory.addView(itemLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            loadUserData();
            loadTransactionHistory();
        }
    }
}