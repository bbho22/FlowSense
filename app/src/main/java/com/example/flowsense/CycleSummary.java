package com.example.flowsense;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class CycleSummary extends AppCompatActivity {
    private TextView tvCycleHeader, tvCycleDates, tvCycleType, tvCycleLength;
    private RecyclerView recyclerDailyLogs;
    private DailyLogAdapter logAdapter;
    private List<DailyLogModel> logList = new ArrayList<>();

    private String cycleId, safeEmailKey;
    private MaterialButton btnPrint;
    private LinearLayout summaryLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cycle_summary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setContentView(R.layout.activity_cycle_summary);
        tvCycleHeader = findViewById(R.id.tv_cycle_header);
        tvCycleDates = findViewById(R.id.tv_cycle_dates);
        tvCycleType = findViewById(R.id.tv_cycle_type);
        tvCycleLength = findViewById(R.id.tv_cycle_length);
        recyclerDailyLogs = findViewById(R.id.recycler_daily_logs);
        btnPrint = findViewById(R.id.btn_print);
        summaryLayout = findViewById(R.id.main); // give your root LinearLayout an id in XML

        recyclerDailyLogs.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new DailyLogAdapter(logList);
        recyclerDailyLogs.setAdapter(logAdapter);

        cycleId = getIntent().getStringExtra("cycleId");
        safeEmailKey = getIntent().getStringExtra("safeEmailKey");

        if (cycleId == null || safeEmailKey == null) {
            Toast.makeText(this, "Missing cycle data", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadCycleSummary();
    }

    private void loadCycleSummary() {
        DatabaseReference cycleRef = FirebaseDatabase.getInstance("https://flowsense-1f327-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(safeEmailKey)
                .child("cycles")
                .child(cycleId);

        cycleRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String startDate = snapshot.child("startDate").getValue(String.class);
                String endDate = snapshot.child("endDate").getValue(String.class);
                String cycleType = snapshot.child("cycleType").getValue(String.class);
                Long cycleLength = snapshot.child("cycleLength").getValue(Long.class);

                tvCycleHeader.setText("Cycle: " + cycleId);
                tvCycleDates.setText("Start: " + startDate + " | End: " + endDate);
                tvCycleType.setText("Type: " + cycleType);
                tvCycleLength.setText("Length: " + cycleLength + " days");

                // Load symptoms + fertility logs
                loadDailyLogs(snapshot);
            }
        });
        btnPrint.setOnClickListener(v -> printSummary());
    }

    private void loadDailyLogs(DataSnapshot cycleSnap) {
        // Symptoms
        DataSnapshot symptomsSnap = cycleSnap.child("symptoms");
        for (DataSnapshot daySnap : symptomsSnap.getChildren()) {
            String date = daySnap.getKey();
            String bleeding = daySnap.child("bleeding").getValue(String.class);

            List<String> mood = new ArrayList<>();
            for (DataSnapshot moodSnap : daySnap.child("mood").getChildren()) {
                mood.add(moodSnap.getValue(String.class));
            }

            List<String> pain = new ArrayList<>();
            for (DataSnapshot painSnap : daySnap.child("pain").getChildren()) {
                pain.add(painSnap.getValue(String.class));
            }

            String sex = daySnap.child("sex").getValue(String.class);

            // Fertility
            DataSnapshot fertSnap = cycleSnap.child("fertility").child(date);
            String mucus = fertSnap.child("mucus").getValue(String.class);
            String temp = fertSnap.child("temperature").getValue(String.class);

            logList.add(new DailyLogModel(date, bleeding, mood, pain, sex, mucus, temp));
        }

        logAdapter.notifyDataSetChanged();

    }
    private void printSummary() {
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);

        PrintDocumentAdapter adapter = new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                 CancellationSignal cancellationSignal,
                                 LayoutResultCallback callback, Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }
                PrintDocumentInfo info = new PrintDocumentInfo
                        .Builder("CycleSummary.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build();
                callback.onLayoutFinished(info, true);
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                                CancellationSignal cancellationSignal,
                                WriteResultCallback callback) {
                try {
                    PdfDocument document = new PdfDocument();
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                            summaryLayout.getWidth(),
                            summaryLayout.getHeight(),
                            1).create();
                    PdfDocument.Page page = document.startPage(pageInfo);

                    summaryLayout.draw(page.getCanvas());
                    document.finishPage(page);

                    FileOutputStream fos = new FileOutputStream(destination.getFileDescriptor());
                    document.writeTo(fos);
                    document.close();
                    fos.close();

                    callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                } catch (Exception e) {
                    callback.onWriteFailed(e.getMessage());
                }
            }
        };

        printManager.print("CycleSummary", adapter, null);
    }

}