package com.example.myschool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Map;

public class AllClassesAdapter
        extends RecyclerView.Adapter<AllClassesAdapter.ViewHolder> {

    public interface Listener {
        void onClassClick(String className);
    }

    private final Context context;
    private final List<String> classList;
    private final Map<String, Integer> classStudentCount;
    private final Listener listener;

    public AllClassesAdapter(
            Context context,
            List<String> classList,
            Map<String, Integer> classStudentCount,
            Listener listener) {

        this.context = context;
        this.classList = classList;
        this.classStudentCount = classStudentCount;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_all_class,
                parent,
                false
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        String className =
                classList.get(position);

        int count =
                classStudentCount.containsKey(className)
                        ? classStudentCount.get(className)
                        : 0;

        holder.tvClassName.setText(
                className
        );

        holder.tvStudentCount.setText(
                count + (
                        count == 1
                                ? " Student"
                                : " Students"
                )
        );

        holder.card.setOnClickListener(
                v -> listener.onClassClick(
                        className
                )
        );
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        MaterialCardView card;
        TextView tvClassName;
        TextView tvStudentCount;

        ViewHolder(
                @NonNull View itemView) {

            super(itemView);

            card = itemView.findViewById(
                    R.id.cardAllClass
            );

            tvClassName = itemView.findViewById(
                    R.id.tvAllClassName
            );

            tvStudentCount = itemView.findViewById(
                    R.id.tvAllClassStudentCount
            );
        }
    }
}