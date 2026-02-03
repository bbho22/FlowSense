package com.example.flowsense;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;


public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private List<Request> requestList;
    private DatabaseReference usersRef, requestsRef;
    private Context context;

    public RequestAdapter(Context context, List<Request> requestList, DatabaseReference usersRef, DatabaseReference requestsRef) {
        this.context = context;
        this.requestList = requestList;
        this.usersRef = usersRef;
        this.requestsRef = requestsRef;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request req = requestList.get(position);

        holder.tvEmail.setText(req.getEmail());
        holder.tvMessage.setText(req.getMessage());

        // Activate
        holder.btnActivate.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Activate User")
                    .setMessage("Approve request for " + req.getEmail() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        String userKey = req.getEmail().replace(".", "_dot_").replace("@", "_at_");
                        usersRef.child(userKey).child("isActive").setValue(true);
                        requestsRef.child(req.getRequestId()).child("status").setValue("approved");
                        Toast.makeText(context, "User activated", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Decline
        holder.btnDecline.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Decline Request")
                    .setMessage("Reject request for " + req.getEmail() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        requestsRef.child(req.getRequestId()).child("status").setValue("rejected");
                        Toast.makeText(context, "Request declined", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvMessage;
        Button btnActivate, btnDecline;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvMessage = itemView.findViewById(R.id.tv_message);
            btnActivate = itemView.findViewById(R.id.btn_activate);
            btnDecline = itemView.findViewById(R.id.btn_decline);
        }
    }
}
