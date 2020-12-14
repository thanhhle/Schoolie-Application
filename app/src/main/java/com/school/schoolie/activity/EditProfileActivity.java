package com.school.schoolie.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.school.schoolie.R;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;


import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView profileImageView;
    private EditText eFirstName, eLastName, eSchoolName, eEmail, eStudentID, eMajor;
    private Button saveBtn, backBtn;
    private ImageView signOutBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    //Firebase
    private StorageReference sReference;
    private FirebaseStorage fStorage;

    private User user;
    private String imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        fStorage = FirebaseStorage.getInstance();
        sReference = fStorage.getReference();

        profileImageView = findViewById(R.id.profile_image);
        eFirstName = findViewById(R.id.editFirstName);
        eLastName = findViewById(R.id.editLastName);
        eSchoolName = findViewById(R.id.editSchoolName);
        eEmail = findViewById(R.id.editEmailAddress);
        eStudentID = findViewById(R.id.editStudentID);
        eMajor = findViewById(R.id.major);

        backBtn = findViewById(R.id.backBtn);
        saveBtn = findViewById(R.id.saveBtn);
        signOutBtn = findViewById(R.id.signOutBtn);

        user = User.getInstance(); //get instance of the user
        imageUri = user.getProfilePicture();
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadProfile();
        Picasso.get().load(imageUri).into(profileImageView);

        saveBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        signOutBtn.setOnClickListener(this);
        profileImageView.setClickable(true);
        profileImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveBtn:
                save();
                break;
            case R.id.profile_image:
                editProfilePic();
                break;
            case R.id.backBtn:
                finish();
                break;
            case R.id.signOutBtn:
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;
        }
    }

    public void loadProfile()
    {
        eFirstName.setText(user.getFirstName(), TextView.BufferType.EDITABLE);
        eLastName.setText(user.getLastName(), TextView.BufferType.EDITABLE);
        eEmail.setText(user.getEmail(), TextView.BufferType.EDITABLE);
        eSchoolName.setText(user.getSchoolName(), TextView.BufferType.EDITABLE);
        eStudentID.setText(user.getStudentID(), TextView.BufferType.EDITABLE);
        eMajor.setText(user.getMajor(), TextView.BufferType.EDITABLE);
    }

    public void editProfilePic()
    {
        CropImage.activity().setAspectRatio(1, 1).start(EditProfileActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri().toString();
        }
        else
        {
            Toast.makeText(this, "Error Updating Photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfileImage(final FirestoreCallback firestoreCallback)
    {
        // Defining the child of storageReference
        StorageReference ref = sReference.child("ProfileImages/" + user.getId());

        // adding listeners on upload
        // or failure of image
        ref.putFile(Uri.parse(imageUri))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image uploaded successfullySyllabus
                        // Dismiss dialog
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                user.setProfilePicture(uri.toString());
                                firestoreCallback.onCallback();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });
    }

    public void save()
    {
        // Get the text in the textField as string
        final String firstName = eFirstName.getText().toString().trim();
        final String lastName = eLastName.getText().toString().trim();
        final String email = eEmail.getText().toString().trim();
        final String schoolName = eSchoolName.getText().toString().trim();
        final String studentID = eStudentID.getText().toString().trim();
        final String major = eMajor.getText().toString().trim();

        // Fields' validator
        if (TextUtils.isEmpty(firstName)) {
            eFirstName.setError("Required");
            eFirstName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            eLastName.setError("Required");
            eLastName.requestFocus();
            return;
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setMajor(major);
        user.setSchoolName(schoolName);
        user.setStudentID(studentID);

        if(!imageUri.equals(user.getProfilePicture()))
        {
            uploadProfileImage(new FirestoreCallback() {
                @Override
                public void onCallback()
                {
                    mFirestore.collection("users").document(user.getId()).set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    Toast.makeText(EditProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                    return;
                }
            });
        }

        // Upload that new created user to the fireStore
        mFirestore.collection("users").document(user.getId()).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Toast.makeText(EditProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    // Hide the soft keypad when user click on anywhere other than a EditText
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


    private interface FirestoreCallback
    {
        void onCallback();
    }
}