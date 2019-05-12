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
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.ist.cmu.api.ApiService;
import pt.ist.cmu.api.RetrofitInstance;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.helpers.ImageHelper;
import pt.ist.cmu.models.Album;
import pt.ist.cmu.models.Membership;
import pt.ist.cmu.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageAlbumActivity extends AppCompatActivity {

    private String token;

    static int GET_FROM_GALLERY = 1; // ID of the activity started, used to process the result on the onActivityResult() function
    int nPhotos = 0;

    Album album;

    TextView title;

    static double col1Height;
    static double col2Height;
    static double col3Height;

    LinearLayout col1;
    LinearLayout col2;
    LinearLayout col3;

    // TODO right now as it is static it is shared with all the albums
    static List<Uri> photoList = new ArrayList<>();
    LinearLayout.LayoutParams imageParams;

    Button addPhotoBtn;
    Button addUserBtn;
    Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managealbum);

        if (getIntent().getStringExtra(Constants.CURRENT_ALBUM_KEY) != null) {
            album = new Album(getIntent().getStringExtra(Constants.CURRENT_ALBUM_KEY));
        } else if (Hawk.contains(Constants.CURRENT_ALBUM_KEY)) {
            album = Hawk.get(Constants.CURRENT_ALBUM_KEY);
        } else return;

        title = findViewById(R.id.managealbum_title);
        title.setText("Managing '" + album.getName() + "' album");

        addPhotoBtn = findViewById(R.id.managealbum_addphoto);
        addUserBtn = findViewById(R.id.managealbum_adduser);
        backBtn = findViewById(R.id.managealbum_back);

        col1 = findViewById(R.id.managealbum_ll1);
        col2 = findViewById(R.id.managealbum_ll2);
        col3 = findViewById(R.id.managealbum_ll3);

        col1Height = 0;
        col2Height = 0;
        col3Height = 0;

        imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageParams.setMargins(0,0,0,20);

        // get token
        User caller = Hawk.get(Constants.CURRENT_USER_KEY);
        token = "Token " + caller.getToken();

        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<List<Membership>> memberCall = service.getAlbum(token, album.getName());

        memberCall.enqueue(new Callback<List<Membership>>() {
            @Override
            public void onResponse(Call<List<Membership>> memberCall, Response<List<Membership>> response) {
                switch (response.code()) {
                    case 200:
                        album.setCatalogs(response.body());

                        // save album to preferences
                        Hawk.put(Constants.CURRENT_ALBUM_KEY, album);
                        break;
                    case 401:
                        Toast.makeText(getApplicationContext(), "You are not logged in.", Toast.LENGTH_LONG).show();
                        break;
                    case 403:
                        Toast.makeText(getApplicationContext(), "You are not a member of this album.", Toast.LENGTH_SHORT).show();
                        break;
                    case 404:
                        Toast.makeText(getApplicationContext(), "This album does not exist.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Something went wrong on the server side.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(Call<List<Membership>> memberCall, Throwable t) {
                Toast.makeText(getApplicationContext(), "Something went wrong... Network may be down...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();

        for (; nPhotos < photoList.size(); nPhotos++) {
            final Uri uri = photoList.get(nPhotos);

            Bitmap b = ImageHelper.getBitmapFromURI(uri, this.getContentResolver());

            if (b != null) {
                // This works because column widths are equal
                double imageRatio = (double) b.getHeight() / b.getWidth();

                ImageView iv = new ImageView(this);
                iv.setImageBitmap(b); // Change this to photoList elements
                iv.setAdjustViewBounds(true);
                iv.setLayoutParams(imageParams);

                iv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent viewPhotoIntent = new Intent(ManageAlbumActivity.this, ViewPhotoActivity.class);
                        viewPhotoIntent.putExtra("photoURI", uri.toString());
                        startActivity(viewPhotoIntent);
                    }
                });

                // Add photo to column with the smallest height
                double minColHeight = Math.min(Math.min(col1Height, col2Height), col3Height);
                if (minColHeight == col1Height) {
                    col1.addView(iv);
                    col1Height += imageRatio;
                } else if (minColHeight == col2Height) {
                    col2.addView(iv);
                    col2Height += imageRatio;
                } else {
                    col3.addView(iv);
                    col3Height += imageRatio;
                }

            }
        }
    }

    public void addPhotoClick(View v) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            photoList.add(selectedImage);
        }
    }

    public void addUserClick(View v) {
        Intent addUserIntent = new Intent(ManageAlbumActivity.this, AddUserActivity.class);
        startActivity(addUserIntent);
    }

    public void backOnClick(View v) {
        // delete current album
        Hawk.delete(Constants.CURRENT_ALBUM_KEY);

        finish();
    }
}
