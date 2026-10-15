package com.example.myschool;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ParentMessagesAdapter
        extends RecyclerView.Adapter<ParentMessagesAdapter.MessageViewHolder> {

    private final Context context;
    private final List<TeacherParentMessagesActivity.ParentMessageModel> messageList;

    private String selectedMessageId = "";

    public ParentMessagesAdapter(
            Context context,
            List<TeacherParentMessagesActivity.ParentMessageModel> messageList) {

        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_teacher_parent_message,
                        parent,
                        false
                );

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MessageViewHolder holder,
            int position) {

        TeacherParentMessagesActivity.ParentMessageModel message =
                messageList.get(position);

        // Parent name
        if (message.parentName != null &&
                !message.parentName.trim().isEmpty()) {

            holder.tvParentName.setText(message.parentName);

        } else {

            holder.tvParentName.setText("Parent");
        }

        // Student name
        if (message.studentName != null &&
                !message.studentName.trim().isEmpty()) {

            holder.tvStudent.setText(
                    "Student: " + message.studentName
            );

        } else {

            holder.tvStudent.setText("Student: -");
        }

        // Parent message
        if (message.message != null &&
                !message.message.trim().isEmpty()) {

            holder.tvMessage.setText(message.message);

        } else {

            holder.tvMessage.setText("No message");
        }

        // Teacher reply
        if (message.reply != null &&
                !message.reply.trim().isEmpty()) {

            holder.tvReplyLabel.setVisibility(View.VISIBLE);
            holder.tvReply.setVisibility(View.VISIBLE);

            holder.tvReply.setText(message.reply);

        } else {

            holder.tvReplyLabel.setVisibility(View.GONE);
            holder.tvReply.setVisibility(View.GONE);
        }

        // Selected message
        if (message.messageId != null &&
                message.messageId.equals(selectedMessageId)) {

            holder.cardParentMessage.setCardBackgroundColor(
                    Color.parseColor("#E8E4FF")
            );

            holder.tvSelectMessage.setText(
                    "✓ Selected for reply"
            );

        } else {

            holder.cardParentMessage.setCardBackgroundColor(
                    Color.WHITE
            );

            holder.tvSelectMessage.setText(
                    "Tap to reply"
            );
        }

        holder.cardParentMessage.setOnClickListener(v -> {

            if (message.messageId != null) {

                selectedMessageId = message.messageId;

                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public String getSelectedMessageId() {
        return selectedMessageId;
    }

    public static class MessageViewHolder
            extends RecyclerView.ViewHolder {

        CardView cardParentMessage;

        TextView tvParentName;
        TextView tvStudent;
        TextView tvMessage;

        TextView tvReplyLabel;
        TextView tvReply;

        TextView tvSelectMessage;

        public MessageViewHolder(
                @NonNull View itemView) {

            super(itemView);

            cardParentMessage =
                    itemView.findViewById(
                            R.id.cardParentMessage
                    );

            tvParentName =
                    itemView.findViewById(
                            R.id.tvParentMessageParentName
                    );

            tvStudent =
                    itemView.findViewById(
                            R.id.tvParentMessageStudent
                    );

            tvMessage =
                    itemView.findViewById(
                            R.id.tvParentMessageText
                    );

            tvReplyLabel =
                    itemView.findViewById(
                            R.id.tvParentReplyLabel
                    );

            tvReply =
                    itemView.findViewById(
                            R.id.tvParentReplyText
                    );

            tvSelectMessage =
                    itemView.findViewById(
                            R.id.tvSelectMessage
                    );
        }
    }
}
