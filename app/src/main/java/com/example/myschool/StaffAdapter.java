package com.example.myschool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class StaffAdapter
        extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    // =========================================================
    // LISTENER
    // =========================================================

    public interface OnStaffActionListener {

        void onStaffClick(StaffModel staff);

        void onEditClick(StaffModel staff);

        void onDeleteClick(StaffModel staff);
    }


    // =========================================================
    // DATA
    // =========================================================

    private final List<StaffModel> staffList =
            new ArrayList<>();

    private final OnStaffActionListener listener;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public StaffAdapter(
            List<StaffModel> staffList,
            OnStaffActionListener listener
    ) {

        if (staffList != null) {

            this.staffList.addAll(
                    staffList
            );
        }

        this.listener = listener;
    }


    // =========================================================
    // UPDATE LIST
    // =========================================================

    public void updateList(
            List<StaffModel> newList
    ) {

        staffList.clear();

        if (newList != null) {

            staffList.addAll(
                    newList
            );
        }

        notifyDataSetChanged();
    }


    // =========================================================
    // CREATE VIEW HOLDER
    // =========================================================

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_staff_card,
                        parent,
                        false
                );

        return new StaffViewHolder(
                view
        );
    }


    // =========================================================
    // BIND DATA
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull StaffViewHolder holder,
            int position
    ) {

        StaffModel staff =
                staffList.get(
                        position
                );


        // =====================================================
        // STAFF NAME
        // =====================================================

        String firstName =
                safe(
                        staff.getFirstName()
                );

        String middleName =
                safe(
                        staff.getMiddleName()
                );

        String lastName =
                safe(
                        staff.getLastName()
                );


        String fullName =
                (
                        firstName
                                + " "
                                + middleName
                                + " "
                                + lastName
                )
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();


        if (fullName.isEmpty()) {

            fullName =
                    "Staff / Teacher";
        }


        holder.tvName.setText(
                fullName
        );


        // =====================================================
        // QUALIFICATION
        // =====================================================

        String qualification =
                safe(
                        staff.getQualification()
                );


        if (qualification.isEmpty()) {

            qualification =
                    "Not provided";
        }


        holder.tvQualification.setText(
                "Qualification: "
                        + qualification
        );


        // =====================================================
        // MOBILE
        // =====================================================

        String mobile =
                safe(
                        staff.getStaffPhone()
                );


        if (mobile.isEmpty()) {

            mobile =
                    "Not provided";
        }


        holder.tvMobile.setText(
                "Mobile: "
                        + mobile
        );


        // =====================================================
        // STAFF CARD CLICK
        // =====================================================

        holder.cardStaff.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onStaffClick(
                                staff
                        );
                    }
                }
        );


        // =====================================================
        // EDIT CLICK
        // =====================================================

        holder.btnEditStaff.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onEditClick(
                                staff
                        );
                    }
                }
        );


        // =====================================================
        // DELETE CLICK
        // =====================================================

        holder.btnDeleteStaff.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onDeleteClick(
                                staff
                        );
                    }
                }
        );
    }


    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        return staffList.size();
    }


    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class StaffViewHolder
            extends RecyclerView.ViewHolder {

        MaterialCardView cardStaff;

        TextView tvName;
        TextView tvQualification;
        TextView tvMobile;

        ImageButton btnEditStaff;
        ImageButton btnDeleteStaff;


        public StaffViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            // =================================================
            // CARD
            // =================================================

            cardStaff =
                    itemView.findViewById(
                            R.id.cardStaff
                    );


            // =================================================
            // TEXT
            // =================================================

            tvName =
                    itemView.findViewById(
                            R.id.tvStaffName
                    );


            tvQualification =
                    itemView.findViewById(
                            R.id.tvStaffQualification
                    );


            tvMobile =
                    itemView.findViewById(
                            R.id.tvStaffMobile
                    );


            // =================================================
            // EDIT
            // =================================================

            btnEditStaff =
                    itemView.findViewById(
                            R.id.btnEditStaff
                    );


            // =================================================
            // DELETE
            // =================================================

            btnDeleteStaff =
                    itemView.findViewById(
                            R.id.btnDeleteStaff
                    );
        }
    }


    // =========================================================
    // SAFE STRING
    // =========================================================

    private static String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }
}