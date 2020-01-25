package com.example.societyfy.Activities.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.societyfy.Activities.LoginFragment;
import com.example.societyfy.Activities.MainActivity;
import com.example.societyfy.Activities.PermissionFragment;
import com.example.societyfy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class SettingsFragment extends Fragment {

    private final String TAG = "DELETE";
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    EditText email, password, reason;
    Button mail_update, password_update, delete_account;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    ProgressBar setting_pro;
    private View v;
    private ExpandableListView information;
    private String[] groups;
    private String[][] children;
    private FirebaseFirestore db;
    private Fragment fragment;

    FragmentTransaction fragmentTransaction;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        groups = new String[]{"About Societyfy"};

        children = new String[][]{
                {"Created by Jitesh Jain.",
                        "Aimed at providing people the opportunity to meet others with similar interests around them.",
                        "Important contributions from Arnesh Agrawal and Prateek Sachan."},
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_settings, container, false);

        ((MainActivity)getActivity()).drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ((MainActivity)getActivity()).toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        ((MainActivity)getActivity()).toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent i = new Intent(getActivity(), MainActivity.class);
                startActivity(i);
            }
        });

        preferences = Objects.requireNonNull(getActivity()).getSharedPreferences("User_pref", Context.MODE_PRIVATE);
        editor = preferences.edit();

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        email = v.findViewById(R.id.new_email);
        password = v.findViewById(R.id.new_pwd);
        reason = v.findViewById(R.id.reason);
        mail_update = v.findViewById(R.id.update_email);
        password_update = v.findViewById(R.id.update_pwd);
        delete_account = v.findViewById(R.id.delete_account);
        setting_pro = v.findViewById(R.id.setting);

        setting_pro.setVisibility(View.INVISIBLE);

        mail_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setting_pro.setVisibility(View.VISIBLE);

                final String mail = email.getText().toString();
                String EMAIL = currentUser.getEmail();
                String PASSWORD = preferences.getString("password", "NO");

                if (!mail.isEmpty()) {

                    assert EMAIL != null;
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(EMAIL, PASSWORD); // Current Login Credentials \\
                    // Prompt the user to re-provide their sign-in credentials
                    currentUser.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    FirebaseAuth.getInstance().getCurrentUser().updateEmail(mail)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                        DocumentReference Ref = db.collection("Users").document(currentUser.getUid());

                                                        Ref.update("email", mail)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.w(TAG, "Error updating document", e);
                                                                    }
                                                                });

                                                        showMessage("Email updated");
                                                        updateUI();
                                                        setting_pro.setVisibility(View.INVISIBLE);
                                                    } else {
                                                        showMessage("Unsuccessful");
                                                        setting_pro.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            });
                                    //----------------------------------------------------------\\
                                }
                            });

                }

            }
        });

        password_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setting_pro.setVisibility(View.VISIBLE);

                final String pwd = password.getText().toString();
                String EMAIL = currentUser.getEmail();
                String PASSWORD = preferences.getString("password", "NO");

                if (!pwd.isEmpty()) {

                    assert EMAIL != null;
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(EMAIL, PASSWORD); // Current Login Credentials \\
                    // Prompt the user to re-provide their sign-in credentials
                    currentUser.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Now change your email address \\
                                    //----------------Code for Changing Email Address----------\\
                                    FirebaseAuth.getInstance().getCurrentUser().updatePassword(pwd)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        editor.putString("password", pwd);
                                                        editor.apply();
                                                        showMessage("Password updated");
                                                        updateUI();
                                                        setting_pro.setVisibility(View.INVISIBLE);
                                                    } else {
                                                        showMessage("Unsuccessful");
                                                        setting_pro.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            });
                                    //----------------------------------------------------------\\
                                }
                            });

                }

            }
        });

        delete_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String EMAIL = currentUser.getEmail();
                setting_pro.setVisibility(View.VISIBLE);
                String PASSWORD = preferences.getString("password", "NO");


                assert EMAIL != null;
                AuthCredential credential = EmailAuthProvider
                        .getCredential(EMAIL, PASSWORD); // Current Login Credentials \\
                // Prompt the user to re-provide their sign-in credentials
                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //Now change your email address \\
                                //----------------Code for Changing Email Address----------\\
                                FirebaseAuth.getInstance().getCurrentUser().delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {


                                                    DocumentReference reference = db.collection("Users").document(currentUser.getUid());

                                                            reference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.d(TAG,"Deleted");
                                                                }
                                                            });

                                                    DocumentReference reference1 =  db.collection("Users' Locations").document(currentUser.getUid()) ;
                                                    reference1.delete()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.d(TAG,"Deleted");
                                                                }
                                                            });


                                                    showMessage("Account deleted");
                                                    updateUI();
                                                    setting_pro.setVisibility(View.INVISIBLE);

                                                } else {
                                                    showMessage("Unsuccessful");
                                                    setting_pro.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        });
                                //----------------------------------------------------------\\
                            }
                        });

            }


        });


        return v;
    }

    private void showMessage(String message) {

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

    }

    private void updateUI() {
        currentUser = null;
        getActivity().finish();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        information = (ExpandableListView) view.findViewById(R.id.Info);
        information.setAdapter(new ExpandableListAdapter(groups, children));
        information.setGroupIndicator(null);

    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private final LayoutInflater inf;
        private String[] groups;
        private String[][] children;

        ExpandableListAdapter(String[] groups, String[][] children) {
            this.groups = groups;
            this.children = children;
            inf = LayoutInflater.from(getActivity());
        }

        @Override
        public int getGroupCount() {
            return groups.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return children[groupPosition].length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return children[groupPosition][childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = inf.inflate(R.layout.infos, parent, false);
                holder = new ViewHolder();

                holder.text = (TextView) convertView.findViewById(R.id.infos);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(getChild(groupPosition, childPosition).toString());

            return convertView;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = inf.inflate(R.layout.list_grp, parent, false);

                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.list_parent);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(getGroup(groupPosition).toString());

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private class ViewHolder {
            TextView text;
        }

    }
}
