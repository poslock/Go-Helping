package com.gohelping;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore; // Tambahkan ini

import java.util.HashMap; // Tambahkan ini
import java.util.Map;     // Tambahkan ini

public class RegisterActivity extends AppCompatActivity {

    private EditText etNama, etEmail, etPassword;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Tambahkan ini

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Inisialisasi Firestore

        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String nama = etNama.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(nama)) {
            etNama.setError("Nama wajib diisi");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email wajib diisi");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password wajib diisi");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Minimal 6 karakter");
            return;
        }

        // Jalankan pendaftaran
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Jika Auth berhasil, simpan data tambahan ke Firestore
                        saveUserToFirestore(nama);
                    } else {
                        Toast.makeText(this, "Gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String fullName) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> userMap = new HashMap<>();

        // Data dari input user
        userMap.put("name", fullName);
        userMap.put("email", mAuth.getCurrentUser().getEmail());

        // Data default profile
        userMap.put("uid", uid);
        userMap.put("gdBalance", 50);         // Saldo awal
        userMap.put("questComplete", 0);      // Quest selesai
        userMap.put("totalGdEarned", 0);      // Total penghasilan
        userMap.put("level", 1);              // Level awal
        userMap.put("rating", "5.0");         // Rating awal

        db.collection("users")
                .document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(
                            this,
                            "Registrasi berhasil! Bonus 50 GD diberikan.",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Gagal simpan user: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}