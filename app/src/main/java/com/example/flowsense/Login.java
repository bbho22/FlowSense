package com.example.flowsense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private CheckBox checkboxRemember;

    private TextInputEditText etEmail, etPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView backToLogin = findViewById(R.id.tv_register);
        Button buttonToLogin = findViewById(R.id.btn_login);
        checkboxRemember = findViewById(R.id.checkbox_remember);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);


        mAuth = FirebaseAuth.getInstance();


        // Remember me functionality
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean remember = prefs.getBoolean("remember", false);

        if (remember && FirebaseAuth.getInstance().getCurrentUser() != null) {
            // User is already logged in and chose Remember Me
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String safeEmailKey = email.replace(".", "_dot_").replace("@", "_at_");

            Intent intent = new Intent(Login.this, Dashboard.class);
            intent.putExtra("safeEmailKey", safeEmailKey);
            startActivity(intent);
            finish();
        }




        // Set click listener to open register
        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to go to LoginActivity
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                // Optional: finish current activity so user can't return with back button
                finish();
            }
        });
        buttonToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firebase login
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("remember", checkboxRemember.isChecked());
                            editor.apply();
                            // Sanitize email key
                            String safeEmailKey = email.replace(".", "_dot_")
                                    .replace("@", "_at_");

                            // Go to Dashboard
                            Intent intent = new Intent(Login.this, Dashboard.class);
                            intent.putExtra("safeEmailKey", safeEmailKey);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Login.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });


            }
        });

    }
}