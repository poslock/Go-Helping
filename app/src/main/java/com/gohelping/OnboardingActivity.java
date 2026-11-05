package com.gohelping;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Penjelasan Go Helping untuk pengguna baru (5 halaman bergerak).
 * Tampil sekali saja; tombol "Lewati" / "Mulai Berpetualang" menandainya selesai.
 */
public class OnboardingActivity extends AppCompatActivity {

    public static final String PREFS = "gohelping_prefs";
    public static final String KEY_DONE = "onboarding_done";

    private static final int COUNT = 5;
    private static final int[] PAGE_IDS = {R.id.page0, R.id.page1, R.id.page2, R.id.page3, R.id.page4};
    private static final int[] DOT_IDS = {R.id.dot0, R.id.dot1, R.id.dot2, R.id.dot3, R.id.dot4};
    private static final int[] RANK_IDS = {R.id.rank0, R.id.rank1, R.id.rank2, R.id.rank3};
    private static final String[] RANK_NAMES = {"🌱 Newbie Helper", "🤝 Helper", "🦸 Hero", "👑 Legend"};

    private final View[] pages = new View[COUNT];
    private final View[] dots = new View[COUNT];
    private final List<Animator> pageAnims = new ArrayList<>();
    private final List<Animator> ambientAnims = new ArrayList<>();

    private Button btnNext;
    private int current = 0;
    private GestureDetector gestures;

