package com.example.question.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.question.Model.Comment;
import com.example.question.Model.User;
import com.example.question.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{


    private Context mContext;
    private List<Comment> mCommentList;
    private String postid;
    private FirebaseUser firebaseUser;

    public CommentAdapter(Context mContext, List<Comment> mCommentList, String postid) {
        this.mContext = mContext;
        this.mCommentList = mCommentList;
        this.postid = postid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.comments_layout,parent,false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        Comment comment=mCommentList.get(position);

        holder.commentor_comment.setText(comment.getComment());
        holder.commentDate.setText("commented in : "+comment.getDate());
        //for getting the person information we are calling this method
        getUserInformation(holder.commentor_profile_image,holder.commentorUserName,comment.getPublisher());


        isUpvoted(comment.getPostid(),comment.getCommentid(),holder.upvote);
        isDownvoted(comment.getPostid(),comment.getCommentid(),holder.downvote);
        getUpvotes(holder.upvotes,comment.getCommentid(),comment.getPostid());
        getDownvotes(holder.downvotes,comment.getCommentid(),comment.getPostid());


        holder.upvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.upvote.getTag().equals("upvote") && holder.downvote.getTag().equals("downvote")){
                    FirebaseDatabase.getInstance().getReference().child("upvotes1").child(comment.getPostid()).child(comment.getCommentid()).child(firebaseUser.getUid()).setValue(true);
                    //addNotifications(post.getPublisher(), post.getPostid());
                }
                else if (holder.upvote.getTag().equals("upvote") && holder.downvote.getTag().equals("downvoted")){
                    FirebaseDatabase.getInstance().getReference().child("downvotes1").child(comment.getPostid()).child(comment.getCommentid()).child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("upvotes1").child(comment.getPostid()).child(comment.getCommentid()).child(firebaseUser.getUid()).setValue(true);
                    //addNotifications(post.getPublisher(), post.getPostid());

                }else {
                    FirebaseDatabase.getInstance().getReference().child("upvotes1").child(comment.getPostid()).child(comment.getCommentid()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.downvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.downvote.getTag().equals("downvote") && holder.upvote.getTag().equals("upvote")){
                    FirebaseDatabase.getInstance().getReference().child("downvotes1").child(comment.getPostid()).child(comment.getCommentid()).child(firebaseUser.getUid()).setValue(true);
                }else if (holder.downvote.getTag().equals("downvote") && holder.upvote.getTag().equals("upvoted")){
                    FirebaseDatabase.getInstance().getReference().child("upvotes1").child(comment.getPostid()).child(comment.getCommentid()).child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("downvotes1").child(comment.getPostid()).child(comment.getCommentid()).child(firebaseUser.getUid()).setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("downvotes1").child(comment.getPostid()).child(comment.getCommentid()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });



    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView commentor_profile_image;
        public ImageView  upvote, downvote;
        public TextView commentorUserName,commentor_comment,commentDate, upvotes, downvotes;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentor_profile_image=itemView.findViewById(R.id.commentor_profile_image);
            commentorUserName=itemView.findViewById(R.id.commentorUserName);
            commentor_comment=itemView.findViewById(R.id.commentor_comment);
            commentDate=itemView.findViewById(R.id.CommentDate);
            upvotes =itemView.findViewById(R.id.upvotes1);
            downvotes =itemView.findViewById(R.id.downvotes1);
            upvote =itemView.findViewById(R.id.upvote1);
            downvote =itemView.findViewById(R.id.downvote1);
        }
    }
    //for getting the person name,id and image i created this method
  private void  getUserInformation(final CircleImageView circleImageView,final TextView username, String publisherid)
    {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("users").child(publisherid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               User user=snapshot.getValue(User.class);
                Glide.with(mContext).load(user.getProfileimageurl()).into(circleImageView);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private  void isUpvoted(String postid, String commentid, ImageView imageView)
    {
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("upvotes1").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(commentid).hasChild(firebaseUser.getUid()))
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
    private  void isDownvoted(String postid, String commentid, ImageView imageView)
    {
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("downvotes1").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(commentid).hasChild(firebaseUser.getUid()))

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
    private void getUpvotes(TextView upvotes, String commentid, String postid)
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("upvotes1").child(postid).child(commentid);
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
    private void getDownvotes(TextView downvotes, String commentid, String postid)
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("downvotes1").child(postid).child(commentid);
        reference.addValueEventListener(new ValueEventListener() {
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
