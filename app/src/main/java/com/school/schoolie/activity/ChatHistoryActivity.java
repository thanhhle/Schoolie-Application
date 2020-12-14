package com.school.schoolie.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.school.schoolie.R;
import com.school.schoolie.adapter.ChatHistoryAdapter;
import com.school.schoolie.model.ChatMessage;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHistoryActivity extends AppCompatActivity implements View.OnClickListener
{
    private RecyclerView recyclerView;
    private Button backBtn;
    private CircleImageView profileImage;

    private FirebaseFirestore mFirestore;
    private DatabaseReference mDatabase;

    private List<User> receivers;
    private List<ChatMessage> lastMessages;

    private ChatHistoryAdapter chatHistoryAdapter;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(linearLayoutManager);
        backBtn = findViewById(R.id.backBtn);
        profileImage = findViewById(R.id.profileImage);

        mFirestore = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("messages");

        receivers = new ArrayList<User>();
        lastMessages = new ArrayList<ChatMessage>();
        user = User.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        loadUsers(new FirestoreCallback()
        {
            @Override
            public void onCallback(List<User> users)
            {
                loadChatHistory(users);
            }
        });

        backBtn.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);
    }

    // Adding a listener to the buttons
    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.backBtn:
                finish();
                break;
            case R.id.profileImage:
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                break;
        }
    }

    // Hide the softkeypad when user click on anywhere other than a EditText
    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int[] scrCoords = new int[2];
            w.getLocationOnScreen(scrCoords);
            float x = event.getRawX() + w.getLeft() - scrCoords[0];
            float y = event.getRawY() + w.getTop() - scrCoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) )
            {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }


    private void loadUsers(FirestoreCallback firestoreCallback)
    {
        mFirestore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    List<User> users = new ArrayList<User>();
                    for(DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        User user  = documentSnapshot.toObject(User.class);
                        users.add(user);
                    }
                    firestoreCallback.onCallback(users);
                }
            }
        });
    }


    private void loadChatHistory(List<User> users)
    {
        mDatabase.child(user.getId()).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                receivers.clear();
                lastMessages.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    String receiverID = snapshot.getKey();
                    for (User receiver : users)
                    {
                        if (receiver.getId().equals(receiverID))
                        {
                            receivers.add(receiver);
                            mDatabase.child(user.getId()).child(receiverID).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    ChatMessage lastMessage = new ChatMessage();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                    {
                                        lastMessage = snapshot.getValue(ChatMessage.class);
                                    }

                                    lastMessages.add(lastMessage);

                                    if(receivers.size() == lastMessages.size())
                                    {
                                        chatHistoryAdapter = new ChatHistoryAdapter(ChatHistoryActivity.this, receivers, lastMessages);
                                        recyclerView.setAdapter(chatHistoryAdapter);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private interface FirestoreCallback
    {
        void onCallback(List<User> users);
    }
}