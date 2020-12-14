package com.school.schoolie.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.school.schoolie.R;
import com.school.schoolie.model.Class;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddReminderActivity extends AppCompatActivity implements View.OnClickListener ,AdapterView.OnItemSelectedListener{
    private TextView date;
    private Button backBtn, addBtn, deleteBtn;
    private TextView reminderNote;
    private LinearLayout reminderList;
    private Spinner mSpinner;
    private CircleImageView profileImage;

    private FirebaseFirestore mFirestore;

    private User user;
    private ArrayList<String> mClass;
    private String dateNow;
    private String classSelected;
    private View lastViewClickedOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        mFirestore = FirebaseFirestore.getInstance();

        user = User.getInstance();
        mClass = new ArrayList<String>();
        lastViewClickedOn = null;

        date = findViewById(R.id.date);
        backBtn = findViewById(R.id.backBtn);
        mSpinner = findViewById(R.id.spinner2);
        addBtn = findViewById(R.id.addBtn);
        reminderNote = findViewById(R.id.noteReminder);
        reminderList = findViewById(R.id.reminderList);
        deleteBtn = findViewById(R.id.deleteBtn);
        profileImage = findViewById(R.id.profileImage);
    }

    @Override
    protected void onStart() {
        super.onStart();

        addBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        reminderList.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);

        deleteBtn.setVisibility(View.INVISIBLE); // only when a user highlight a reminder will user see this button
        deleteBtn.setEnabled(false);
        dateNow = getIntent().getStringExtra("date");
        date.setText(dateNow);
        loadExistingReminders();
        loadClassList();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddReminderActivity.this, android.R.layout.simple_spinner_dropdown_item, mClass);

        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        deleteBtn.setVisibility(View.INVISIBLE); // only when a user highlight a reminder will user see this button
        deleteBtn.setEnabled(false);
        dateNow = getIntent().getStringExtra("date");
        date.setText(dateNow);
        loadExistingReminders();
        loadClassList();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddReminderActivity.this, android.R.layout.simple_spinner_dropdown_item, mClass);

        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(this);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.addBtn:
                save();
                break;
            case R.id.deleteBtn:
                removeReminder();
                loadExistingReminders();
                break;
            case R.id.reminderList:
                unFocusReminder();
                break;
            case R.id.profileImage:
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                break;
        }
    }

    //load the user class into an arrayList
    private void loadClassList()
    {
        mClass.clear();
        for(Class c: user.getClassList()) {
            if(!c.getIsArchived()) {
                mClass.add(c.getClassName());
            }
        }

        mClass.add("None");
    }

    private void loadExistingReminders() {
        reminderList.removeAllViews();
        List<String> listOfReminders = new ArrayList<String>();

        // Get reminder for each class on this date
        for(Class c: user.getClassList())
        {
            if(c.getReminders().get(dateNow) != null) {
                for(String reminder: c.getReminders().get(dateNow)){
                    listOfReminders.add(c.getClassName() + " - " + reminder);
                }
            }

        }

        if(listOfReminders.size() == 0) {
            listOfReminders.add("None");
        }

        for(String reminder: listOfReminders)
        {
            TextView temp = new TextView(this);
            temp.setText(reminder);
            reminderList.addView(temp);
            //user selects a reminder
            temp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reminderSelectedHandler(v);
                }
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //this is the class the user selected in the spinner
        classSelected = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
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

    private void save()
    {
        final String reminder = reminderNote.getText().toString().trim();

        // Fields' validator
        if(TextUtils.isEmpty(reminder)) {
            reminderNote.setError("Required");
            reminderNote.requestFocus();
            return;
        }

        // checking if user input duplicates
        for(Class c: user.getClassList())
        {
            if(c.getReminders().get(dateNow) != null)
            {
                for(String r: c.getReminders().get(dateNow))
                {
                    if(reminder.equals(r) && classSelected.equals(c.getClassName())){
                        reminderNote.setError("Reminder already exist");
                        reminderNote.requestFocus();
                        return;
                    }
                }
            }
        }

        user.getClassByName(classSelected).addReminder(dateNow, reminder);
        reminderNote.setText("");

        // Update the user to the User database
        mFirestore.collection("users").document(user.getId()).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddReminderActivity.this, "Reminder Added on " + dateNow, Toast.LENGTH_LONG).show();
                        loadExistingReminders();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddReminderActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void reminderSelectedHandler(View v){
        TextView text = (TextView) v;
        String reminderText = text.getText().toString();
        if(reminderText.equals("None")){return;}

        if(lastViewClickedOn != null){
            lastViewClickedOn.setBackgroundColor(Color.TRANSPARENT);
        }
        lastViewClickedOn = v;

        v.setBackgroundColor(Color.parseColor("#0c94f5"));
        deleteBtn.setEnabled(true);
        deleteBtn.setVisibility(View.VISIBLE);
    }

    private void unFocusReminder(){
        // only when a user highlight a reminder will user see this button
        deleteBtn.setVisibility(View.INVISIBLE);
        deleteBtn.setEnabled(false);

        if(lastViewClickedOn != null){
            lastViewClickedOn.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void removeReminder(){
        TextView temp = (TextView) lastViewClickedOn;
        String rArr[] = temp.getText().toString().split("-");
        String className = rArr[0].substring(0,rArr[0].length()-1); //getting rid of the whitespace at the end of string
        String myReminder = rArr[1].substring(1);

        // Get reminder for each class on this date
        for(Class c: user.getClassList()) {
            if(c.getReminders().get(dateNow) != null) {
                for(String reminder: c.getReminders().get(dateNow)){
                    if(c.getClassName().contains(className) && reminder.contains(myReminder)){
                        c.removeReminder(dateNow, myReminder);
                    }
                }
            }
        }

        // Update the user to the User database
        mFirestore.collection("users").document(user.getId()).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddReminderActivity.this, "Reminder Deleted", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddReminderActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        onResume();
    }
}