package com.example.myschool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeworkAdapter
        extends RecyclerView.Adapter<HomeworkAdapter.HomeworkViewHolder> {

    public interface OnHomeworkActionListener {

        void onEdit(
                TeacherHomeworkActivity.HomeworkItem item
        );

        void onDelete(
                TeacherHomeworkActivity.HomeworkItem item
        );
    }

    private final List<TeacherHomeworkActivity.HomeworkItem>
            homeworkList;

    private final OnHomeworkActionListener listener;

    public HomeworkAdapter(
            List<TeacherHomeworkActivity.HomeworkItem> homeworkList,
            OnHomeworkActionListener listener) {

        this.homeworkList = homeworkList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HomeworkViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_teacher_homework,
                        parent,
                        false
                );

        return new HomeworkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull HomeworkViewHolder holder,
            int position) {

        TeacherHomeworkActivity.HomeworkItem item =
                homeworkList.get(position);

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

        return homeworkList.size();
    }

    static class HomeworkViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvClassSubject;
        TextView tvDescription;

        Button btnEdit;
        Button btnDelete;

        HomeworkViewHolder(
                @NonNull View itemView) {

            super(itemView);

            tvTitle =
                    itemView.findViewById(
                            R.id.tvHomeworkItemTitle
                    );

            tvClassSubject =
                    itemView.findViewById(
                            R.id.tvHomeworkItemClassSubject
                    );

            tvDescription =
                    itemView.findViewById(
                            R.id.tvHomeworkItemDescription
                    );

            btnEdit =
                    itemView.findViewById(
                            R.id.btnEditHomework
                    );

            btnDelete =
                    itemView.findViewById(
                            R.id.btnDeleteHomework
                    );
        }
    }
}