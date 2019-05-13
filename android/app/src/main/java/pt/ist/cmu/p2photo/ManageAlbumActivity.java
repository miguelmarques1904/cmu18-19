package pt.ist.cmu.p2photo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pt.ist.cmu.api.ApiService;
import pt.ist.cmu.api.RetrofitInstance;
import pt.ist.cmu.cloud.DropboxActivity;
import pt.ist.cmu.cloud.DropboxClientFactory;
import pt.ist.cmu.cloud.UploadFileTask;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.helpers.ImageHelper;
import pt.ist.cmu.helpers.StringGenerator;
import pt.ist.cmu.models.Album;
import pt.ist.cmu.models.Membership;
import pt.ist.cmu.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageAlbumActivity extends DropboxActivity {

    private User user;

    private Fetch fetch;
    private FetchListener fetchListener;

    private List<String> imageLinks = new ArrayList<>();

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

    List<Uri> photoList = new ArrayList<>();
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
        user = Hawk.get(Constants.CURRENT_USER_KEY);
    }

    @Override
    protected void loadData() {
        // configure fetch
        configureFetch();

        String token = "Token " + user.getToken();

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

                        // download catalogs
                        for (Membership m : album.getCatalogs()) {
                            String url = m.getCatalog();
                            String file = ManageAlbumActivity.this.getFilesDir().getPath() + "/" + StringGenerator.generateName(5);

                            final Request request = new Request(url, file);
                            request.setPriority(Priority.HIGH);
                            request.setNetworkType(NetworkType.ALL);

                            fetch.enqueue(request, updatedRequest -> {
                                // request was enqueued for download
                            }, error -> {
                                Toast.makeText(getApplicationContext(), "Could not download catalog.", Toast.LENGTH_LONG).show();
                            });
                        }
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

    private void configureLayout() {
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

                iv.setOnClickListener(v -> {
                    Intent viewPhotoIntent = new Intent(ManageAlbumActivity.this, ViewPhotoActivity.class);
                    viewPhotoIntent.putExtra("photoURI", uri.toString());
                    startActivity(viewPhotoIntent);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri imageURI = data.getData();
            photoList.add(imageURI);

            // reconfigure layout
            configureLayout();

            // get catalog for user
            String userCatalog = null;
            for (Membership m : album.getCatalogs()) {
                if (m.getUsername() == user.getUsername()) {
                    userCatalog = m.getCatalog();
                    break;
                }
            }

            // upload picture
            String url;
            new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
                @Override
                public void onUploadComplete(String result) {
                    // url = result;
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ManageAlbumActivity.this, "An error occurred when uploading image to Dropbox.", Toast.LENGTH_LONG).show();
                }
            }).execute(imageURI.getPath(), "");

            // TODO: download & edit catalog

            // TODO: re-upload catalog

        }
    }

    private void configureFetch() {
        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(ManageAlbumActivity.this)
                .setDownloadConcurrentLimit(3)
                .build();
        fetch = Fetch.Impl.getInstance(fetchConfiguration);

        fetchListener = new FetchListener() {
            @Override
            public void onCompleted(@NotNull Download download) {
                try {
                    File file = new File(download.getFile());
                    Scanner sc = new Scanner(file);

                    while (sc.hasNextLine()) {
                        imageLinks.add(sc.nextLine());
                    }

                    file.delete();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Cannot read catalog file", Toast.LENGTH_SHORT).show();
                }

                // TODO: add photos
                configureLayout();
            }
            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                Toast.makeText(getApplicationContext(), "Could not download catalog.", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onAdded(@NotNull Download download) {}
            @Override
            public void onQueued(@NotNull Download download, boolean b) { }
            @Override
            public void onWaitingNetwork(@NotNull Download download) {}
            @Override
            public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {}
            @Override
            public void onStarted(@NotNull Download download, @NotNull List<? extends DownloadBlock> list, int i) {}
            @Override
            public void onProgress(@NotNull Download download, long l, long l1) {}
            @Override
            public void onPaused(@NotNull Download download) {}
            @Override
            public void onResumed(@NotNull Download download) {}
            @Override
            public void onCancelled(@NotNull Download download) {}
            @Override
            public void onRemoved(@NotNull Download download) {}
            @Override
            public void onDeleted(@NotNull Download download) {}
        };

        fetch.addListener(fetchListener);
    }

    public void addPhotoClick(View v) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    public void addUserClick(View v) {
        Intent addUserIntent = new Intent(ManageAlbumActivity.this, AddUserActivity.class);
        startActivity(addUserIntent);
    }

    public void backOnClick(View v) {
        // delete current album
        Hawk.delete(Constants.CURRENT_ALBUM_KEY);

        fetch.removeListener(fetchListener);
        fetch.close();
        finish();
    }
}
