package com.example.flowsense;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Calendar;
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {
    private List<Integer> days;
    private Set<Integer> fertileDays;

    public CalendarAdapter(List<Integer> days, Set<Integer> fertileDays) {
        this.days = days;
        this.fertileDays = fertileDays;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        int day = days.get(position);
        holder.tvDay.setText(String.valueOf(day));

        if (fertileDays.contains(day)) {
            holder.tvDay.setBackgroundColor(Color.GREEN);
            holder.tvDay.setTextColor(Color.WHITE);
        } else {
            holder.tvDay.setBackgroundColor(Color.TRANSPARENT);
            holder.tvDay.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        DayViewHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
        }
    }
}

