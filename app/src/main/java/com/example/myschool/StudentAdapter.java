package com.example.myschool;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class StudentAdapter
        extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private final Context context;

    private final List<StudentModel> studentList;


    public StudentAdapter(
            Context context,
            List<StudentModel> studentList) {

        this.context = context;
        this.studentList = studentList;
    }


    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_student,
                                parent,
                                false
                        );

        return new StudentViewHolder(view);
    }


    @Override
    public void onBindViewHolder(
            @NonNull StudentViewHolder holder,
            int position) {

        StudentModel student =
                studentList.get(position);


        // =====================================================
        // NAME
        // =====================================================

        holder.tvName.setText(
                fullName(student)
        );


        // =====================================================
        // ADMISSION
        // =====================================================

        holder.tvAdmission.setText(
                "Admission No: " +
                        safe(student.getAdmissionNumber())
        );


        // =====================================================
        // CLASS
        // =====================================================

        holder.tvClass.setText(
                "Class: " +
                        safe(student.getClassName()) +
                        "  |  Division: " +
                        safe(student.getDivision())
        );


        // =====================================================
        // PHONE
        // =====================================================

        holder.tvPhone.setText(
                "📱 " +
                        safe(student.getStudentPhone())
        );


        // =====================================================
        // EMAIL
        // =====================================================

        holder.tvEmail.setText(
                "✉ " +
                        safe(student.getStudentEmail())
        );


        // =====================================================
        // PHOTO
        // =====================================================

        holder.ivStudentPhoto.setImageResource(
                android.R.drawable.ic_menu_camera
        );


        String photo =
                student.getStudentPhoto();


        if (!TextUtils.isEmpty(photo)) {

            try {

                if (photo.startsWith("http://") ||
                        photo.startsWith("https://")) {

                    Glide.with(context)
                            .load(photo)
                            .placeholder(
                                    android.R.drawable.ic_menu_camera
                            )
                            .error(
                                    android.R.drawable.ic_menu_camera
                            )
                            .centerCrop()
                            .into(
                                    holder.ivStudentPhoto
                            );

                } else {

                    holder.ivStudentPhoto.setImageURI(
                            Uri.parse(photo)
                    );
                }

            } catch (Exception e) {

                holder.ivStudentPhoto.setImageResource(
                        android.R.drawable.ic_menu_camera
                );
            }
        }


        // =====================================================
        // CLICK
        // =====================================================

        holder.itemView.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            context,
                            StudentDetailsActivity.class
                    );


            // Send ALL DATA

            put(intent, "studentId",
                    student.getStudentId());

            put(intent, "schoolId",
                    student.getSchoolId());

            put(intent, "studentPhoto",
                    student.getStudentPhoto());

            put(intent, "firstName",
                    student.getFirstName());

            put(intent, "middleName",
                    student.getMiddleName());

            put(intent, "lastName",
                    student.getLastName());

            put(intent, "dateOfBirth",
                    student.getDateOfBirth());

            put(intent, "gender",
                    student.getGender());

            put(intent, "bloodGroup",
                    student.getBloodGroup());

            put(intent, "aadhaar",
                    student.getAadhaar());

            put(intent, "birthPlace",
                    student.getBirthPlace());

            put(intent, "nationality",
                    student.getNationality());

            put(intent, "motherTongue",
                    student.getMotherTongue());

            put(intent, "religion",
                    student.getReligion());

            put(intent, "category",
                    student.getCategory());

            put(intent, "caste",
                    student.getCaste());

            put(intent, "subCaste",
                    student.getSubCaste());

            put(intent, "specialNeeds",
                    student.getSpecialNeeds());


            // ACADEMIC

            put(intent, "admissionNumber",
                    student.getAdmissionNumber());

            put(intent, "admissionDate",
                    student.getAdmissionDate());

            put(intent, "academicYear",
                    student.getAcademicYear());

            put(intent, "className",
                    student.getClassName());

            put(intent, "division",
                    student.getDivision());

            put(intent, "rollNumber",
                    student.getRollNumber());

            put(intent, "previousSchool",
                    student.getPreviousSchool());

            put(intent, "previousClass",
                    student.getPreviousClass());

            put(intent, "leavingCertificate",
                    student.getLeavingCertificate());


            // FATHER

            put(intent, "fatherName",
                    student.getFatherName());

            put(intent, "fatherMobile",
                    student.getFatherMobile());

            put(intent, "fatherEmail",
                    student.getFatherEmail());

            put(intent, "fatherOccupation",
                    student.getFatherOccupation());


            // MOTHER

            put(intent, "motherName",
                    student.getMotherName());

            put(intent, "motherMobile",
                    student.getMotherMobile());

            put(intent, "motherEmail",
                    student.getMotherEmail());

            put(intent, "motherOccupation",
                    student.getMotherOccupation());


            // GUARDIAN

            put(intent, "guardianName",
                    student.getGuardianName());

            put(intent, "guardianRelation",
                    student.getGuardianRelation());

            put(intent, "guardianMobile",
                    student.getGuardianMobile());


            // ADDRESS

            put(intent, "currentAddress",
                    student.getCurrentAddress());

            put(intent, "permanentAddress",
                    student.getPermanentAddress());

            put(intent, "city",
                    student.getCity());

            put(intent, "district",
                    student.getDistrict());

            put(intent, "state",
                    student.getState());

            put(intent, "pinCode",
                    student.getPinCode());


            // EMERGENCY

            put(intent, "emergencyName",
                    student.getEmergencyName());

            put(intent, "emergencyRelation",
                    student.getEmergencyRelation());

            put(intent, "emergencyMobile",
                    student.getEmergencyMobile());


            // FEES

            put(intent, "totalFees",
                    student.getTotalFees());

            put(intent, "paidFees",
                    student.getPaidFees());

            put(intent, "remainingFees",
                    student.getRemainingFees());

            put(intent, "paymentMode",
                    student.getPaymentMode());

            put(intent, "installmentType",
                    student.getInstallmentType());

            put(intent, "installmentAmount",
                    student.getInstallmentAmount());

            put(intent, "paymentDate",
                    student.getPaymentDate());


            // LOGIN

            put(intent, "studentEmail",
                    student.getStudentEmail());

            put(intent, "studentPhone",
                    student.getStudentPhone());


            context.startActivity(intent);
        });
    }


    // =========================================================
    // PUT
    // =========================================================

    private void put(
            Intent intent,
            String key,
            String value) {

        if (value != null &&
                !value.trim().isEmpty()) {

            intent.putExtra(
                    key,
                    value
            );
        }
    }


    // =========================================================
    // FULL NAME
    // =========================================================

    private String fullName(
            StudentModel student) {

        StringBuilder name =
                new StringBuilder();


        addName(
                name,
                student.getFirstName()
        );

        addName(
                name,
                student.getMiddleName()
        );

        addName(
                name,
                student.getLastName()
        );


        if (name.length() == 0) {
            return "Student";
        }


        return name.toString();
    }


    private void addName(
            StringBuilder builder,
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return;
        }


        if (builder.length() > 0) {
            builder.append(" ");
        }


        builder.append(
                value.trim()
        );
    }


    // =========================================================
    // SAFE
    // =========================================================

    private String safe(String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "Not provided";
        }

        return value;
    }


    // =========================================================
    // COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        return studentList.size();
    }


    // =========================================================
    // VIEW HOLDER
    // =========================================================

    static class StudentViewHolder
            extends RecyclerView.ViewHolder {

        ImageView ivStudentPhoto;

        TextView tvName;
        TextView tvAdmission;
        TextView tvClass;
        TextView tvPhone;
        TextView tvEmail;


        StudentViewHolder(
                @NonNull View itemView) {

            super(itemView);


            ivStudentPhoto =
                    itemView.findViewById(
                            R.id.ivStudentPhoto
                    );

            tvName =
                    itemView.findViewById(
                            R.id.tvName
                    );

            tvAdmission =
                    itemView.findViewById(
                            R.id.tvAdmission
                    );

            tvClass =
                    itemView.findViewById(
                            R.id.tvClass
                    );

            tvPhone =
                    itemView.findViewById(
                            R.id.tvPhone
                    );

            tvEmail =
                    itemView.findViewById(
                            R.id.tvEmail
                    );
        }
    }
}