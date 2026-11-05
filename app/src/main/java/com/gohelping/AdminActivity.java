package com.gohelping;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvReports;
    private ReportAdapter reportAdapter;
    private List<Report> reportList = new ArrayList<>();
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView tvAdminTitle, tvReportCount;
    private LinearLayout emptyStateAdmin; // ⬅️ Jadikan field class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();

        // Inisialisasi views
        rvReports = findViewById(R.id.rvReports);
        progressBar = findViewById(R.id.progressBar);
        tvAdminTitle = findViewById(R.id.tvAdminTitle);
        tvReportCount = findViewById(R.id.tvReportCount);
        emptyStateAdmin = findViewById(R.id.emptyStateAdmin); // ⬅️ Pakai field class

        rvReports.setLayoutManager(new LinearLayoutManager(this));

        // Cek apakah user benar admin
        checkAdminAccess();

        // Setup adapter
        reportAdapter = new ReportAdapter(reportList, new ReportAdapter.OnReportActionListener() {
            @Override
            public void onBlockUser(Report report, int position) {
                showBlockDialog(report, position);
            }

            @Override
            public void onChatUser(Report report) {
                openChatWithReportedUser(report);
            }

            @Override
            public void onDismissReport(Report report, int position) {
                dismissReport(report, position);
            }
        });

        rvReports.setAdapter(reportAdapter);

        // Tombol back to home
        Button btnBackToHome = findViewById(R.id.btnBackToHome);
        if (btnBackToHome != null) {
            btnBackToHome.setOnClickListener(v -> {
                startActivity(new Intent(AdminActivity.this, HomeActivity.class));
                finish();
            });
        }

        // Tombol daftar blokir
        Button btnBlockedUsers = findViewById(R.id.btnBlockedUsers);
        if (btnBlockedUsers != null) {
            btnBlockedUsers.setOnClickListener(v -> {
                Toast.makeText(this, "Fitur Daftar Blokir coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Load laporan
        loadReports();
    }

    private void checkAdminAccess() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    String role = doc.getString("role");
                    if (!"admin".equals(role)) {
                        Toast.makeText(this, "⛔ Akses ditolak! Hanya admin.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(AdminActivity.this, HomeActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal verifikasi akses", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("reports")
                .whereEqualTo("status", "pending")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(this, "Gagal memuat laporan: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null) return;

                    reportList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Report report = doc.toObject(Report.class);
                        report.setReportId(doc.getId());
                        reportList.add(report);
                    }

                    reportAdapter.notifyDataSetChanged();
                    tvReportCount.setText("Total Laporan: " + reportList.size());

                    // ⬅️ Update empty state
                    updateEmptyState();
                });
    }

    private void updateEmptyState() {
        if (reportList.isEmpty()) {
            rvReports.setVisibility(View.GONE);
            emptyStateAdmin.setVisibility(View.VISIBLE);
            tvReportCount.setText("✅ Tidak ada laporan");
        } else {
            rvReports.setVisibility(View.VISIBLE);
            emptyStateAdmin.setVisibility(View.GONE);
        }
    }

    private void showBlockDialog(Report report, int position) {
        String[] blockDurations = {"1 Hari", "3 Hari", "7 Hari", "30 Hari", "Permanen"};

        new AlertDialog.Builder(this)
                .setTitle("🔒 Blokir User")
                .setMessage("Pilih durasi blokir untuk user:\n" + report.getReportedUserId())
                .setItems(blockDurations, (dialog, which) -> {
                    long durationMs = getDurationMs(which);
                    blockUser(report, position, durationMs);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private long getDurationMs(int index) {
        switch (index) {
            case 0: return 86400000L;       // 1 Hari
            case 1: return 259200000L;      // 3 Hari
            case 2: return 604800000L;      // 7 Hari
            case 3: return 2592000000L;     // 30 Hari
            case 4: return Long.MAX_VALUE;  // Permanen
            default: return 86400000L;
        }
    }

    private void blockUser(Report report, int position, long durationMs) {
        long blockedUntil = System.currentTimeMillis() + durationMs;

        Map<String, Object> updates = new HashMap<>();
        updates.put("isBlocked", true);
        updates.put("blockedUntil", blockedUntil);
        updates.put("blockedReason", report.getReason());

        db.collection("users")
                .document(report.getReportedUserId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    updateReportStatus(report, "blocked", position);
                    Toast.makeText(this, "✅ User berhasil diblokir!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Gagal memblokir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openChatWithReportedUser(Report report) {
        String adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String chatId = "admin_" + report.getReportedUserId();

        db.collection("users")
                .document(report.getReportedUserId())
                .get()
                .addOnSuccessListener(doc -> {
                    String userName = doc.getString("name");
                    if (userName == null) userName = "User";

                    Intent intent = new Intent(AdminActivity.this, ChatActivity.class);
                    intent.putExtra("QUEST_ID", report.getQuestId());
                    intent.putExtra("CHAT_ID", chatId);
                    intent.putExtra("RECEIVER_ID", report.getReportedUserId());
                    intent.putExtra("RECEIVER_NAME", userName);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat data user", Toast.LENGTH_SHORT).show();
                });
    }

    private void dismissReport(Report report, int position) {
        new AlertDialog.Builder(this)
                .setTitle("✅ Tutup Laporan")
                .setMessage("Apakah laporan ini akan ditutup?")
                .setPositiveButton("Ya, Tutup", (dialog, which) -> {
                    updateReportStatus(report, "dismissed", position);
                    Toast.makeText(this, "Laporan berhasil ditutup", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void updateReportStatus(Report report, String status, int position) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("adminNote", "Diproses oleh admin pada " + System.currentTimeMillis());

        db.collection("reports")
                .document(report.getReportId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Hapus dari list dan update UI
                    if (position >= 0 && position < reportList.size()) {
                        reportList.remove(position);
                        reportAdapter.notifyItemRemoved(position);
                        reportAdapter.notifyItemRangeChanged(position, reportList.size());
                        updateEmptyState();
                        tvReportCount.setText("Total Laporan: " + reportList.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Gagal update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}