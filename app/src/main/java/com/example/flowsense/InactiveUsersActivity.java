package com.example.flowsense;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;   // ✅ Correct

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InactiveUsersActivity extends AppCompatActivity {
    private RecyclerView recyclerInactive;
    private InactiveAdapter inactiveAdapter;
    private List<User> inactiveList = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inactive_users);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.inactivemanagementtoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Inactive Users");

        recyclerInactive = findViewById(R.id.recycler_inactive);
        recyclerInactive.setLayoutManager(new LinearLayoutManager(this));

        dbRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        // Load only inactive users
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                inactiveList.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    User user = userSnap.getValue(User.class);
                    if (user != null && !user.getIsActive()) {
                        inactiveList.add(user);
                    }
                }
                inactiveAdapter = new InactiveAdapter(InactiveUsersActivity.this, inactiveList, dbRef);
                recyclerInactive.setAdapter(inactiveAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InactiveUsersActivity.this, "Failed to load inactive users: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}