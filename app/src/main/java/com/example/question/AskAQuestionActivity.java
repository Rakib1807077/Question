package com.example.question;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class AskAQuestionActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Spinner spinner;
    private EditText questionBox;
    private ImageView imageView;
    private Button cancelBtn,postQuestionBtn;
    private String askedByName="";  //for the person who will ask the question
    private DatabaseReference askedByRef;
    private ProgressDialog loader;
    private String myUrl=""; //for storing the image url
    StorageTask uploadTask;
    StorageReference storageReference;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ask_a_question);

        toolbar=findViewById(R.id.question_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ask a question");

        spinner=findViewById(R.id.spinner);
        questionBox=findViewById(R.id.question_text);
        imageView=findViewById(R.id.questionImage);
        cancelBtn=findViewById(R.id.cancel);
        postQuestionBtn=findViewById(R.id.PostQuestion);

        loader= new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        onlineUserId=mUser.getUid();

        askedByRef= FirebaseDatabase.getInstance().getReference("users").child(onlineUserId);
        //Ekhane full name retrieve kore profile e boshaysi
        askedByRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                askedByName=snapshot.child("filename").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storageReference= FirebaseStorage.getInstance().getReference("questions");
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.topics));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinner.getSelectedItem().equals("select a topic"))
                {
                    Toast.makeText(AskAQuestionActivity.this,"Please select a valid topic",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //jodi image a click kore tahole ami gallery te chole jabo and image select korbo...tar jonno ei kaj
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });
        //Now we are working with the cancel Button
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //r mane jkn user ete click korbe tkn previous activity te chole ashbe . that means home activity
            }
        });
        postQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performValidations();
            }
        });

    }

    String getQuestionText()
    {
        return questionBox.getText().toString().trim();
    }
    String getTopic()
    {
        return spinner.getSelectedItem().toString();
    }
    String mDate= DateFormat.getInstance().format(new Date());
    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("questions posts");

    //amra ei method e edittext e je question paysi ota o selected topic ta nibo
    private void performValidations() {
        if(getQuestionText().isEmpty())
        {
            questionBox.setError("Question needed");
        }
        if(getTopic().equals("select a topic"))
        {
            Toast.makeText(this,"Select a valid topic",Toast.LENGTH_SHORT).show();
        }
        if(!getQuestionText().isEmpty() && !getTopic().equals("") && imageUri == null)  //mane image bade shob select korsi
        {
            uploadAQuestionwithNoImage();
        }
        else if(!getQuestionText().isEmpty() && !getTopic().equals("") && imageUri != null)//image shoho select
        {
            uploadAQuestionwithImage();
        }
    }




    //eta muloto progress ta dekhay actually loading r je beparta
    private void startLoader()
    {
        loader.setMessage("Posting your question");
        loader.setCanceledOnTouchOutside(false); //Etar ki kaj
        loader.show();
    }

    //This function is for taking the extension of the image
    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void uploadAQuestionwithNoImage() {
        startLoader();
        String postid=ref.push().getKey();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("postid",postid);
        hashMap.put("question",getQuestionText());
        hashMap.put("publisher",onlineUserId);
        hashMap.put("topic",getTopic());
        hashMap.put("asked",askedByName);
        hashMap.put("date",mDate);
        ref.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(AskAQuestionActivity.this,"Your question is posted successfully",Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                    startActivity(new Intent(AskAQuestionActivity.this,HomeActivity.class));
                    finish();
                }
                else
                {
                    Toast.makeText(AskAQuestionActivity.this,"Error "+task.getException().toString(),Toast.LENGTH_SHORT).show();
                   // loader.dismiss();
                }
                loader.dismiss();
            }

        });

    }


    private void uploadAQuestionwithImage() {
        startLoader();
        final StorageReference fileReference;
        fileReference=storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
        uploadTask=fileReference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if(!task.isComplete())
                {
                    throw task.getException();
                }

                return    fileReference.getDownloadUrl();


            }
        }).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    Uri downloadUri=(Uri) task.getResult();
                    myUrl=downloadUri.toString();
                    String postid=ref.push().getKey();
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("postid",postid);
                    hashMap.put("question",getQuestionText());
                    hashMap.put("publisher",onlineUserId);
                    hashMap.put("topic",getTopic());
                    hashMap.put("asked",askedByName);
                    hashMap.put("questionimage",myUrl);
                    hashMap.put("date",mDate);
                    ref.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(AskAQuestionActivity.this,"Your question is posted successfully",Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                                startActivity(new Intent(AskAQuestionActivity.this,HomeActivity.class));
                                finish();
                            }
                            else
                            {
                                Toast.makeText(AskAQuestionActivity.this,"Error "+task.getException().toString(),Toast.LENGTH_SHORT).show();
                                // loader.dismiss();
                            }
                            loader.dismiss();

                        }
                    });
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AskAQuestionActivity.this,"Failed to upload the question",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //next e oi imageView.setOnClickListener() override kora hoyese mane image ta je select korlam ta thikmoton select hoyse kina, uri payse kina

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data !=null)
        {
            imageUri=data.getData();
            imageView.setImageURI(imageUri);
        }
    }
}