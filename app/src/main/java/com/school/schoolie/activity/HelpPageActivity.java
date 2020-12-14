package com.school.schoolie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.barteksc.pdfviewer.PDFView;
import com.school.schoolie.R;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HelpPageActivity extends AppCompatActivity implements View.OnClickListener
{
    private PDFView mPDFView;
    private Button backBtn;
    private CircleImageView profileImage;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_page);
        mPDFView = findViewById(R.id.pdfView);
        backBtn = findViewById(R.id.backBtn);
        profileImage = findViewById(R.id.profileImage);


        user = User.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mPDFView.fromAsset("helpPage.pdf").load();

        backBtn.setOnClickListener(this);
        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.backBtn:
                finish();
                break;
            case R.id.profileImage:
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                break;
        }
    }
}