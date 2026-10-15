package com.example.myschool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AssignmentAdapter
        extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    public interface OnAssignmentActionListener {

        void onEdit(
                TeacherAssignmentActivity.AssignmentModel item
        );

        void onDelete(
                TeacherAssignmentActivity.AssignmentModel item
        );
    }

    private final List<TeacherAssignmentActivity.AssignmentModel>
            assignmentList;

    private final OnAssignmentActionListener listener;

    public AssignmentAdapter(
            List<TeacherAssignmentActivity.AssignmentModel> assignmentList,
            OnAssignmentActionListener listener) {

        this.assignmentList = assignmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_teacher_assignment,
                        parent,
                        false
                );

        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AssignmentViewHolder holder,
            int position) {

        TeacherAssignmentActivity.AssignmentModel item =
                assignmentList.get(position);

        holder.tvTitle.setText(
                item.title
        );

        holder.tvClassSubject.setText(
                item.className +
                        " • " +
                        item.subject
        );

        holder.tvDescription.setText(
                item.description
        );

        holder.btnEdit.setOnClickListener(
                v -> listener.onEdit(item)
        );

        holder.btnDelete.setOnClickListener(
                v -> listener.onDelete(item)
        );
    }

    @Override
    public int getItemCount() {

        return assignmentList.size();
    }

    static class AssignmentViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvClassSubject;
        TextView tvDescription;

        Button btnEdit;
        Button btnDelete;

        AssignmentViewHolder(
                @NonNull View itemView) {

            super(itemView);

            tvTitle =
                    itemView.findViewById(
                            R.id.tvAssignmentItemTitle
                    );

            tvClassSubject =
                    itemView.findViewById(
                            R.id.tvAssignmentItemClassSubject
                    );

            tvDescription =
                    itemView.findViewById(
                            R.id.tvAssignmentItemDescription
                    );

            btnEdit =
                    itemView.findViewById(
                            R.id.btnEditAssignment
                    );

            btnDelete =
                    itemView.findViewById(
                            R.id.btnDeleteAssignment
                    );
        }
    }
}