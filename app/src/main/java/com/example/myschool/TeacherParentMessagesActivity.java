package com.example.myschool;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TeacherParentMessagesActivity extends AppCompatActivity {

    private AppCompatImageButton btnBack;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    private EditText etReply;
    private AppCompatImageButton btnSendReply;

    private FirebaseFirestore db;

    private String staffId = "";
    private String schoolId = "";

    private final List<ParentMessageModel> messageList =
            new ArrayList<>();

    private ParentMessagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_parent_messages
        );

        db = FirebaseFirestore.getInstance();

        staffId = getIntent().getStringExtra("staffId");

        if (staffId == null) {
            staffId = "";
        }

        initializeViews();
        setupRecyclerView();

        btnBack.setOnClickListener(v -> finish());

        loadTeacherData();
    }

    private void initializeViews() {

        btnBack = findViewById(
                R.id.btnParentMessagesBack
        );

        progressBar = findViewById(
                R.id.parentMessagesProgressBar
        );

        recyclerView = findViewById(
                R.id.recyclerParentMessages
        );

        tvEmpty = findViewById(
                R.id.tvParentMessagesEmpty
        );

        etReply = findViewById(
                R.id.etParentMessageReply
        );

        btnSendReply = findViewById(
                R.id.btnSendParentReply
        );

        btnSendReply.setOnClickListener(
                v -> sendReply()
        );
    }

    private void setupRecyclerView() {

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new ParentMessagesAdapter(
                this,
                messageList
        );

        recyclerView.setAdapter(adapter);
    }

    private void loadTeacherData() {

        if (staffId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Teacher information not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {

                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(
                                this,
                                "Teacher profile not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    schoolId =
                            documentSnapshot.getString("schoolId");

                    if (schoolId == null) {
                        schoolId = "";
                    }

                    listenToParentMessages();
                })
                .addOnFailureListener(e -> {

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(
                            this,
                            "Failed to load teacher data: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void listenToParentMessages() {

        if (schoolId.isEmpty()) {

            progressBar.setVisibility(View.GONE);

            Toast.makeText(
                    this,
                    "School information not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * IMPORTANT:
         * Removed orderBy("createdAt").
         *
         * This avoids Firestore composite-index
         * problems with whereEqualTo().
         */
        db.collection("parentMessages")
                .whereEqualTo("schoolId", schoolId)
                .addSnapshotListener((snapshots, error) -> {

                    progressBar.setVisibility(View.GONE);

                    if (error != null) {

                        Toast.makeText(
                                this,
                                "Failed to load messages: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    messageList.clear();

                    if (snapshots != null) {

                        for (DocumentSnapshot document :
                                snapshots.getDocuments()) {

                            ParentMessageModel model =
                                    new ParentMessageModel();

                            model.messageId =
                                    document.getString("messageId");

                            model.schoolId =
                                    document.getString("schoolId");

                            model.parentId =
                                    document.getString("parentId");

                            model.parentName =
                                    document.getString("parentName");

                            model.studentId =
                                    document.getString("studentId");

                            model.studentName =
                                    document.getString("studentName");

                            model.message =
                                    document.getString("message");

                            model.reply =
                                    document.getString("reply");

                            model.repliedBy =
                                    document.getString("repliedBy");

                            messageList.add(model);
                        }
                    }

                    if (messageList.isEmpty()) {

                        tvEmpty.setVisibility(
                                View.VISIBLE
                        );

                    } else {

                        tvEmpty.setVisibility(
                                View.GONE
                        );
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void sendReply() {

        String reply =
                etReply.getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(reply)) {

            etReply.setError("Enter reply");
            etReply.requestFocus();

            return;
        }

        String selectedMessageId =
                adapter.getSelectedMessageId();

        if (selectedMessageId == null ||
                selectedMessageId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select a parent message first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnSendReply.setEnabled(false);

        db.collection("parentMessages")
                .document(selectedMessageId)
                .update(
                        "reply",
                        reply,

                        "repliedBy",
                        staffId,

                        "repliedAt",
                        FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(unused -> {

                    btnSendReply.setEnabled(true);

                    etReply.setText("");

                    Toast.makeText(
                            this,
                            "Reply sent",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    btnSendReply.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Failed to send reply: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    public static class ParentMessageModel {

        String messageId;

        String schoolId;

        String parentId;
        String parentName;

        String studentId;
        String studentName;

        String message;

        String reply;
        String repliedBy;
    }
}