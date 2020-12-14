package com.school.schoolie.model;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatMessage
{
    private String senderID;
    private String receiverID;
    private String userName;
    private String message;
    private String createdTime;

    public ChatMessage()
    {

    }

    public ChatMessage(String senderID, String receiverID, String userName, String message, String createdTime)
    {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.userName = userName;
        this.message = message;
        this.createdTime = createdTime;
    }

    public String getSenderID()
    {
        return this.senderID;
    }

    public String getReceiverID()
    {
        return this.receiverID;
    }

    public String getMessage()
    {
        return this.message;
    }

    public String getCreatedTime()
    {
        return this.createdTime;
    }

    public String getUserName()
    {
        return this.userName;
    }
}
