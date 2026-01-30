package com.example.flowsense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private TextInputEditText txtFName, txtSName, txtEmail, txtDCycle, txtPassword;
    private MaterialButton btnRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        // ✅ Point to correct path in Realtime Database
        dbRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        // Views
        txtFName = findViewById(R.id.txt_FName);
        txtSName = findViewById(R.id.txt_SName);
        txtEmail = findViewById(R.id.txt_Email);
        txtDCycle = findViewById(R.id.txt_DCycle);
        txtPassword = findViewById(R.id.txt_password);
        btnRegister = findViewById(R.id.btn_register);

        TextView backToLogin = findViewById(R.id.tv_back_login);
        backToLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> validateFields());
    }

    private void validateFields() {
        String firstName = txtFName.getText().toString().trim();
        String secondName = txtSName.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String cycleStr = txtDCycle.getText().toString().trim();
        String typedPassword = txtPassword.getText().toString().trim();

        boolean isValid = true;
        int cycleLength = 0;

        // First name
        if (firstName.isEmpty()) {
            txtFName.setError("First name is required");
            isValid = false;
        } else txtFName.setError(null);

        // Second name
        if (secondName.isEmpty()) {
            txtSName.setError("Second name is required");
            isValid = false;
        } else txtSName.setError(null);

        // Email
        if (email.isEmpty()) {
            txtEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Enter a valid email");
            isValid = false;
        } else txtEmail.setError(null);

        // Cycle
        if (cycleStr.isEmpty()) {
            txtDCycle.setError("Cycle length is required");
            isValid = false;
        } else {
            try {
                cycleLength = Integer.parseInt(cycleStr);
                if (cycleLength < 21 || cycleLength > 35) {
                    txtDCycle.setError("Cycle must be 21–35 days");
                    isValid = false;
                } else txtDCycle.setError(null);
            } catch (NumberFormatException e) {
                txtDCycle.setError("Enter a valid number");
                isValid = false;
            }
        }
        final int finalCycleLength = cycleLength;

        // Password
        if (typedPassword.isEmpty()) {
            txtPassword.setError("Password is required");
            isValid = false;
        } else if (typedPassword.length() < 6) {
            txtPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else txtPassword.setError(null);

        if (!isValid) return;

        btnRegister.setEnabled(false);

        String safeEmailKey = sanitizeEmail(email);

        mAuth.createUserWithEmailAndPassword(email, typedPassword)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        btnRegister.setEnabled(true);
                        Toast.makeText(MainActivity.this,
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Registration failed",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser == null) {
                        btnRegister.setEnabled(true);
                        Toast.makeText(MainActivity.this,
                                "User creation failed",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("firstName", firstName);
                    userMap.put("secondName", secondName);
                    userMap.put("email", email);
                    userMap.put("cycleLength", finalCycleLength);
                    userMap.put("createdAt", System.currentTimeMillis());
                    userMap.put("uid", firebaseUser.getUid());

                    Log.d("FirebaseDB", "Attempting to write user data...");

                    dbRef.child(safeEmailKey).setValue(userMap)
                            .addOnSuccessListener(unused -> {
                                Log.d("FirebaseDB", "Write succeeded!");
                                btnRegister.setEnabled(true);
                                Toast.makeText(MainActivity.this,
                                        "Registration successful",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, Dashboard.class);
                                intent.putExtra("safeEmailKey", safeEmailKey); // pass the key to Dashboard
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseDB", "Write failed: " + e.getMessage(), e);
                                btnRegister.setEnabled(true);
                                Toast.makeText(MainActivity.this,
                                        "DB Error: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                });
    }

    private String sanitizeEmail(String email) {
        return email.replace(".", "_dot_")
                .replace("@", "_at_");
    }
}