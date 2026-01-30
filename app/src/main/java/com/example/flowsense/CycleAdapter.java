package com.example.flowsense;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import java.util.List;
public class CycleAdapter extends RecyclerView.Adapter<CycleAdapter.CycleViewHolder>  {

    private List<CycleModel> cycleList;
    private Context context;

    public CycleAdapter(List<CycleModel> cycleList, Context context) {
        this.cycleList = cycleList;
        this.context = context;
    }

    public void updateData(List<CycleModel> newList) {
        this.cycleList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cycle, parent, false);
        return new CycleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CycleViewHolder holder, int position) {
        CycleModel cycle = cycleList.get(position);

        holder.tvStart.setText("Start: " + cycle.getStartDate());
        holder.tvEnd.setText("End: " + cycle.getEndDate());
        holder.tvType.setText("Type: " + cycle.getCycleType());
        holder.tvLength.setText("Length: " + cycle.getCycleLength() + " days");

        // ✅ Click listener goes here
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CycleSummary.class);
            intent.putExtra("cycleId", cycle.getCycleId());
            intent.putExtra("safeEmailKey", cycle.getSafeEmailKey());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cycleList.size();
    }

    static class CycleViewHolder extends RecyclerView.ViewHolder {
        TextView tvStart, tvEnd, tvType, tvLength;

        public CycleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStart = itemView.findViewById(R.id.tv_start);
            tvEnd = itemView.findViewById(R.id.tv_end);
            tvType = itemView.findViewById(R.id.tv_type);
            tvLength = itemView.findViewById(R.id.tv_length);
        }
    }
}
