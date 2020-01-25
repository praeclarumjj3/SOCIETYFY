package com.example.societyfy.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.societyfy.Activities.Adapters.ChatsAdapter;
import com.example.societyfy.Activities.Fragments.HomeFragment;
import com.example.societyfy.Activities.Fragments.UserListFragment;
import com.example.societyfy.Activities.models.Chat;
import com.example.societyfy.Activities.models.User;
import com.example.societyfy.Activities.models.UserLocation;
import com.example.societyfy.Activities.models.UserRepo;
import com.example.societyfy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class HangoutFragment extends Fragment {




    private static final String CURRENT_USER_KEY = "CURRENT_USER_KEY";
    private static final String CURRENT_USER_NAME = "CURRENT_USER_NAME";
    private static final String CURRENT_USER_IMAGE = "CURRENT_USER_IMAGE";

    private RecyclerView chats;
    private ChatsAdapter adapter;

    private Fragment fragment;

    FragmentTransaction fragmentTransaction;

    private String userId = "";
    private String username="";
    private String userImage="";

    private UserRepo userRepo;

    private EditText message;
    private ImageButton send;
    View v;

    private FirebaseFirestore db;
    private ImageView back;
    private final String TAG = "DELETE";

    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) Objects.requireNonNull(getActivity())).setActionBarTitle("Hangout Channel");
    }

    @Override
    public void onResume() {
        super.onResume();

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_hangout, container, false);


        userRepo = new UserRepo(FirebaseFirestore.getInstance());
        db = FirebaseFirestore.getInstance();
        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ((MainActivity)getActivity()).toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        ((MainActivity)getActivity()).toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id2 = FirebaseAuth.getInstance().getUid();
                CollectionReference Ref = db.collection("Hangout");
                Query query = Ref.whereEqualTo("user_id",id2);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                                snapshot.getReference().delete();
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");


                            }
                        }

                    }
                });

                final Intent i = new Intent(getActivity(), MainActivity.class);
                startActivity(i);
            }
        });

        userId = getCurrentUserKey();
        username=getCurrentUserName();
        userImage=getCurrentUserImage();

        initUI();
        showChatMessages();
        getUsers();




        return v;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.setting_menu).setVisible(false).setEnabled(false);
        menu.findItem(R.id.study_users).setVisible(true).setEnabled(true);
        menu.findItem(R.id.chat_study).setVisible(true).setEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch(id) {

            case R.id.chat_study:
                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                fragmentTransaction.replace(R.id.fragment, new HangoutFragment());
                fragmentTransaction.commit();
                break;

            case R.id.study_users:
                fragment = new Hangout_list();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(getString(R.string.intent_huser_locations), mUserLocations);
                fragment.setArguments(bundle);

                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                fragmentTransaction.replace(R.id.fragment, fragment);
                fragmentTransaction.commit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getCurrentUserKey() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getString(CURRENT_USER_KEY, "");
    }

    private String getCurrentUserName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getString(CURRENT_USER_NAME, "");
    }

    private String getCurrentUserImage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getString(CURRENT_USER_IMAGE, "");
    }

    private void getUsers() {

        userRepo.getHangoutUsers(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("UserList", "Listen failed.", e);
                    return;
                }
                List<User> userList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    userList.add(new User(doc.getString("email"), doc.getString("image"), doc.getString("name"), doc.getString("user_id")));
                    User user = new User(doc.getString("email"), doc.getString("image"), doc.getString("name"), doc.getString("user_id"));
                    getUserLocation(user);
                }


            }
        });
    }

    private void getUserLocation(User user) {

        DocumentReference locationRef = db.collection("Users' Locations").document(user.getUser_id());

        locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(Objects.requireNonNull(task.getResult()).toObject(UserLocation.class)!= null){
                        mUserLocations.add(task.getResult().toObject(UserLocation.class));
                    }

                }

            }
        });

    }


    private void initUI() {
        message = v.findViewById(R.id.message_text);
        send = v.findViewById(R.id.send_message);
        chats = v.findViewById(R.id.chats);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setReverseLayout(true);
        chats.setLayoutManager(manager);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(),
                            getString(R.string.error_empty_message),
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    addMessageToChatRoom();
                    showChatMessages();
                }
            }
        });
    }


    private void addMessageToChatRoom() {
        String chatMessage = message.getText().toString();
        message.setText("");
        send.setEnabled(false);
        userRepo.addMessageToChatRoom(
                "Hangout",
                userId,
                userImage,
                username,
                chatMessage,
                new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        send.setEnabled(true);
                    }
                }
        );
    }

    private void showChatMessages() {
        userRepo.getChats("Hangout", new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("ChatRoomActivity", "Listen failed.", e);
                    return;
                }

                List<Chat> messages = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    messages.add(
                            new Chat(
                                    doc.getId(),
                                    doc.getString("chat_room_name"),
                                    doc.getString("sender_id"),
                                    doc.getString("sender_image"),
                                    doc.getString("sender name"),
                                    doc.getString("message"),
                                    doc.getLong("sent")
                            )
                    );
                }

                adapter = new ChatsAdapter(messages,getContext(), userId);
                chats.setAdapter(adapter);
            }
        });
    }


}
