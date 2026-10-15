package com.example.myschool;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaGalleryActivity
        extends AppCompatActivity {

    private static final String TAG =
            "MediaGallery";

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;
    private Button btnAddPhotos;
    private RecyclerView recyclerGallery;
    private TextView tvEmpty;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // =========================================================
    // DATA
    // =========================================================

    private final List<MediaGalleryItem> galleryList =
            new ArrayList<>();

    private MediaGalleryAdapter adapter;

    // =========================================================
    // IMAGE PICKER
    // =========================================================

    private final ActivityResultLauncher<String[]>
            imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenMultipleDocuments(),
                    uris -> {

                        if (uris == null
                                || uris.isEmpty()) {
                            return;
                        }

                        uploadSelectedImages(uris);
                    }
            );

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_media_gallery
        );

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();
        setupRecycler();
        setupListeners();
        loadGallery();
    }

    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(R.id.btnBack);

        btnAddPhotos =
                findViewById(R.id.btnAddPhotos);

        recyclerGallery =
                findViewById(R.id.recyclerGallery);

        tvEmpty =
                findViewById(R.id.tvEmpty);
    }

    // =========================================================
    // RECYCLER
    // =========================================================

    private void setupRecycler() {

        recyclerGallery.setLayoutManager(
                new GridLayoutManager(
                        this,
                        3
                )
        );

        adapter =
                new MediaGalleryAdapter(
                        this::confirmDelete,
                        this::openPhoto
                );

        recyclerGallery.setAdapter(adapter);
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnAddPhotos.setOnClickListener(
                v -> openImagePicker()
        );
    }

    // =========================================================
    // OPEN IMAGE PICKER
    // =========================================================

    private void openImagePicker() {

        try {

            imagePickerLauncher.launch(
                    new String[]{
                            "image/*"
                    }
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to open gallery.",
                    Toast.LENGTH_SHORT
            ).show();

            Log.e(
                    TAG,
                    "Image picker error",
                    e
            );
        }
    }

    // =========================================================
    // LOAD GALLERY
    // =========================================================

    private void loadGallery() {

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

        db.collection("schoolMediaGallery")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    galleryList.clear();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        String imageUrl =
                                getString(
                                        document,
                                        "imageUrl"
                                );

                        String storagePath =
                                getString(
                                        document,
                                        "storagePath"
                                );

                        /*
                         * Keep document if either URL or
                         * Storage path exists.
                         */
                        if (imageUrl.isEmpty()
                                && storagePath.isEmpty()) {
                            continue;
                        }

                        galleryList.add(
                                new MediaGalleryItem(
                                        document.getId(),
                                        imageUrl,
                                        storagePath
                                )
                        );
                    }

                    adapter.updateList(
                            galleryList
                    );

                    updateEmptyState();

                })
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "Load gallery failed",
                            e
                    );

                    Toast.makeText(
                            this,
                            "Unable to load gallery: "
                                    + getErrorMessage(e),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // EMPTY STATE
    // =========================================================

    private void updateEmptyState() {

        if (galleryList.isEmpty()) {

            tvEmpty.setVisibility(
                    View.VISIBLE
            );

            recyclerGallery.setVisibility(
                    View.GONE
            );

        } else {

            tvEmpty.setVisibility(
                    View.GONE
            );

            recyclerGallery.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // =========================================================
    // UPLOAD SELECTED IMAGES
    // =========================================================

    private void uploadSelectedImages(
            List<Uri> uris
    ) {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (uris == null
                || uris.isEmpty()) {
            return;
        }

        btnAddPhotos.setEnabled(false);

        btnAddPhotos.setText(
                "Uploading..."
        );

        uploadNextImage(
                uris,
                0
        );
    }

    // =========================================================
    // UPLOAD NEXT IMAGE
    // =========================================================

    private void uploadNextImage(
            List<Uri> uris,
            int index
    ) {

        if (index >= uris.size()) {

            btnAddPhotos.setEnabled(true);

            btnAddPhotos.setText(
                    "＋ Add Photos"
            );

            Toast.makeText(
                    this,
                    "Photos uploaded successfully.",
                    Toast.LENGTH_LONG
            ).show();

            loadGallery();

            return;
        }

        Uri imageUri =
                uris.get(index);

        if (imageUri == null) {

            uploadNextImage(
                    uris,
                    index + 1
            );

            return;
        }

        String schoolId =
                auth.getCurrentUser().getUid();

        String fileName =
                "gallery_"
                        + System.currentTimeMillis()
                        + "_"
                        + index
                        + ".jpg";

        StorageReference imageRef =
                storage
                        .getReference()
                        .child(
                                "schools/"
                                        + schoolId
                                        + "/gallery/"
                                        + fileName
                        );

        Log.d(
                TAG,
                "Uploading image: "
                        + imageUri
        );

        Log.d(
                TAG,
                "Storage path: "
                        + imageRef.getPath()
        );

        imageRef
                .putFile(imageUri)
                .continueWithTask(task -> {

                    if (!task.isSuccessful()) {

                        Exception exception =
                                task.getException();

                        if (exception != null) {
                            throw exception;
                        }

                        throw new Exception(
                                "Upload task failed."
                        );
                    }

                    return imageRef.getDownloadUrl();

                })
                .addOnSuccessListener(downloadUri -> {

                    saveGalleryDocument(
                            schoolId,
                            downloadUri.toString(),
                            imageRef.getPath(),
                            uris,
                            index
                    );

                })
                .addOnFailureListener(e -> {

                    String errorMessage =
                            getErrorMessage(e);

                    Log.e(
                            TAG,
                            "Photo upload failed: "
                                    + errorMessage,
                            e
                    );

                    Toast.makeText(
                            this,
                            "Upload failed: "
                                    + errorMessage,
                            Toast.LENGTH_LONG
                    ).show();

                    uploadNextImage(
                            uris,
                            index + 1
                    );
                });
    }

    // =========================================================
    // SAVE FIRESTORE DOCUMENT
    // =========================================================

    private void saveGalleryDocument(
            String schoolId,
            String imageUrl,
            String storagePath,
            List<Uri> uris,
            int index
    ) {

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "schoolId",
                schoolId
        );

        data.put(
                "imageUrl",
                imageUrl
        );

        data.put(
                "storagePath",
                storagePath
        );

        data.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        db.collection("schoolMediaGallery")
                .add(data)
                .addOnSuccessListener(
                        documentReference -> {

                            Log.d(
                                    TAG,
                                    "Gallery document saved: "
                                            + documentReference.getId()
                            );

                            uploadNextImage(
                                    uris,
                                    index + 1
                            );
                        }
                )
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "Firestore gallery save failed",
                            e
                    );

                    Toast.makeText(
                            this,
                            "Photo uploaded but save failed: "
                                    + getErrorMessage(e),
                            Toast.LENGTH_LONG
                    ).show();

                    uploadNextImage(
                            uris,
                            index + 1
                    );
                });
    }

    // =========================================================
    // OPEN PHOTO
    // =========================================================

    private void openPhoto(
            MediaGalleryItem item
    ) {

        View view =
                LayoutInflater
                        .from(this)
                        .inflate(
                                R.layout.dialog_gallery_photo,
                                null
                        );

        ImageView imageView =
                view.findViewById(
                        R.id.ivFullPhoto
                );

        String storagePath =
                item.getStoragePath();

        if (storagePath != null
                && !storagePath.trim().isEmpty()) {

            StorageReference imageRef =
                    storage
                            .getReference()
                            .child(storagePath);

            imageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {

                        Glide.with(this)
                                .load(uri)
                                .placeholder(
                                        android.R.drawable.ic_menu_gallery
                                )
                                .error(
                                        android.R.drawable.ic_menu_gallery
                                )
                                .into(imageView);
                    })
                    .addOnFailureListener(e -> {

                        Glide.with(this)
                                .load(
                                        android.R.drawable.ic_menu_gallery
                                )
                                .into(imageView);

                        Toast.makeText(
                                this,
                                "Photo is no longer available.",
                                Toast.LENGTH_SHORT
                        ).show();
                    });

        } else {

            Glide.with(this)
                    .load(item.getImageUrl())
                    .placeholder(
                            android.R.drawable.ic_menu_gallery
                    )
                    .error(
                            android.R.drawable.ic_menu_gallery
                    )
                    .into(imageView);
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(view)
                        .setPositiveButton(
                                "Close",
                                null
                        )
                        .create();

        dialog.show();
    }

    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

    private void confirmDelete(
            MediaGalleryItem item
    ) {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Delete Photo"
                )
                .setMessage(
                        "Are you sure you want to delete this photo?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deletePhoto(item)
                )
                .show();
    }

    // =========================================================
    // DELETE PHOTO
    // =========================================================

    private void deletePhoto(
            MediaGalleryItem item
    ) {

        db.collection("schoolMediaGallery")
                .document(
                        item.getDocumentId()
                )
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            deleteStorageFile(item);
                        }
                )
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "Firestore delete failed",
                            e
                    );

                    Toast.makeText(
                            this,
                            "Unable to delete photo: "
                                    + getErrorMessage(e),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // DELETE STORAGE FILE
    // =========================================================

    private void deleteStorageFile(
            MediaGalleryItem item
    ) {

        String storagePath =
                item.getStoragePath();

        if (storagePath == null
                || storagePath.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Photo deleted.",
                    Toast.LENGTH_SHORT
            ).show();

            loadGallery();

            return;
        }

        storage
                .getReference()
                .child(storagePath)
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Photo deleted.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadGallery();
                        }
                )
                .addOnFailureListener(e -> {

                    /*
                     * Firestore record is already deleted.
                     * If Storage object doesn't exist,
                     * don't show a serious error to user.
                     */
                    if (isStorageObjectMissing(e)) {

                        Toast.makeText(
                                this,
                                "Photo deleted.",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "Photo deleted, but storage cleanup failed.",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.e(
                                TAG,
                                "Storage delete failed",
                                e
                        );
                    }

                    loadGallery();
                });
    }

    // =========================================================
    // CHECK STORAGE MISSING
    // =========================================================

    private boolean isStorageObjectMissing(
            Exception exception
    ) {

        if (exception instanceof StorageException) {

            StorageException storageException =
                    (StorageException) exception;

            return storageException.getErrorCode()
                    == StorageException.ERROR_OBJECT_NOT_FOUND;
        }

        String message =
                exception.getMessage();

        return message != null
                && message.toLowerCase()
                .contains("object-not-found");
    }

    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String key
    ) {

        Object value =
                document.get(key);

        if (value == null) {
            return "";
        }

        return String.valueOf(value).trim();
    }

    // =========================================================
    // ERROR MESSAGE
    // =========================================================

    private String getErrorMessage(
            Exception exception
    ) {

        if (exception == null) {
            return "Unknown error";
        }

        if (exception instanceof StorageException) {

            StorageException storageException =
                    (StorageException) exception;

            int errorCode =
                    storageException.getErrorCode();

            String message =
                    storageException.getMessage();

            if (message == null
                    || message.trim().isEmpty()) {

                message = "Storage error";
            }

            return "Code "
                    + errorCode
                    + ": "
                    + message;
        }

        String message =
                exception.getMessage();

        if (message == null
                || message.trim().isEmpty()) {

            return exception.getClass()
                    .getSimpleName();
        }

        return message;
    }
}