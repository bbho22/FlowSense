package com.example.flowsense;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;


import java.util.List;

public class InactiveAdapter extends RecyclerView.Adapter<InactiveAdapter.InactiveViewHolder>{
    private List<User> inactiveList;
    private DatabaseReference dbRef;
    private Context context;

    public InactiveAdapter(Context context, List<User> inactiveList, DatabaseReference dbRef) {
        this.context = context;
        this.inactiveList = inactiveList;
        this.dbRef = dbRef;
    }

    @NonNull
    @Override
    public InactiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inactive_user, parent, false);
        return new InactiveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InactiveViewHolder holder, int position) {
        User user = inactiveList.get(position);

        holder.tvFullname.setText(user.getFirstName() + " " + user.getSecondName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvStatus.setText("Status: Inactive");
        holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));

        holder.btnActivate.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Activate User")
                    .setMessage("Activate " + user.getFirstName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbRef.child(user.getEmail().replace(".", "_dot_").replace("@", "_at_"))
                                .child("isActive").setValue(true);
                        Toast.makeText(context, "User activated", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return inactiveList.size();
    }

    public static class InactiveViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullname, tvEmail, tvStatus;
        Button btnActivate;

        public InactiveViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullname = itemView.findViewById(R.id.tv_fullname);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnActivate = itemView.findViewById(R.id.btn_activate);
        }
    }
}
