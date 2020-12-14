package com.school.schoolie.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.school.schoolie.R;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class AddSyllabusActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button backBtn, uploadBtn;
    private ImageView documentView;
    private CircleImageView profileImage;

    private String fileUri;
    private int PICK_IMAGE_REQUEST = 71;

    //Firebase
    private StorageReference sReference;
    private FirebaseStorage fStorage;
    private FirebaseFirestore mFirestore;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_syllabus);

        mFirestore = FirebaseFirestore.getInstance();

        documentView = findViewById(R.id.documentView);
        backBtn = findViewById(R.id.backBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        profileImage = findViewById(R.id.profileImage);

        fStorage = FirebaseStorage.getInstance();
        sReference = fStorage.getReference();

        user = User.getInstance(); //get instance of the user

        fileUri = user.getClassById(getIntent().getStringExtra("classID")).getSyllabus();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!fileUri.equals(""))
        {
            Picasso.get().load(fileUri).into(documentView);
        }

        uploadBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.documentView:
                chooseImage();
                break;
            case R.id.backBtn:
                finish();
                break;
            case R.id.uploadBtn:
                uploadDocument();
                break;
            case R.id.profileImage:
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                break;
        }
    }

    private void chooseImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the Uri of data
            fileUri = data.getData().toString();
            Picasso.get().load(fileUri).into(documentView);
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


    private void uploadDocument()
    {
        if (!fileUri.equals(""))
        {
            // Defining the child of storageReference
            StorageReference ref = sReference.child("Syllabus/" + user.getId() + "/" + getIntent().getStringExtra("classID"));

            // adding listeners on upload
            // or failure of image
            ref.putFile(Uri.parse(fileUri))
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
                                    user.getClassById(getIntent().getStringExtra("classID")).setSyllabus(uri.toString());
                                    mFirestore.collection("users").document(user.getId()).set(user);
                                    Toast.makeText(AddSyllabusActivity.this, "Document Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddSyllabusActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
        }
    }
}