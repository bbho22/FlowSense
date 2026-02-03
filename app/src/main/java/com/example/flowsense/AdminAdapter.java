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

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {
    private List<User> adminList;
    private DatabaseReference dbRef;
    private Context context;

    public AdminAdapter(Context context, List<User> adminList, DatabaseReference dbRef) {
        this.context = context;
        this.adminList = adminList;
        this.dbRef = dbRef;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        User admin = adminList.get(position);

        holder.tvFullname.setText(admin.getFirstName() + " " + admin.getSecondName());
        holder.tvEmail.setText(admin.getEmail());
        holder.tvStatus.setText(admin.getIsActive() ? "Status: Active" : "Status: Inactive");
        holder.tvStatus.setTextColor(admin.getIsActive() ?
                ContextCompat.getColor(context, android.R.color.holo_green_dark) :
                ContextCompat.getColor(context, android.R.color.holo_red_dark));

        // Remove Admin
        holder.btnRemoveAdmin.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Remove Admin")
                    .setMessage("Demote " + admin.getFirstName() + " to User?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbRef.child(admin.getEmail().replace(".", "_dot_").replace("@", "_at_"))
                                .child("role").setValue("user");
                        Toast.makeText(context, "Admin removed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Block Admin
        holder.btnBlock.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Block Admin")
                    .setMessage("Block " + admin.getFirstName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbRef.child(admin.getEmail().replace(".", "_dot_").replace("@", "_at_"))
                                .child("isActive").setValue(false);
                        Toast.makeText(context, "Admin blocked", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }

    public static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullname, tvEmail, tvStatus;
        Button btnRemoveAdmin, btnBlock;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullname = itemView.findViewById(R.id.tv_fullname);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnRemoveAdmin = itemView.findViewById(R.id.btn_remove_admin);
            btnBlock = itemView.findViewById(R.id.btn_block);
        }
    }
}
