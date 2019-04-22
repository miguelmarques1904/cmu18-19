package pt.ist.cmu.p2photo;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class ManageAlbumActivity extends AppCompatActivity {

    String albumName;

    TextView title;

    LinearLayout col1;
    LinearLayout col2;
    LinearLayout col3;

    List<String> photoList = new ArrayList<>();

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


        // TODO populate image list. maybe contains urls
        for(int i=0; i<20; i++) {
            photoList.add("placeholder");
        }

        int nPhotos = photoList.size();

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageParams.setMargins(0,0,0,20);

        for(int i=0; i < nPhotos; i++) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.profile); // Change this to photoList elements
            iv.setAdjustViewBounds(true);
            iv.setLayoutParams(imageParams);

            switch(i%3) {
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
        Intent addPhotoIntent = new Intent(ManageAlbumActivity.this, AddPhotoActivity.class);
        addPhotoIntent.putExtra("albumName", albumName);
        startActivity(addPhotoIntent);
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
