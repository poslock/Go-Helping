package com.gohelping;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout; // ✅ Import LinearLayout
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MyQuestActivity extends AppCompatActivity {

    private RecyclerView rvMyQuests;
    private QuestAdapter questAdapter;
    private List<Quest> myQuestList = new ArrayList<>();
    private FirebaseFirestore db;
    private LinearLayout emptyStateLayout; // ✅ Ubah ke LinearLayout
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_quest);

        rvMyQuests = findViewById(R.id.rvMyQuests);
        emptyStateLayout = findViewById(R.id.tvEmptyState); // ✅ Casting LinearLayout

        rvMyQuests.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        questAdapter = new QuestAdapter(myQuestList, new QuestAdapter.OnQuestActionListener() {
            @Override
            public void onQuestTaken(int position) {
                Toast.makeText(MyQuestActivity.this, "Quest berhasil diambil!", Toast.LENGTH_SHORT).show();
                loadMyQuests();
            }

            @Override
            public void onQuestDeleted(int position) {
                Toast.makeText(MyQuestActivity.this, "Quest berhasil dihapus!", Toast.LENGTH_SHORT).show();
                loadMyQuests();
            }

            @Override
            public void onChatOpened(String questId) {
                Log.d("MyQuestActivity", "Membuka chat untuk quest: " + questId);
            }
        });

        rvMyQuests.setAdapter(questAdapter);
        loadMyQuests();
    }

    private void loadMyQuests() {
        db.collection("quests")
                .whereEqualTo("helperId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("MyQuestActivity", "Error: " + error.getMessage());
                        return;
                    }

                    myQuestList.clear();

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Quest quest = doc.toObject(Quest.class);
                                quest.setQuestId(doc.getId());
                                myQuestList.add(quest);
                            } catch (Exception e) {
                                Log.e("MyQuestActivity", "Error parsing quest: " + e.getMessage());
                            }
                        }
                    }

                    loadCreatedQuests();
                });
    }

    private void loadCreatedQuests() {
        db.collection("quests")
                .whereEqualTo("creatorId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Quest quest = doc.toObject(Quest.class);
                            quest.setQuestId(doc.getId());

                            boolean exists = false;
                            for (Quest q : myQuestList) {
                                if (q.getQuestId() != null && q.getQuestId().equals(quest.getQuestId())) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (!exists) {
                                myQuestList.add(quest);
                            }
                        } catch (Exception e) {
                            Log.e("MyQuestActivity", "Error parsing created quest: " + e.getMessage());
                        }
                    }

                    updateUI();
                    questAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("MyQuestActivity", "Error loading created quests: " + e.getMessage());
                    updateUI();
                    questAdapter.notifyDataSetChanged();
                });
    }

    private void updateUI() {
        if (myQuestList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            rvMyQuests.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvMyQuests.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (questAdapter != null) {
            loadMyQuests();
        }
    }
}