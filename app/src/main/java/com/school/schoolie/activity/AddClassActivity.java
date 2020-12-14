package com.school.schoolie.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.school.schoolie.model.Class;
import com.school.schoolie.model.ClassDatabase;
import com.school.schoolie.R;
import com.school.schoolie.model.SCalendar;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddClassActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
    private CircleImageView profileImage;
    private Spinner mSpinner,yearSemesterSpinner;
    private SCalendar calendar;

    private EditText mClassNumber, mClassSection;
    private Button mAddClassBtn, mCancelBtn;
    private ProgressBar mProgressBar;

    private FirebaseFirestore mFirestore;

    private List<String> mSubjects;
    private String mClassSubject, userSelectSemester;
    private int currentSemester;

    private boolean classDBExisted = false;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        mFirestore = FirebaseFirestore.getInstance();

        mSpinner = findViewById(R.id.spinner);
        yearSemesterSpinner = findViewById(R.id.yearSemesterSpin);
        mClassNumber = findViewById(R.id.classNumber);
        mClassSection = findViewById(R.id.classSection);
        mAddClassBtn = findViewById(R.id.addClassBtn);
        mCancelBtn = findViewById(R.id.cancelBtn);
        mProgressBar = findViewById(R.id.progressBar);
        profileImage = findViewById(R.id.profileImage);

        calendar = new SCalendar();
        mSubjects = new ArrayList<String>();
        user = User.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        getSubjects(new FirestoreCallback() {
            @Override
            public void onCallback() {
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddClassActivity.this, android.R.layout.simple_spinner_dropdown_item, mSubjects);

                // Apply the adapter to the spinner
                mSpinner.setAdapter(adapter);
            }
        });

        ArrayAdapter<String> semesterAdapter = getSemesterAdapter();

        yearSemesterSpinner.setAdapter(semesterAdapter);
        yearSemesterSpinner.setSelection(currentSemester);
        yearSemesterSpinner.setOnItemSelectedListener(this);

        mSpinner.setOnItemSelectedListener(this);
        mAddClassBtn.setOnClickListener(this);
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
            case R.id.addClassBtn:
                addClass();
                break;
            case R.id.cancelBtn:
                finish();
                break;
            case R.id.profileImage:
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        //parsing parent to extract the view Name
        String nameArr[] = parent.toString().split("/");
        String nameOfView = nameArr[1].substring(0, nameArr[1].length() -1);

        if(nameOfView.equals("yearSemesterSpin")){
            //user selection for semester
            userSelectSemester = parent.getItemAtPosition(pos).toString();
        }
        if(nameOfView.equals("spinner")){
            // On selecting a spinner item
            mClassSubject = parent.getItemAtPosition(pos).toString();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
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

    // Get the list of subjects from FireStore database
    private void getSubjects(final FirestoreCallback firestoreCallback)
    {
        mProgressBar.setVisibility(View.VISIBLE);

        mFirestore.collection("subjects").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        mSubjects.add(document.getId());
                    }
                    firestoreCallback.onCallback();
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(AddClassActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Add the chosen class
    private void addClass()
    {
        final String classNumber = mClassNumber.getText().toString().trim().toUpperCase();
        final String classSection = mClassSection.getText().toString().trim().toUpperCase();

        // Fields' validator
        if(TextUtils.isEmpty(classNumber))
        {
            mClassNumber.setError("Required");
            mClassNumber.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(classSection))
        {
            mClassSection.setError("Required");
            mClassSection.requestFocus();
            return;
        }

        // Hide keyboard after sign in button clicked
        mClassSection.onEditorAction(EditorInfo.IME_ACTION_DONE);

        //show progress bar so user knows google firebase is querying database
        mProgressBar.setVisibility(View.VISIBLE);

        // Create an unique classID to be stored in the database
        final String classID = mClassSubject + " " + classNumber + " - Section "  + classSection + " - " + userSelectSemester;

        // Check if the class exists in the database
        isClassDBExisted(classID, new FirestoreCallback() {
            @Override
            public void onCallback()
            {
                if(classDBExisted)
                {
                    // The class exists in the database
                    mFirestore.collection("classDB").document(classID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot)
                        {
                            ClassDatabase classDB = documentSnapshot.toObject(ClassDatabase.class);
                            if(classDB.getUserIds().contains(user.getId()))
                            {
                                // If the class was already added by the user
                                Toast.makeText(AddClassActivity.this, "This class was already added!", Toast.LENGTH_LONG).show();
                                mProgressBar.setVisibility(View.GONE);
                            }
                            else
                            {
                                // If the class was not added by the user
                                classDB.getUserIds().add(user.getId());
                                addClassToDB(classDB, classID);
                                addClassToUser(classID);
                            }
                        }
                    });
                }
                else {
                    // The class does not exist in the database
                    // Add the current user to the list of user id in the class database
                    ClassDatabase classDB = new ClassDatabase(mClassSubject, classNumber, classSection, userSelectSemester);
                    classDB.getUserIds().add(user.getId());

                    addClassToDB(classDB, classID);
                    addClassToUser(classID);
                }
            }
        });

    }

    // Add the chosen class to the "ClassDB" collection on Firestore
    private void addClassToDB(ClassDatabase classDB, String classID)
    {
        mFirestore.collection("classDB").document(classID).set(classDB).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
                // Show successful message when new account is written to the database
                Toast.makeText(AddClassActivity.this, "Class Added Successfully", Toast.LENGTH_LONG).show();

                // Navigate user to home screen
                startActivity(new Intent(getApplicationContext(), HomescreenActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            // Show error message when new account cannot be written to the database
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(AddClassActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Add the chosen class to the current user database
    private void addClassToUser(final String classID)
    {
        user.getClassList().add(new Class(classID));
        mFirestore.collection("users").document(user.getId()).set(user);
    }

    // Check if the class exists in the "ClassDB" collection on Firebase
    private void isClassDBExisted(final String classId, final FirestoreCallback firestoreCallback)
    {
        mFirestore.collection("classDB").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        if (document.getId().equals(classId)) {
                            classDBExisted = true;
                        }
                    }
                    firestoreCallback.onCallback();
                } else {
                    Toast.makeText(AddClassActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private ArrayAdapter<String> getSemesterAdapter(){
        String year = calendar.getYear();
        int month = calendar.getMonthInteger() + 1; //month starts at zero
        String arr[] = { "Spring " + year, "Summer " + year, "Fall " + year, "Winter " + year };
        ArrayAdapter<String> semesterAdapt = new ArrayAdapter<String>(AddClassActivity.this, android.R.layout.simple_spinner_dropdown_item, arr);

        if(month <= 5)
        {
            currentSemester = 0;
        }
        else if(month > 5 && month < 9)
        {
            currentSemester = 1;
        }
        else if(month >= 9 && month < 12)
        {
            currentSemester = 2;
        }
        else {
            currentSemester = 3;
        }

        return semesterAdapt;
    }

    private interface FirestoreCallback {
        void onCallback();
    }

}
