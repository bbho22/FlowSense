package com.example.flowsense;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Dashboard extends AppCompatActivity {
    private TextView tvToolbarName, tvDate, tvInfo;
    private DatabaseReference dbRef;
    private int cycleLength = 28; // default, overwritten by Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnLogout = findViewById(R.id.btn_logout);
        Button btnCycle = findViewById(R.id.btn_cycle);
        Button btnSymptoms = findViewById(R.id.btn_symptoms);
        Button btnFertility = findViewById(R.id.btn_fertility);
        Button btnOpenProfile = findViewById(R.id.btn_update_details);
        tvToolbarName = findViewById(R.id.tv_toolbar_name);
        tvDate = findViewById(R.id.tv_date);
        tvInfo = findViewById(R.id.tv_info);

        String safeEmailKey = getIntent().getStringExtra("safeEmailKey");

        // Point to the correct user node in Realtime Database
        dbRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(safeEmailKey);

        // Load user data
        dbRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                Integer cycleVal = snapshot.child("cycleLength").getValue(Integer.class);
                if (cycleVal != null) {
                    cycleLength = cycleVal; // 👈 set class field
                }

                // Update UI
                tvToolbarName.setText("Welcome, " + firstName);
                tvInfo.setText("Email: " + email + " | Cycle Days: " + cycleLength);

                // Show today’s date dynamically
                String today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
                tvDate.setText("Date: " + today);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(Dashboard.this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply(); // clear Remember Me

            Intent intent = new Intent(Dashboard.this, Login.class);
            startActivity(intent);
            finish();
        });

        btnCycle.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Cycle.class);
            intent.putExtra("cycleLength", cycleLength); // 👈 pass cycleLength
            intent.putExtra("safeEmailKey", safeEmailKey);
            startActivity(intent);
        });

        btnSymptoms.setOnClickListener(v -> {
            CycleUtils.getCurrentCycleId(Dashboard.this, dbRef, new CycleUtils.OnCycleCheckListener() {
                @Override
                public void onCycleFound(String cycleId) {
                    Intent intent = new Intent(Dashboard.this, Symptom.class);
                    intent.putExtra("safeEmailKey", safeEmailKey);
                    intent.putExtra("cycleId", cycleId);
                    startActivity(intent);
                }

                @Override
                public void onNoCycle() {
                    // Toast already shown in helper
                }
            });

        });

        btnFertility.setOnClickListener(v -> {
            CycleUtils.getCurrentCycleId(Dashboard.this, dbRef, new CycleUtils.OnCycleCheckListener() {
                @Override
                public void onCycleFound(String cycleId) {
                    Intent intent = new Intent(Dashboard.this, Fertility.class);
                    intent.putExtra("safeEmailKey", safeEmailKey);
                    intent.putExtra("cycleId", cycleId);
                    intent.putExtra("cycleLength", cycleLength);
                    startActivity(intent);
                }

                @Override
                public void onNoCycle() {
                    // Toast already shown in helper
                }
            });

        });

        btnOpenProfile.setOnClickListener(v -> {
            Intent openProfileIntent = new Intent(Dashboard.this, MyDetails.class);
            openProfileIntent.putExtra("safeEmailKey", safeEmailKey);
            startActivity(openProfileIntent);
        });


    }
}