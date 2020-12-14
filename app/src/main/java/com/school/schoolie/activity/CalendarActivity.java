package com.school.schoolie.activity;

import androidx.appcompat.app.AppCompatActivity;

import com.school.schoolie.model.SCalendar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.school.schoolie.R;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CalendarActivity extends AppCompatActivity implements View.OnClickListener {
    private float y1, y2;
    private SCalendar tCal;
    private TableLayout tLayout;
    private TextView yearLabel, monthLabel, forwardLabel, backLabel;
    private CircleImageView profileImage;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        monthLabel = findViewById(R.id.monthLabel);
        forwardLabel = findViewById(R.id.forwardLabel);
        backLabel = findViewById(R.id.backLabel);
        yearLabel = findViewById(R.id.yearLabel);
        tLayout = findViewById(R.id.table);
        forwardLabel = findViewById(R.id.forwardLabel);
        backLabel = findViewById(R.id.backLabel);
        profileImage = findViewById(R.id.profileImage);

        tCal = new SCalendar();
        user = User.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        initializeDates();
        forwardLabel.setOnClickListener(this);
        backLabel.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);
    }

    //Function allows actor to swipe back to homeScreen
    public boolean onTouchEvent(MotionEvent touchEvent){
        switch(touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                y2 = touchEvent.getY();
                if(y1 < y2)
                {
                    finish();
                }
                break;
        }
        return false;
    }

    public void monthFoward(){
        tLayout.removeAllViewsInLayout();
        tCal.monthForward();
        initializeDates();
    }

    public void monthBack(){
        tLayout.removeAllViewsInLayout();
        tCal.monthBack();
        initializeDates();
    }

    private void initializeDates(){
        tLayout.removeAllViewsInLayout();
        ArrayList<String> rawDates = tCal.getDates();
        String day = null;

        yearLabel.setText(tCal.getYear());
        monthLabel.setText(tCal.getMonth());
        tLayout.setShrinkAllColumns(true);
        tLayout.setPadding(1,1,1,1);

        for(int row = 0; row < tCal.getTotalWeeks(); row++){
            String splitDates[] = rawDates.get(row).split(",");
            TableRow tRow = new TableRow(this);
            tRow.setPadding(1,1,1,1);
            for(int col = 0; col < 7; col++){

                Button btn = new Button(this);
                btn.setPadding(20,20,10,10);
                btn.setGravity(Gravity.TOP);
                btn.getBackground().setColorFilter(Color.parseColor("#fad5a7"), PorterDuff.Mode.MULTIPLY);
                String dates = splitDates[col];
                //setting dates for button node only if dates exist that month
                if(dates.equals("___")){
                    btn.setEnabled(false);
                }else{
                   btn.setText(dates);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addReminder(v);
                        }
                    });
                }

                //days like then 10 has extra white space in front, getting rid of that white space
                day = btn.getText().toString();
                if(day.length() >1 && day.substring(0,1).equals(" ")){
                    day = day.substring(1,2);
                }

                // if date has reminders then change the color of the date
                if(user.isReminderOn(tCal.getMonth() + " " + day + ", " + tCal.getYear())){
                    btn.setTextColor(getApplication().getResources().getColor(R.color.material_on_surface_emphasis_medium));
                }

                tRow.addView(btn);
            }
            tLayout.addView(tRow);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.forwardLabel:
                monthFoward();
                break;
            case R.id.backLabel:
                monthBack();
                break;
            case R.id.profileImage:
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                break;
        }
    }

    private void addReminder(View v){
        Button btn = (Button)v;
        String day = btn.getText().toString();
        //making sure day doesn't store white space in the front. Example for date 2, its /2  instead of / 2
        if(day.substring(0,1).equals(" ")){
            day = day.substring(1,2);
        }

        // Date clicked on MM DD, YYYY
        String dateClickedOn = tCal.getMonth() + " " + day + ", "+ tCal.getYear();

        Intent intent = new Intent(this, AddReminderActivity.class);
        intent.putExtra("date", dateClickedOn);
        startActivity(intent);
    }
}