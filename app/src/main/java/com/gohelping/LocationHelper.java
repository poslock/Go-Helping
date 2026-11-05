package com.gohelping;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Membantu menyimpan & membaca wilayah user (kota + kelurahan).
 *
 * Kenapa ada "key"? User bisa menulis "Kendari", "kendari ", "Kota Kendari".
 * Supaya semuanya dianggap SAMA, yang dipakai untuk filter query adalah
 * versi ter-normalisasi (cityKey / kelurahanKey), sedangkan versi aslinya
 * (city / kelurahan) dipakai untuk tampilan.
 */
public final class LocationHelper {

    private LocationHelper() {}

    public interface Callback {
        void onLoaded(String cityKey, String kelurahanKey, String cityLabel, String kelurahanLabel);
        void onMissing();              // user belum punya kota/kelurahan (user lama)
        void onError(Exception e);
    }

    /** "  Kota   Kendari " -> "kendari" ; "Kel. Mandonga" -> "mandonga" */
    public static String normalize(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        t = t.replaceFirst("^(kota|kabupaten|kab\\.?)\\s+", "");
        t = t.replaceFirst("^(kelurahan|kel\\.?|desa)\\s+", "");
        return t;
    }

    /** Field yang disimpan ke dokumen users/{uid} (dan disalin ke quest). */
    public static Map<String, Object> toFields(String city, String kelurahan) {
        Map<String, Object> m = new HashMap<>();
        m.put("city", city.trim());
        m.put("kelurahan", kelurahan.trim());
        m.put("cityKey", normalize(city));
        m.put("kelurahanKey", normalize(kelurahan));
        return m;
    }

    /** Baca wilayah user yang sedang login. */
    public static void loadMyArea(final Callback cb) {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) { cb.onError(new IllegalStateException("Belum login")); return; }

        FirebaseFirestore.getInstance().collection("users").document(u.getUid()).get()
                .addOnSuccessListener((DocumentSnapshot d) -> {
                    String cityKey = d.getString("cityKey");
                    String kelKey = d.getString("kelurahanKey");
                    if (cityKey == null || cityKey.isEmpty() || kelKey == null || kelKey.isEmpty()) {
                        cb.onMissing();
                    } else {
                        cb.onLoaded(cityKey, kelKey, d.getString("city"), d.getString("kelurahan"));
                    }
                })
                .addOnFailureListener(cb::onError);
    }

    /** Simpan wilayah untuk user yang sedang login (dipakai dialog user lama). */
    public static void saveMyArea(String city, String kelurahan,
                                  com.google.android.gms.tasks.OnSuccessListener<Void> ok,
                                  com.google.android.gms.tasks.OnFailureListener fail) {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) return;
        FirebaseFirestore.getInstance().collection("users").document(u.getUid())
                .update(toFields(city, kelurahan))
                .addOnSuccessListener(ok)
                .addOnFailureListener(fail);
    }
}
