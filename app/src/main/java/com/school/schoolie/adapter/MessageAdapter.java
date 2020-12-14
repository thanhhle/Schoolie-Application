package com.school.schoolie.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.school.schoolie.R;
import com.school.schoolie.model.ChatMessage;
import com.school.schoolie.model.User;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>
{
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT= 1;

    private Context context;
    private List<ChatMessage> messages;

    private FirebaseAuth mAuth;

    public MessageAdapter(Context context, List<ChatMessage> messages)
    {
        this.context = context;
        this.messages = messages;
    }


    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view;
        if(viewType == MSG_TYPE_RIGHT)
        {
            view = LayoutInflater.from(context).inflate(R.layout.item_sender_message, parent, false);
        }
        else
        {
            view = LayoutInflater.from(context).inflate(R.layout.item_receiver_message, parent, false);
        }
        return new MessageAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position)
    {
        ChatMessage chatMessage = messages.get(position);
        holder.message.setText(chatMessage.getMessage());
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView message;

        public ViewHolder(View itemView)
        {
            super(itemView);
            message = itemView.findViewById(R.id.message_body);
        }
    }


    @Override
    public int getItemViewType(int position)
    {
        mAuth = FirebaseAuth.getInstance();

        if(messages.get(position).getSenderID().equals(mAuth.getCurrentUser().getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }
    }
}
