package com.example.flowsense;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AccountRequest extends AppCompatActivity {
    private EditText textArea;
    private MaterialButton btnSendRequest;
    private TextView tvBackLogin;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_request);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textArea = findViewById(R.id.textArea);
        btnSendRequest = findViewById(R.id.btn_send_request);
        tvBackLogin = findViewById(R.id.tv_back_login);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("activation_requests");

        btnSendRequest.setOnClickListener(v -> {
            String message = textArea.getText().toString().trim();
            String email = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "unknown";

            if (message.isEmpty()) {
                Toast.makeText(AccountRequest.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save request to Firebase
            String requestId = dbRef.push().getKey();
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("email", email);
            requestData.put("message", message);
            requestData.put("timestamp", System.currentTimeMillis());
            requestData.put("status", "pending"); // ✅ default


            dbRef.child(requestId).setValue(requestData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AccountRequest.this, "Request sent to admin", Toast.LENGTH_LONG).show();
                            textArea.setText("");
                        } else {
                            Toast.makeText(AccountRequest.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvBackLogin.setOnClickListener(v -> {
            startActivity(new Intent(AccountRequest.this, Login.class));
            finish();
        });
    }
}