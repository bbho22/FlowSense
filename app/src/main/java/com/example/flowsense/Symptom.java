package com.example.flowsense;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Symptom extends AppCompatActivity {
    Spinner spinnerBleeding;
    CheckBox cbCramps, cbHeadache, cbBackache;
    CheckBox cbHappy, cbSad, cbAnxious, cbAngry;
    RadioGroup radioSex;
    Button btnSaveSymptom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_symptom);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.symptomtoolbar);
        setSupportActionBar(toolbar);
        setContentView(R.layout.activity_symptom);
        spinnerBleeding = findViewById(R.id.spinner_bleeding);
        cbCramps = findViewById(R.id.checkbox_cramps);
        cbHeadache = findViewById(R.id.checkbox_headache);
        cbBackache = findViewById(R.id.checkbox_backache);

        cbHappy = findViewById(R.id.checkbox_happy);
        cbSad = findViewById(R.id.checkbox_sad);
        cbAnxious = findViewById(R.id.checkbox_anxious);
        cbAngry = findViewById(R.id.checkbox_angry);

        radioSex = findViewById(R.id.radio_sex);
        btnSaveSymptom = findViewById(R.id.btn_save_symptom);

        btnSaveSymptom.setOnClickListener(v -> saveSymptoms());


        spinnerBleeding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String bleeding = parent.getItemAtPosition(position).toString();
                Toast.makeText(Symptom.this, "Bleeding: " + bleeding, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        // end of on create
    }

    private void saveSymptoms() {
        // Bleeding intensity (single value from spinner)
        String bleeding = spinnerBleeding.getSelectedItem().toString();

        // Pain (multiple values from checkboxes)
        List<String> painList = new ArrayList<>();
        if (cbCramps.isChecked()) painList.add("cramps");
        if (cbHeadache.isChecked()) painList.add("headache");
        if (cbBackache.isChecked()) painList.add("backache");

        // Mood (multiple values from checkboxes)
        List<String> moodList = new ArrayList<>();
        if (cbHappy.isChecked()) moodList.add("happy");
        if (cbSad.isChecked()) moodList.add("sad");
        if (cbAnxious.isChecked()) moodList.add("anxious");
        if (cbAngry.isChecked()) moodList.add("angry");

        // Sex (single value from radio group)
        int selectedSexId = radioSex.getCheckedRadioButtonId();
        String sex = "";
        if (selectedSexId != -1) {
            RadioButton selectedRadio = findViewById(selectedSexId);
            sex = selectedRadio.getText().toString().toLowerCase();
        }

        // Build symptom data map
        Map<String, Object> symptomData = new HashMap<>();
        symptomData.put("bleeding", bleeding);
        symptomData.put("pain", painList);
        symptomData.put("mood", moodList);
        symptomData.put("sex", sex);

        // Current date (you can format with LocalDate.now())
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Firebase reference
        String safeEmailKey = getIntent().getStringExtra("safeEmailKey");
        String cycleId = getIntent().getStringExtra("cycleId"); // pass current cycleId from Cycle.java

        DatabaseReference userRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users")
                .child(safeEmailKey);

        userRef.child("cycles")
                .child(cycleId)
                .child("symptoms")
                .child(today)
                .setValue(symptomData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Symptom.this, "Symptoms saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Symptom.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}