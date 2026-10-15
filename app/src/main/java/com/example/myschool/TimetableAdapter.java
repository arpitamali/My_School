package com.example.myschool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TimetableAdapter
        extends RecyclerView.Adapter<TimetableAdapter.TimetableViewHolder> {

    private final List<TimetableModel> timetableList;

    public TimetableAdapter(List<TimetableModel> timetableList) {
        this.timetableList = timetableList;
    }

    @NonNull
    @Override
    public TimetableViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_teacher_timetable,
                        parent,
                        false
                );

        return new TimetableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TimetableViewHolder holder,
            int position
    ) {

        TimetableModel model = timetableList.get(position);

        // START TIME
        if (model.getStartTime() == null ||
                model.getStartTime().trim().isEmpty()) {

            holder.tvStartTime.setText("--");

        } else {

            holder.tvStartTime.setText(
                    model.getStartTime()
            );
        }

        // END TIME
        if (model.getEndTime() == null ||
                model.getEndTime().trim().isEmpty()) {

            holder.tvEndTime.setText("--");

        } else {

            holder.tvEndTime.setText(
                    model.getEndTime()
            );
        }

        // RECESS
        if (model.isRecess()) {

            holder.tvSubject.setText("☕ Break");

            holder.tvClassDivision.setText(
                    "Class "
                            + safe(model.getClassName())
                            + " • "
                            + safe(model.getDivision())
            );

            holder.tvRecess.setText("RECESS");
            holder.tvRecess.setVisibility(View.VISIBLE);

        } else {

            // SUBJECT
            if (model.getSubject() == null ||
                    model.getSubject().trim().isEmpty()) {

                holder.tvSubject.setText("Subject");

            } else {

                holder.tvSubject.setText(
                        model.getSubject()
                );
            }

            // CLASS + DIVISION
            holder.tvClassDivision.setText(
                    "Class "
                            + safe(model.getClassName())
                            + " • "
                            + safe(model.getDivision())
            );

            holder.tvRecess.setText("");
            holder.tvRecess.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {

        if (timetableList == null) {
            return 0;
        }

        return timetableList.size();
    }

    private String safe(String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "--";
        }

        return value;
    }

    public static class TimetableViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvStartTime;
        TextView tvEndTime;
        TextView tvSubject;
        TextView tvClassDivision;
        TextView tvRecess;

        public TimetableViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvStartTime = itemView.findViewById(
                    R.id.tvTimetableStartTime
            );

            tvEndTime = itemView.findViewById(
                    R.id.tvTimetableEndTime
            );

            tvSubject = itemView.findViewById(
                    R.id.tvTimetableSubject
            );

            tvClassDivision = itemView.findViewById(
                    R.id.tvTimetableClassDivision
            );

            tvRecess = itemView.findViewById(
                    R.id.tvTimetableRecess
            );
        }
    }
}