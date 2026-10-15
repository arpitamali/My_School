package com.example.myschool;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FillMarksAdapter
        extends RecyclerView.Adapter<FillMarksAdapter.MarksViewHolder> {

    private final List<TeacherFillMarksActivity.MarksStudentModel>
            studentList;

    public FillMarksAdapter(
            android.content.Context context,
            List<TeacherFillMarksActivity.MarksStudentModel>
                    studentList) {

        this.studentList = studentList;
    }

    @NonNull
    @Override
    public MarksViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_teacher_fill_marks,
                                parent,
                                false
                        );

        return new MarksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MarksViewHolder holder,
            int position) {

        TeacherFillMarksActivity.MarksStudentModel student =
                studentList.get(position);

        holder.tvRoll.setText(
                student.rollNumber
        );

        holder.tvName.setText(
                student.studentName
        );

        // Remove old watcher
        if (holder.textWatcher != null) {

            holder.etMarks.removeTextChangedListener(
                    holder.textWatcher
            );
        }

        holder.etMarks.setText(
                student.marks
        );

        holder.textWatcher =
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        int adapterPosition =
                                holder.getBindingAdapterPosition();

                        if (adapterPosition !=
                                RecyclerView.NO_POSITION) {

                            studentList.get(
                                    adapterPosition
                            ).marks =
                                    s.toString().trim();
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                };

        holder.etMarks.addTextChangedListener(
                holder.textWatcher
        );
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class MarksViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvRoll;
        TextView tvName;
        EditText etMarks;

        TextWatcher textWatcher;

        MarksViewHolder(
                @NonNull View itemView) {

            super(itemView);

            tvRoll =
                    itemView.findViewById(
                            R.id.tvMarksRoll
                    );

            tvName =
                    itemView.findViewById(
                            R.id.tvMarksStudentName
                    );

            etMarks =
                    itemView.findViewById(
                            R.id.etStudentMarks
                    );
        }
    }
}