    /** Dipakai MainActivity untuk memutuskan perlu menampilkan onboarding atau tidak. */
    public static boolean shouldShow(Context c) {
        return !c.getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_DONE, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        getWindow().setStatusBarColor(Color.parseColor("#020819"));
        getWindow().setNavigationBarColor(Color.parseColor("#0A46C8"));

        for (int i = 0; i < COUNT; i++) {
            pages[i] = findViewById(PAGE_IDS[i]);
            dots[i] = findViewById(DOT_IDS[i]);
        }

        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            if (current == COUNT - 1) finishOnboarding();
            else goTo(current + 1);
        });
        findViewById(R.id.tvSkip).setOnClickListener(v -> finishOnboarding());

        gestures = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (e1 == null || e2 == null) return false;
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();
                if (Math.abs(dx) > 120 && Math.abs(dx) > Math.abs(dy) * 1.5f) {
                    goTo(dx < 0 ? current + 1 : current - 1);
                    return true;
                }
                return false;
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (current > 0) goTo(current - 1);
                else { setEnabled(false); getOnBackPressedDispatcher().onBackPressed(); }
            }
        });

        // Glow latar yang bergerak pelan
        play(ambientAnims, ViewMotion.floatY(findViewById(R.id.glowA), 18, 4200, 0));
        play(ambientAnims, ViewMotion.floatY(findViewById(R.id.glowB), 22, 5200, 600));

        updateDots(false);
        updateButton();
        startPageAnimation(0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestures.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        cancelAll(pageAnims);
        cancelAll(ambientAnims);
        super.onDestroy();
    }

    // ---------------------------------------------------------------- navigasi

    private void goTo(int next) {
        if (next < 0 || next >= COUNT || next == current) return;
        boolean forward = next > current;

        cancelAll(pageAnims);

        final View out = pages[current];
        final View in = pages[next];
        float shift = ViewMotion.dp(in, 40) * (forward ? 1 : -1);

        out.animate().cancel();
        out.animate().alpha(0f).translationX(-shift).setDuration(220).withEndAction(() -> {
            if (pages[current] != out) {
                out.setVisibility(View.GONE);
                ViewMotion.resetTree(out);
            }
        }).start();

        ViewMotion.resetTree(in);
        in.animate().cancel();
        in.setVisibility(View.VISIBLE);
        in.setAlpha(0f);
        in.setTranslationX(shift);
        in.animate().alpha(1f).translationX(0f).setDuration(320).setStartDelay(120).start();

        current = next;
        updateDots(true);
        updateButton();
        startPageAnimation(next);
    }

    private void updateDots(boolean animate) {
        for (int i = 0; i < COUNT; i++) {
            boolean active = i == current;
            dots[i].setBackgroundResource(active ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
            int w = (int) ViewMotion.dp(dots[i], active ? 22 : 8);
            if (animate) ViewMotion.animateWidth(dots[i], w);
            else {
                dots[i].getLayoutParams().width = w;
                dots[i].requestLayout();
            }
        }
    }

    private void updateButton() {
        btnNext.setText(current == COUNT - 1 ? "Mulai Berpetualang 🚀" : "Lanjut");
        findViewById(R.id.tvSkip).setVisibility(current == COUNT - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    private void finishOnboarding() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_DONE, true).apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // --------------------------------------------------------------- animasi

    private void play(List<Animator> bucket, Animator a) {
        bucket.add(a);
        a.start();
    }

    private void cancelAll(List<Animator> bucket) {
        for (Animator a : bucket) a.cancel();
        bucket.clear();
    }

    private <T extends View> T v(int id) {
        return findViewById(id);
    }

    private void enterText(int titleId, int descId) {
        play(pageAnims, ViewMotion.enter(v(titleId), 0, 24, 250));
        play(pageAnims, ViewMotion.enter(v(descId), 0, 24, 420));
    }

    private void startPageAnimation(int index) {
        switch (index) {
            case 0: page0(); break;
            case 1: page1(); break;
            case 2: page2(); break;
            case 3: page3(); break;
            default: page4(); break;
        }
    }

    /** Selamat datang: logo berdenyut, cincin menyebar, ikon melayang. */
    private void page0() {
        View logo = v(R.id.logoCircle);
        play(pageAnims, ViewMotion.popIn(logo, 0));
        play(pageAnims, ViewMotion.pulse(logo, 1.07f, 1500, 600));
        play(pageAnims, ViewMotion.wave(v(R.id.ring0a), 2600, 300));
        play(pageAnims, ViewMotion.wave(v(R.id.ring0b), 2600, 1600));

        int[] floaters = {R.id.f0a, R.id.f0b, R.id.f0c, R.id.f0d};
        for (int i = 0; i < floaters.length; i++) {
            View f = v(floaters[i]);
            play(pageAnims, ViewMotion.popIn(f, 500 + i * 160L));
            play(pageAnims, ViewMotion.floatY(f, 8 + i * 2, 1900 + i * 300L, 900 + i * 160L));
        }
        enterText(R.id.tvTitle0, R.id.tvDesc0);
    }

    /** Quest: kartu bantuan masuk satu per satu. */
    private void page1() {
        play(pageAnims, ViewMotion.enter(v(R.id.quest1), 90, 0, 250));
        play(pageAnims, ViewMotion.enter(v(R.id.quest2), 90, 0, 650));
        play(pageAnims, ViewMotion.enter(v(R.id.quest3), 90, 0, 1050));
        enterText(R.id.tvTitle1, R.id.tvDesc1);
    }

    /** Poin & rank: GD menghitung naik, bar terisi, rank menyala berurutan. */
    private void page2() {
        final TextView rankName = v(R.id.tvRankName);
        ProgressBar pb = v(R.id.pbRank);

        for (int id : RANK_IDS) v(id).setBackgroundResource(R.drawable.bg_chip_unselected);
        v(RANK_IDS[0]).setBackgroundResource(R.drawable.bg_chip_selected);
        rankName.setText(RANK_NAMES[0]);

        play(pageAnims, ViewMotion.countUp((TextView) v(R.id.tvGdCount), 250, " GD", 3000, 400));
        play(pageAnims, ViewMotion.progressTo(pb, 100, 3000, 400));

        for (int i = 1; i < RANK_IDS.length; i++) {
            final int idx = i;
            final View chip = v(RANK_IDS[i]);
            play(pageAnims, ViewMotion.bump(chip, 400 + i * 850L, () -> {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                rankName.setText(RANK_NAMES[idx]);
            }));
        }
        enterText(R.id.tvTitle2, R.id.tvDesc2);
    }

    /** Pertemanan: dua orang mendekat, hati muncul, obrolan terbentuk. */
    private void page3() {
        play(pageAnims, ViewMotion.enter(v(R.id.chipArea), 0, -20, 150));
        play(pageAnims, ViewMotion.enter(v(R.id.avatarA), -120, 0, 300));
        play(pageAnims, ViewMotion.enter(v(R.id.avatarB), 120, 0, 300));

        View heart = v(R.id.heart3);
        play(pageAnims, ViewMotion.popIn(heart, 1000));
        play(pageAnims, ViewMotion.pulse(heart, 1.25f, 900, 1500));

        play(pageAnims, ViewMotion.popIn(v(R.id.bubble1), 1400));
        play(pageAnims, ViewMotion.popIn(v(R.id.bubble2), 2100));
        enterText(R.id.tvTitle3, R.id.tvDesc3);
    }

    /** Slogan: gelombang kebaikan menyebar dari hati. */
    private void page4() {
        View heart = v(R.id.heart4);
        play(pageAnims, ViewMotion.popIn(heart, 0));
        play(pageAnims, ViewMotion.pulse(heart, 1.08f, 1200, 600));
        play(pageAnims, ViewMotion.wave(v(R.id.ring4a), 3000, 300));
        play(pageAnims, ViewMotion.wave(v(R.id.ring4b), 3000, 1300));
        play(pageAnims, ViewMotion.wave(v(R.id.ring4c), 3000, 2300));
        enterText(R.id.tvTitle4, R.id.tvDesc4);
    }
}
