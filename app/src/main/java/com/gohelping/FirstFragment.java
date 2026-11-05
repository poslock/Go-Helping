package com.gohelping;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirstFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        progressBar = view.findViewById(R.id.progressBar); // ⬅️ PASTIKAN ID BENAR

        // Null check
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> loginUser());
        }

        return view;
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email harus diisi!");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password harus diisi!");
            etPassword.requestFocus();
            return;
        }

        // ✅ Null safety untuk progressBar
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(false);
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserRole();
                    } else {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        if (btnLogin != null) {
                            btnLogin.setEnabled(true);
                        }
                        Toast.makeText(getContext(),
                                "Login gagal: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserRole() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    String role = "user";
                    String name = "User";

                    if (documentSnapshot.exists()) {
                        role = documentSnapshot.getString("role");
                        name = documentSnapshot.getString("name");
                        if (role == null) role = "user";
                        updateLastLogin(userId);
                    } else {
                        createNewUserDocument(userId);
                    }

                    if ("admin".equals(role)) {
                        Toast.makeText(getContext(), "Selamat datang, Admin!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), AdminActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    } else {
                        Toast.makeText(getContext(), "Selamat datang!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), HomeActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }

                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (btnLogin != null) {
                        btnLogin.setEnabled(true);
                    }
                    Toast.makeText(getContext(), "Gagal memuat data user", Toast.LENGTH_SHORT).show();
                });
    }

    private void createNewUserDocument(String userId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", mAuth.getCurrentUser().getEmail());
        userData.put("email", mAuth.getCurrentUser().getEmail());
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

        db.collection("users").document(userId).set(userData);
    }

    private void updateLastLogin(String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastLogin", com.google.firebase.Timestamp.now());
        db.collection("users").document(userId).update(updates);
    }
}