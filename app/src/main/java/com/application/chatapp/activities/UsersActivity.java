package com.application.chatapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.application.chatapp.R;
import com.application.chatapp.adapters.UserAdapter;
import com.application.chatapp.databinding.ActivityUsersBinding;
import com.application.chatapp.listeners.UserListener;
import com.application.chatapp.models.User;
import com.application.chatapp.utilities.Constants;
import com.application.chatapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener{

    ActivityUsersBinding binding;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);
        getUsers();
        setListeners();

    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                loading(false);
                String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                if (task.isSuccessful() && task.getResult() != null) {
                    List<User> users = new ArrayList<>();

                    for (QueryDocumentSnapshot queryDocumentSnapshots : task.getResult()) {

                        if (currentUserId.equalsIgnoreCase(queryDocumentSnapshots.getId())) {
                            continue;
                        }
                        User user = new User();
                        user.name = queryDocumentSnapshots.getString(Constants.KEY_NAME);
                        user.email = queryDocumentSnapshots.getString(Constants.KEY_EMAIL);
                        user.image = queryDocumentSnapshots.getString(Constants.KEY_IMAGE);
                        user.token = queryDocumentSnapshots.getString(Constants.KEY_FCM_TOKEN);
                        user.id = queryDocumentSnapshots.getId();
                        users.add(user);
                    }

                    if (users.size() > 0) {
                        adapter(users);
                    } else {
                        showErrorMessage();
                    }
                } else {
                    showErrorMessage();
                }
            }
        });
    }

    private void adapter(List<User> users) {
        UserAdapter userAdapter = new UserAdapter(users,this);
        binding.usersRecyclerView.setAdapter(userAdapter);
        binding.usersRecyclerView.setVisibility(View.VISIBLE);
        userAdapter.notifyDataSetChanged();
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No User Available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setListener(User user) {

        Intent intent=new Intent(this,ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}