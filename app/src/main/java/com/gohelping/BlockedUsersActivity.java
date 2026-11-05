package com.gohelping;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BlockedUsersActivity extends AppCompatActivity {

    private RecyclerView rvBlockedUsers;
    private BlockedUserAdapter adapter;
    private List<User> blockedUsers = new ArrayList<>();
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private LinearLayout emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        db = FirebaseFirestore.getInstance();

        rvBlockedUsers = findViewById(R.id.rvBlockedUsers);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvBlockedUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlockedUserAdapter(blockedUsers, this::showUnblockConfirmation);
        rvBlockedUsers.setAdapter(adapter);

        loadBlockedUsers();
    }

    private void loadBlockedUsers() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users")
                .whereEqualTo("isBlocked", true)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    blockedUsers.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            User user = doc.toObject(User.class);
                            user.setUserId(doc.getId());
                            blockedUsers.add(user);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    emptyState.setVisibility(blockedUsers.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void showUnblockConfirmation(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Buka Blokir")
                .setMessage("Apakah Anda yakin ingin membuka blokir untuk " + user.getName() + "?")
                .setPositiveButton("Ya, Buka", (dialog, which) -> unblockUser(user))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void unblockUser(User user) {
        db.collection("users")
                .document(user.getUserId())
                .update("isBlocked", false, "blockedUntil", 0)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Blokir dibuka untuk " + user.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal membuka blokir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
