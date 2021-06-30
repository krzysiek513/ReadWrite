package pl.studia.writeandreadfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase mDatabase;
    DatabaseReference dataRef;
    FirebaseStorage mStorage;
    ImageView imageView;
    Button addBtn;
    EditText nameEt, descriptionEt;
    private static final int GALLERY_CODE = 1;
    Uri imageUrl = null;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.recIv);
        addBtn = findViewById(R.id.addBtn);
        nameEt = findViewById(R.id.recEt1);
        descriptionEt = findViewById(R.id.recEt2);

        mDatabase = FirebaseDatabase.getInstance();
        dataRef = mDatabase.getReference().child("student");
        mStorage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(this);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();

                if(!(name.isEmpty() && description.isEmpty() && imageUrl != null)) {
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();

                    StorageReference filePath = mStorage.getReference().child("imagePost").child(imageUrl.getLastPathSegment());
                    filePath.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String t = task.getResult().toString();

                                    DatabaseReference newPost = dataRef.push();

                                    newPost.child("name").setValue(name);
                                    newPost.child("description").setValue(description);
                                    newPost.child("image").setValue(task.getResult().toString());
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            imageUrl = data.getData();
            imageView.setImageURI(imageUrl);
        }
    }
}