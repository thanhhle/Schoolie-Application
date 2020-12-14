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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.school.schoolie.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mEmail, mPassword;
    private Button mSignUpBtn, mSignInBtn, mForgotPasswordBtn;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();             // singleton to get the instance to firebase authenticator

        mEmail = findViewById(R.id.email);              // connecting this variable to the email textField
        mPassword = findViewById(R.id.password);        // same, connecting this variable to the password textField

        mSignUpBtn = findViewById(R.id.signUpBtn);
        mSignInBtn = findViewById(R.id.signInBtn);
        mForgotPasswordBtn = findViewById(R.id.forgotPasswordBtn);

        mProgressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        //if user already logged into their account, send them to homepage
        if(mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified())
        {
            startActivity(new Intent(getApplicationContext(), HomescreenActivity.class)); //send user to home screen
            finish();
        }

        mSignUpBtn.setOnClickListener(this);
        mSignInBtn.setOnClickListener(this);
        mForgotPasswordBtn.setOnClickListener(this);
    }

    // Adding a listener to the buttons
    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.signInBtn:
                signIn();
                break;
            case R.id.signUpBtn:
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
                finish();
                break;
            case R.id.forgotPasswordBtn:
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
                break;
        }
    }

    private void signIn()
    {
        final String email = mEmail.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();

        // Fields' validator
        if(TextUtils.isEmpty(email))
        {
            mEmail.setError("Required");
            mEmail.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(password))
        {
            mPassword.setError("Required");
            mPassword.requestFocus();
            return;
        }

        // Hide keyboard after sign in button clicked
        mPassword.onEditorAction(EditorInfo.IME_ACTION_DONE);

        // Show progress bar so user knows google firebase is querying database
        mProgressBar.setVisibility(View.VISIBLE);

        // Sign in
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful()) {
                    if(!mAuth.getCurrentUser().isEmailVerified())
                    {
                        // Show message if email has not been verified
                        Toast.makeText(LoginActivity.this, "Email has not been verified. Please verify your email to continue", Toast.LENGTH_LONG).show();

                        // Resend email verification
                        mAuth.getCurrentUser().sendEmailVerification();
                    }
                    else
                    {
                        // Show a successful sign in message
                        Toast.makeText(LoginActivity.this, "Signed In Successfully", Toast.LENGTH_LONG).show();

                        // Navigate user to home screen
                        startActivity(new Intent(getApplicationContext(), HomescreenActivity.class));//send user to home screen
                        finish();
                    }
                } else {
                    // showing a error message to user
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
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

            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }
}