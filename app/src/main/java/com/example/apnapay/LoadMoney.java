package com.example.apnapay;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apnapay.data.Db;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LoadMoney extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_load_money);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        TextView tvAccount = findViewById(R.id.tvAccount);
        ImageView ivCopyLocal = findViewById(R.id.ivCopyLocal);
        TextView tvCopyLocal = findViewById(R.id.tvCopyLocal);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        Db.db().getReference("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String acc = snapshot.child("accountNumber").getValue(String.class);
                if (acc != null) tvAccount.setText(acc);
            }
            @Override public void onCancelled(DatabaseError error) { }
        });

        ivCopyLocal.setOnClickListener(v -> {
            String acc = tvAccount.getText().toString();
            android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(android.content.ClipData.newPlainText("account", acc));
            Toast.makeText(this, "Account copied", Toast.LENGTH_SHORT).show();
        });
        tvCopyLocal.setOnClickListener(v -> ivCopyLocal.performClick());
    }
}