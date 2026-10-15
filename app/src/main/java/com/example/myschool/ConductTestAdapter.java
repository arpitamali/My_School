package com.example.myschool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConductTestAdapter
        extends RecyclerView.Adapter<ConductTestAdapter.TestViewHolder> {

    private final Context context;
    private final List<TeacherConductTestActivity.TestModel> testList;

    public ConductTestAdapter(
            Context context,
            List<TeacherConductTestActivity.TestModel> testList
    ) {
        this.context = context;
        this.testList = testList;
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context).inflate(
                R.layout.item_teacher_conduct_test,
                parent,
                false
        );

        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TestViewHolder holder,
            int position
    ) {

        TeacherConductTestActivity.TestModel test =
                testList.get(position);

        holder.tvTopic.setText(
                test.topic != null && !test.topic.isEmpty()
                        ? test.topic
                        : "Test"
        );

        String classSubject =
                (test.className != null ? test.className : "")
                        + " • "
                        + (test.subject != null ? test.subject : "");

        holder.tvClassSubject.setText(classSubject);

        holder.tvDate.setText(
                test.testDate != null && !test.testDate.isEmpty()
                        ? "📅 " + test.testDate
                        : "📅 Date not selected"
        );

        holder.tvTime.setText(
                test.testTime != null && !test.testTime.isEmpty()
                        ? "🕐 " + test.testTime
                        : "🕐 Time not selected"
        );

        holder.btnMore.setOnClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(
                    context,
                    holder.btnMore
            );

            popupMenu.getMenu().add("✏ Edit Test");
            popupMenu.getMenu().add("🗑 Delete Test");

            popupMenu.setOnMenuItemClickListener(item -> {

                String selected =
                        item.getTitle().toString();

                if (selected.contains("Edit")) {

                    if (context instanceof TeacherConductTestActivity) {

                        ((TeacherConductTestActivity) context)
                                .editTest(test);
                    }

                    return true;
                }

                if (selected.contains("Delete")) {

                    if (context instanceof TeacherConductTestActivity) {

                        ((TeacherConductTestActivity) context)
                                .deleteTest(test);
                    }

                    return true;
                }

                return false;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

    static class TestViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvTopic;
        TextView tvClassSubject;
        TextView tvDate;
        TextView tvTime;
        TextView btnMore;

        public TestViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTopic = itemView.findViewById(
                    R.id.tvTestTopic
            );

            tvClassSubject = itemView.findViewById(
                    R.id.tvTestClassSubject
            );

            tvDate = itemView.findViewById(
                    R.id.tvTestDate
            );

            tvTime = itemView.findViewById(
                    R.id.tvTestTime
            );

            btnMore = itemView.findViewById(
                    R.id.btnTestMore
            );
        }
    }
}