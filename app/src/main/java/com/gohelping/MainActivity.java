package com.gohelping;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button btnMasuk;
    private TextView tvRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Cek jika user sudah login, langsung ke HomeActivity
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
            return;
        }

        btnMasuk = findViewById(R.id.btnMasuk);
        tvRegister = findViewById(R.id.tvRegister);

        // Tombol Masuk
        btnMasuk.setOnClickListener(v -> {
            // Sembunyikan tombol masuk agar tidak menumpuk dengan fragment
            btnMasuk.setVisibility(View.GONE);

            if (tvRegister != null) {
                tvRegister.setVisibility(View.GONE);
            }

            // Tampilkan fragment login
            FirstFragment loginFragment = new FirstFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
            transaction.replace(R.id.fragment_container, loginFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Tombol Register (opsional)
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                // TODO: Buka RegisterActivity atau RegisterFragment
                Toast.makeText(this, "Fitur Register coming soon!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onBackPressed() {
        // Jika fragment container visible, kembalikan tombol masuk
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            btnMasuk.setVisibility(View.VISIBLE);
            if (tvRegister != null) {
                tvRegister.setVisibility(View.VISIBLE);
            }
        } else {
            super.onBackPressed();
        }
    }
}