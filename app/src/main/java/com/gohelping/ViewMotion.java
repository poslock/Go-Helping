package com.gohelping;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Kumpulan animasi tanpa library tambahan.
 * Semua method mengembalikan Animator yang BELUM dijalankan -> panggil .start()
 * (di OnboardingActivity dipakai lewat play()).
 */
public final class ViewMotion {

    private ViewMotion() {}

    public static float dp(View v, float d) {
        return d * v.getResources().getDisplayMetrics().density;
    }

    /** Kembalikan semua transformasi view (dan anak-anaknya) ke normal. */
    public static void resetTree(View v) {
        v.setAlpha(1f);
        v.setScaleX(1f);
        v.setScaleY(1f);
        v.setTranslationX(0f);
        v.setTranslationY(0f);
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) resetTree(g.getChildAt(i));
        }
    }

    /** Fade + geser masuk dari offset (dp). */
    public static Animator enter(View v, float fromXdp, float fromYdp, long delay) {
        v.setAlpha(0f);
        v.setTranslationX(dp(v, fromXdp));
        v.setTranslationY(dp(v, fromYdp));
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, dp(v, fromXdp), 0f),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, dp(v, fromYdp), 0f));
        a.setDuration(550);
        a.setStartDelay(delay);
        a.setInterpolator(new DecelerateInterpolator(1.6f));
        return a;
    }

    /** Muncul membesar dengan efek "pop". */
    public static Animator popIn(View v, long delay) {
        v.setAlpha(0f);
        v.setScaleX(0.3f);
        v.setScaleY(0.3f);
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.3f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.3f, 1f));
        a.setDuration(480);
        a.setStartDelay(delay);
        a.setInterpolator(new OvershootInterpolator(1.7f));
        return a;
    }

    /** Melayang naik-turun terus-menerus. */
    public static Animator floatY(View v, float amplitudeDp, long duration, long delay) {
        ObjectAnimator a = ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, 0f, -dp(v, amplitudeDp));
        a.setDuration(duration);
        a.setStartDelay(delay);
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.setRepeatMode(ValueAnimator.REVERSE);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        return a;
    }

    /** Berdenyut (membesar-mengecil) terus-menerus. */
    public static Animator pulse(View v, float scaleTo, long duration, long delay) {
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, scaleTo),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, scaleTo));
        a.setDuration(duration);
        a.setStartDelay(delay);
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.setRepeatMode(ValueAnimator.REVERSE);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        return a;
    }

    /** Gelombang cincin yang melebar lalu memudar (efek "sinyal kebaikan"). */
    public static Animator wave(View v, long duration, long delay) {
        v.setAlpha(0f);
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.8f, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.7f, 2.1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.7f, 2.1f));
        a.setDuration(duration);
        a.setStartDelay(delay);
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.setRepeatMode(ValueAnimator.RESTART);
        a.setInterpolator(new LinearInterpolator());
        return a;
    }

    /** Membesar sebentar lalu kembali; onStart dipanggil saat animasi mulai. */
    public static Animator bump(View v, long delay, final Runnable onStart) {
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.18f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.18f, 1f));
        a.setDuration(420);
        a.setStartDelay(delay);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        if (onStart != null) {
            a.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationStart(Animator animation) { onStart.run(); }
            });
        }
        return a;
    }

    /** Angka naik dari 0 ke target, mis. "250 GD". */
    public static Animator countUp(final TextView tv, int to, final String suffix, long duration, long delay) {
        tv.setText("0" + suffix);
        ValueAnimator a = ValueAnimator.ofInt(0, to);
        a.setDuration(duration);
        a.setStartDelay(delay);
        a.setInterpolator(new DecelerateInterpolator());
        a.addUpdateListener(anim -> tv.setText(anim.getAnimatedValue() + suffix));
        return a;
    }

    /** Progress bar terisi dari 0 ke target. */
    public static Animator progressTo(ProgressBar pb, int to, long duration, long delay) {
        pb.setProgress(0);
        ObjectAnimator a = ObjectAnimator.ofInt(pb, "progress", 0, to);
        a.setDuration(duration);
        a.setStartDelay(delay);
        a.setInterpolator(new DecelerateInterpolator());
        return a;
    }

    /** Ubah lebar view dengan animasi (untuk indikator titik). */
    public static void animateWidth(final View v, final int toPx) {
        final ViewGroup.LayoutParams lp = v.getLayoutParams();
        ValueAnimator a = ValueAnimator.ofInt(lp.width, toPx);
        a.setDuration(240);
        a.addUpdateListener(anim -> {
            lp.width = (int) anim.getAnimatedValue();
            v.setLayoutParams(lp);
        });
        a.start();
    }
}
