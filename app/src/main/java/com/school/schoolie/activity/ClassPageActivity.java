
package com.school.schoolie.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.school.schoolie.model.Class;
import com.school.schoolie.model.ClassDatabase;
import com.school.schoolie.model.GradeItem;
import com.school.schoolie.R;
import com.school.schoolie.model.SCalendar;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClassPageActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView mClassId, mClassGrade, mClassGradeLetter, reminderTomrw, reminderToday;
    private Button mDocBtn, mClassmateBtn, mSyllabusBtn, mViewGradesBtn, mBackBtn, mArchiveBtn;
    private TableLayout mTableLayout;
    private CircleImageView profileImage;

    private FirebaseFirestore mFirestore;

    private SCalendar cal;
    private User user;
    private Class mClass;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_page);

        mFirestore = FirebaseFirestore.getInstance();

        mClassId = findViewById(R.id.classId);
        mClassGrade = findViewById(R.id.classGrade);
        mClassGradeLetter = findViewById(R.id.classGradeLetter);
        reminderTomrw = findViewById(R.id.reminderTomorrow);
        reminderToday = findViewById(R.id.reminderToday);
        mDocBtn = findViewById(R.id.docBtn);
        mClassmateBtn = findViewById(R.id.classmatesBtn);
        mSyllabusBtn = findViewById(R.id.syllabusBtn);
        mViewGradesBtn = findViewById(R.id.viewGradesBtn);
        mBackBtn = findViewById(R.id.backBtn);
        mArchiveBtn = findViewById(R.id.archiveBtn);
        mTableLayout = findViewById(R.id.tableLayout);
        profileImage = findViewById(R.id.profileImage);

        cal = new SCalendar();
        user = User.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mClass = user.getClassByName(getIntent().getStringExtra("className"));
        String className = mClass.getClassName();

        mClassId.setText(className);
        loadGrades();
        checkReminder();

        if(mClass.getIsArchived())
        {
            mArchiveBtn.setText("Unarchive This Class");
        }
        else
        {
            mArchiveBtn.setText("Archive This Class");
        }

        reminderTomrw.setGravity(Gravity.CENTER);
        reminderToday.setGravity(Gravity.CENTER);

        mDocBtn.setOnClickListener(this);
        mClassmateBtn.setOnClickListener(this);
        mSyllabusBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mViewGradesBtn.setOnClickListener(this);
        mArchiveBtn.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.docBtn:
                Intent intent = new Intent(this, DocumentGalleryActivity.class);
                intent.putExtra("classID", mClass.getClassId());
                startActivity(intent);
                break;
            case R.id.classmatesBtn:
                intent = new Intent(this, ClassmateListActivity.class);
                intent.putExtra("classID", mClass.getClassId());
                startActivity(intent);
                break;
            case R.id.syllabusBtn:
                intent = new Intent(this, AddSyllabusActivity.class);
                intent.putExtra("classID", mClass.getClassId());
                startActivity(intent);
                break;
            case R.id.viewGradesBtn:
                intent = new Intent(this, ViewGradesActivity.class);
                intent.putExtra("classID", mClass.getClassId());
                startActivity(intent);
                break;
            case R.id.archiveBtn:
                clickArchiveBtn();
                break;
            case R.id.profileImage:
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                break;
            case R.id.backBtn:
                finish();
                break;
        }
    }


    private void loadGrades()
    {
        HashMap<String, GradeItem> grades = mClass.getGrades();
        Set<String> keys = grades.keySet();

        while (mTableLayout.getChildCount() > 1)
        {
            mTableLayout.removeView(mTableLayout.getChildAt(mTableLayout.getChildCount() - 1));
        }

        if(keys.size() > 0)
        {
            for(String key: keys)
            {
                final TableRow tableRow = new TableRow(ClassPageActivity.this);
                final TextView gradeItem = new TextView(ClassPageActivity.this);
                final TextView grade = new TextView(ClassPageActivity.this);

                gradeItem.setText(key);
                grade.setText(grades.get(key).getGrade() + "%");

                gradeItem.setGravity(Gravity.LEFT);
                grade.setGravity(Gravity.RIGHT);

                gradeItem.setHeight(70);
                grade.setHeight(70);

                tableRow.addView(gradeItem);
                tableRow.addView(grade);

                mTableLayout.addView(tableRow);
            }
        }

        int totalGrade = user.getTotalGrade(mClass.getClassId());
        mClassGrade.setText(String.valueOf(totalGrade) + "%");
        mClassGradeLetter.setText(getGradeLetter(totalGrade));
    }

    private String getGradeLetter(int totalGrade)
    {
        if(totalGrade >= 90) { return "A"; }
        else if(totalGrade >= 80) { return "B"; }
        else if(totalGrade >= 70) { return "C"; }
        else if(totalGrade >= 60) { return "D"; }
        else { return "F"; }
    }

    private void clickArchiveBtn()
    {
        if(mClass.getIsArchived())
        {
            unarchiveClass();
        }
        else
        {
            archiveClass();
        }

        startActivity(new Intent(getApplicationContext(), HomescreenActivity.class));
        finish();
    }

    private void archiveClass()
    {
        // Remove the user from the classDB
        mFirestore.collection("classDB").document(mClass.getClassId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                ClassDatabase classDB = documentSnapshot.toObject(ClassDatabase.class);
                classDB.getUserIds().remove(user.getId());
                mFirestore.collection("classDB").document(mClass.getClassId()).set(classDB);
            }
        });

        // Set the isArchived variable in the class object to true
        mClass.setIsArchived(true);

        // Update the class in the user instance
        user.getClassList().set(user.getClassList().indexOf(mClass), mClass);

        // Update the user to the User database
        mFirestore.collection("users").document(user.getId()).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ClassPageActivity.this, "Class Archived Successfully", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ClassPageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void unarchiveClass()
    {
        // Add the user id back to the classDB
        mFirestore.collection("classDB").document(mClass.getClassId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                ClassDatabase classDB = documentSnapshot.toObject(ClassDatabase.class);
                classDB.getUserIds().add(user.getId());
                mFirestore.collection("classDB").document(mClass.getClassId()).set(classDB);
            }
        });

        // Set the isArchived variable in the class object to true
        mClass.setIsArchived(false);

        // Update the class in the user instance
        user.getClassList().set(user.getClassList().indexOf(mClass), mClass);

        // Update the user to the User database
        mFirestore.collection("users").document(user.getId()).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ClassPageActivity.this, "Class Unarchived Successfully", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ClassPageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkReminder(){
        String myClassID = mClass.getClassId();
        //the minus 1 for the end index gets rid of the white space behind the name
        String myClass = myClassID.substring(0,myClassID.indexOf("-") - 1);
        ArrayList<String> reminderListTomrrw = null;
        ArrayList<String> reminderListToday = null;
        String tomorrow = cal.getDateFromTodayLONG(1);
        String today = cal.getDateTodayLONG();
        String reminderTagTomrrw = "";
        String reminderTagToday = "";

        //checking tomorrow & today reminders
        for(Class c: user.getClassList()){
            if(c.getClassName().equals(myClass)){
                reminderListToday = c.getReminderListOn(today); //get today if exist
                reminderListTomrrw = c.getReminderListOn(tomorrow);  //get tomorrow if exist
            }
        }

        //there are reminders today
        if(reminderListToday != null){
            reminderTagToday += "Today's Reminder: ";
            for(String r: reminderListToday){
                reminderTagToday += r + ", ";
            }
            reminderTagToday = reminderTagToday.substring(0,reminderTagToday.length()-2); //removing the last comma
            reminderToday.setText(reminderTagToday);
            reminderToday.setVisibility(View.VISIBLE);
        }

        //there are reminders tomorrow
        if(reminderListTomrrw != null){
            reminderTagTomrrw += "Tomorrow's Reminder: ";
            for(String r: reminderListTomrrw){
                reminderTagTomrrw += r + ", ";
            }
            reminderTagTomrrw = reminderTagTomrrw.substring(0,reminderTagTomrrw.length()-2); //removing the last comma
            reminderTomrw.setText(reminderTagTomrrw);
            reminderTomrw.setVisibility(View.VISIBLE);
        }
    }
}