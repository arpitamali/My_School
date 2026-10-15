package com.example.myschool;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EnquiryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etSearchEnquiry;
    private RecyclerView recyclerEnquiries;
    private TextView tvEmptyEnquiry;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EnquiryAdapter adapter;

    private final List<EnquiryModel> allEnquiries =
            new ArrayList<>();

    private final List<EnquiryModel> filteredEnquiries =
            new ArrayList<>();

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_enquiry
        );

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecycler();
        setupListeners();

        loadEnquiries();
    }

    // =========================================================
    // INIT
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(R.id.btnBack);

        etSearchEnquiry =
                findViewById(
                        R.id.etSearchEnquiry
                );

        recyclerEnquiries =
                findViewById(
                        R.id.recyclerEnquiries
                );

        tvEmptyEnquiry =
                findViewById(
                        R.id.tvEmptyEnquiry
                );
    }

    // =========================================================
    // RECYCLER
    // =========================================================

    private void setupRecycler() {

        recyclerEnquiries.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new EnquiryAdapter(
                        this::showStatusDialog,
                        this::confirmDelete
                );

        recyclerEnquiries.setAdapter(
                adapter
        );
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBack.setOnClickListener(v ->
                finish()
        );

        etSearchEnquiry.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        filterEnquiries(
                                s.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }

    // =========================================================
    // LOAD
    // =========================================================

    private void loadEnquiries() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String schoolId =
                auth.getCurrentUser().getUid();

        db.collection("enquiries")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    allEnquiries.clear();

                    for (DocumentSnapshot doc :
                            snapshot.getDocuments()) {

                        String name =
                                getString(
                                        doc,
                                        "name"
                                );

                        String phone =
                                getString(
                                        doc,
                                        "phone"
                                );

                        String email =
                                getString(
                                        doc,
                                        "email"
                                );

                        String message =
                                getString(
                                        doc,
                                        "message"
                                );

                        String status =
                                getString(
                                        doc,
                                        "status"
                                );

                        String date =
                                getString(
                                        doc,
                                        "date"
                                );

                        if (status.isEmpty()) {
                            status = "New";
                        }

                        EnquiryModel enquiry =
                                new EnquiryModel(
                                        doc.getId(),
                                        name,
                                        phone,
                                        email,
                                        message,
                                        status,
                                        schoolId,
                                        date
                                );

                        allEnquiries.add(
                                enquiry
                        );
                    }

                    sortEnquiries();

                    filterEnquiries(
                            etSearchEnquiry
                                    .getText()
                                    .toString()
                    );

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load enquiries: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // SORT
    // =========================================================

    private void sortEnquiries() {

        Collections.sort(
                allEnquiries,
                new Comparator<EnquiryModel>() {

                    @Override
                    public int compare(
                            EnquiryModel o1,
                            EnquiryModel o2
                    ) {

                        return o2.getDate()
                                .compareToIgnoreCase(
                                        o1.getDate()
                                );
                    }
                }
        );
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void filterEnquiries(
            String query
    ) {

        String search =
                query == null
                        ? ""
                        : query.trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        filteredEnquiries.clear();

        for (EnquiryModel enquiry :
                allEnquiries) {

            String name =
                    safe(enquiry.getName());

            String phone =
                    safe(enquiry.getPhone());

            String email =
                    safe(enquiry.getEmail());

            String message =
                    safe(enquiry.getMessage());

            String status =
                    safe(enquiry.getStatus());

            if (search.isEmpty()
                    || name.contains(search)
                    || phone.contains(search)
                    || email.contains(search)
                    || message.contains(search)
                    || status.contains(search)) {

                filteredEnquiries.add(
                        enquiry
                );
            }
        }

        adapter.setEnquiryList(
                filteredEnquiries
        );

        updateEmpty();
    }

    // =========================================================
    // STATUS
    // =========================================================

    private void showStatusDialog(
            EnquiryModel enquiry
    ) {

        String[] statuses = {
                "New",
                "Contacted",
                "Closed"
        };

        int selected = 0;

        for (int i = 0; i < statuses.length; i++) {

            if (statuses[i].equalsIgnoreCase(
                    enquiry.getStatus()
            )) {

                selected = i;
                break;
            }
        }

        final int[] checkedItem = {
                selected
        };

        new AlertDialog.Builder(this)
                .setTitle("Change Status")
                .setSingleChoiceItems(
                        statuses,
                        selected,
                        (dialog, which) -> {

                            checkedItem[0] =
                                    which;

                            updateStatus(
                                    enquiry,
                                    statuses[which]
                            );

                            dialog.dismiss();
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    // =========================================================
    // UPDATE STATUS
    // =========================================================

    private void updateStatus(
            EnquiryModel enquiry,
            String status
    ) {

        db.collection("enquiries")
                .document(
                        enquiry.getId()
                )
                .update(
                        "status",
                        status
                )
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Status updated.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadEnquiries();
                        }
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to update status: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

    private void confirmDelete(
            EnquiryModel enquiry
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Enquiry")
                .setMessage(
                        "Delete enquiry from "
                                + enquiry.getName()
                                + "?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteEnquiry(
                                        enquiry
                                )
                )
                .show();
    }

    // =========================================================
    // DELETE
    // =========================================================

    private void deleteEnquiry(
            EnquiryModel enquiry
    ) {

        db.collection("enquiries")
                .document(
                        enquiry.getId()
                )
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Enquiry deleted.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadEnquiries();
                        }
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to delete enquiry: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =========================================================
    // EMPTY
    // =========================================================

    private void updateEmpty() {

        if (filteredEnquiries.isEmpty()) {

            tvEmptyEnquiry.setVisibility(
                    View.VISIBLE
            );

            recyclerEnquiries.setVisibility(
                    View.GONE
            );

        } else {

            tvEmptyEnquiry.setVisibility(
                    View.GONE
            );

            recyclerEnquiries.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String getString(
            DocumentSnapshot doc,
            String field
    ) {

        Object value =
                doc.get(field);

        if (value == null) {
            return "";
        }

        return String.valueOf(value).trim();
    }

    private String safe(String value) {

        if (value == null) {
            return "";
        }

        return value.toLowerCase(
                Locale.getDefault()
        );
    }
}