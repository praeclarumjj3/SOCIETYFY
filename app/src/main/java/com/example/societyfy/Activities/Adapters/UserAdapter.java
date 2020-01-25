package com.example.societyfy.Activities.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.societyfy.Activities.models.User;
import com.example.societyfy.R;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> Users;
    private Context mcontext;
    private UserListRecyclerClickListener mClickListener;

    public UserAdapter(List<User> Users, Context mcontext , UserListRecyclerClickListener clickListener){
        this.Users = Users;
        this.mcontext = mcontext;
        mClickListener = clickListener;
    }




    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent,false);
        return new UserViewHolder(view,mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        holder.bind(Users.get(position));

    }



    @Override
    public int getItemCount() {
        return Users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name , mail;
        CircleImageView profile_image;
        User  user;
        UserListRecyclerClickListener mClickListener;

        public UserViewHolder(View itemView , UserListRecyclerClickListener clickListener) {
            super(itemView);
            name = itemView.findViewById(R.id.nameOfUser);
            mail =  itemView.findViewById(R.id.mailOfUser);
            profile_image = itemView.findViewById(R.id.User_imageList);

            mClickListener = clickListener;
            itemView.setOnClickListener(this);
        }

        public void bind(User user) {
            this.user= user;
            mail.setText(user.getEmail());
            name.setText(user.getName());

            Glide.with(mcontext).load(user.getImage()).into(profile_image);

        }

        @Override
        public void onClick(View v) {
            mClickListener.onUserClicked(getAdapterPosition());
        }
    }

    public interface UserListRecyclerClickListener{

        void onUserClicked(int position);
    }
}
