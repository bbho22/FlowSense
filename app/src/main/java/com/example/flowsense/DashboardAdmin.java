package com.example.flowsense;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.widget.Toolbar;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardAdmin extends AppCompatActivity {
    private TextView tvToolbarName, tvTotalUsers, tvTotalAdmins, tvInactiveUsers, tvBlockedUsers;
    private Button btnLogout, btnViewUsers, btnManageAdmins, btnManageInactive, btnUserRequest;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bind views
        tvToolbarName = findViewById(R.id.tv_toolbar_name);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvTotalAdmins = findViewById(R.id.tv_total_admins);
        tvInactiveUsers = findViewById(R.id.tv_inactive_users);
        tvBlockedUsers = findViewById(R.id.tv_blocked_users);

        btnLogout = findViewById(R.id.btn_logout);
        btnViewUsers = findViewById(R.id.btn_view_users);
        btnManageAdmins = findViewById(R.id.btn_manage_admins);
        btnManageInactive = findViewById(R.id.btn_manage_inactive);
        btnUserRequest = findViewById(R.id.btn_user_request);
        // Get safeEmailKey from intent
        String safeEmailKey = getIntent().getStringExtra("safeEmailKey");

        // Point to user node
        dbRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        // Load current admin’s profile
        dbRef.child(safeEmailKey).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                tvToolbarName.setText("Welcome, " + firstName);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(DashboardAdmin.this, "Failed to load admin profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });

        // Load counts dynamically
        dbRef.get().addOnSuccessListener(snapshot -> {
            int totalUsers = (int) snapshot.getChildrenCount();
            int totalAdmins = 0;
            int inactiveUsers = 0;
            int blockedUsers = 0;

            for (DataSnapshot userSnap : snapshot.getChildren()) {
                String role = userSnap.child("role").getValue(String.class);
                Boolean isActive = userSnap.child("isActive").getValue(Boolean.class);
                Boolean isBlocked = userSnap.child("isBlocked").getValue(Boolean.class);

                if ("admin".equalsIgnoreCase(role)) {
                    totalAdmins++;
                }
                if (isActive != null && !isActive) {
                    inactiveUsers++;
                }
                if (isBlocked != null && isBlocked) {
                    blockedUsers++;
                }
            }

            tvTotalUsers.setText("Total Users " + totalUsers);
            tvTotalAdmins.setText("Total Admins " + totalAdmins);
            tvInactiveUsers.setText("Total Inactive Users " + inactiveUsers);
            tvBlockedUsers.setText("Total Blocked Users " + blockedUsers);
        }).addOnFailureListener(e -> {
            Toast.makeText(DashboardAdmin.this, "Failed to load counts: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });


        //count pending
        DatabaseReference requestsRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("activation_requests");

        // Count pending requests
        requestsRef.orderByChild("status").equalTo("pending")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long pendingCount = snapshot.getChildrenCount();
                        tvBlockedUsers.setText("User Request " + pendingCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvBlockedUsers.setText("Error loading requests");
                    }
                });


        // Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            Intent intent = new Intent(DashboardAdmin.this, Login.class);
            startActivity(intent);
            finish();
        });

        // Navigation buttons
        btnViewUsers.setOnClickListener(v -> {
            startActivity(new Intent(DashboardAdmin.this, UserManagementActivity.class));
        });

        btnManageAdmins.setOnClickListener(v -> {
            startActivity(new Intent(DashboardAdmin.this, AdminManagementActivity.class));
        });

        btnManageInactive.setOnClickListener(v -> {
            startActivity(new Intent(DashboardAdmin.this, InactiveUsersActivity.class));
        });

        btnUserRequest.setOnClickListener(v -> {
            startActivity(new Intent(DashboardAdmin.this, UseRequestManagement.class));
        });
    }
}