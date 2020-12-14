package com.school.schoolie.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.school.schoolie.R;
import com.school.schoolie.activity.ChatActivity;
import com.school.schoolie.activity.ChatHistoryActivity;
import com.school.schoolie.activity.ForgotPasswordActivity;
import com.school.schoolie.model.ChatMessage;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder>{
    private Context context;
    private List<User> receivers;
    private List<ChatMessage> lastMessages;

    public ChatHistoryAdapter(Context context, List<User> receivers, List<ChatMessage> lastMessages)
    {
        this.context = context;
        this.receivers = receivers;
        this.lastMessages = lastMessages;
    }


    @NonNull
    @Override
    public ChatHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_view, parent, false);
        return new ChatHistoryAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ChatHistoryAdapter.ViewHolder holder, int position)
    {

        holder.receiverName.setText(receivers.get(position).toString());
        holder.lastMessage.setText(lastMessages.get(position).getMessage());

        Picasso.get().load(receivers.get(position).getProfilePicture()).into(holder.receiverProfileImage);

        String displayedTime = lastMessages.get(position).getCreatedTime();

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        String currentTime = dateFormat.format(new Date());

        Log.i("Hello", displayedTime);
        Log.i("Hello", currentTime);

        if(!displayedTime.substring(0, 10).equals(currentTime.substring(0, 10)))
        {
            // Display date if the message is sent on other day
            displayedTime = displayedTime.substring(0, 10);
        }
        else
        {
            // Display time if the message is sent on the same day
            displayedTime = displayedTime.substring(11, 16) + displayedTime.substring(19);
        }

        holder.sentTime.setText(displayedTime);

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(context, ChatActivity.class);
               intent.putExtra("receiverID", receivers.get(position).getId());
               context.startActivity(intent);
           }
        });
    }


    @Override
    public int getItemCount() {
        return receivers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView receiverName;
        public TextView lastMessage;
        public TextView sentTime;
        public CircleImageView receiverProfileImage;

        public ViewHolder(View itemView)
        {
            super(itemView);
            receiverName = itemView.findViewById(R.id.receiverName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            sentTime = itemView.findViewById(R.id.sentTime);
            receiverProfileImage = itemView.findViewById(R.id.receiverProfileImage);
        }
    }
}
