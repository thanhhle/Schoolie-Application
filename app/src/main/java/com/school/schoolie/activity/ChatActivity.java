package com.school.schoolie.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.school.schoolie.R;
import com.school.schoolie.adapter.MessageAdapter;
import com.school.schoolie.model.ChatMessage;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener
{
    private TextView receiverName;
    private EditText messageText;
    private ImageButton backBtn, sendBtn;
    private RecyclerView messagesView;
    private CircleImageView receiverProfileImage;

    private FirebaseFirestore mFirestore;
    private DatabaseReference mDatabase;

    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatMessages;

    private String receiverID;
    private User receiver;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mFirestore = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("messages");

        receiverName = findViewById(R.id.receiverName);
        receiverProfileImage = findViewById(R.id.receiverProfileImage);
        messageText = findViewById(R.id.messageText);
        backBtn = findViewById(R.id.backBtn);
        sendBtn = findViewById(R.id.sendBtn);

        messagesView = findViewById(R.id.messagesView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);
        messagesView.setLayoutManager(linearLayoutManager);

        chatMessages = new ArrayList<ChatMessage>();
        user = User.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        receiverID = getIntent().getStringExtra("receiverID");

        getReceiver(new FirestoreCallback()
        {
            @Override
            public void onCallback()
            {
                receiverName.setText(receiver.toString());
                Picasso.get().load(receiver.getProfilePicture()).into(receiverProfileImage);
            }
        });

        loadMessages();

        messagesView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    messagesView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(chatMessages.size() != 0) {
                                messagesView.smoothScrollToPosition(chatMessages.size() - 1);
                            }
                        }
                    }, 0);
                }
            }
        });

        backBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
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
            case R.id.sendBtn:
                sendMessage();
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


    private void getReceiver(final FirestoreCallback firestoreCallback)
    {
        mFirestore.collection("users").document(receiverID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                receiver = documentSnapshot.toObject(User.class);
                firestoreCallback.onCallback();
            }
        });
    }


    private void sendMessage()
    {
        final String message = messageText.getText().toString().trim();
        final String rName = receiverName.getText().toString().trim();

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        String currentTime = dateFormat.format(new Date());

        if(!TextUtils.isEmpty(message))
        {
            String messageID = mDatabase.push().getKey();

            ChatMessage sentMessage = new ChatMessage(user.getId(), receiverID, rName, message, currentTime);
            mDatabase.child(user.getId()).child(receiverID).child(messageID).setValue(sentMessage);

            ChatMessage receivedMessage = new ChatMessage(user.getId(), receiverID, User.getInstance().toString(), message, currentTime);
            mDatabase.child(receiverID).child(user.getId()).child(messageID).setValue(receivedMessage);

            messageText.setText("");
        }
    }


    private void loadMessages()
    {
        mDatabase.child(user.getId()).child(receiverID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                chatMessages.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    chatMessages.add(chatMessage);

                    messageAdapter = new MessageAdapter(ChatActivity.this, chatMessages);
                    messagesView.setAdapter(messageAdapter);
                    messagesView.scrollToPosition(chatMessages.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private interface FirestoreCallback
    {
        void onCallback();
    }
}