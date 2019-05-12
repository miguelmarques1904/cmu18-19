package pt.ist.cmu.p2photo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import pt.ist.cmu.api.ApiService;
import pt.ist.cmu.api.RetrofitInstance;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddUserActivity extends AppCompatActivity {

    private String albumName;
    private LinearLayout.LayoutParams layoutParams;
    private ApiService service;

    private String token;

    TextView title;

    TextView newUsers;
    TextView ownUsers;

    LinearLayout userAddListLayout;
    LinearLayout userOwnListLayout;

    Button addBtn;
    Button cancelBtn;

    List<String> memberList = new ArrayList<>();
    List<CheckBox> checkboxList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adduser);

        albumName = getIntent().getStringExtra("albumName");
        title = findViewById(R.id.adduser_title);
        title.setText("Managing Users of '" + albumName + "' album");

        newUsers = findViewById(R.id.adduser_newusers);
        ownUsers = findViewById(R.id.adduser_ownusers);

        userAddListLayout = findViewById(R.id.adduser_canaddll);
        userOwnListLayout = findViewById(R.id.adduser_cantaddll);

        addBtn = findViewById(R.id.adduser_add);
        cancelBtn = findViewById(R.id.adduser_cancel);

        // layout parameters
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16,0,16,16);

        // initialize hawk
        Hawk.init(AddUserActivity.this).build();

        // get token
        final User caller = Hawk.get(Constants.CURRENT_USER_KEY);
        token = "Token " + caller.getToken();

        // get members of the album
        service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<List<User>> memberCall = service.getAlbum(token, albumName);

        memberCall.enqueue(new Callback<List<User>>() {
            Toast toast;

            @Override
            public void onResponse(Call<List<User>> memberCall, Response<List<User>> response) {
                switch (response.code()) {
                    case 200:
                        for (User user : response.body()) {
                            String username = user.getUsername();

                            if (username != caller.getUsername()) {
                                addMember(username);
                                memberList.add(username);
                            }
                        }

                        if (response.body().size() <= 1) {
                            ownUsers.setText("You don't share the album with any user.");
                        }
                        break;
                    case 401:
                        toast = Toast.makeText(getApplicationContext(), "You are not logged in.", Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    case 403:
                        toast = Toast.makeText(getApplicationContext(), "You are not a member of this album.", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    case 404:
                        toast = Toast.makeText(getApplicationContext(), "This album does not exist.", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    default:
                        toast = Toast.makeText(getApplicationContext(), "Something went wrong on the server side.", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                }
            }

            @Override
            public void onFailure(Call<List<User>> memberCall, Throwable t) {
                toast = Toast.makeText(getApplicationContext(), "Something went wrong... Network may be down...", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        // get all users
        Call<List<User>> allUsersCall = service.getUsers(token);

        allUsersCall.enqueue(new Callback<List<User>>() {
            Toast toast;

            @Override
            public void onResponse(Call<List<User>> allUsersCall, Response<List<User>> response) {
                switch (response.code()) {
                    case 200:
                        for (User user : response.body()) {
                            String username = user.getUsername();

                            if (username != caller.getUsername() && !memberList.contains(username)) {
                                CheckBox cb = new CheckBox(getApplicationContext());
                                cb.setText(username);
                                cb.setTextSize(16);
                                userAddListLayout.addView(cb);
                                checkboxList.add(cb);
                            }
                        }

                        if (response.body().size() <= 1) {
                            newUsers.setText("There are no users available to add.");
                        }
                        break;
                    case 401:
                        toast = Toast.makeText(getApplicationContext(), "You were not logged in.", Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    default:
                        toast = Toast.makeText(getApplicationContext(), "Something went wrong on the server side.", Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                }
            }

            @Override
            public void onFailure(Call<List<User>> allUsersCall, Throwable t) {
                toast = Toast.makeText(getApplicationContext(), "Something went wrong... Network may be down...", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }

    public void addUserClick(View v) {
        for (CheckBox cb: checkboxList) {
            final String username = cb.getText().toString();
            if (cb.isChecked() && !memberList.contains(username)) {
                // TODO create catalog for new users on dropbox
                String catalog = "http://google.com";

                Call<Void> addUserCall = service.addUserToAlbum(token, albumName, username, catalog);

                addUserCall.enqueue(new Callback<Void>() {
                    Toast toast;

                    @Override
                    public void onResponse(Call<Void> addUserCall, Response<Void> response) {
                        switch (response.code()) {
                            case 200:
                                toast = Toast.makeText(getApplicationContext(), "Users were added successfully.", Toast.LENGTH_SHORT);
                                toast.show();
                                break;
                            case 400:
                                toast = Toast.makeText(getApplicationContext(), "Catalog URL is invalid.", Toast.LENGTH_SHORT);
                                toast.show();
                                break;
                            case 401:
                                toast = Toast.makeText(getApplicationContext(), "You were not logged in.", Toast.LENGTH_SHORT);
                                toast.show();
                                break;
                            case 403:
                                toast = Toast.makeText(getApplicationContext(), "You are not a member of this album.", Toast.LENGTH_SHORT);
                                toast.show();
                                break;
                            case 404:
                                toast = Toast.makeText(getApplicationContext(), "This album does not exist.", Toast.LENGTH_SHORT);
                                toast.show();
                                break;
                            case 409:
                                toast = Toast.makeText(getApplicationContext(), "User '" + username + "' is already a member of this album.", Toast.LENGTH_SHORT);
                                toast.show();
                                break;
                            case 500:
                                toast = Toast.makeText(getApplicationContext(), "Something went wrong on the server side.", Toast.LENGTH_SHORT);
                                toast.show();
                                break;
                            default:
                                toast = Toast.makeText(getApplicationContext(), "Something went wrong on the server side.", Toast.LENGTH_SHORT);
                                toast.show();
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> addUserCall, Throwable t) {
                        toast = Toast.makeText(getApplicationContext(), "Something went wrong... Network may be down...", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });

            }
        }
    }

    public void backOnClick(View v) {
        finish();
    }

    private void addMember(String username) {
        TextView tv = new TextView(getApplicationContext());
        tv.setText(username);
        tv.setLayoutParams(layoutParams);
        tv.setTextSize(16);
        tv.setTextColor(Color.parseColor("#000000"));
        userOwnListLayout.addView(tv);
    }

}
