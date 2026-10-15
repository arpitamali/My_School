package com.example.myschool;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceAdapter
        extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    // =========================================================
    // DATA
    // =========================================================

    private final List<StaffModel> staffList =
            new ArrayList<>();

    /*
     * staffId -> attendance status
     *
     * P   = Present
     * A   = Absent
     * HD  = Half Day
     * HDY = Holiday
     *
     * Empty = Not Marked
     */
    private final Map<String, String> attendanceMap =
            new HashMap<>();


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AttendanceAdapter(
            List<StaffModel> staffList
    ) {

        if (staffList != null) {
            this.staffList.addAll(staffList);
        }
    }


    // =========================================================
    // UPDATE LIST
    // =========================================================

    public void updateList(
            List<StaffModel> newList
    ) {

        staffList.clear();

        if (newList != null) {
            staffList.addAll(newList);
        }

        notifyDataSetChanged();
    }


    // =========================================================
    // CLEAR ATTENDANCE
    // =========================================================

    public void clearAttendance() {

        attendanceMap.clear();

        notifyDataSetChanged();
    }


    // =========================================================
    // SET ATTENDANCE
    // =========================================================

    public void setAttendance(
            String staffId,
            String status
    ) {

        if (staffId == null ||
                staffId.trim().isEmpty()) {

            return;
        }

        if (status == null ||
                status.trim().isEmpty()) {

            attendanceMap.remove(staffId);

        } else {

            attendanceMap.put(
                    staffId,
                    status
            );
        }

        notifyDataSetChanged();
    }


    // =========================================================
    // MARK ALL HOLIDAY
    // =========================================================

    public void markAllHoliday() {

        attendanceMap.clear();

        for (StaffModel staff : staffList) {

            String staffId =
                    staff.getDocumentId();

            if (staffId != null &&
                    !staffId.trim().isEmpty()) {

                attendanceMap.put(
                        staffId,
                        "HDY"
                );
            }
        }

        notifyDataSetChanged();
    }


    // =========================================================
    // GET ATTENDANCE
    // =========================================================

    public String getAttendance(
            String staffId
    ) {

        if (staffId == null ||
                staffId.trim().isEmpty()) {

            return "";
        }

        String status =
                attendanceMap.get(staffId);

        if (status == null) {
            return "";
        }

        return status;
    }


    // =========================================================
    // CHECK ALL MARKED
    // =========================================================

    public boolean areAllMarked() {

        if (staffList.isEmpty()) {
            return false;
        }

        for (StaffModel staff : staffList) {

            String staffId =
                    staff.getDocumentId();

            if (staffId == null ||
                    staffId.trim().isEmpty()) {

                return false;
            }

            String status =
                    attendanceMap.get(staffId);

            if (status == null ||
                    status.trim().isEmpty()) {

                return false;
            }
        }

        return true;
    }


    // =========================================================
    // GET MAP
    // =========================================================

    public Map<String, String> getAttendanceMap() {

        return new HashMap<>(
                attendanceMap
        );
    }


    // =========================================================
    // ON CREATE
    // =========================================================

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_attendance_staff,
                        parent,
                        false
                );

        return new AttendanceViewHolder(view);
    }


    // =========================================================
    // ON BIND
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull AttendanceViewHolder holder,
            int position
    ) {

        StaffModel staff =
                staffList.get(position);


        // =====================================================
        // NAME
        // =====================================================

        String name =
                (
                        safe(staff.getFirstName())
                                + " "
                                + safe(staff.getMiddleName())
                                + " "
                                + safe(staff.getLastName())
                )
                        .trim();

        if (name.isEmpty()) {
            name = "Staff / Teacher";
        }

        holder.tvName.setText(name);


        // =====================================================
        // QUALIFICATION
        // =====================================================

        String qualification =
                safe(
                        staff.getQualification()
                );

        if (qualification.isEmpty()) {
            qualification =
                    "Qualification not provided";
        }

        holder.tvQualification.setText(
                qualification
        );


        // =====================================================
        // STAFF ID
        // =====================================================

        String staffId =
                staff.getDocumentId();


        // =====================================================
        // CURRENT STATUS
        // =====================================================

        String currentStatus =
                getAttendance(
                        staffId
                );


        // =====================================================
        // RESET BUTTONS
        // =====================================================

        setUnselected(
                holder.btnPresent
        );

        setUnselected(
                holder.btnAbsent
        );

        setUnselected(
                holder.btnHalfDay
        );


        // =====================================================
        // RESET STATUS
        // =====================================================

        holder.tvStatus.setText(
                "⚠ Not Marked"
        );

        holder.tvStatus.setTextColor(
                Color.rgb(
                        138,
                        122,
                        0
                )
        );


        // =====================================================
        // SHOW CURRENT STATUS
        // =====================================================

        if (currentStatus.equals("P")) {

            setPresent(
                    holder.btnPresent
            );

            holder.tvStatus.setText(
                    "✓ Present"
            );

            holder.tvStatus.setTextColor(
                    Color.rgb(
                            46,
                            125,
                            50
                    )
            );


        } else if (
                currentStatus.equals("A")
        ) {

            setAbsent(
                    holder.btnAbsent
            );

            holder.tvStatus.setText(
                    "✕ Absent"
            );

            holder.tvStatus.setTextColor(
                    Color.rgb(
                            211,
                            47,
                            47
                    )
            );


        } else if (
                currentStatus.equals("HD")
        ) {

            setHalfDay(
                    holder.btnHalfDay
            );

            holder.tvStatus.setText(
                    "◐ Half Day"
            );

            holder.tvStatus.setTextColor(
                    Color.rgb(
                            245,
                            124,
                            0
                    )
            );


        } else if (
                currentStatus.equals("HDY")
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
        }


        // =====================================================
        // PRESENT BUTTON
        // =====================================================

        holder.btnPresent.setOnClickListener(
                v -> {

                    attendanceMap.put(
                            staffId,
                            "P"
                    );

                    notifyItemChanged(
                            holder.getBindingAdapterPosition()
                    );
                }
        );


        // =====================================================
        // ABSENT BUTTON
        // =====================================================

        holder.btnAbsent.setOnClickListener(
                v -> {

                    attendanceMap.put(
                            staffId,
                            "A"
                    );

                    notifyItemChanged(
                            holder.getBindingAdapterPosition()
                    );
                }
        );


        // =====================================================
        // HALF DAY BUTTON
        // =====================================================

        holder.btnHalfDay.setOnClickListener(
                v -> {

                    attendanceMap.put(
                            staffId,
                            "HD"
                    );

                    notifyItemChanged(
                            holder.getBindingAdapterPosition()
                    );
                }
        );
    }


    // =========================================================
    // UNSELECTED STYLE
    // =========================================================

    private void setUnselected(
            AppCompatButton button
    ) {

        button.setBackgroundResource(
                R.drawable.attendance_unselected
        );

        button.setTextColor(
                Color.rgb(
                        90,
                        90,
                        90
                )
        );

        button.setElevation(0);
    }


    // =========================================================
    // PRESENT STYLE
    // =========================================================

    private void setPresent(
            AppCompatButton button
    ) {

        button.setBackgroundResource(
                R.drawable.attendance_present
        );

        button.setTextColor(
                Color.rgb(
                        46,
                        125,
                        50
                )
        );

        button.setElevation(4);
    }


    // =========================================================
    // ABSENT STYLE
    // =========================================================

    private void setAbsent(
            AppCompatButton button
    ) {

        button.setBackgroundResource(
                R.drawable.attendance_absent
        );

        button.setTextColor(
                Color.rgb(
                        211,
                        47,
                        47
                )
        );

        button.setElevation(4);
    }


    // =========================================================
    // HALF DAY STYLE
    // =========================================================

    private void setHalfDay(
            AppCompatButton button
    ) {

        button.setBackgroundResource(
                R.drawable.attendance_halfday
        );

        button.setTextColor(
                Color.rgb(
                        245,
                        124,
                        0
                )
        );

        button.setElevation(4);
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

    static class AttendanceViewHolder
            extends RecyclerView.ViewHolder {

        MaterialCardView card;

        TextView tvName;
        TextView tvQualification;
        TextView tvStatus;

        AppCompatButton btnPresent;
        AppCompatButton btnAbsent;
        AppCompatButton btnHalfDay;


        AttendanceViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            card =
                    itemView.findViewById(
                            R.id.cardAttendanceStaff
                    );


            tvName =
                    itemView.findViewById(
                            R.id.tvAttendanceName
                    );


            tvQualification =
                    itemView.findViewById(
                            R.id.tvAttendanceQualification
                    );


            tvStatus =
                    itemView.findViewById(
                            R.id.tvAttendanceStatus
                    );


            btnPresent =
                    itemView.findViewById(
                            R.id.btnPresent
                    );


            btnAbsent =
                    itemView.findViewById(
                            R.id.btnAbsent
                    );


            btnHalfDay =
                    itemView.findViewById(
                            R.id.btnHalfDay
                    );
        }
    }


    // =========================================================
    // SAFE
    // =========================================================

    private static String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }
}