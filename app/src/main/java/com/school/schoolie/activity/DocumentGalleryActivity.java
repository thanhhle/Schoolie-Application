package com.school.schoolie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.school.schoolie.R;
import com.school.schoolie.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DocumentGalleryActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button backBtn, addDocBtn;
    private TableLayout tableLayout;
    private CircleImageView profileImage;

    private List<String> documentNames;
    private List<String> documentURLs;
    private String classId;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_gallery);

        tableLayout = findViewById(R.id.tableLayout);
        backBtn = findViewById(R.id.backBtn);
        addDocBtn = findViewById(R.id.addDocBtn);
        profileImage = findViewById(R.id.profileImage);

        documentNames = new ArrayList<String>();
        documentURLs = new ArrayList<String>();
        classId = getIntent().getStringExtra("classID");
        user = User.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        backBtn.setOnClickListener(this);
        addDocBtn.setOnClickListener(this);

        profileImage.setClickable(true);
        profileImage.setOnClickListener(this);
        Picasso.get().load(user.getProfilePicture()).into(profileImage);

        HashMap<String, String> documents = user.getDocuments(classId);
        documentNames = new ArrayList<String>(documents.keySet());
        documentURLs = new ArrayList<String>(documents.values());
        loadDocuments();
    }


    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.backBtn :
                finish();
                break;

            case R.id.addDocBtn:
                Intent intent = new Intent(this, AddDocumentActivity.class);
                intent.putExtra("classID", classId);
                startActivity(intent);
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


    private void loadDocuments()
    {
        tableLayout.removeAllViews();
        tableLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                tableLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int documentPerRow = 3;
                int numRows = (int) Math.ceil(documentURLs.size() / new Double(documentPerRow));

                int docCount = 0;
                for(int i = 0; i < numRows; i++)
                {
                    TableRow tableRow = new TableRow(getApplicationContext());
                    for(int j = 0; j < documentPerRow; j++)
                    {
                        ImageView docView = new ImageView(getApplicationContext());
                        docView.setPadding(2, 2, 2, 2);

                        Picasso.get().load(documentURLs.get(docCount)).resize(tableLayout.getWidth()/documentPerRow, tableLayout.getWidth()/documentPerRow).centerCrop().into(docView);
                        docView.setClickable(true);

                        int docIndex = docCount;
                        docView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent intent = new Intent(getApplicationContext(), ViewRemoveDocumentActivity.class);
                                intent.putExtra("classID", classId);
                                intent.putExtra("documentName", documentNames.get(docIndex));
                                startActivity(intent);
                            }
                        });

                        tableRow.addView(docView, tableLayout.getWidth()/documentPerRow, tableLayout.getWidth()/documentPerRow);
                        docCount++;

                        if(docCount == documentURLs.size())
                        {
                            tableLayout.addView(tableRow);
                            return;
                        }
                    }
                    tableLayout.addView(tableRow);
                }
            }
        });
    }
}