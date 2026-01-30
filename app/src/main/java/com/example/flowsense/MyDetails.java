package com.example.flowsense;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class MyDetails extends AppCompatActivity {
    private DatabaseReference usersRef;
    private String origFname, origSname, origCycle, origAge, origLocation, origPhone;

    // Views as class fields
    private TextInputEditText inputFname, inputSname, inputCycleLength, inputAge, inputLocation, inputPhone;
    private MaterialButton btnSaveProfile, btnUpdatePassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String safeEmailKey = getIntent().getStringExtra("safeEmailKey");
        // Initialize views
        inputFname = findViewById(R.id.input_fname);
        inputSname = findViewById(R.id.input_sname);
        inputCycleLength = findViewById(R.id.input_cycle_length);
        inputAge = findViewById(R.id.input_age);
        inputLocation = findViewById(R.id.input_location);
        inputPhone = findViewById(R.id.input_phone);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnUpdatePassword = findViewById(R.id.btn_update_password);

        // Disable button initially
        //btnSaveProfile.setEnabled(false);

        // Firebase reference
        usersRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(safeEmailKey);

        // Load user details
        if (safeEmailKey != null) {
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        origFname = snapshot.child("firstName").getValue(String.class);
                        origSname = snapshot.child("secondName").getValue(String.class);
                        origCycle = snapshot.child("cycleLength").getValue(String.class);
                        origAge = snapshot.child("age").getValue(String.class);
                        origLocation = snapshot.child("location").getValue(String.class);
                        origPhone = snapshot.child("phone").getValue(String.class);

                        // Populate fields
                        inputFname.setText(origFname);
                        inputSname.setText(origSname);
                        inputCycleLength.setText(origCycle);
                        inputAge.setText(origAge);
                        inputLocation.setText(origLocation);
                        inputPhone.setText(origPhone);

                        // Watch for changes
                        addTextWatchers();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MyDetails.this, "Failed to load profile", Toast.LENGTH_LONG).show();
                }
            });
        }

        // Save updates
        btnSaveProfile.setOnClickListener(v -> updateProfile());
    }

    private void addTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIfChanged();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        inputFname.addTextChangedListener(watcher);
        inputSname.addTextChangedListener(watcher);
        inputCycleLength.addTextChangedListener(watcher);
        inputAge.addTextChangedListener(watcher);
        inputLocation.addTextChangedListener(watcher);
        inputPhone.addTextChangedListener(watcher);
    }

    private void checkIfChanged() {
        boolean changed =
                !getText(inputFname).equals(origFname) ||
                        !getText(inputSname).equals(origSname) ||
                        !getText(inputCycleLength).equals(origCycle) ||
                        !getText(inputAge).equals(origAge) ||
                        !getText(inputLocation).equals(origLocation) ||
                        !getText(inputPhone).equals(origPhone);

        btnSaveProfile.setEnabled(changed);
    }

    private void updateProfile() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", getText(inputFname));
        updates.put("secondName", getText(inputSname));
        updates.put("cycleLength", getText(inputCycleLength));
        updates.put("age", getText(inputAge));
        updates.put("location", getText(inputLocation));
        updates.put("phone", getText(inputPhone));

        usersRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_LONG).show();
                    // Reset originals
                    origFname = getText(inputFname);
                    origSname = getText(inputSname);
                    origCycle = getText(inputCycleLength);
                    origAge = getText(inputAge);
                    origLocation = getText(inputLocation);
                    origPhone = getText(inputPhone);
                    btnSaveProfile.setEnabled(false); // disable again until next change
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }


}