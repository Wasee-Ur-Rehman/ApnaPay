package com.example.apnapay.data;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseRepository {

    private static final SecureRandom random = new SecureRandom();

    public interface SimpleCallback { void onComplete(); }

    public static void ensureUserBootstrap(@NonNull FirebaseUser user, @NonNull SimpleCallback onComplete) {
        Log.d("ApnaPay", "Bootstrap start for uid=" + user.getUid());
        DatabaseReference usersRef = Db.db().getReference("Users").child(user.getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("ApnaPay", "Users/"+user.getUid()+" exists="+snapshot.exists());
                if (snapshot.exists()) {
                    if (!snapshot.hasChild("accountNumber")) {
                        String acc = generateDigits(10);
                        Log.d("ApnaPay", "Assigning missing accountNumber="+acc);
                        usersRef.child("accountNumber").setValue(acc);
                    }
                    onComplete.onComplete();
                } else {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid", user.getUid());
                    userData.put("name", user.getDisplayName());
                    userData.put("email", user.getEmail());
                    userData.put("balance", 0.0);
                    String acc = generateDigits(10);
                    userData.put("accountNumber", acc);
                    Log.d("ApnaPay", "Creating user with accountNumber="+acc);
                    usersRef.updateChildren(userData).addOnCompleteListener(t -> {
                        if (!t.isSuccessful()) {
                            Log.e("ApnaPay", "Failed to create user: "+ (t.getException()!=null?t.getException().getMessage():"unknown"));
                        }
                        createDefaultCard(user);
                        onComplete.onComplete();
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ApnaPay", "Bootstrap cancelled: "+error.getMessage());
                onComplete.onComplete();
            }
        });
    }

    private static void createDefaultCard(@NonNull FirebaseUser user) {
        DatabaseReference cardsRef = Db.db().getReference("Cards").child(user.getUid());
        String pushId = cardsRef.push().getKey();
        if (pushId == null) return;
        Map<String, Object> card = new HashMap<>();
        card.put("cardNumber", generateGroupedCardNumber());
        card.put("expiryDate", generateExpiry());
        card.put("cvv", generateDigits(3));
        String holder = !TextUtils.isEmpty(user.getDisplayName()) ? user.getDisplayName() : (user.getEmail() != null ? user.getEmail().split("@")[0] : "ApnaPay User");
        card.put("cardHolderName", holder);
        Log.d("ApnaPay", "Creating default card id="+pushId);
        cardsRef.child(pushId).updateChildren(card);
    }

    private static String generateDigits(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private static String generateGroupedCardNumber() {
        String raw = generateDigits(16);
        return raw.substring(0,4)+" "+raw.substring(4,8)+" "+raw.substring(8,12)+" "+raw.substring(12);
    }

    private static String generateExpiry() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 3);
        int mm = cal.get(Calendar.MONTH) + 1;
        int yy = cal.get(Calendar.YEAR) % 100;
        return String.format(Locale.US, "%02d/%02d", mm, yy);
    }
}
