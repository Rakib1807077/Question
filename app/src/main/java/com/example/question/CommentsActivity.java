package com.example.question;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import  androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.question.Adapters.CommentAdapter;
import com.example.question.Model.Comment;
import com.example.question.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private CircleImageView circleImageView;
    private TextView textView;
    private EditText editText;

    //This progress Dialog is for adding comments
    private ProgressDialog loader;

    String postid;

    //Creating adapter for retreving data
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        toolbar=findViewById(R.id.home_toolbarComment);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");

        Intent intent=getIntent();
        postid=intent.getStringExtra("postid");


        recyclerView=findViewById(R.id.recyclerViewComment);
        circleImageView=findViewById(R.id.comment_profile_imeage);
        textView=findViewById(R.id.commenting_post_textview);
        editText=findViewById(R.id.adding_comment);

        loader=new ProgressDialog(this);


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText=editText.getText().toString();
                if(TextUtils.isEmpty(commentText))
                {
                    editText.setError("You didn't write anything");
                }
                else
                {
                    addComment();
                }
            }
        });
        recyclerView=findViewById(R.id.recyclerViewComment);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        commentList=new ArrayList<>();
        commentAdapter=new CommentAdapter(CommentsActivity.this,commentList,postid);
        recyclerView.setAdapter(commentAdapter);

        //calling the getImage method in Oncreate class
        getImage();
        readComments();
    }



    private void addComment() {
        loader.setMessage("Adding a comment");
        loader.setCanceledOnTouchOutside(false);
        loader.show();

        //storing comment in firebase Database
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("comments").child(postid); //ei post id ta postAdapter.java theke pass kora hoyese
        String commentid=reference.push().getKey();
        String date= DateFormat.getDateInstance().format(new Date());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("comment",editText.getText().toString());
        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap.put("commentid",commentid);
        hashMap.put("postid",postid);
        hashMap.put("date",date);

        reference.child(commentid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(CommentsActivity.this,"Comment added successfully",Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                }
                else
                {
                    Toast.makeText(CommentsActivity.this,"Error in adding comment "+task.getException(),Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                }
                editText.setText("");//User jeno anoter comment dite pare tay eti
            }
        });
    }
    //je comment korese tar image o jeno show kore tay ei process
    private void getImage()
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                Glide.with(CommentsActivity.this).load(user.getProfileimageurl()).into(circleImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentsActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readComments() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("comments").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren())
                {
                    Comment comment=dataSnapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentsActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}