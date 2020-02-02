package com.example.societyfy.Activities.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.societyfy.Activities.models.Chat;
import com.example.societyfy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.sql.Ref;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.opencensus.internal.Utils;

public class ChatsAdapter extends RecyclerView.Adapter {
    private static final int SENT = 1;
    private static final int RECEIVED = 2;
    private final String TAG = "DELETE";

    private Context mContext;
    private List<Chat> chats;
    private String userId;

    private FirebaseFirestore db;


    public ChatsAdapter(List<Chat> chats, Context mContext , String userId) {
        this.chats = chats;
        this.mContext = mContext;
        this.userId = userId;
    }

    @Override
    public  RecyclerView.ViewHolder  onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_sent_item, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_recieved_item, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Chat chat = chats.get(position);


        switch (holder.getItemViewType()) {
            case SENT:
                ((SentMessageHolder) holder).bind(chat);
                break;
            case RECEIVED:
                ((ReceivedMessageHolder) holder).bind(chat);
        }
    }



    @Override
    public int getItemViewType(int position) {
        if (chats.get(position).getSenderId().contentEquals(userId)) {
            return SENT;
        } else {
            return RECEIVED;
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, dateText;
        ConstraintLayout msg;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            dateText = itemView.findViewById(R.id.text_message_date);
            msg = itemView.findViewById(R.id.sent_msg);
        }

        void bind(Chat chat) {
            messageText.setText(chat.getMessage());
            dateText.setText(DateUtils.formatDateTime(mContext,chat.getSent(),DateUtils.FORMAT_NO_YEAR));

            // Format the stored timestamp into a readable String using method.
            timeText.setText(DateUtils.formatDateTime(mContext,chat.getSent(),DateUtils.FORMAT_NO_YEAR|DateUtils.FORMAT_SHOW_TIME));

            msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final int i = getAdapterPosition();
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Delete Message");
                    builder.setMessage("Are you sure about deleting the message?");

                    builder.setPositiveButton("Delete" , new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            deleteMessage(i);

                        }
                    });

                    builder.setNegativeButton("Keep the message", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    });

                    builder.create().show();

                }
            });
        }
    }

    private void deleteMessage(int x) {




        db = FirebaseFirestore.getInstance();
       Long  msgTime = chats.get(x).getSent();



        CollectionReference Ref = db.collection("chats");
        Query query = Ref.whereEqualTo("sent",msgTime);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (QueryDocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                        snapshot.getReference().delete();
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        Toast.makeText(mContext, "message deleted...", Toast.LENGTH_SHORT).show();


                    }
                }

            }
        });



}

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText, dateText;
        CircleImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            dateText = itemView.findViewById(R.id.text_message_date);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
            profileImage = itemView.findViewById(R.id.image_message_profile);
        }

        void bind(Chat chat) {
            messageText.setText(chat.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(DateUtils.formatDateTime(mContext,chat.getSent(),DateUtils.FORMAT_SHOW_TIME ));
            dateText.setText(DateUtils.formatDateTime(mContext,chat.getSent(),DateUtils.FORMAT_NO_YEAR));

            nameText.setText(chat.getSenderName());

            // Insert the profile image from the URL into the ImageView.
            Glide.with(mContext).load(chat.getUserImage()).into(profileImage);
        }
    }

}
