package com.example.myschool;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MonthlyAttendanceAdapter
        extends RecyclerView.Adapter<
        MonthlyAttendanceAdapter.AttendanceViewHolder> {

    private final List<AttendanceRecord> attendanceList;


    public MonthlyAttendanceAdapter(
            List<AttendanceRecord> attendanceList
    ) {

        this.attendanceList =
                attendanceList;
    }


    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_monthly_attendance,
                                parent,
                                false
                        );


        return new AttendanceViewHolder(
                view
        );
    }


    @Override
    public void onBindViewHolder(
            @NonNull AttendanceViewHolder holder,
            int position
    ) {

        AttendanceRecord record =
                attendanceList.get(
                        position
                );


        // =====================================================
        // DATE
        // =====================================================

        holder.tvDate.setText(
                record.getDate()
        );


        // =====================================================
        // STATUS
        // =====================================================

        String status =
                record.getStatus();


        // =====================================================
        // PRESENT
        // =====================================================

        if (
                "P".equalsIgnoreCase(
                        status
                )
        ) {

            holder.tvStatus.setText(
                    "✓ Present"
            );

            holder.tvStatus.setTextColor(
                    Color.rgb(
                            22,
                            128,
                            60
                    )
            );


            // =====================================================
            // ABSENT
            // =====================================================

        } else if (
                "A".equalsIgnoreCase(
                        status
                )
        ) {

            holder.tvStatus.setText(
                    "✕ Absent"
            );

            holder.tvStatus.setTextColor(
                    Color.rgb(
                            200,
                            40,
                            40
                    )
            );


            // =====================================================
            // HALF DAY
            // =====================================================

        } else if (
                "HD".equalsIgnoreCase(
                        status
                )
        ) {

            holder.tvStatus.setText(
                    "◐ Half Day"
            );

            holder.tvStatus.setTextColor(
                    Color.rgb(
                            220,
                            137,
                            0
                    )
            );


            // =====================================================
            // HOLIDAY
            // =====================================================

        } else if (
                "HDY".equalsIgnoreCase(
                        status
                )
        ) {

            holder.tvStatus.setText(
                    "🏖 Holiday"
            );

            holder.tvStatus.setTextColor(
                    Color.rgb(
                            123,
                            31,
                            162
                    )
            );


            // =====================================================
            // NOT MARKED
            // =====================================================

        } else {

            holder.tvStatus.setText(
                    "Not Marked"
            );

            holder.tvStatus.setTextColor(
                    Color.GRAY
            );
        }
    }


    @Override
    public int getItemCount() {

        return attendanceList.size();
    }


    // =========================================================
    // VIEW HOLDER
    // =========================================================

    static class AttendanceViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvDate;
        TextView tvStatus;


        AttendanceViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            tvDate =
                    itemView.findViewById(
                            R.id.tvAttendanceDate
                    );


            tvStatus =
                    itemView.findViewById(
                            R.id.tvAttendanceStatus
                    );
        }
    }
}