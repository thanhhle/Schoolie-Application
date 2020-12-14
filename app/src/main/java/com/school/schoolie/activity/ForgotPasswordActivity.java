package com.school.schoolie.activity;

import android.content.Context;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.school.schoolie.R;


public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mEmail;
    private Button mResetPasswordBtn, mBackToSignInBtn;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.email);

        mResetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        mBackToSignInBtn = findViewById(R.id.backToSignInBtn);

        mProgressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mResetPasswordBtn.setOnClickListener(this);
        mBackToSignInBtn.setOnClickListener(this);
    }

    // Adding a listener to the buttons
    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.resetPasswordBtn:
                resetPassword();
                finish();
                break;
            case R.id.backToSignInBtn:
                finish();
                break;
        }
    }

    private void resetPassword()
    {
        final String email = mEmail.getText().toString().trim();

        // Fields' validator
        if(TextUtils.isEmpty(email))
        {
            mEmail.setError("Required");
            mEmail.requestFocus();
            return;
        }

        // Hide keyboard after sign in button clicked
        mEmail.onEditorAction(EditorInfo.IME_ACTION_DONE);

        //show progress bar so user knows google firebase is querying database
        mProgressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
                // Show successful message when password reset email is sent
                Toast.makeText(ForgotPasswordActivity.this, "Password Reset Email Sent: Follow the direction in the email to reset your password.", Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            // Show error message when password reset password cannot be sent
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        mProgressBar.setVisibility(View.GONE);
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
