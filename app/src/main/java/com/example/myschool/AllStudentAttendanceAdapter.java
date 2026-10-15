package com.example.myschool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class AllStudentAttendanceAdapter
        extends RecyclerView.Adapter<AllStudentAttendanceAdapter.ViewHolder> {

    public interface OnStudentClickListener {
        void onStudentClick(AttendanceStudent student);
    }

    private final Context context;
    private final List<AttendanceStudent> studentList;
    private final OnStudentClickListener listener;

    public AllStudentAttendanceAdapter(
            Context context,
            List<AttendanceStudent> studentList,
            OnStudentClickListener listener
    ) {

        this.context = context;
        this.studentList = studentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.item_all_student_attendance,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        AttendanceStudent student =
                studentList.get(position);

        holder.tvStudentName.setText(
                student.studentName
        );

        String classDivision =
                buildClassDivision(
                        student.className,
                        student.division
                );

        holder.tvClassDivision.setText(
                classDivision
        );

        if (student.rollNumber == null ||
                student.rollNumber.trim().isEmpty()) {

            holder.tvRollNumber.setVisibility(
                    View.GONE
            );

        } else {

            holder.tvRollNumber.setVisibility(
                    View.VISIBLE
            );

            holder.tvRollNumber.setText(
                    "Roll No: " + student.rollNumber
            );
        }

        holder.tvPercentage.setText(
                String.format(
                        Locale.getDefault(),
                        "%.0f%%",
                        student.percentage
                )
        );

        holder.tvPresent.setText(
                "Present  " + student.present
        );

        holder.tvAbsent.setText(
                "Absent  " + student.absent
        );

        holder.tvHoliday.setText(
                "Holiday  " + student.holiday
        );

        holder.itemView.setOnClickListener(
                v -> {

                    if (listener != null) {
                        listener.onStudentClick(student);
                    }
                }
        );
    }

    private String buildClassDivision(
            String className,
            String division
    ) {

        StringBuilder builder =
                new StringBuilder();

        if (className != null &&
                !className.trim().isEmpty()) {

            builder.append(className.trim());
        }

        if (division != null &&
                !division.trim().isEmpty()) {

            if (builder.length() > 0) {
                builder.append(" - ");
            }

            builder.append(
                    division.trim()
            );
        }

        if (builder.length() == 0) {
            return "Class / Division";
        }

        return builder.toString();
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvStudentName;
        TextView tvClassDivision;
        TextView tvRollNumber;
        TextView tvPercentage;
        TextView tvPresent;
        TextView tvAbsent;
        TextView tvHoliday;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvStudentName =
                    itemView.findViewById(
                            R.id.tvAllAttendanceStudentName
                    );

            tvClassDivision =
                    itemView.findViewById(
                            R.id.tvAllAttendanceClassDivision
                    );

            tvRollNumber =
                    itemView.findViewById(
                            R.id.tvAllAttendanceRollNumber
                    );

            tvPercentage =
                    itemView.findViewById(
                            R.id.tvAllAttendancePercentage
                    );

            tvPresent =
                    itemView.findViewById(
                            R.id.tvAllAttendancePresent
                    );

            tvAbsent =
                    itemView.findViewById(
                            R.id.tvAllAttendanceAbsent
                    );

            tvHoliday =
                    itemView.findViewById(
                            R.id.tvAllAttendanceHoliday
                    );
        }
    }

    public static class AttendanceStudent {

        public String studentId = "";
        public String studentName = "";
        public String className = "";
        public String division = "";
        public String rollNumber = "";

        public int present = 0;
        public int absent = 0;
        public int holiday = 0;

        public double percentage = 0;
    }
}