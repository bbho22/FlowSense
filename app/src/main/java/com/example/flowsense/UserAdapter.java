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
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{
    private List<User> userList;
    private DatabaseReference dbRef;
    private Context context;

    public UserAdapter(Context context, List<User> userList, DatabaseReference dbRef) {
        this.context = context;
        this.userList = userList;
        this.dbRef = dbRef;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvFullname.setText(user.getFirstName() + " " + user.getSecondName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRole.setText("Role: " + user.getRole());
        holder.tvStatus.setText(user.getIsActive() ? "Status: Active" : "Status: Inactive");
        holder.tvStatus.setTextColor(user.getIsActive() ?
                ContextCompat.getColor(context, android.R.color.holo_green_dark) :
                ContextCompat.getColor(context, android.R.color.holo_red_dark));

        // Block action with confirmation
        holder.btnBlock.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Block User")
                    .setMessage("Are you sure you want to block " + user.getFirstName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbRef.child(user.getEmail().replace(".", "_dot_").replace("@", "_at_"))
                                .child("isActive").setValue(false);
                        Toast.makeText(context, "User blocked", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Make Admin action with confirmation
        holder.btnMakeAdmin.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Promote User")
                    .setMessage("Make " + user.getFirstName() + " an Admin?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbRef.child(user.getEmail().replace(".", "_dot_").replace("@", "_at_"))
                                .child("role").setValue("admin");
                        Toast.makeText(context, "User promoted to Admin", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullname, tvEmail, tvRole, tvStatus;
        Button btnBlock, btnMakeAdmin;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullname = itemView.findViewById(R.id.tv_fullname);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnBlock = itemView.findViewById(R.id.btn_block);
            btnMakeAdmin = itemView.findViewById(R.id.btn_make_admin);
        }
    }
}
