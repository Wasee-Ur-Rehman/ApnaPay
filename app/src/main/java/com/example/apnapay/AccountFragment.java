package com.example.apnapay;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.apnapay.data.Db;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail, tvProfileAccountNo;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // 1. Initialize Views
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileAccountNo = view.findViewById(R.id.tvProfileAccountNo);

        LinearLayout btnReportIssue = view.findViewById(R.id.btnReportIssue);
        LinearLayout btnAboutUs = view.findViewById(R.id.btnAboutUs);
        LinearLayout btnLogout = view.findViewById(R.id.btnLogout);

        // 2. Fetch and display logged-in user data
        loadUserData();

        // 3. Set up Click Listeners
        btnReportIssue.setOnClickListener(v -> showReportIssueDialog());
        btnAboutUs.setOnClickListener(v -> showAboutUsDialog());
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Set the email directly from Firebase Auth
        if (user.getEmail() != null) {
            tvProfileEmail.setText(user.getEmail());
        }

        // Fetch Name and Account Number from Realtime Database
        DatabaseReference userRef = Db.db().getReference("Users").child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String accountNo = snapshot.child("accountNumber").getValue(String.class);

                    if (name != null && !name.trim().isEmpty()) {
                        tvProfileName.setText(name);
                    } else {
                        tvProfileName.setText("ApnaPay User");
                    }

                    if (accountNo != null) {
                        tvProfileAccountNo.setText(accountNo);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load account data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReportIssueDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Report an Issue")
                .setMessage("Are you experiencing any issues? Reach out to our support team directly at support@apnapay.com")
                .setPositiveButton("Email Us", (dialog, which) -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:support@apnapay.com"));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ApnaPay User Support Request");

                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send Email..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(requireContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAboutUsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("About ApnaPay")
                .setMessage("ApnaPay is a modern, secure fintech platform designed to provide users with a seamless digital payment experience. \n\nVersion 1.0.0")
                .setPositiveButton("Close", null)
                .show();
    }

    private void logoutUser() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of your account?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    // Sign out of Firebase
                    FirebaseAuth.getInstance().signOut();

                    // Redirect to Auth/Login screen and clear backstack
                    Intent intent = new Intent(requireActivity(), LoginActivity.class); // Change to AuthActivity if you prefer
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}