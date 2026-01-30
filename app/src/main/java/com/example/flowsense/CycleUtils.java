package com.example.flowsense;
import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class CycleUtils {
    // Callback interface
    public interface OnCycleCheckListener {
        void onCycleFound(String cycleId);
        void onNoCycle();
    }

    // Helper method to get current cycleId
    public static void getCurrentCycleId(Context context, DatabaseReference dbRef, OnCycleCheckListener listener) {
        dbRef.child("cycles").limitToLast(1).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                for (DataSnapshot cycleSnap : snapshot.getChildren()) {
                    String cycleId = cycleSnap.getKey();
                    String startDate = cycleSnap.child("startDate").getValue(String.class);
                    String endDate = cycleSnap.child("endDate").getValue(String.class);

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
                        Date todayDate = sdf.parse(new SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(new Date()));
                        Date start = sdf.parse(startDate);
                        Date end = sdf.parse(endDate);

                        if (todayDate != null && start != null && end != null) {
                            if (!todayDate.before(start) && !todayDate.after(end)) {
                                listener.onCycleFound(cycleId);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    listener.onNoCycle();
                    Toast.makeText(context, "No current cycle available, please create one.", Toast.LENGTH_LONG).show();
                }
            } else {
                listener.onNoCycle();
                Toast.makeText(context, "No current cycle available, please create one.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to check cycle: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
