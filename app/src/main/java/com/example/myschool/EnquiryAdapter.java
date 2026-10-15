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

public class EnquiryAdapter
        extends RecyclerView.Adapter<EnquiryAdapter.EnquiryViewHolder> {

    public interface OnStatusClickListener {
        void onStatusClick(EnquiryModel enquiry);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(EnquiryModel enquiry);
    }

    private final List<EnquiryModel> enquiryList =
            new ArrayList<>();

    private final OnStatusClickListener statusListener;
    private final OnDeleteClickListener deleteListener;

    public EnquiryAdapter(
            OnStatusClickListener statusListener,
            OnDeleteClickListener deleteListener
    ) {
        this.statusListener = statusListener;
        this.deleteListener = deleteListener;
    }

    public void setEnquiryList(
            List<EnquiryModel> list
    ) {

        enquiryList.clear();

        if (list != null) {
            enquiryList.addAll(list);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EnquiryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_enquiry,
                parent,
                false
        );

        return new EnquiryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull EnquiryViewHolder holder,
            int position
    ) {

        EnquiryModel enquiry =
                enquiryList.get(position);

        holder.tvName.setText(
                enquiry.getName()
        );

        holder.tvPhone.setText(
                "📞 " + enquiry.getPhone()
        );

        holder.tvEmail.setText(
                "✉ " + enquiry.getEmail()
        );

        holder.tvMessage.setText(
                enquiry.getMessage()
        );

        holder.tvDate.setText(
                enquiry.getDate()
        );

        holder.tvStatus.setText(
                enquiry.getStatus()
        );

        holder.tvStatus.setOnClickListener(v ->
                statusListener.onStatusClick(enquiry)
        );

        holder.btnDelete.setOnClickListener(v ->
                deleteListener.onDeleteClick(enquiry)
        );
    }

    @Override
    public int getItemCount() {
        return enquiryList.size();
    }

    static class EnquiryViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvPhone;
        TextView tvEmail;
        TextView tvMessage;
        TextView tvDate;
        TextView tvStatus;
        ImageButton btnDelete;

        public EnquiryViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            tvName =
                    itemView.findViewById(
                            R.id.tvEnquiryName
                    );

            tvPhone =
                    itemView.findViewById(
                            R.id.tvEnquiryPhone
                    );

            tvEmail =
                    itemView.findViewById(
                            R.id.tvEnquiryEmail
                    );

            tvMessage =
                    itemView.findViewById(
                            R.id.tvEnquiryMessage
                    );

            tvDate =
                    itemView.findViewById(
                            R.id.tvEnquiryDate
                    );

            tvStatus =
                    itemView.findViewById(
                            R.id.tvEnquiryStatus
                    );

            btnDelete =
                    itemView.findViewById(
                            R.id.btnDeleteEnquiry
                    );
        }
    }
}