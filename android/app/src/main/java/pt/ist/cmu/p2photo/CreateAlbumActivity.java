package pt.ist.cmu.p2photo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.orhanobut.hawk.Hawk;

import pt.ist.cmu.api.ApiService;
import pt.ist.cmu.api.RetrofitInstance;
import pt.ist.cmu.cloud.DropboxActivity;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAlbumActivity extends DropboxActivity {

    private String albumName;
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

    /*
     *  Create album action button
     */

    public void createAlbumOnClick(View v) {
        createBtn.setEnabled(false);
        createBtn.setText("Creating...");

        message.setTextColor(Color.rgb(194, 38, 38));

        // get album name
        albumName = albumNameField.getText().toString();

        if (!albumName.isEmpty()) {
            // call function to create
            createAlbum();
        } else {
            message.setText("Album Name is empty.");
        }
    }

    /*
     *  POST album/create
     *
     *  Auxiliary function to create album
     */

    private void createAlbum() {
        String token = "Token " + user.getToken();

        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<Void> call = service.createAlbum(token, albumName);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                switch (response.code()) {
                    case 201:
                        message.setTextColor(Color.rgb(0, 133, 119));
                        message.setText("Album '" + albumName + "' was created successfully.");
                        break;
                    case 400:
                        message.setText("Album name is invalid.");
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

                createBtn.setEnabled(true);
                createBtn.setText("Create");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                message.setText("Something went wrong... Network may be down...");
            }
        });

    }

    public void cancelOnClick(View v) {
        finish();
    }

    /*
     *  Function called onResume()
     *  Check out DropboxActivity
     *
     *  Unused
     */

    @Override
    protected void loadData() {
    }
}