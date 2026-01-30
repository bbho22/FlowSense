package com.example.flowsense;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class DailyLogAdapter extends RecyclerView.Adapter<DailyLogAdapter.LogViewHolder>{
    private List<DailyLogModel> logList;

    public DailyLogAdapter(List<DailyLogModel> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        DailyLogModel log = logList.get(position);
        Log.d("DailyLogAdapter", "Log: " + log.getDate() +
                ", bleeding=" + log.getBleeding() +
                ", mood=" + log.getMood() +
                ", pain=" + log.getPain() +
                ", sex=" + log.getSex() +
                ", mucus=" + log.getMucus() +
                ", temp=" + log.getTemperature());

        holder.tvDate.setText(log.getDate());
        holder.tvBleeding.setText("Bleeding: " + log.getBleeding());
        holder.tvMood.setText("Mood: " + String.join(", ", log.getMood()));
        holder.tvPain.setText("Pain: " + String.join(", ", log.getPain()));
        holder.tvSex.setText("Sex: " + log.getSex());
        holder.tvMucus.setText("Mucus: " + log.getMucus());
        holder.tvTemp.setText("Temperature: " + log.getTemperature() + " °C");
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvBleeding, tvMood, tvPain, tvSex, tvMucus, tvTemp;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvBleeding = itemView.findViewById(R.id.tv_bleeding);
            tvMood = itemView.findViewById(R.id.tv_mood);
            tvPain = itemView.findViewById(R.id.tv_pain);
            tvSex = itemView.findViewById(R.id.tv_sex);
            tvMucus = itemView.findViewById(R.id.tv_mucus);
            tvTemp = itemView.findViewById(R.id.tv_temp);
        }
    }
}
