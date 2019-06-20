package co.particket.particket;


import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UserDetails {
    private String userUid;
    private String email;
    private String firstName;
    private String lastName;
    private double revenueA;
    private double revenueB;
    private String userDetailsID;

    public UserDetails(){ }
    public UserDetails(String userUid, String email, String firstName , String lastName, String id){
        this.userUid = userUid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.revenueA = 0;
        this.revenueB = 0;
        this.userDetailsID = id;
    }

    public String getUserUid() { return userUid; }
    public void setUserUid(String userUid) { this.userUid = userUid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public double getRevenueA() { return revenueA; }
    public void setRevenueA(double revenue) { this.revenueA = revenue; }

    public double getRevenueB() { return revenueB; }
    public void setRevenueB(double revenue) { this.revenueB = revenue; }

    public String getUserDetailsID() { return userDetailsID; }
    public void setUserDetailsID(String userDetailsID) { this.userDetailsID = userDetailsID; }


    public void setProfileImg(Uri uri){
        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRef.child(userUid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                mStorageRef.child(userUid).delete();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // File not found
            }
        });
        StorageReference fileRef = mStorageRef.child(userUid);
        fileRef.putFile(uri);
    }
    @Override
    public String toString() {
        return firstName +" "+ lastName;
    }
}
