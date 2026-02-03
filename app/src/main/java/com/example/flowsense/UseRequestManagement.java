package com.example.flowsense;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;import android.os.Bundle;

public class UseRequestManagement extends AppCompatActivity {
    private RecyclerView recyclerRequests;
    private RequestAdapter requestAdapter;
    private List<Request> requestList = new ArrayList<>();
    private DatabaseReference usersRef, requestsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_use_request_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.useraccountrequestttoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User Requests");

        recyclerRequests = findViewById(R.id.recycler_requests);
        recyclerRequests.setLayoutManager(new LinearLayoutManager(this));

        usersRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");
        requestsRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("activation_requests");

        // Load only pending requests
        requestsRef.orderByChild("status").equalTo("pending")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        requestList.clear();
                        for (DataSnapshot reqSnap : snapshot.getChildren()) {
                            Request req = reqSnap.getValue(Request.class);
                            if (req != null) {
                                req.setRequestId(reqSnap.getKey()); // keep ID for updates
                                requestList.add(req);
                            }
                        }
                        requestAdapter = new RequestAdapter(UseRequestManagement.this, requestList, usersRef, requestsRef);
                        recyclerRequests.setAdapter(requestAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UseRequestManagement.this, "Failed to load requests: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}