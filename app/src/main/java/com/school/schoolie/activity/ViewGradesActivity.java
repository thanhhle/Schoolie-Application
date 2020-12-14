package com.school.schoolie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.school.schoolie.model.GradeItem;
import com.school.schoolie.R;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewGradesActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button mAddGradeBtn, mEditGradeBtn, mCancelBtn;
    private TextView mTotalGrade;
    private TableLayout mTableLayout;
    private CircleImageView profileImage;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_grades);

        mTableLayout = findViewById(R.id.tableLayout);
        mTotalGrade = findViewById(R.id.totalGrade);
        mAddGradeBtn = findViewById(R.id.addGradeBtn);
        mEditGradeBtn = findViewById(R.id.editGradeBtn);
        mCancelBtn = findViewById(R.id.cancelBtn);
        profileImage = findViewById(R.id.profileImage);

        user = User.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mAddGradeBtn.setOnClickListener(this);
        mEditGradeBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);

        loadGrades();
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.addGradeBtn:
                Intent intent = new Intent(this, AddGradeActivity.class);
                intent.putExtra("classID", getIntent().getStringExtra("classID"));
                startActivity(intent);
                break;
            case R.id.editGradeBtn:
                intent = new Intent(this, EditGradeActivity.class);
                intent.putExtra("classID", getIntent().getStringExtra("classID"));
                startActivity(intent);
                break;
            case R.id.profileImage:
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                break;
            case R.id.cancelBtn:
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

    private void loadGrades()
    {
        HashMap<String, GradeItem> grades = user.getGrades(getIntent().getStringExtra("classID"));
        Set<String> keys = grades.keySet();

        while (mTableLayout.getChildCount() > 1)
        {
            mTableLayout.removeView(mTableLayout.getChildAt(mTableLayout.getChildCount() - 1));
        }

        if(keys.size() > 0)
        {
            for(String key: keys)
            {
                final TableRow tableRow = new TableRow(ViewGradesActivity.this);
                final TextView gradeItem = new TextView(ViewGradesActivity.this);
                final TextView point = new TextView(ViewGradesActivity.this);
                final TextView weight = new TextView(ViewGradesActivity.this);
                final TextView grade = new TextView(ViewGradesActivity.this);

                gradeItem.setText(key);
                point.setText(grades.get(key).getPointAchieved() + "/" + grades.get(key).getTotalPoint());
                weight.setText(grades.get(key).getWeightAchieved() + "/" + grades.get(key).getWeight());
                grade.setText(grades.get(key).getGrade() + "%");

                gradeItem.setGravity(Gravity.LEFT);
                point.setGravity(Gravity.RIGHT);
                weight.setGravity(Gravity.RIGHT);
                grade.setGravity(Gravity.RIGHT);

                gradeItem.setHeight(70);
                point.setHeight(70);
                weight.setHeight(70);
                grade.setHeight(70);

                tableRow.addView(gradeItem);
                tableRow.addView(point);
                tableRow.addView(weight);
                tableRow.addView(grade);

                mTableLayout.addView(tableRow);
            }
        }

        int totalGrade = user.getTotalGrade(getIntent().getStringExtra("classID"));
        mTotalGrade.setText(String.valueOf(totalGrade) + "%");
    }
}