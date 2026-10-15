package com.example.myschool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MediaGalleryAdapter
        extends RecyclerView.Adapter<MediaGalleryAdapter.GalleryViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(MediaGalleryItem item);
    }

    public interface OnPhotoClickListener {
        void onClick(MediaGalleryItem item);
    }

    private final List<MediaGalleryItem> imageList =
            new ArrayList<>();

    private final OnDeleteClickListener deleteListener;
    private final OnPhotoClickListener photoListener;

    public MediaGalleryAdapter(
            OnDeleteClickListener deleteListener,
            OnPhotoClickListener photoListener
    ) {
        this.deleteListener = deleteListener;
        this.photoListener = photoListener;
    }

    public void updateList(List<MediaGalleryItem> newList) {

        imageList.clear();

        if (newList != null) {
            imageList.addAll(newList);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_media_gallery,
                        parent,
                        false
                );

        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull GalleryViewHolder holder,
            int position
    ) {

        MediaGalleryItem item =
                imageList.get(position);

        /*
         * Prefer Firebase Storage path.
         * This prevents problems caused by old/broken
         * download URLs stored in Firestore.
         */
        String storagePath = item.getStoragePath();

        if (storagePath != null
                && !storagePath.trim().isEmpty()) {

            StorageReference imageRef =
                    FirebaseStorage
                            .getInstance()
                            .getReference()
                            .child(storagePath);

            imageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {

                        if (holder.getBindingAdapterPosition()
                                == position) {

                            Glide.with(
                                            holder.ivPhoto.getContext()
                                    )
                                    .load(uri)
                                    .centerCrop()
                                    .placeholder(
                                            android.R.drawable.ic_menu_gallery
                                    )
                                    .error(
                                            android.R.drawable.ic_menu_gallery
                                    )
                                    .into(holder.ivPhoto);
                        }
                    })
                    .addOnFailureListener(e -> {

                        /*
                         * If Storage object is missing,
                         * simply show placeholder.
                         * Do NOT crash the app.
                         */
                        Glide.with(
                                        holder.ivPhoto.getContext()
                                )
                                .load(
                                        android.R.drawable.ic_menu_gallery
                                )
                                .into(holder.ivPhoto);
                    });

        } else {

            /*
             * Fallback for older records which may
             * only contain imageUrl.
             */
            Glide.with(
                            holder.ivPhoto.getContext()
                    )
                    .load(item.getImageUrl())
                    .centerCrop()
                    .placeholder(
                            android.R.drawable.ic_menu_gallery
                    )
                    .error(
                            android.R.drawable.ic_menu_gallery
                    )
                    .into(holder.ivPhoto);
        }

        holder.ivPhoto.setOnClickListener(v ->
                photoListener.onClick(item)
        );

        holder.btnDelete.setOnClickListener(v ->
                deleteListener.onDelete(item)
        );
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class GalleryViewHolder
            extends RecyclerView.ViewHolder {

        ImageView ivPhoto;
        ImageButton btnDelete;

        GalleryViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            ivPhoto =
                    itemView.findViewById(
                            R.id.ivPhoto
                    );

            btnDelete =
                    itemView.findViewById(
                            R.id.btnDelete
                    );
        }
    }
}