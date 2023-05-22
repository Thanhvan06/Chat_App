package com.mianasad.chatsss.Activities;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;

import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mianasad.chatsss.Adapters.MessagesAdapter;
import com.mianasad.chatsss.Models.Message;
import com.mianasad.chatsss.Models.User;
import com.mianasad.chatsss.R;
import com.mianasad.chatsss.databinding.ActivityChatBinding;


import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private String audioFilePath;
    private MediaRecorder recorder;
    private boolean isRecording = false;

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    public static final String TAG = "ChatActivity";

    ActivityChatBinding binding;

    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;


    ProgressDialog dialog;
    String senderUid;
    String receiverUid;
    String token,name,profile, about;

    Menu menu;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        // Lấy đường dẫn file audio
        audioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";

//        getSupportActionBar().setTitle(name);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setSupportActionBar(binding.toolbar);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending image...");
        dialog.setCancelable(false);

        messages = new ArrayList<>();

         name = getIntent().getStringExtra("name");
         profile = getIntent().getStringExtra("image");
        token = getIntent().getStringExtra("token");
         about = getIntent().getStringExtra("about");

        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        database.getReference("users")
                .child(senderUid)
                .child("friendList")
                .orderByValue()
                .equalTo(receiverUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            binding.notice.setVisibility(View.GONE);
                        }else {
                            binding.notice.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.name.setText(name);
        Glide.with(ChatActivity.this).load(profile)
                .placeholder(R.drawable.avatar)
                .into(binding.profile);

        binding.imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if(!status.isEmpty()) {
                        if(status.equals("Offline")) {
                            binding.statusMain.setVisibility(View.GONE);

                        } else {
                            binding.statusMain.setVisibility(View.VISIBLE);
                            binding.status.setText(status);

                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;
//
        adapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
//
        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();

                    Date date = new Date();
                    Message message = new Message(messageTxt, senderUid, date.getTime());
                    binding.messageBox.setText("");

                    String randomKey = database.getReference().push().getKey();

                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                    lastMsgObj.put("lastMsg", message.getMessage());
                    lastMsgObj.put("lastMsgTime", date.getTime());

                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                    database.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    database.getReference().child("chats")
                                            .child(receiverRoom)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    sendNotification(name, message.getMessage(), token);
                                                }
                                            });
                                }
                            });




            }
        });

