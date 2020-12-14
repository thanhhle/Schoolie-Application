package com.school.schoolie.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.school.schoolie.model.GradeItem;
import com.school.schoolie.R;
import com.school.schoolie.model.User;
import com.school.schoolie.model.Validator;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddGradeActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button mAddGradeBtn, mCancelBtn;
    private EditText mGradeItem, mPointAchieved, mTotalPoint, mWeight;
    private ProgressBar mProgressBar;
    private CircleImageView profileImage;

    private FirebaseFirestore mFirestore;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_grade);

        mFirestore = FirebaseFirestore.getInstance();

        mProgressBar = findViewById(R.id.progressBar);
        mGradeItem = findViewById(R.id.gradeItem);
        mPointAchieved = findViewById(R.id.pointAchieved);
        mTotalPoint = findViewById(R.id.totalPoint);
        mWeight = findViewById(R.id.weight);
        mAddGradeBtn = findViewById(R.id.addGradeBtn);
        mCancelBtn = findViewById(R.id.cancelBtn);
        profileImage = findViewById(R.id.profileImage);

        user = User.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mAddGradeBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.addGradeBtn:
                addGrade();
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

    private void addGrade()
    {
        final String gradeItem = mGradeItem.getText().toString().trim();
        final String pointAchieved = mPointAchieved.getText().toString().trim();
        final String totalPoint = mTotalPoint.getText().toString().trim();
        final String weight = mWeight.getText().toString().trim();

        // Fields' validator
        if(TextUtils.isEmpty(gradeItem))
        {
            mGradeItem.setError("Required");
            mGradeItem.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(pointAchieved))
        {
            mPointAchieved.setError("Required");
            mPointAchieved.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(totalPoint))
        {
            mTotalPoint.setError("Required");
            mTotalPoint.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(weight))
        {
            mWeight.setError("Required");
            mWeight.requestFocus();
            return;
        }

        if(!Validator.isNumeric(pointAchieved))
        {
            mPointAchieved.setError(Validator.mustBeNumericMessage);
            mPointAchieved.requestFocus();
            return;
        }

        if(!Validator.isNumeric(totalPoint))
        {
            mTotalPoint.setError(Validator.mustBeNumericMessage);
            mTotalPoint.requestFocus();
            return;
        }

        if(!Validator.isNumeric(weight))
        {
            mWeight.setError(Validator.mustBeNumericMessage);
            mWeight.requestFocus();
            return;
        }

        // Hide keyboard after sign in button clicked
        mWeight.onEditorAction(EditorInfo.IME_ACTION_DONE);

        // Show progress bar so user knows the google firebase is querying database
        mProgressBar.setVisibility(View.VISIBLE);

        // Add new grade to the User object
        user.getGrades(getIntent().getStringExtra("classID")).put(gradeItem, new GradeItem(pointAchieved, totalPoint, weight));

        // Update the user to the User database
        mFirestore.collection("users").document(user.getId()).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddGradeActivity.this, "Grade Added Successfully", Toast.LENGTH_LONG).show();
                        mGradeItem.setText("");
                        mPointAchieved.setText("");
                        mTotalPoint.setText("");
                        mWeight.setText("");
                        mGradeItem.requestFocus();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddGradeActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        mProgressBar.setVisibility(View.GONE);
    }
}