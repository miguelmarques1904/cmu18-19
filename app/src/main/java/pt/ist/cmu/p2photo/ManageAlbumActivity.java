package pt.ist.cmu.p2photo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ManageAlbumActivity extends AppCompatActivity {

    static int GET_FROM_GALLERY = 1; // ID of the activity started, used to process the result on the onActivityResult() function
    static int nPhotos = 0;

    String albumName;

    TextView title;

    LinearLayout col1;
    LinearLayout col2;
    LinearLayout col3;

    // TODO make this persistent
    List<Bitmap> photoList = new ArrayList<>();
    LinearLayout.LayoutParams imageParams;

    Button addPhotoBtn;
    Button addUserBtn;
    Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managealbum);

        albumName = getIntent().getStringExtra("albumName");
        title = (TextView) findViewById(R.id.managealbum_title);
        title.setText("Managing \"" + albumName + "\" album.");

        addPhotoBtn = (Button) findViewById(R.id.managealbum_addphoto);
        addUserBtn = (Button) findViewById(R.id.managealbum_adduser);
        backBtn = (Button) findViewById(R.id.managealbum_back);

        col1 = (LinearLayout) findViewById(R.id.managealbum_ll1);
        col2 = (LinearLayout) findViewById(R.id.managealbum_ll2);
        col3 = (LinearLayout) findViewById(R.id.managealbum_ll3);



        imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageParams.setMargins(0,0,0,20);

    }

    @Override
    protected void onResume() {
        super.onResume();

        for(;nPhotos < photoList.size(); nPhotos++) {
            ImageView iv = new ImageView(this);
            iv.setImageBitmap(photoList.get(nPhotos)); // Change this to photoList elements
            iv.setAdjustViewBounds(true);
            iv.setLayoutParams(imageParams);

            switch(nPhotos%3) {
                case 0:
                    col1.addView(iv);
                    break;
                case 1:
                    col2.addView(iv);
                    break;
                case 2:
                    col3.addView(iv);
                    break;
            }
        }


    }


    public void addPhotoClick(View v) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //Detects request codes
        if(requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                photoList.add(bitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void addUserClick(View v) {
        Intent addUserIntent = new Intent(ManageAlbumActivity.this, AddUserActivity.class);
        addUserIntent.putExtra("albumName", albumName);
        startActivity(addUserIntent);
    }

    public void backOnClick(View v) {

        finish();
    }
}
