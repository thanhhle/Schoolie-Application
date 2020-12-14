package com.school.schoolie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.school.schoolie.R;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class SelectedClassmateActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView studentName, studentEmail;
    private Button backBtn, chatBtn;
    private CircleImageView profileImage, profileImageView;

    private User user;
    private String userID;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_classmate);

        mFirestore = FirebaseFirestore.getInstance();

        studentName = findViewById(R.id.studentName);
        studentEmail = findViewById(R.id.email);
        backBtn = findViewById(R.id.backBtn);
        chatBtn = findViewById(R.id.chatBtn);
        profileImageView = findViewById(R.id.profileImageView);
        profileImage = findViewById(R.id.profileImage);

        userID = getIntent().getStringExtra("studentID");

        user = User.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        loadUserInfo();
        backBtn.setOnClickListener(this);
        chatBtn.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);
    }

    private void loadUserInfo()
    {
        mFirestore.collection("users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                User classmate = documentSnapshot.toObject(User.class);
                String fullName = classmate.getFirstName() + " " + classmate.getLastName();
                String email = classmate.getEmail();

                studentName.setText(fullName);
                studentEmail.setText(email);
                Picasso.get().load(classmate.getProfilePicture()).into(profileImageView);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.chatBtn:
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("receiverID", userID);
                startActivity(intent);
                break;
            case R.id.backBtn:
                finish();
                break;
            case R.id.profileImage:
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                break;
        }
    }
}