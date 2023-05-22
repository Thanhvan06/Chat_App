package com.mianasad.chatsss.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.mianasad.chatsss.R;
import com.mianasad.chatsss.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;
    String name, image, about;
    String senderId, receiveId;

    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        getSupportActionBar().hide();

        name = getIntent().getStringExtra("name");
        image = getIntent().getStringExtra("image");
        about = getIntent().getStringExtra("about");

        receiveId = getIntent().getStringExtra("friendId");
        senderId = FirebaseAuth.getInstance().getUid();

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        database.getReference("users")
                .child(senderId)
                .child("friendList")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean isFriend = false;
                        if (dataSnapshot.exists()){
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                String friendID = snapshot.getValue(String.class);
                                if (friendID.equals(receiveId)){
                                    isFriend = true;
                                }else {
                                    isFriend = false;
                                }
                            }
                            System.out.println(isFriend);
                            if (isFriend == true){
                                binding.btnAddFriend.setVisibility(View.GONE);
                                binding.btnCancelFriend.setVisibility(View.VISIBLE);
                            }else {
                                binding.btnAddFriend.setVisibility(View.VISIBLE);
                                binding.btnCancelFriend.setVisibility(View.GONE);
                            }
                        }else{
                            System.out.println("Nothing");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Log.e("Firebase", "Error: " + databaseError.getMessage());
                    }
                });

        Glide.with(ProfileActivity.this)
                .load(image)
                .placeholder(R.drawable.avatar)
                .into(binding.profileImage);

        binding.etStatus.setText(about);
        binding.etUserName.setText(name);

        binding.btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFriendRequest(receiveId,senderId);
            }
        });

        binding.btnCancelFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelFriendRequest(receiveId, senderId);
            }
        });

    }

    private void cancelFriendRequest(String receiverUid, String senderId) {

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        Query query = databaseRef.child("users")
                .child(senderId)
                .child("friendList")
                .orderByValue()
                .equalTo(receiverUid);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    // Xóa dữ liệu thành công
                                    Toast.makeText(ProfileActivity.this, "Cancel friend success", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xóa dữ liệu thất bại
                                    Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra
                Log.e("Firebase", "Error: " + databaseError.getMessage());
            }
        });

        DatabaseReference databaseReff = FirebaseDatabase.getInstance().getReference();
        Query queryy = databaseRef.child("users")
                .child(receiverUid)
                .child("friendList")
                .orderByValue()
                .equalTo(senderId);

        queryy.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {
                for (DataSnapshot snapshott : dataSnapshot1.getChildren()) {
                    snapshott.getRef().removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    // Xóa dữ liệu thành công
                                    Toast.makeText(ProfileActivity.this, "Cancel friend success", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xóa dữ liệu thất bại
                                    Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra
                Log.e("Firebase", "Error: " + databaseError.getMessage());
            }
        });

    }

    public void sendFriendRequest(String friendId, String currentUserId){
        database.getReference()
                .child("users")
                .child(currentUserId)
                .child("friendList")
                .push()
                .setValue(friendId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ProfileActivity.this, "Friend request success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });

        database.getReference()
                .child("users")
                .child(friendId)
                .child("friendList")
                .push()
                .setValue(currentUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ProfileActivity.this, "Friend request success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}