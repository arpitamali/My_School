package com.example.myschool;

import android.content.Context;
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
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MyClassStudentAdapter extends RecyclerView.Adapter<MyClassStudentAdapter.VH> {

    public interface Listener {
        void onView(StudentModel student);
        void onAttendance(StudentModel student);
        void onMarks(StudentModel student);
        void onProgress(StudentModel student);
        void onReport(StudentModel student);
    }

    private final Context context;
    private final List<StudentModel> list;
    private final Listener listener;

    public MyClassStudentAdapter(Context context, List<StudentModel> list, Listener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_class_student, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        StudentModel s = list.get(position);

        String name = fullName(s);
        h.name.setText(name);

        String roll = safe(s.getRollNumber());
        if (roll.isEmpty()) roll = "-";
        String admission = safe(s.getAdmissionNumber());
        if (admission.isEmpty()) admission = "-";
        h.meta.setText("Roll " + roll + " • Admission " + admission);

        String phone = safe(s.getStudentPhone());
        String email = safe(s.getStudentEmail());
        h.contact.setText(phone.isEmpty() && email.isEmpty() ? "" :
                (phone.isEmpty() ? email : phone));

        h.status.setText(safe(s.getStatus()).isEmpty() ? "ACTIVE" : safe(s.getStatus()).toUpperCase());

        h.photo.setImageResource(android.R.drawable.ic_menu_camera);
        String photo = safe(s.getStudentPhoto());
        if (!TextUtils.isEmpty(photo)) {
            try {
                if (photo.startsWith("http://") || photo.startsWith("https://")) {
                    Glide.with(context).load(photo)
                            .placeholder(android.R.drawable.ic_menu_camera)
                            .error(android.R.drawable.ic_menu_camera)
                            .centerCrop().into(h.photo);
                } else {
                    h.photo.setImageURI(Uri.parse(photo));
                }
            } catch (Exception ignored) {
                h.photo.setImageResource(android.R.drawable.ic_menu_camera);
            }
        }

        h.view.setOnClickListener(v -> listener.onView(s));
        h.attendance.setOnClickListener(v -> listener.onAttendance(s));
        h.marks.setOnClickListener(v -> listener.onMarks(s));
        h.progress.setOnClickListener(v -> listener.onProgress(s));
        h.report.setOnClickListener(v -> listener.onReport(s));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView name, meta, contact, status;
        MaterialButton view, attendance, marks, progress, report;

        VH(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.ivMyClassStudentPhoto);
            name = itemView.findViewById(R.id.tvMyClassStudentName);
            meta = itemView.findViewById(R.id.tvMyClassStudentMeta);
            contact = itemView.findViewById(R.id.tvMyClassStudentContact);
            status = itemView.findViewById(R.id.tvMyClassStudentStatus);
            view = itemView.findViewById(R.id.btnStudentView);
            attendance = itemView.findViewById(R.id.btnStudentAttendance);
            marks = itemView.findViewById(R.id.btnStudentMarks);
            progress = itemView.findViewById(R.id.btnStudentProgress);
            report = itemView.findViewById(R.id.btnStudentReport);
        }
    }

    private String fullName(StudentModel s) {
        String value = (safe(s.getFirstName()) + " " + safe(s.getMiddleName()) + " " + safe(s.getLastName())).trim();
        return value.isEmpty() ? "Student" : value.replaceAll("\\s+", " ");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
