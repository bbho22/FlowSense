package com.example.flowsense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.graphics.Color;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Fertility extends AppCompatActivity {
    private TextInputEditText inputBBT;
    private Spinner spinnerMucus;
    private Button btnSaveFertility;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fertility);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        PieChart pieChart = findViewById(R.id.pieChart);
        inputBBT = findViewById(R.id.input_bbt);
        spinnerMucus = findViewById(R.id.spinner_mucus);
        btnSaveFertility = findViewById(R.id.btn_save_fertility);



        // Get cycleLength from Intent
        int cycleLength = getIntent().getIntExtra("cycleLength", 28);
        String safeEmailKey = getIntent().getStringExtra("safeEmailKey");
        String cycleId = getIntent().getStringExtra("cycleId");



        // Calculate dynamic distribution
        int menstrualDays = 5;
        int follicularDays = 7;
        int ovulationDays = 4;
        int lutealDays = cycleLength - (menstrualDays + follicularDays + ovulationDays);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(menstrualDays, "Menstrual (1-5)"));
        entries.add(new PieEntry(follicularDays, "Follicular (6-12)"));
        entries.add(new PieEntry(ovulationDays, "Ovulation (13-16)"));
        entries.add(new PieEntry(lutealDays, "Luteal (17-" + cycleLength + ")"));

        PieDataSet dataSet = new PieDataSet(entries, "Cycle Phases");
        dataSet.setColors(Arrays.asList(
                Color.parseColor("#FF6F61"), // Menstrual
                Color.parseColor("#6BAED6"), // Follicular
                Color.parseColor("#FFD700"), // Ovulation
                Color.parseColor("#9ACD32")  // Luteal
        ));
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Donut effect
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);

        pieChart.setCenterText(cycleLength + "-Day Cycle");
        pieChart.setCenterTextSize(16f);
        pieChart.invalidate();



        btnSaveFertility.setOnClickListener(v -> {
            saveFertilityData(safeEmailKey, cycleId);
        });
// end of oncreate
    }

    private void saveFertilityData(String safeEmailKey, String cycleId) {
        if (safeEmailKey == null || cycleId == null) {
            Toast.makeText(this, "Missing cycle info!", Toast.LENGTH_LONG).show();
            return;
        }

        // Get values
        String bbtValue = inputBBT.getText() != null ? inputBBT.getText().toString().trim() : "";
        String mucusValue = spinnerMucus.getSelectedItem().toString();

        // Current date
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Build fertility data
        Map<String, Object> fertilityData = new HashMap<>();
        fertilityData.put("temperature", bbtValue);
        fertilityData.put("mucus", mucusValue);

        // Save under cycle → fertility → date
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(safeEmailKey);

        userRef.child("cycles")
                .child(cycleId)
                .child("fertility")
                .child(today)
                .setValue(fertilityData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Fertility.this, "Fertility data saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Fertility.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}