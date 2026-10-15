package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class SeeStaffActivity
        extends AppCompatActivity
        implements StaffAdapter.OnStaffActionListener {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private Spinner spinnerStaffType;

    private SearchView searchStaff;

    private RecyclerView recyclerStaff;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    private ListenerRegistration staffListener;


    // =========================================================
    // ADAPTER
    // =========================================================

    private StaffAdapter adapter;

    private final List<StaffModel> staffList =
            new ArrayList<>();


    // =========================================================
    // ID CARD MODE
    // =========================================================

    private boolean idCardMode = false;


    // =========================================================
    // FILTER TYPES
    // =========================================================

    private final String[] staffTypes = {
            "All Staff / Teachers",
            "Teaching Staff",
            "Non Teaching Staff",
            "Administrative"
    };


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );


        setContentView(
                R.layout.activity_see_staff
        );


        // =====================================================
        // GET ID CARD MODE
        // =====================================================

        idCardMode =
                getIntent().getBooleanExtra(
                        "idCardMode",
                        false
                );


        // =====================================================
        // FIREBASE
        // =====================================================

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // =====================================================
        // INITIALIZE
        // =====================================================

        initViews();

        setupSpinner();

        setupRecyclerView();

        setupSearch();

        setupListeners();

        startRealtimeStaffListener();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );


        spinnerStaffType =
                findViewById(
                        R.id.spinnerStaffType
                );


        searchStaff =
                findViewById(
                        R.id.searchStaff
                );


        recyclerStaff =
                findViewById(
                        R.id.recyclerStaff
                );
    }


    // =========================================================
    // SPINNER
    // =========================================================

    private void setupSpinner() {

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        staffTypes
                );


        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spinnerStaffType.setAdapter(
                spinnerAdapter
        );


        spinnerStaffType.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id
                    ) {

                        filterStaff();
                    }


                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                }
        );
    }


    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        adapter =
                new StaffAdapter(
                        staffList,
                        this
                );


        recyclerStaff.setLayoutManager(
                new LinearLayoutManager(
                        this
                )
        );


        recyclerStaff.setAdapter(
                adapter
        );
    }


    // =========================================================
    // SEARCH
    // =========================================================

    private void setupSearch() {

        searchStaff.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(
                            String query
                    ) {

                        filterStaff();

                        return true;
                    }


                    @Override
                    public boolean onQueryTextChange(
                            String newText
                    ) {

                        filterStaff();

                        return true;
                    }
                }
        );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // REAL-TIME FIRESTORE LISTENER
    // =========================================================

    private void startRealtimeStaffListener() {

        if (
                mAuth == null
                        ||
                        mAuth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        // =====================================================
        // REMOVE OLD LISTENER
        // =====================================================

        if (staffListener != null) {

            staffListener.remove();

            staffListener = null;
        }


        // =====================================================
        // LISTEN STAFF
        // =====================================================

        staffListener =
                db.collection("staff")

                        .whereEqualTo(
                                "schoolId",
                                schoolId
                        )

                        .whereEqualTo(
                                "active",
                                true
                        )

                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                SeeStaffActivity.this,
                                                "Unable to load staff.",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        return;
                                    }


                                    if (snapshot == null) {

                                        return;
                                    }


                                    // =================================================
                                    // CLEAR
                                    // =================================================

                                    staffList.clear();


                                    // =================================================
                                    // ADD DATA
                                    // =================================================

                                    for (
                                            DocumentSnapshot document
                                            : snapshot.getDocuments()
                                    ) {

                                        StaffModel staff =
                                                document.toObject(
                                                        StaffModel.class
                                                );


                                        if (staff != null) {

                                            // FIRESTORE DOCUMENT ID
                                            staff.setDocumentId(
                                                    document.getId()
                                            );


                                            staffList.add(
                                                    staff
                                            );
                                        }
                                    }


                                    // =================================================
                                    // REFRESH
                                    // =================================================

                                    filterStaff();
                                }
                        );
    }


    // =========================================================
    // FILTER
    // =========================================================

    private void filterStaff() {

        if (adapter == null) {

            return;
        }


        String selectedType =
                "All Staff / Teachers";


        if (
                spinnerStaffType != null
                        &&
                        spinnerStaffType.getSelectedItem() != null
        ) {

            selectedType =
                    spinnerStaffType
                            .getSelectedItem()
                            .toString();
        }


        String searchText =
                "";


        if (searchStaff != null) {

            searchText =
                    searchStaff.getQuery()
                            .toString()
                            .trim()
                            .toLowerCase();
        }


        List<StaffModel> filteredList =
                new ArrayList<>();


        for (StaffModel staff : staffList) {

            boolean typeMatch =
                    matchesStaffType(
                            staff,
                            selectedType
                    );


            boolean searchMatch =
                    matchesSearch(
                            staff,
                            searchText
                    );


            if (
                    typeMatch
                            &&
                            searchMatch
            ) {

                filteredList.add(
                        staff
                );
            }
        }


        adapter.updateList(
                filteredList
        );
    }


    // =========================================================
    // STAFF TYPE FILTER
    // =========================================================

    private boolean matchesStaffType(
            StaffModel staff,
            String selectedType
    ) {

        if (
                selectedType.equals(
                        "All Staff / Teachers"
                )
        ) {

            return true;
        }


        String staffType =
                safe(
                        staff.getStaffType()
                ).toLowerCase();


        switch (selectedType) {

            case "Teaching Staff":

                return staffType.equals(
                        "teacher"
                )
                        ||
                        (
                                staffType.contains(
                                        "teaching"
                                )
                                        &&
                                        !staffType.contains(
                                                "non"
                                        )
                        );


            case "Non Teaching Staff":

                return staffType.contains(
                        "non"
                )
                        &&
                        staffType.contains(
                                "teaching"
                        );


            case "Administrative":

                return staffType.contains(
                        "administrative"
                );


            default:

                return true;
        }
    }


    // =========================================================
    // SEARCH MATCH
    // =========================================================

    private boolean matchesSearch(
            StaffModel staff,
            String search
    ) {

        if (search.isEmpty()) {

            return true;
        }


        // =====================================================
        // FULL NAME
        // =====================================================

        String fullName =
                (
                        safe(
                                staff.getFirstName()
                        )
                                + " "
                                +
                                safe(
                                        staff.getMiddleName()
                                )
                                + " "
                                +
                                safe(
                                        staff.getLastName()
                                )
                )
                        .trim()
                        .toLowerCase();


        // =====================================================
        // QUALIFICATION
        // =====================================================

        String qualification =
                safe(
                        staff.getQualification()
                )
                        .toLowerCase();


        // =====================================================
        // MOBILE
        // =====================================================

        String mobile =
                safe(
                        staff.getStaffPhone()
                )
                        .toLowerCase();


        // =====================================================
        // EMPLOYEE ID
        // =====================================================

        String employeeId =
                safe(
                        staff.getEmployeeId()
                )
                        .toLowerCase();


        // =====================================================
        // ROLE
        // =====================================================

        String role =
                safe(
                        staff.getRole()
                )
                        .toLowerCase();


        return fullName.contains(
                search
        )
                ||
                qualification.contains(
                        search
                )
                ||
                mobile.contains(
                        search
                )
                ||
                employeeId.contains(
                        search
                )
                ||
                role.contains(
                        search
                );
    }


    // =========================================================
    // STAFF CARD CLICK
    // =========================================================

    @Override
    public void onStaffClick(
            StaffModel staff
    ) {

        // =====================================================
        // NORMAL STAFF DETAILS MODE
        // =====================================================

        if (!idCardMode) {

            Intent intent =
                    new Intent(
                            SeeStaffActivity.this,
                            StaffDetailsActivity.class
                    );

            intent.putExtra(
                    "staffId",
                    staff.getDocumentId()
            );

            startActivity(intent);

            return;
        }


        // =====================================================
        // ID CARD MODE
        // =====================================================

        String employeeId =
                staff.getEmployeeId();

        if (employeeId == null) {
            employeeId = "";
        }

        employeeId =
                employeeId.trim();


        if (employeeId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Employee ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // FIND TEACHER BY EMPLOYEE ID
        // =====================================================

        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        db.collection("staff")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .whereEqualTo(
                        "employeeId",
                        employeeId
                )
                .limit(1)
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            if (
                                    snapshot == null
                                            ||
                                            snapshot.isEmpty()
                            ) {

                                Toast.makeText(
                                        this,
                                        "Teacher not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }


                            DocumentSnapshot document =
                                    snapshot.getDocuments()
                                            .get(0);


                            // REAL FIRESTORE DOCUMENT ID
                            String staffId =
                                    document.getId();


                            if (
                                    staffId == null
                                            ||
                                            staffId.trim().isEmpty()
                            ) {

                                Toast.makeText(
                                        this,
                                        "Staff ID not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }


                            // =================================================
                            // OPEN ID CARD
                            // =================================================

                            Intent intent =
                                    new Intent(
                                            SeeStaffActivity.this,
                                            TeacherIDCardActivity.class
                                    );


                            intent.putExtra(
                                    "staffId",
                                    staffId
                            );


                            startActivity(intent);
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to open ID card.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // EDIT
    // =========================================================

    @Override
    public void onEditClick(
            StaffModel staff
    ) {

        Intent intent =
                new Intent(
                        SeeStaffActivity.this,
                        AddStaffStep1.class
                );


        intent.putExtra(
                "isEdit",
                true
        );


        intent.putExtra(
                "staffId",
                staff.getDocumentId()
        );


        startActivity(
                intent
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void onDeleteClick(
            StaffModel staff
    ) {

        String staffName =
                buildName(
                        staff
                );


        new androidx.appcompat.app.AlertDialog.Builder(
                this
        )

                .setTitle(
                        "Delete Staff?"
                )

                .setMessage(
                        "Are you sure you want to delete "
                                + staffName
                                + "?"
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteStaff(
                                        staff
                                )
                )

                .show();
    }


    // =========================================================
    // DELETE STAFF
    // =========================================================

    private void deleteStaff(
            StaffModel staff
    ) {

        String documentId =
                staff.getDocumentId();


        if (
                documentId == null
                        ||
                        documentId.trim().isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Staff ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        db.collection("staff")
                .document(documentId)

                .update(
                        "active",
                        false,

                        "status",
                        "Deleted"
                )

                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Staff deleted successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )

                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to delete staff.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // BUILD NAME
    // =========================================================

    private String buildName(
            StaffModel staff
    ) {

        String name =
                (
                        safe(
                                staff.getFirstName()
                        )
                                + " "
                                +
                                safe(
                                        staff.getMiddleName()
                                )
                                + " "
                                +
                                safe(
                                        staff.getLastName()
                                )
                )
                        .trim();


        if (name.isEmpty()) {

            return "this staff member";
        }


        return name;
    }


    // =========================================================
    // SAFE
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }


    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();


        if (staffListener != null) {

            staffListener.remove();

            staffListener = null;
        }
    }
}