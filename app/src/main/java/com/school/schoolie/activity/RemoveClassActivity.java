package com.school.schoolie.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.school.schoolie.model.Class;
import com.school.schoolie.model.ClassDatabase;
import com.school.schoolie.R;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RemoveClassActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button mRemoveClassBtn, mCancelBtn;
    private LinearLayout mLinearLayout;
    private CircleImageView profileImage;

    private FirebaseFirestore mFirestore;

    private List<Class> removedClasses;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_class);

        mFirestore = FirebaseFirestore.getInstance();

        mRemoveClassBtn = findViewById(R.id.removeClassBtn);
        mCancelBtn = findViewById(R.id.cancelBtn);
        mLinearLayout = findViewById(R.id.linearLayout);
        profileImage = findViewById(R.id.profileImage);

        user = User.getInstance();
        removedClasses = new ArrayList<Class>();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mRemoveClassBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);
        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);

        loadCheckBoxes();
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.removeClassBtn:
                removeClass();
                break;
            case R.id.cancelBtn:
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

            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }

    private void loadCheckBoxes()
    {
        mLinearLayout.removeAllViews();
        for(int i = 0; i < user.getClassList().size(); i++)
        {
            final CheckBox checkBox = new CheckBox(RemoveClassActivity.this);
            checkBox.setText(user.getClassList().get(i).getClassName());
            checkBox.setId(i);
            checkBox.setTextSize(20);
            checkBox.setHeight(150);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Class c = user.getClassList().get(checkBox.getId());
                    if(isChecked)
                    {
                        removedClasses.add(c);
                    }
                    else
                    {
                        removedClasses.remove(c);
                    }
                }
            });

            mLinearLayout.addView(checkBox);
        }
    }

    private void removeClass()
    {
        for (final Class c: removedClasses)
        {
            // Remove the current user ID in the list of users registered for the class on classDB
            mFirestore.collection("classDB").document(c.getClassId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    ClassDatabase classDB = documentSnapshot.toObject(ClassDatabase.class);
                    classDB.getUserIds().remove(user.getId());
                    mFirestore.collection("classDB").document(c.getClassId()).set(classDB);
                }
            });

            // Remove the class from the user instance
            user.getClassList().remove(c);
        }

        // Update the user to the User database
        mFirestore.collection("users").document(user.getId()).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RemoveClassActivity.this, "Class Removed Successfully", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RemoveClassActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}