package com.example.apnapay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleFallbackLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        setupGoogleFallback();

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        Button btnGoogle = findViewById(R.id.btnGoogle);
        Button btnFacebook = findViewById(R.id.btnFacebook);

        btnLogin.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, LoginActivity.class)));
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, SignUpActivity.class)));

        btnFacebook.setOnClickListener(v -> Toast.makeText(this, "Facebook login is not enabled yet.", Toast.LENGTH_SHORT).show());

        btnGoogle.setOnClickListener(v -> startGoogleSignIn());
    }

    private void setupGoogleFallback() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleFallbackLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() == null) {
                Toast.makeText(this, "Google sign-in canceled.", Toast.LENGTH_SHORT).show();
                return;
            }
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(credential).addOnCompleteListener(this, t -> {
                        if (t.isSuccessful()) {
                            FirebaseUser user = t.getResult() != null ? t.getResult().getUser() : mAuth.getCurrentUser();
                            if (user != null) upsertUser(user); 
                            goToDashboard();
                        } else {
                            Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "No Google ID token.", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in error: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startGoogleSignIn() {
        CredentialManager credentialManager = CredentialManager.create(this);
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        if (credential instanceof CustomCredential) {
                            CustomCredential customCred = (CustomCredential) credential;
                            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCred.getType())) {
                                try {
                                    GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(customCred.getData());
                                    String idToken = googleIdTokenCredential.getIdToken();
                                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                                    mAuth.signInWithCredential(firebaseCredential)
                                            .addOnCompleteListener(AuthActivity.this, task -> {
                                                if (task.isSuccessful()) {
                                                    FirebaseUser user = task.getResult() != null ? task.getResult().getUser() : mAuth.getCurrentUser();
                                                    if (user != null) upsertUser(user);
                                                    goToDashboard();
                                                } else {
                                                    Toast.makeText(AuthActivity.this, "Google sign-in failed.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } catch (Exception e) {
                                    launchGoogleFallback();
                                }
                            } else {
                                launchGoogleFallback();
                            }
                        } else {
                            launchGoogleFallback();
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        if (e instanceof NoCredentialException) {
                            launchGoogleFallback();
                        } else {
                            Toast.makeText(AuthActivity.this, "Google sign-in error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void launchGoogleFallback() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleFallbackLauncher.launch(signInIntent);
    }

    private void goToDashboard() {
        Intent i = new Intent(AuthActivity.this, DashboardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void upsertUser(FirebaseUser user) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        Map<String, Object> updates = new HashMap<>();
        updates.put("uid", user.getUid());
        updates.put("name", user.getDisplayName());
        updates.put("email", user.getEmail());
        ref.updateChildren(updates);
    }
}