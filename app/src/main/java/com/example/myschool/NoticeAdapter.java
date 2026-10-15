package com.example.myschool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoticeAdapter
        extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private final Context context;
    private final List<TeacherNoticeActivity.NoticeModel> noticeList;

    public NoticeAdapter(
            Context context,
            List<TeacherNoticeActivity.NoticeModel> noticeList) {

        this.context = context;
        this.noticeList = noticeList;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_teacher_notice,
                        parent,
                        false
                );

        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NoticeViewHolder holder,
            int position) {

        TeacherNoticeActivity.NoticeModel notice =
                noticeList.get(position);

        // Title
        if (notice.title != null &&
                !notice.title.trim().isEmpty()) {

            holder.tvTitle.setText(notice.title);

        } else {

            holder.tvTitle.setText("Notice");
        }

        // Class
        if (notice.className != null &&
                !notice.className.trim().isEmpty()) {

            holder.tvClass.setText(
                    "Class " + notice.className
            );

        } else {

            holder.tvClass.setText("All Classes");
        }

        // Description
        if (notice.description != null &&
                !notice.description.trim().isEmpty()) {

            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(
                    notice.description
            );

        } else {

            holder.tvDescription.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    public static class NoticeViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvClass;
        TextView tvDescription;

        public NoticeViewHolder(
                @NonNull View itemView) {

            super(itemView);

            tvTitle = itemView.findViewById(
                    R.id.tvNoticeItemTitle
            );

            tvClass = itemView.findViewById(
                    R.id.tvNoticeItemClass
            );

            tvDescription = itemView.findViewById(
                    R.id.tvNoticeItemDescription
            );
        }
    }
}