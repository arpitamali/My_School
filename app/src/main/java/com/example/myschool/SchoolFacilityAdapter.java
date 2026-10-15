package com.example.myschool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SchoolFacilityAdapter
        extends RecyclerView.Adapter<SchoolFacilityAdapter.FacilityViewHolder> {

    public interface OnEditClickListener {
        void onEdit(SchoolFacilityModel facility);
    }

    public interface OnDeleteClickListener {
        void onDelete(SchoolFacilityModel facility);
    }

    private final List<SchoolFacilityModel> facilityList =
            new ArrayList<>();

    private final OnEditClickListener editListener;
    private final OnDeleteClickListener deleteListener;

    public SchoolFacilityAdapter(
            OnEditClickListener editListener,
            OnDeleteClickListener deleteListener
    ) {
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    public void setFacilityList(
            List<SchoolFacilityModel> list
    ) {

        facilityList.clear();

        if (list != null) {
            facilityList.addAll(list);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FacilityViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_school_facility,
                parent,
                false
        );

        return new FacilityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull FacilityViewHolder holder,
            int position
    ) {

        SchoolFacilityModel facility =
                facilityList.get(position);

        holder.tvFacilityName.setText(
                facility.getName()
        );

        holder.tvFacilityDescription.setText(
                facility.getDescription()
        );

        if (editListener == null) {
            holder.btnEdit.setVisibility(View.GONE);
        } else {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v ->
                    editListener.onEdit(facility)
            );
        }

        if (deleteListener == null) {
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v ->
                    deleteListener.onDelete(facility)
            );
        }
    }

    @Override
    public int getItemCount() {
        return facilityList.size();
    }

    static class FacilityViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvFacilityName;
        TextView tvFacilityDescription;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public FacilityViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            tvFacilityName =
                    itemView.findViewById(
                            R.id.tvFacilityName
                    );

            tvFacilityDescription =
                    itemView.findViewById(
                            R.id.tvFacilityDescription
                    );

            btnEdit =
                    itemView.findViewById(
                            R.id.btnEditFacility
                    );

            btnDelete =
                    itemView.findViewById(
                            R.id.btnDeleteFacility
                    );
        }
    }
}
