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

        int cycleLength = getIntent().getIntExtra("cycleLength", 28);
        String safeEmailKey = getIntent().getStringExtra("safeEmailKey");

        // Auto-update summary when start date picked
        dateStart.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> {
            tvSummary.setText("Cycle length: " + cycleLength + " days");
        });

        btnSaveCycle.setOnClickListener(v -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            // Cycle start (also menstruation start)
            Calendar mensStartCal = new GregorianCalendar(
                    dateStart.getYear(),
                    dateStart.getMonth(),
                    dateStart.getDayOfMonth()
            );
            String cycleStartDate = sdf.format(mensStartCal.getTime());
            String mensStartDate = cycleStartDate;

            // Menstruation end
            Calendar mensEndCal = new GregorianCalendar(
                    dateEnd.getYear(),
                    dateEnd.getMonth(),
                    dateEnd.getDayOfMonth()
            );
            String mensEndDate = sdf.format(mensEndCal.getTime());

            // Validation
            if (mensEndCal.before(mensStartCal)) {
                Toast.makeText(Cycle.this,
                        "Menstruation end date cannot be before start date",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Period length
            long diffMillis = mensEndCal.getTimeInMillis() - mensStartCal.getTimeInMillis();
            int periodLength = (int) (diffMillis / (1000 * 60 * 60 * 24)) + 1;

            // Cycle type
            String cycleType = radioRegular.isChecked() ? "regular" : "irregular";

            // Cycle end
            Calendar cycleEndCal = (Calendar) mensStartCal.clone();
            if (cycleType.equals("regular")) {
                cycleEndCal.add(Calendar.DAY_OF_MONTH, cycleLength - 1);
            } else {
                cycleEndCal.add(Calendar.DAY_OF_MONTH, 26 - 1);
            }
            String cycleEndDate = sdf.format(cycleEndCal.getTime());

            // Unique ID
            String cycleId = "cycle_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                    .format(mensStartCal.getTime());

            // Save data
            Map<String, Object> cycleData = new HashMap<>();
            cycleData.put("startDate", cycleStartDate);
            cycleData.put("endDate", cycleEndDate);
            cycleData.put("cycleType", cycleType);
            cycleData.put("cycleLength", cycleLength);
            cycleData.put("mensStartDate", mensStartDate);
            cycleData.put("mensEndDate", mensEndDate);
            cycleData.put("periodLength", periodLength);

            DatabaseReference userRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users")
                    .child(safeEmailKey);

            userRef.child("cycles").child(cycleId).setValue(cycleData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(Cycle.this, "Cycle saved successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(Cycle.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        // Fetch cycles
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
                        cycle.setCycleId(cycleSnap.getKey());
                        cycle.setSafeEmailKey(safeEmailKey);
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