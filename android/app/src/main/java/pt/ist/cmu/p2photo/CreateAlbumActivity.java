package pt.ist.cmu.p2photo;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import pt.ist.cmu.api.ApiService;
import pt.ist.cmu.api.RetrofitInstance;
import pt.ist.cmu.cloud.DropboxActivity;
import pt.ist.cmu.cloud.DropboxClientFactory;
import pt.ist.cmu.cloud.UploadFileTask;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.helpers.StringGenerator;
import pt.ist.cmu.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAlbumActivity extends DropboxActivity {

    private String albumName;
    private File catalog;
    private User user;

    EditText albumNameField;
    TextView message;

    Button createBtn;
    Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createalbum);

        albumNameField = findViewById(R.id.createalbum_albumName);

        message = findViewById(R.id.createalbum_message);

        createBtn = findViewById(R.id.createalbum_create);
        cancelBtn = findViewById(R.id.createalbum_cancel);

        // get username
        user = Hawk.get(Constants.CURRENT_USER_KEY);
    }

    public void createAlbumOnClick(View v) {
        createBtn.setEnabled(false);
        createBtn.setText("Creating...");

        message.setTextColor(Color.rgb(194, 38, 38));

        // get album name
        albumName = albumNameField.getText().toString();

        // catalog name
        String catalogName = "/" + StringGenerator.generateName(25);

        if (!albumName.isEmpty()) {

            catalog = new File(CreateAlbumActivity.this.getFilesDir().getPath() + catalogName);
            try {
                catalog.createNewFile();
            } catch (IOException e) {
                message.setText("An error occurred when creating the catalog file.");
                return;
            }

            String file_uri = Uri.fromFile(catalog).toString();

            // Upload catalog to dropbox
            new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
                @Override
                public void onUploadComplete(String result) {
                    createBtn.setEnabled(true);
                    createBtn.setText("Create");

                    createAlbum(result);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(CreateAlbumActivity.this, "An error occurred when uploading catalog to Dropbox.", Toast.LENGTH_LONG).show();
                    return;
                }
            }).execute(file_uri, "");

        } else {
            message.setText("Album Name is empty.");
        }
    }

    private void createAlbum(String url) {
        String token = "Token " + user.getToken();

        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<Void> call = service.createAlbum(token, albumName, url);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                switch (response.code()) {
                    case 201:
                        message.setTextColor(Color.rgb(0, 133, 119));
                        message.setText("Album '" + albumName + "' was created successfully.");
                        break;
                    case 400:
                        message.setText("Album name or catalog URL are invalid.");
                        break;
                    case 401:
                        message.setText("Credentials are invalid.");
                        break;
                    case 409:
                        message.setText("Album '" + albumName + "' is already in use.");
                        break;
                    case 500:
                        message.setText("Something went wrong on the server side.");
                        break;
                    default:
                        message.setText("Something went wrong...");
                        break;
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                message.setText("Something went wrong... Network may be down...");
            }
        });
    }

    public void cancelOnClick(View v) {
        if (catalog != null) catalog.delete();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (catalog != null) catalog.delete();
        finish();
    }

    @Override
    protected void loadData() {
    }
}