package com.example.societyfy.Activities.models;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class UserRepo {

    private static final String TAG = "UserRepo";

    private FirebaseFirestore db;

    public UserRepo(FirebaseFirestore db) {
        this.db = db;
    }


    public void getUsers(EventListener<QuerySnapshot> listener) {
        db.collection("Users")
                .orderBy("name")
                .addSnapshotListener(listener);
    }

    public void getStudyUsers(EventListener<QuerySnapshot> listener) {
        db.collection("Study")
                .orderBy("name")
                .addSnapshotListener(listener);
    }

    public void getPlayUsers(EventListener<QuerySnapshot> listener) {
        db.collection("Play")
                .orderBy("name")
                .addSnapshotListener(listener);
    }

    public void getFoodUsers(EventListener<QuerySnapshot> listener) {
        db.collection("Food")
                .orderBy("name")
                .addSnapshotListener(listener);
    }

    public void getHangoutUsers(EventListener<QuerySnapshot> listener) {
        db.collection("Hangout")
                .orderBy("name")
                .addSnapshotListener(listener);
    }

    public void getOtherUsers(EventListener<QuerySnapshot> listener) {
        db.collection("Other")
                .orderBy("name")
                .addSnapshotListener(listener);
    }





    public void addMessageToChatRoom(String roomName,
                                     String senderId,
                                     String senderImage,
                                     String senderName,
                                     String message,
                                     final OnSuccessListener<DocumentReference> successCallback) {
        Map<String, Object> chat = new HashMap<>();
        chat.put("chat_room_name", roomName);
        chat.put("sender_id", senderId);
        chat.put("sender_image",senderImage);
        chat.put("sender name",senderName);
        chat.put("message", message);
        chat.put("sent", System.currentTimeMillis());

        db.collection("chats")
                .add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        successCallback.onSuccess(documentReference);
                    }
                });

    }

    public void getChats(String roomName, EventListener<QuerySnapshot> listener) {
        db.collection("chats")
                .whereEqualTo("chat_room_name", roomName)
                .orderBy("sent", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }


}
