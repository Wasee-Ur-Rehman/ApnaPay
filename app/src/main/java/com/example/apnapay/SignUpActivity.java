package com.example.apnapay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword, etConfirmPassword;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleFallbackLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        setupGoogleFallback();

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnSubmitSignUp = findViewById(R.id.btnSubmitSignUp);
        Button btnGoogle = findViewById(R.id.btnGoogle);
        Button btnFacebook = findViewById(R.id.btnFacebook);

        TextView tvLogin = findViewById(R.id.tvLogin);
        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));
        }

        btnSubmitSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                                Map<String, Object> data = new HashMap<>();
                                data.put("uid", user.getUid());
                                data.put("name", "");
                                data.put("email", email);
                                data.put("balance", 0);
                                ref.updateChildren(data);
                                // Go to Login after successful signup
                                goToLogin();
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign up failed: " + (task.getException()!=null?task.getException().getMessage():"Unknown error"), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

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
                return;
            }
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(credential).addOnCompleteListener(this, t -> {
                        if (t.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) upsertUserAndGo(user);
                        }
                    });
                }
            } catch (ApiException ignored) { }
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
                                    GoogleIdTokenCredential googleCred = GoogleIdTokenCredential.createFrom(customCred.getData());
                                    String idToken = googleCred.getIdToken();
                                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                                    mAuth.signInWithCredential(firebaseCredential)
                                            .addOnCompleteListener(SignUpActivity.this, task -> {
                                                if (task.isSuccessful()) {
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                    if (user != null) {
                                                        upsertUserAndGo(user);
                                                    }
                                                } else {
                                                    Toast.makeText(SignUpActivity.this, "Google sign-in failed.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(SignUpActivity.this, "Google sign-in error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void launchGoogleFallback() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleFallbackLauncher.launch(signInIntent);
    }

    private void goToLogin() {
        Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void upsertUserAndGo(FirebaseUser user) {
        // Navigate immediately; then write user to DB
        Intent i = new Intent(SignUpActivity.this, DashboardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        Map<String, Object> updates = new HashMap<>();
        updates.put("uid", user.getUid());
        updates.put("name", user.getDisplayName());
        updates.put("email", user.getEmail());
        updates.put("balance", 0);
        ref.updateChildren(updates);
    }
}