//
        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            }
        });

        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentCamera = new Intent(ACTION_IMAGE_CAPTURE);
                intentCamera.setType("image/*");
                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                    return;
                }
                startActivityForResult(intentCamera, 20);
            }
        });

        final Handler handler = new Handler();
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0){
                    binding.sendBtn.setVisibility(View.VISIBLE);
                    binding.voiceBtn.setVisibility(View.INVISIBLE);
                }else {
                    binding.sendBtn.setVisibility(View.INVISIBLE);
                    binding.voiceBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("Typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });

        binding.voiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecordDialog();
            }
        });
        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        getSupportActionBar().setTitle(name);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void showRecordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        // Gán layout cho hộp thoại
        View view = getLayoutInflater().inflate(R.layout.dialog_record, null);
        builder.setView(view);

        // Ánh xạ các view trong layout
        Button btnRecord = view.findViewById(R.id.btnRecord);
        Button btnStop = view.findViewById(R.id.btnStop);

        // Khởi tạo đường dẫn file audio
        audioFilePath = getExternalCacheDir().getAbsolutePath() + "/recording.3gp";

        // Bắt đầu ghi âm khi nhấn nút "Ghi âm"
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startRecording();
                } catch (IOException e) {
                    Toast.makeText(ChatActivity.this, " "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Dừng ghi âm khi nhấn nút "Dừng"
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                // Lưu và tải file âm thanh lên Firebase Storage ở đây
                // ...
                Uri audioUri = Uri.parse(audioFilePath.toString());

                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference audioRef = storageReference.child("audio").child(audioUri.getLastPathSegment());
                UploadTask uploadTask = audioRef.putFile(audioUri);

                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            audioRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String uriDown = uri.toString();
                                    Date date = new Date();
                                    Message message = new Message("", senderUid, date.getTime());
                                    message.setMessage("audio");
                                    message.setAudioFile(uriDown);
                                    binding.messageBox.setText("");

                                    String randomKey = database.getReference().push().getKey();

                                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                                    lastMsgObj.put("lastMsg", message.getMessage());
                                    lastMsgObj.put("lastMsgTime", date.getTime());

                                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                                    database.getReference().child("chats")
                                            .child(senderRoom)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    database.getReference().child("chats")
                                                            .child(receiverRoom)
                                                            .child("messages")
                                                            .child(randomKey)
                                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                }
                                                            });
                                                }
                                            });
                                }

                            });
                        }else {
                            Exception exception = task.getException();
                            Toast.makeText(ChatActivity.this, " " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // Hiển thị hộp thoại
        builder.show();
    }

    private void startRecording() throws IOException {
        // Khởi tạo recorder và cấu hình
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(audioFilePath);

        // Bắt đầu ghi âm
        recorder.prepare();
        recorder.start();

        isRecording = true;
    }

    private void stopRecording() {
        if (recorder != null) {
            // Dừng ghi âm và giải phóng tài nguyên
            recorder.stop();
            recorder.release();
            recorder = null;
            isRecording = false;
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }



    void sendNotification(String name, String message, String token) {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);
            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            notificationData.put("to",token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
                    , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                   // Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ChatActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    String key = "Key=AAAAWVT2ocs:APA91bHy47R3sT5D3Nc1lRxh37Pn9NeHdLbIdO0IdqYV7OtGqytFZnRrKfMkzye8IP55nES3WMxDd2sPPgaAmLEDfgF58jrkDHw9WIz4-nd968yIopxDYVm6x5rYq4bSzMy2hanNRjft";
                    map.put("Content-Type", "application/json");
                    map.put("Authorization", key);

                    return map;
                }
            };

            queue.add(request);


        } catch (Exception ex) {

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 25) {
            if(data != null) {
                if(data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();

                                        String messageTxt = binding.messageBox.getText().toString();

                                        Date date = new Date();
                                        Message message = new Message(messageTxt, senderUid, date.getTime());
                                        message.setMessage("photo");
                                        message.setImageUrl(filePath);
                                        binding.messageBox.setText("");

                                        String randomKey = database.getReference().push().getKey();

                                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                                        lastMsgObj.put("lastMsg", message.getMessage());
                                        lastMsgObj.put("lastMsgTime", date.getTime());

                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                database.getReference().child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(randomKey)
                                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });
                                            }
                                        });


                                    }
                                });
                            }
                        }
                    });
                }
            }
        }else if (requestCode == 20 && resultCode == Activity.RESULT_OK){
            if(data != null) {
                if(data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();

                                        String messageTxt = binding.messageBox.getText().toString();

                                        Date date = new Date();
                                        Message message = new Message(messageTxt, senderUid, date.getTime());
                                        message.setMessage("photo");
                                        message.setImageUrl(filePath);
                                        binding.messageBox.setText("");

                                        String randomKey = database.getReference().push().getKey();

                                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                                        lastMsgObj.put("lastMsg", message.getMessage());
                                        lastMsgObj.put("lastMsgTime", date.getTime());

                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        database.getReference().child("chats")
                                                                .child(receiverRoom)
                                                                .child("messages")
                                                                .child(randomKey)
                                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                    }
                                                                });
                                                    }
                                                });


                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnCallVideo:

                break;
            case R.id.profile:
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("image", profile);
                intent.putExtra("friendId", receiverUid);
                intent.putExtra("about", about);
                startActivity(intent);

//                sendFriendRequest(receiverUid, senderUid);

                break;

        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.chat_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

}