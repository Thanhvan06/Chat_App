package com.mianasad.chatsss.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mianasad.chatsss.Activities.ChatActivity;
import com.mianasad.chatsss.R;
import com.mianasad.chatsss.Models.User;
import com.mianasad.chatsss.databinding.RowConversationBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> implements Filterable {

    Context context;
    ArrayList<User> users;
    ArrayList<User> oldUsers;

    public ContactsAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
        this.oldUsers = users;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);

        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        User user = users.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();

        String senderRoom = senderId + user.getUid();

//        FirebaseDatabase.getInstance().getReference()
//                .child("chats")
//                .child(senderRoom)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if(snapshot.exists()) {
//                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
//                            long time = snapshot.child("lastMsgTime").getValue(Long.class);
//                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
//                            holder.binding.msgTime.setText(dateFormat.format(new Date(time)));
//                            holder.binding.lastMsg.setText(lastMsg);
//                        } else {
//                            holder.binding.lastMsg.setText("Tap to chat");
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });


        holder.binding.username.setText(user.getName());

        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name", user.getName());
                intent.putExtra("image", user.getProfileImage());
                intent.putExtra("uid", user.getUid());
                intent.putExtra("token", user.getToken());
                intent.putExtra("about", user.getAbout());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String atrSearch = constraint.toString();

                if (atrSearch.isEmpty()){
                    users = oldUsers;

                }else {
                    ArrayList<User> list = new ArrayList<>();
                    for (User user : oldUsers){
                        FirebaseDatabase.getInstance().getReference().child("users");
                        if (user.getName().toLowerCase().contains(atrSearch.toLowerCase())){
                            list.add(user);
                        }
                    }

                    users = list;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = users;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                users = (ArrayList<User>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }

}
