package com.school.schoolie.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.school.schoolie.R;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewRemoveDocumentActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button backBtn, removeDocBtn;
    private CircleImageView profileImage;
    private ImageView documentImage;
    private TextView documentNameView;

    private FirebaseStorage mFirebaseStorage;
    private FirebaseFirestore mFirestore;

    private String classId;
    private String documentURL;
    private String documentName;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_document);

        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        backBtn = findViewById(R.id.backButton);
        removeDocBtn = findViewById(R.id.removeDocBtn);
        profileImage = findViewById(R.id.profileImage);
        documentImage = findViewById(R.id.documentImage);
        documentNameView = findViewById(R.id.documentName);

        user = User.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        backBtn.setOnClickListener(this);
        removeDocBtn.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);

        documentName = getIntent().getStringExtra("documentName");
        documentNameView.setText(documentName);

        classId = getIntent().getStringExtra("classID");
        documentURL = user.getDocuments(classId).get(documentName);
        Picasso.get().load(documentURL).into(documentImage);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.backButton:
                finish();
                break;

            case R.id.removeDocBtn:
                removeDocument();
                finish();
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


    private void removeDocument()
    {
        // Remove the document from User instance
        user.getDocuments(classId).remove(documentName);

        // Update the User instance to the database
        mFirestore.collection("users").document(user.getId()).set(user);

        // Remove the document from the Firebase storage
        mFirebaseStorage.getReferenceFromUrl(documentURL).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ViewRemoveDocumentActivity.this, "Document Removed Successfully", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ViewRemoveDocumentActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}