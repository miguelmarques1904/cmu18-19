package pt.ist.cmu.p2photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ManageAlbumActivity extends AppCompatActivity {

    String albumName;

    TextView title;

    LinearLayout photoLl;

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

        photoLl = (LinearLayout) findViewById(R.id.managealbum_photoll);


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
