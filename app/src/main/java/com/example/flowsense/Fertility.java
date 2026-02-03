package com.example.flowsense;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.graphics.Color;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Fertility extends AppCompatActivity {
    private TextInputEditText inputBBT;
    private Spinner spinnerMucus;
    private Button btnSaveFertility;
    private RecyclerView calendarRecycler;

    private String safeEmailKey;
    private String cycleId;

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
        calendarRecycler = findViewById(R.id.calendarRecycler);

        // Get cycle info from Intent
        int cycleLength = getIntent().getIntExtra("cycleLength", 28);
        safeEmailKey = getIntent().getStringExtra("safeEmailKey");
        cycleId = getIntent().getStringExtra("cycleId");
        int periodLength = getIntent().getIntExtra("periodLength", -1);

        // Pie chart setup
        int menstrualDays = periodLength;
        int follicularDays = periodLength + 1;
        int ovulationDays = 4;
        int lutealDays = cycleLength - (menstrualDays + follicularDays + ovulationDays);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(menstrualDays, "Menstrual (0-" + periodLength + ")"));
        entries.add(new PieEntry(follicularDays, "Follicular (" + follicularDays + "-12)"));
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
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setCenterText(cycleLength + "-Day Cycle");
        pieChart.setCenterTextSize(16f);
        pieChart.invalidate();

        btnSaveFertility.setOnClickListener(v -> {
            saveFertilityData(safeEmailKey, cycleId);
        });

        // Query cycle data
        DatabaseReference cycleRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("cycles")
                .child(safeEmailKey)
                .child(cycleId);

        cycleRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String mensEndDate = snapshot.child("mensEndDate").getValue(String.class);
                if (mensEndDate != null) {
                    markFertileDays(mensEndDate);
                }
            }
        });
    }

    private void saveFertilityData(String safeEmailKey, String cycleId) {
        if (safeEmailKey == null || cycleId == null) {
            Toast.makeText(this, "Missing cycle info!", Toast.LENGTH_LONG).show();
            return;
        }

        String bbtValue = inputBBT.getText() != null ? inputBBT.getText().toString().trim() : "";
        String mucusValue = spinnerMucus.getSelectedItem().toString();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> fertilityData = new HashMap<>();
        fertilityData.put("temperature", bbtValue);
        fertilityData.put("mucus", mucusValue);

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

    private void markFertileDays(String mensEndDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            Date endDate = sdf.parse(mensEndDate);

            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);

            // Collect fertile days (next 3 days after period end)
            HashSet<Integer> fertileDays = new HashSet<>();
            for (int i = 1; i <= 3; i++) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                fertileDays.add(cal.get(Calendar.DAY_OF_MONTH));
            }

            // Build current month days
            Calendar now = Calendar.getInstance();
            int daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH);
            List<Integer> days = new ArrayList<>();
            for (int i = 1; i <= daysInMonth; i++) {
                days.add(i);
            }

            // Show custom calendar
            calendarRecycler.setLayoutManager(new GridLayoutManager(this, 7));
            CalendarAdapter adapter = new CalendarAdapter(days, fertileDays);
            calendarRecycler.setAdapter(adapter);

            Toast.makeText(this, "Marked fertile days after " + mensEndDate, Toast.LENGTH_LONG).show();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}