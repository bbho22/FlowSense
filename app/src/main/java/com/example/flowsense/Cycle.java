package com.example.flowsense;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import androidx.annotation.NonNull;

public class Cycle extends AppCompatActivity {
    DatePicker dateStart, dateEnd;
    RadioGroup radioCycleType;
    RadioButton radioRegular, radioIrregular;
    TextView tvSummary;
    Button btnSaveCycle;
    RecyclerView recyclerCycles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cycle);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dateStart = findViewById(R.id.date_start);
        dateEnd = findViewById(R.id.date_end);
        radioCycleType = findViewById(R.id.radio_cycle_type);
        radioRegular = findViewById(R.id.radio_regular);
        radioIrregular = findViewById(R.id.radio_irregular);
        tvSummary = findViewById(R.id.tv_summary);
        btnSaveCycle = findViewById(R.id.btn_save_cycle);
        recyclerCycles = findViewById(R.id.recycler_cycles);


        // Assume cycleLength comes from Dashboard intent or Firebase
        int cycleLength = getIntent().getIntExtra("cycleLength", 28);
        String safeEmailKey = getIntent().getStringExtra("safeEmailKey");

        dateStart.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> {
            Calendar startCal = Calendar.getInstance();
            startCal.set(year, monthOfYear, dayOfMonth);

            Calendar endCal = (Calendar) startCal.clone();

            if (radioRegular.isChecked()) {
                // Regular cycle: straight cycleLength days
                endCal.add(Calendar.DAY_OF_MONTH, cycleLength - 1);
            } else {
                // Irregular cycle: allow range (26–35 days)
                endCal.add(Calendar.DAY_OF_MONTH, 26 - 1); // default min
                // You can later show dialog to let user pick within range
            }

            // Update End Date Picker
            dateEnd.updateDate(endCal.get(Calendar.YEAR),
                    endCal.get(Calendar.MONTH),
                    endCal.get(Calendar.DAY_OF_MONTH));

            // Update summary text
            tvSummary.setText("Cycle length: " + cycleLength + " days");
        });


        // event to save
        btnSaveCycle.setOnClickListener(v -> {
            int startYear = dateStart.getYear();
            int startMonth = dateStart.getMonth() + 1;
            int startDay = dateStart.getDayOfMonth();
            String startDate = startYear + "-" + startMonth + "-" + startDay;

            int endYear = dateEnd.getYear();
            int endMonth = dateEnd.getMonth() + 1;
            int endDay = dateEnd.getDayOfMonth();
            String endDate = endYear + "-" + endMonth + "-" + endDay;

            String cycleType = radioRegular.isChecked() ? "regular" : "irregular";

            Calendar cal = new GregorianCalendar(startYear, startMonth - 1, startDay);
            String cycleId = "cycle_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime());



            Map<String, Object> cycleData = new HashMap<>();
            cycleData.put("startDate", startDate);
            cycleData.put("endDate", endDate);
            cycleData.put("cycleType", cycleType);
            cycleData.put("cycleLength", cycleLength);

            DatabaseReference userRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users")
                    .child(safeEmailKey);

            userRef.child("cycles").child(cycleId).setValue(cycleData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Cycle.this, "Cycle saved successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Cycle.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });


        // fetch data
        recyclerCycles.setLayoutManager(new LinearLayoutManager(this));
        CycleAdapter cycleAdapter = new CycleAdapter(new ArrayList<CycleModel>(), Cycle.this);
        recyclerCycles.setAdapter(cycleAdapter);

        DatabaseReference userRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(safeEmailKey);

        userRef.child("cycles").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CycleModel> cycleList = new ArrayList<>();
                for (DataSnapshot cycleSnap : snapshot.getChildren()) {
                    CycleModel cycle = cycleSnap.getValue(CycleModel.class);
                    if (cycle != null) {
                        // ✅ enrich the model with IDs
                        cycle.setCycleId(cycleSnap.getKey());       // Firebase node key
                        cycle.setSafeEmailKey(safeEmailKey);        // user context
                        cycleList.add(cycle);
                    }
                }
                cycleAdapter.updateData(cycleList);
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Cycle.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }
}