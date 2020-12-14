package com.school.schoolie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.school.schoolie.R;
import com.school.schoolie.model.Class;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomescreenActivity extends AppCompatActivity implements View.OnClickListener {
    private Button helpBtn;
    private ImageButton messagesBtn;
    private FloatingActionButton mAddClassBtn, mRemoveClassBtn;
    private Switch mSwitchBtn;
    private TableLayout tLayout;
    private CircleImageView profileImage;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private List<Class> mClasses;
    private User user;
    private float y1, y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mAddClassBtn = findViewById(R.id.addClassBtn);
        mRemoveClassBtn = findViewById(R.id.removeClassBtn);
        mSwitchBtn = findViewById(R.id.switchBtn);

        helpBtn = findViewById(R.id.helpBtn);
        messagesBtn = findViewById(R.id.messagesBtn);
        tLayout = findViewById(R.id.tableLayout);
        profileImage = findViewById(R.id.profileImage);

        mClasses = new ArrayList<Class>();
    }

    @Override
    protected void onStart(){
        super.onStart();

        //if phone is not already signed in
        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class)); //send to login in screen
            finish();
        }

        getCurrentUser(new FirestoreCallback() {
            @Override
            public void onCallback() {
                Picasso.get().load(user.getProfilePicture()).into(profileImage);

                mSwitchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                        {
                            loadClasses(true);
                            addClassButtons();
                        }
                        else
                        {
                            loadClasses(false);
                            addClassButtons();
                        }
                    }
                });

                loadClasses(false);
                addClassButtons();
            }
        });


        mAddClassBtn.setOnClickListener(this);
        mRemoveClassBtn.setOnClickListener(this);
        helpBtn.setOnClickListener(this);
        messagesBtn.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
    }

    public boolean onTouchEvent(MotionEvent touchEvent){
        switch(touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                y2 = touchEvent.getY();
                if(y2 < y1)
                {
                    startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                }
                break;
        }
        return false;
    }

    private void getCurrentUser(FirestoreCallback firestoreCallback)
    {
        mFirestore.collection("users").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User retrievedUser = documentSnapshot.toObject(User.class);
                user = User.getInstance();
                user.clone(retrievedUser);
                firestoreCallback.onCallback();
            }
        });
    }

    private void loadClasses(boolean loadArchivedClass)
    {
        mClasses.clear();
        for(Class c: user.getClassList())
        {
            if(c.getIsArchived() == loadArchivedClass)
            {
                mClasses.add(c);
            }
        }
    }

    private void addClassButtons() {
        tLayout.removeAllViewsInLayout();

        int classBtnPerRow = 2;
        int numRows = (int) Math.ceil(mClasses.size() / new Double(classBtnPerRow));

        int classCount = 0;
        for(int i = 0; i < numRows; i++)
        {
            TableRow tableRow = new TableRow(this);
            for(int j = 0; j < classBtnPerRow; j++)
            {
                Button classBtn = new Button(this);

                // Customize the button
                classBtn.getBackground().setColorFilter(Color.parseColor("#FF9D00"), PorterDuff.Mode.MULTIPLY);
                classBtn.setTextSize(20);
                classBtn.setTextColor(Color.WHITE);

                // Set the width of the button
                classBtn.setWidth(tLayout.getWidth()/classBtnPerRow);

                // Set the height of the button
                if(classCount > 8) {
                    classBtn.setHeight(tLayout.getHeight()/numRows);
                }
                else {
                    classBtn.setHeight(tLayout.getHeight()/4);
                }

                classBtn.setText(mClasses.get(classCount).getClassName());

                int classIndex = classCount;
                classBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(getApplicationContext(), ClassPageActivity.class);
                        intent.putExtra("className", mClasses.get(classIndex).getClassName());
                        startActivity(intent);
                    }
                });

                tableRow.addView(classBtn);
                classCount++;

                if(classCount == mClasses.size()) {
                    tLayout.addView(tableRow);
                    return;
                }
            }
            tLayout.addView(tableRow);
        }
    }

    // Adding a listener to the buttons
    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.addClassBtn:
                startActivity(new Intent(getApplicationContext(), AddClassActivity.class));
                break;

            case R.id.removeClassBtn:
                startActivity(new Intent(getApplicationContext(), RemoveClassActivity.class));
                break;

            case R.id.helpBtn:
                startActivity(new Intent(getApplicationContext(), HelpPageActivity.class));
                break;

            case R.id.messagesBtn:
                startActivity(new Intent(getApplicationContext(), ChatHistoryActivity.class));
                break;

            case R.id.profileImage:
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                break;
        }
    }

    private interface FirestoreCallback {
        void onCallback();
    }
}