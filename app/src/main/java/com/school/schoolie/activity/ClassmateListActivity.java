package com.school.schoolie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.school.schoolie.R;
import com.school.schoolie.model.ClassDatabase;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ClassmateListActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button studentButton;
    private LinearLayout studentList;
    private Button backBtn;
    private CircleImageView profileImage;

    private FirebaseFirestore mFirestore;

    private String className;
    private List<String> usersList;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classmate_list);

        mFirestore = FirebaseFirestore.getInstance();
        backBtn = findViewById(R.id.backBtn);
        studentList = findViewById(R.id.linearLayout);
        profileImage = findViewById(R.id.profileImage);

        usersList = new ArrayList<String>();
        user = User.getInstance();
        className = getIntent().getStringExtra("classID");
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadStudents();

        backBtn.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.profileImage:
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                break;
        }
    }

    private void loadStudents()
    {
        studentList.removeAllViews();
        mFirestore.collection("classDB").document(className).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ClassDatabase classDB = documentSnapshot.toObject(ClassDatabase.class);
                //getting the list of users in this class
                usersList = classDB.getUserIds();

                //looping through every student and creating a button
                for(String userID: usersList)
                {
                    if(!userID.equals(user.getId()))
                    {
                        mFirestore.collection("users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                        {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User user = documentSnapshot.toObject(User.class);
                                String fullName = user.toString();

                                studentButton = new Button(getApplicationContext());
                                studentButton.setText(fullName);
                                studentButton.setTextSize(16);
                                studentButton.setTextColor(Color.WHITE);
                                studentButton.getBackground().setColorFilter(Color.parseColor("#FF9D00"), PorterDuff.Mode.MULTIPLY);

                                studentButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        openStudentPage(v, userID);
                                    }
                                });

                                studentList.addView(studentButton);
                            }
                        });
                    }
                }
            }
        });
    }

    private void openStudentPage(View v, String studentID)
    {
        Intent intent = new Intent(this, SelectedClassmateActivity.class);
        intent.putExtra("studentID", studentID);
        startActivity(intent);
    }
}