package com.gohelping;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvQuests;
    private QuestAdapter questAdapter;
    private List<Quest> questList = new ArrayList<>();
    private FirebaseFirestore db;
    private LinearLayout emptyStateLayout;
    private TextView tvUserPoints;
    private Button btnRefresh;
    private BottomNavigationView bottomNavigation;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvQuests = findViewById(R.id.rvQuests);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvUserPoints = findViewById(R.id.tvUserPoints);
        btnRefresh = findViewById(R.id.btnRefresh);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        rvQuests.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        // Cek user login
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Setup Bottom Navigation
        setupBottomNavigation();

        // Load user points
        loadUserPoints();

        // Refresh button
        btnRefresh.setOnClickListener(v -> loadAvailableQuests());

        // SETUP ADAPTER DENGAN LISTENER
        questAdapter = new QuestAdapter(questList, new QuestAdapter.OnQuestActionListener() {
            @Override
            public void onQuestTaken(int position) {
                onQuestSuccessfullyTaken(position);
            }

            @Override
            public void onQuestDeleted(int position) {
                Toast.makeText(HomeActivity.this, "Quest berhasil dihapus!", Toast.LENGTH_SHORT).show();
                loadAvailableQuests();
            }

            @Override
            public void onChatOpened(String questId) {
                Log.d("HomeActivity", "Membuka chat untuk quest: " + questId);
            }
        });

        rvQuests.setAdapter(questAdapter);
        loadAvailableQuests();
    }

    private void setupBottomNavigation() {
        // Set Home sebagai item yang terpilih
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Sudah di Home, tidak perlu apa-apa
                return true;

            } else if (itemId == R.id.nav_my_quests) {
                startActivity(new Intent(HomeActivity.this, MyQuestActivity.class));
                return true;

            } else if (itemId == R.id.nav_add_quest) {
                startActivity(new Intent(HomeActivity.this, QuestBoardActivity.class));
                return true;

            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(HomeActivity.this, ChatRoomActivity.class));
                return true;

            } else if (itemId == R.id.nav_profile) {
                // TODO: Buka ProfileActivity
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    private void loadUserPoints() {
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("points")) {
                        long points = doc.getLong("points");
                        tvUserPoints.setText(points + " GD");
                    }
                });
    }

    private void onQuestSuccessfullyTaken(int position) {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            Snackbar.make(rootView,
                            "✅ Quest berhasil diambil! Silahkan chat creator.",
                            Snackbar.LENGTH_LONG)
                    .setAction("OK", v -> {})
                    .show();
        }

        new Handler().postDelayed(() -> {
            loadAvailableQuests();
        }, 1500);

        Log.d("HomeActivity", "Quest di posisi " + position + " berhasil diambil");
    }

    private void loadAvailableQuests() {
        db.collection("quests")
                .whereEqualTo("status", "Open")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("HomeActivity", "Error: " + error.getMessage());
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        rvQuests.setVisibility(View.GONE);
                        return;
                    }

                    emptyStateLayout.setVisibility(View.GONE);
                    rvQuests.setVisibility(View.VISIBLE);

                    questList.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            Quest quest = doc.toObject(Quest.class);
                            quest.setQuestId(doc.getId());
                            questList.add(quest);
                        } catch (Exception e) {
                            Log.e("HomeActivity", "Error parsing quest: " + e.getMessage());
                        }
                    }

                    questAdapter.notifyDataSetChanged();
                    Log.d("HomeActivity", "Loaded " + questList.size() + " quests");

                    if (questList.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        rvQuests.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (questAdapter != null) {
            loadAvailableQuests();
            loadUserPoints();
        }
    }
}