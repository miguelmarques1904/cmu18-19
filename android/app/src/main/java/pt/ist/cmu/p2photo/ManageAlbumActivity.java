package pt.ist.cmu.p2photo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Extras;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import pt.ist.cmu.api.ApiService;
import pt.ist.cmu.api.RetrofitInstance;
import pt.ist.cmu.cloud.DropboxActivity;
import pt.ist.cmu.cloud.DropboxClientFactory;
import pt.ist.cmu.cloud.UploadFileTask;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.helpers.StringGenerator;
import pt.ist.cmu.models.Album;
import pt.ist.cmu.models.Membership;
import pt.ist.cmu.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageAlbumActivity extends DropboxActivity {

    private static final int PHOTOS_READ_PERMISSION = 1;
    private static final String DOWNLOAD_CATALOG = "download";
    private static final String UPDATE_CATALOG = "update";

    private User user;

    private Fetch fetch;
    private FetchListener fetchListener;
    private FetchConfiguration fetchConfiguration;

    private Request downloadRequest;
    private Request updateRequest;

    private ApiService service;

    // ID of the activity started, used to process the result on the onActivityResult() function
    static int GET_FROM_GALLERY = 1;

    Album album;

    TextView title;

    static double col1Height;
    static double col2Height;
    static double col3Height;

    LinearLayout col1;
    LinearLayout col2;
    LinearLayout col3;

    int numPhotos = 0;
    List<String> photoList = new ArrayList<>();

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

        // initialize hawk
        Hawk.init(getApplicationContext()).build();

        // get token
        user = Hawk.get(Constants.CURRENT_USER_KEY);

        // configure api service
        service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);

        // fetch configuration
        fetchConfiguration = new FetchConfiguration.Builder(ManageAlbumActivity.this)
                .setDownloadConcurrentLimit(3)
                .build();
    }

    /*
     *  Function called onResume()
     *  Check out DropboxActivity
     */

    @Override
    protected void loadData() {
        // configure fetch
        configureFetch();

        // get user token from preferences
        String token = "Token " + user.getToken();

        Call<List<Membership>> memberCall = service.getAlbum(token, album.getName());

        memberCall.enqueue(new Callback<List<Membership>>() {
            @Override
            public void onResponse(Call<List<Membership>> memberCall, Response<List<Membership>> response) {
                switch (response.code()) {
                    case 200:
                        if (!album.getCatalogs().equals(response.body())) {
                            Toast.makeText(getApplicationContext(), "Getting pictures...", Toast.LENGTH_LONG).show();

                            album.setCatalogs(response.body());

                            // save album to preferences
                            Hawk.put(Constants.CURRENT_ALBUM_KEY, album);

                            // download catalogs
                            for (Membership m : album.getCatalogs()) {
                                // get url from memberships of album
                                String url = m.getCatalog();

                                // check if user has created a catalog (has uploaded any photo)
                                if (!url.equals("0")) {
                                    String file = ManageAlbumActivity.this.getFilesDir().getPath() + "/" + StringGenerator.generateName(5);

                                    downloadRequest = new Request(url, file);
                                    downloadRequest.setPriority(Priority.HIGH);
                                    downloadRequest.setNetworkType(NetworkType.ALL);
                                    downloadRequest.setTag(DOWNLOAD_CATALOG);

                                    fetch.enqueue(downloadRequest, updatedRequest -> {
                                        // request was enqueued for download
                                        Log.d("state", "catalog download was enqueued");
                                    }, error -> {
                                        Toast.makeText(getApplicationContext(), "Could not download catalog.", Toast.LENGTH_LONG).show();
                                    });
                                } else {
                                    Log.d("state", "catalog of " + m.getUsername() + " does not exist");
                                }
                            }
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

    /*
     *  Return from add photo activity
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            // get photo URI from gallery
            Uri imageURI = data.getData();

            // get catalog for user
            String userCatalog = getCatalogURLForUser(user.getUsername());

            boolean catalogExists = !userCatalog.equals("0");

            // show uploading message
            Toast.makeText(ManageAlbumActivity.this, "Uploading...", Toast.LENGTH_LONG).show();

            // upload picture
            new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
                @Override
                public void onUploadComplete(String result) {
                    photoList.add(result);

                    configureLayout();

                    if (!catalogExists) {
                        // create catalog and upload
                        String uri = createCatalog(result);
                        uploadCatalog(uri);
                    } else {
                        // retrieve, edit and upload catalog
                        downloadCatalog(result);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ManageAlbumActivity.this, "An error occurred when uploading image to Dropbox.", Toast.LENGTH_LONG).show();
                }
            }).execute(imageURI.toString(), "");
        }
    }

    /*
     *  Configuration of grid layout
     *  Photos are downloaded using Picasso Library
     */

    private void configureLayout() {
        for (; numPhotos < photoList.size(); numPhotos++) {
            String url = photoList.get(numPhotos);

            ImageView iv = new ImageView(this);
            iv.setAdjustViewBounds(true);
            iv.setLayoutParams(imageParams);

            Picasso.get().load(url).into(iv, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    // get bitmap
                    Bitmap image = ((BitmapDrawable)iv.getDrawable()).getBitmap();

                    // serialize bitmap
                    ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 100, bStream);
                    byte[] bitmap = bStream.toByteArray();

                    iv.setOnClickListener(v -> {
                        Intent viewPhotoIntent = new Intent(ManageAlbumActivity.this, ViewPhotoActivity.class);
                        viewPhotoIntent.putExtra("bitmap", bitmap);
                        startActivity(viewPhotoIntent);
                    });
                }

                @Override
                public void onError(Exception e) {
                }
            });

            // This works because column widths are equal
            double imageRatio = (double) iv.getHeight() / iv.getWidth();

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

    /*
    *  Activity change functions
    */

    // intent to go to add photo view
    public void addPhotoClick(View v) {
        // request permission to use photos
        if (ContextCompat.checkSelfPermission(ManageAlbumActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ManageAlbumActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PHOTOS_READ_PERMISSION);
        } else {
            startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
        }
    }

    // intent to add user to album
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

    /*
    *  Auxiliary functions
    */

    // create catalog file and add first photo url
    private String createCatalog(String pictureURL) {
        File catalog = new File(ManageAlbumActivity.this.getFilesDir().getPath() + "/" + StringGenerator.generateName(25) + ".txt");
        try {
            catalog.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(catalog));
            writer.write(pictureURL);
            writer.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Unable to create user catalog.", Toast.LENGTH_SHORT).show();
        }

        return Uri.fromFile(catalog).toString();
    }

    // upload catalog to dropbox
    private void uploadCatalog(String catalogURI) {
        Toast.makeText(getApplicationContext(), "Updating catalog...", Toast.LENGTH_SHORT).show();

        new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(String result) {
                // update on p2photo server
                updateServerCatalog(result);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageAlbumActivity.this, "An error occurred when uploading catalog to Dropbox.", Toast.LENGTH_LONG).show();
            }
        }).execute(catalogURI, "");
    }

    // download catalog from dropbox (using fetch)
    private void downloadCatalog(String pictureURL) {
        String file = ManageAlbumActivity.this.getFilesDir().getPath() + "/" + StringGenerator.generateName(25) + ".txt";
        String url = getCatalogURLForUser(user.getUsername());

        updateRequest = new Request(url, file);
        updateRequest.setPriority(Priority.HIGH);
        updateRequest.setNetworkType(NetworkType.ALL);
        updateRequest.setTag(UPDATE_CATALOG);

        Map<String, String> extras = new HashMap<>();
        extras.put("url", pictureURL);

        updateRequest.setExtras(new Extras(extras));

        fetch.enqueue(updateRequest, updatedRequest -> {
            // request was enqueued for download
            Log.d("state", "catalog update was enqueued");
        }, error -> {
            Toast.makeText(getApplicationContext(), "Could not download catalog.", Toast.LENGTH_LONG).show();
        });
    }

    // update catalog on p2photo server
    private void updateServerCatalog(String catalogURL) {
        // get user token from preferences
        String token = "Token " + user.getToken();

        Call<Void> call = service.updateAlbum(token, album.getName(), catalogURL);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> memberCall, Response<Void> response) {
                switch (response.code()) {
                    case 200:
                        // successful
                        break;
                    case 400:
                        Toast.makeText(getApplicationContext(), "Catalog URL is invalid.", Toast.LENGTH_SHORT).show();
                        break;
                    case 401:
                        Toast.makeText(getApplicationContext(), "You are not logged in.", Toast.LENGTH_SHORT).show();
                        break;
                    case 403:
                        Toast.makeText(getApplicationContext(), "You are not a member of this album.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Something went wrong on the server side.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(Call<Void> memberCall, Throwable t) {
                Toast.makeText(getApplicationContext(), "Something went wrong... Network may be down...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // encrypt catalog using ...
    private void encryptCatalog(String uri) {

    }

    // decrypt catalog
    private File decryptCatalog(String uri) {
        return null;
    }

    // get catalog URL that belongs to a user
    private String getCatalogURLForUser(String username) {
        for (Membership m : album.getCatalogs()) {
            if (m.getUsername().equals(username)) {
                return m.getCatalog();
            }
        }
        return "";
    }

    /*
     *  Configuration of fetch file downloader
     */

    private void configureFetch() {
        fetch = Fetch.Impl.getInstance(fetchConfiguration);

        fetchListener = new FetchListener() {
            @Override
            public void onCompleted(@NotNull Download download) {
                String tag = download.getTag();
                File file = new File(download.getFile());

                String url = download.getExtras().getString("url", "0");

                if (tag.equals(DOWNLOAD_CATALOG)) {
                    try {
                        Scanner sc = new Scanner(file);

                        // read url photos
                        // add to imageLinks to download
                        while (sc.hasNextLine()) {
                            photoList.add(sc.nextLine());
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Cannot read catalog file", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } finally {
                        file.delete();
                    }

                    configureLayout();
                } else if (tag.equals(UPDATE_CATALOG)) {
                    try {
                        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

                        bw.write('\n' + url);
                        bw.close();

                        // upload back to dropbox
                        uploadCatalog(Uri.fromFile(file).toString());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Cannot write to catalog file", Toast.LENGTH_SHORT).show();
                    }
                }
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

    /*
    *  Callback function of request permission call
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PHOTOS_READ_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(gallery, GET_FROM_GALLERY);
                } else {
                    Toast.makeText(getApplicationContext(), "No permission to access photos.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}
