package com.example.societyfy.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.societyfy.R;

public class PermissionFragment extends Fragment {

    private Button yesbtn;
    private Button nobtn;
    private ProgressBar yespro;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_permission, container, false);

        yesbtn=v.findViewById(R.id.yes_perbtn);
        nobtn=v.findViewById(R.id.no_perbtn);
        yespro=v.findViewById(R.id.yesperm);

        nobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nobtn.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), "Please grant PERMISSION!!!", Toast.LENGTH_LONG).show();
                nobtn.setVisibility(View.VISIBLE);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                fragmentTransaction.replace(R.id.fragment, new PermissionFragment());
                fragmentTransaction.commit();

            }
        });

        yesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yesbtn.setVisibility(View.INVISIBLE);
                yespro.setVisibility(View.VISIBLE);
                updateUI();

            }
        });

        return v;
    }



    private void updateUI() {


        final Intent i = new Intent(getActivity(), MainActivity.class);
        startActivity(i);
    }


}
