package com.example.question.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.question.CommentsActivity;
import com.example.question.Model.Post;
import com.example.question.Model.User;
import com.example.question.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{
    public Context mContext;
    public List<Post> mPostList;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;

    public PostAdapter() {
    }

    public PostAdapter(Context mContext, List<Post> mPostList) {
        this.mContext = mContext;
        this.mPostList = mPostList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.questions_retrieved_layout,parent,false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    //ekhane amra data populate korbo
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

          Post post=mPostList.get(position);
        Log.d("postid",post.getPostid() +" main");

        //image upload na korle
         if(post.getQuestionimage()==null)
         {
             holder.questionImage.setVisibility(View.GONE);
         }
         //image upload  korle
        else
         {
            // holder.questionImage.setVisibility(View.VISIBLE);

         }
        holder.questionImage.setVisibility(View.VISIBLE);
        Glide.with(mContext).load(post.getQuestionimage()).into(holder.questionImage);
         //ekn amra question text ta nibo
        holder.expand_text_view.setText(post.getQuestion());
        holder.topicTextView.setText(post.getTopic());
        holder.askedOnTextView.setText(post.getDate());

        publisherInformation(holder.publisher_profile_image,holder.asked_by_Textview,post.getPublisher());
        if(post.getPostid()  != null) {
            isUpvoted(post.getPostid() , holder.upvote);
            isDownvoted(post.getPostid() , holder.downvote);
            getUpvotes(holder.upvotes, post.getPostid() );
            getDownvotes(holder.downvotes, post.getPostid() );
        }

        holder.upvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.upvote.getTag().equals("upvote") && holder.downvote.getTag().equals("downvote")){
                    FirebaseDatabase.getInstance().getReference().child("upvotes").child(post.getPostid() ).child(firebaseUser.getUid()).setValue(true);

                }
                else if (holder.upvote.getTag().equals("upvote") && holder.downvote.getTag().equals("downvoted")){
                    FirebaseDatabase.getInstance().getReference().child("downvotes").child(post.getPostid() ).child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("upvotes").child(post.getPostid() ).child(firebaseUser.getUid()).setValue(true);


                }else {
                    FirebaseDatabase.getInstance().getReference().child("upvotes").child(post.getPostid() ).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.downvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.downvote.getTag().equals("downvote") && holder.upvote.getTag().equals("upvote")){
                    FirebaseDatabase.getInstance().getReference().child("downvotes").child(post.getPostid() ).child(firebaseUser.getUid()).setValue(true);
                }else if (holder.downvote.getTag().equals("downvote") && holder.upvote.getTag().equals("upvoted")){
                    FirebaseDatabase.getInstance().getReference().child("upvotes").child(post.getPostid() ).child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("downvotes").child(post.getPostid() ).child(firebaseUser.getUid()).setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("downvotes").child(post.getPostid() ).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        //comment is for imageView
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid",post.getPostid() );
                intent.putExtra("pusblisher",post.getPublisher());
                mContext.startActivity(intent);

            }
        });
        //comments is for textView
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,CommentsActivity.class);
                intent.putExtra("postid",post.getPostid() );
                intent.putExtra("pusblisher",post.getPublisher());
                mContext.startActivity(intent);

            }
        });
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(mContext,v);
                popupMenu.inflate(R.menu.post_menu);
                if(!post.getPublisher().equals(firebaseUser.getUid()))
                {
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);  //sudhumatro je post korese,sei delete korte parbe

                }

                else {

                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getItemId()==R.id.delete)
                            {
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                databaseReference.child("questions posts").child(post.getPostid() ).removeValue();
                                databaseReference.child("comments").child(post.getPostid() ).removeValue();
                                databaseReference.child("upvotes").child(post.getPostid() ).removeValue();
                                databaseReference.child("downvotes").child(post.getPostid() ).removeValue();





                                ;


                            }
                            return true;
                        }
                    });

                }










            }
        });


    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView publisher_profile_image;
        public TextView asked_by_Textview, upvotes, downvotes,comments;
        public ImageView more,questionImage, upvote, downvote,comment;
        public TextView topicTextView,askedOnTextView;
        public ExpandableTextView expand_text_view;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            publisher_profile_image=itemView.findViewById(R.id.publisher_profile_image);
            asked_by_Textview=itemView.findViewById(R.id.asked_by_Textview);
            upvotes =itemView.findViewById(R.id.upvotes);
            downvotes =itemView.findViewById(R.id.downvotes);
            comments=itemView.findViewById(R.id.comments);
            more=itemView.findViewById(R.id.more);
            questionImage=itemView.findViewById(R.id.questionImage1);
            upvote =itemView.findViewById(R.id.upvote);
            downvote =itemView.findViewById(R.id.downvote);
            comment=itemView.findViewById(R.id.comment);
            topicTextView=itemView.findViewById(R.id.topicTextView);
            askedOnTextView=itemView.findViewById(R.id.askedOnTextView);
            expand_text_view=itemView.findViewById(R.id.expand_text_view);

            upvote.setTag("upvote");
            downvote.setTag("downvote");

        }
    }
    //post e amar nijer nam r sobi dekhanor jonno
    private void publisherInformation(CircleImageView publisherImage,TextView askedBy, String userid)
    {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Ekhane user r jonno alada ekta model class create korbo
                User user=snapshot.getValue(User.class);
                Glide.with(mContext).load(user.getProfileimageurl()).into(publisherImage);
                askedBy.setText(user.getFullname());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private  void isUpvoted(String postid, ImageView imageView)
    {
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        Log.d("postid",postid+" up");
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("upvotes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists())
                {
                    imageView.setImageResource(R.drawable.ic_upvoted);
                    imageView.setTag("upvoted");
                }
                else
                {
                    imageView.setImageResource(R.drawable.ic_upvote);
                    imageView.setTag("upvote");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private  void isDownvoted(String postid, ImageView imageView)
    {
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("downvotes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists())
                {
                    imageView.setImageResource(R.drawable.ic_downvoted);
                    imageView.setTag("downvoted");
                }
                else
                {
                    imageView.setImageResource(R.drawable.ic_downvote);
                    imageView.setTag("downvote");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //THIS  is for counting the number of upvotes and dislikes, method gula create korar por upore call kora hoyese
    private void getUpvotes(TextView upvotes, String postid)
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("upvotes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long numberOfUpvotes=snapshot.getChildrenCount();
                int NOUv=(int)numberOfUpvotes;
                if(NOUv > 1)
                {
                    upvotes.setText(snapshot.getChildrenCount()+" upvotes");
                }else if(NOUv ==0)
                {
                    upvotes.setText("0 upvotes");
                }
                else

                {
                    upvotes.setText(snapshot.getChildrenCount()+" upvote");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getDownvotes(TextView downvotes, String postid)
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("downvotes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long numberOfDownvotes=snapshot.getChildrenCount();
                int NODv=(int)numberOfDownvotes;
                if(NODv > 1)
                {
                    downvotes.setText(snapshot.getChildrenCount()+" downvotes");
                }else if(NODv ==0)
                {
                    downvotes.setText("0 downvotes");
                }
                else

                {
                    downvotes.setText(snapshot.getChildrenCount()+" downvote");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
