package com.example.celebrare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;

import com.karumi.dexter.listener.single.PermissionListener;

public class EditImage extends AppCompatActivity implements AddFrameListner {
    PhotoEditorView photoEditorView;
    PhotoEditor photoEditor;
    LinearLayout linearLayout;


    Uri imageUri;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    Button uploadimagebtn;
    ImageView firebaseimage;
    FirebaseFirestore mStore;
    String UserID;
    Intent i =getIntent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_image);
        //imageUri=i.getData();

        uploadimagebtn = findViewById(R.id.uploadImagebtn);

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        linearLayout=findViewById(R.id.linearLayoutEdit);

        photoEditorView=findViewById(R.id.photoeditorView);
        photoEditorView.getSource().setImageURI(getIntent().getData());

        photoEditor= new PhotoEditor.Builder(this, photoEditorView).setPinchTextScalable(true).build();

        Button addFrame = findViewById(R.id.addframe);
        Button saveImage = findViewById(R.id.saveimage);

        addFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameFragment ff =FrameFragment.getInstance();
                ff.setListner(EditImage.this);
                ff.show(getSupportFragmentManager(),ff.getTag());
            }
        });

        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
            }
        });

        uploadimagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

    }


    private void uploadImage() {



        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading File....");
        progressDialog.show();


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String fileName = formatter.format(now);
        storageReference = FirebaseStorage.getInstance().getReference("images/"+fileName);


        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        firebaseimage.setImageURI(null);
                        Toast.makeText(EditImage.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();

                        UserID = mAuth.getCurrentUser().getUid();
                        DocumentReference df = mStore.collection("users").document(UserID);

                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                df.update("imgurl", uri);
                            }
                        });

                        if (progressDialog.isShowing())
                            progressDialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        Toast.makeText(EditImage.this,"Failed to Upload",Toast.LENGTH_SHORT).show();


                    }
                });

    }


    public void saveImage(){
        Dexter.withContext (this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            photoEditor.saveAsBitmap(new OnSaveBitmap() {
                                @Override
                                public void onBitmapReady(Bitmap saveBitmap) {
                                    photoEditorView.getSource().setImageBitmap(saveBitmap);
                                    try {
                                        final String path = BitmapClass.insertImage(getContentResolver(),
                                                saveBitmap,
                                                System.currentTimeMillis() + "_profile.jpeg", "a photo");

                                        if (!TextUtils.isEmpty(path)) {
                                            Snackbar snackbar = Snackbar.make(linearLayout, "img saved", Snackbar.LENGTH_LONG).
                                                    setAction("OPEN", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            openImageLocation(path);
                                                        }
                                                    });
                                            snackbar.show();
                                        } else {
                                            Snackbar snackbar = Snackbar.make(linearLayout, "unable", Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                        }


                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken){
                        permissionToken.continuePermissionRequest();
                }
    }).check();
}

    private void openImageLocation(String path){
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse(path), "image/");
        startActivity(i);
    }

    @Override
    public void onAddFrame(int frame) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), frame );
          photoEditor.addImage(bitmap) ;
    }



}