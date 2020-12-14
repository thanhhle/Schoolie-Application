package com.school.schoolie.activity;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.school.schoolie.R;
import com.school.schoolie.model.User;
import com.school.schoolie.model.Validator;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mFirstName, mLastName, mEmail, mPassword;
    private Button mSignUpBtn, mSignInBtn;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mFirstName= findViewById(R.id.firstName);
        mLastName = findViewById(R.id.lastName);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);

        mSignUpBtn = findViewById(R.id.signUpBtn);
        mSignInBtn = findViewById(R.id.signInBtn);

        mProgressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //if user already logged into their account, send them to homepage
        if(mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getApplicationContext(), HomescreenActivity.class)); //send user to home screen
            finish();
        }

        mSignUpBtn.setOnClickListener(this);
        mSignInBtn.setOnClickListener(this);
    }

    // Adding a listener to the buttons
    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.signUpBtn:
                signUp();
                break;
            case R.id.signInBtn:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;
        }
    }

    private void signUp()
    {
        final String firstName = mFirstName.getText().toString().trim();
        final String lastName = mLastName.getText().toString().trim();
        final String email = mEmail.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();

        // Fields' validator
        if(TextUtils.isEmpty(firstName))
        {
            mFirstName.setError("Required");
            mFirstName.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(lastName))
        {
            mLastName.setError("Required");
            mLastName.requestFocus();
            return;
        }

        /*
        if(!Validator.isValidEmail(email))
        {
            mEmail.setError(Validator.invalidEmailMessage);
            mEmail.requestFocus();
            return;
        }
        */

        if(!Validator.isValidPassword(password))
        {
            mPassword.setError(Validator.invalidPasswordMessage);
            mPassword.requestFocus();
            return;
        }

        // Hide keyboard after sign in button clicked
        mPassword.onEditorAction(EditorInfo.IME_ACTION_DONE);

        // Show progress bar so user knows the google firebase is querying database
        mProgressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful())
                {
                    // Set display name of the user on Firebase
                    final FirebaseUser authUser = mAuth.getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(firstName + " " + lastName).build();
                    authUser.updateProfile(profileUpdates);

                    // Send verification link and create user database on Firestore
                    authUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid) {
                            final String userID = authUser.getUid();

                            // Create a User object with given information
                            User user = User.getInstance();
                            user.setFirstName(firstName);
                            user.setLastName(lastName);
                            user.setEmail(email);
                            user.setId(authUser.getUid());
                            user.setProfilePicture("https://firebasestorage.googleapis.com/v0/b/schoolie-e2efa.appspot.com/o/default-profile-image.png?alt=media&token=8a2da0fe-e266-4749-aa13-e370d725b048");

                            // Write the new created User object to firebase database
                            mFirestore.collection("users").document(userID).set(user).addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    Toast.makeText(RegistrationActivity.this, "Email verification has been sent. Please verify your email before logging in.", Toast.LENGTH_LONG).show();

                                    // Navigate user to home screen
                                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                    finish();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                // Show error message when new account cannot be written to the database
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegistrationActivity.this, "Failed to send email verification.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    // Show error message when account creation failed
                    Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }

                mProgressBar.setVisibility(View.GONE);
            }
        });
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
}
