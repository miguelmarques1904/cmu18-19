package pt.ist.cmu.p2photo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import pt.ist.cmu.models.Album;
import pt.ist.cmu.models.Membership;
import pt.ist.cmu.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddUserActivity extends AppCompatActivity {

    private Album album;
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

        // initialize hawk
        Hawk.init(AddUserActivity.this).build();

        // check album (should exist always)
        if (!Hawk.contains(Constants.CURRENT_ALBUM_KEY)) {
            Toast.makeText(getApplicationContext(), "The album is invalid.", Toast.LENGTH_LONG).show();
            return;
        }

        // set album
        album = Hawk.get(Constants.CURRENT_ALBUM_KEY);

        title = findViewById(R.id.adduser_title);
        title.setText("Managing Users of '" + album.getName() + "' album");

        newUsers = findViewById(R.id.adduser_newusers);
        ownUsers = findViewById(R.id.adduser_ownusers);

        userAddListLayout = findViewById(R.id.adduser_canaddll);
        userOwnListLayout = findViewById(R.id.adduser_cantaddll);

        addBtn = findViewById(R.id.adduser_add);
        cancelBtn = findViewById(R.id.adduser_cancel);

        // layout parameters
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16,0,16,16);

        // get token
        final User caller = Hawk.get(Constants.CURRENT_USER_KEY);
        token = "Token " + caller.getToken();

        // get album members from preferences
        // add them to ownership view
        for (Membership m : album.getCatalogs()) {
            String memberUsername = m.getUsername();

            if (!memberUsername.equals(caller.getUsername())) {
                showMember(memberUsername);
            }

            memberList.add(memberUsername);
        }

        if (album.getCatalogs().size() <= 1) {
            ownUsers.setText("You don't share this album with any user.");
        }

        // get all users
        service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<List<User>> allUsersCall = service.getUsers(token);

        allUsersCall.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> allUsersCall, Response<List<User>> response) {
                switch (response.code()) {
                    case 200:
                        for (User user : response.body()) {
                            String username = user.getUsername();

                            if (!memberList.contains(username)) {
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
                        Toast.makeText(getApplicationContext(), "You were not logged in.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Something went wrong on the server side.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(Call<List<User>> allUsersCall, Throwable t) {
                Toast.makeText(getApplicationContext(), "Something went wrong... Network may be down...", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void addUserClick(View v) {
        for (final CheckBox cb : checkboxList) {
            final String username = cb.getText().toString();

            if (cb.isChecked() && !memberList.contains(username)) {

                // TODO create catalog for new users on dropbox
                final String catalog = "http://google.com";

                Call<Void> addUserCall = service.addUserToAlbum(token, album.getName(), username, catalog);

                addUserCall.enqueue(new Callback<Void>() {
                    Toast toast;

                    @Override
                    public void onResponse(Call<Void> addUserCall, Response<Void> response) {
                        switch (response.code()) {
                            case 200:
                                // add to members text area
                                showMember(username);

                                // remove from checkboxes
                                userAddListLayout.removeView(cb);
                                checkboxList.remove(cb);

                                // update album membership
                                List<Membership> newMembers = album.getCatalogs();
                                newMembers.add(new Membership(username, catalog));
                                album.setCatalogs(newMembers);

                                // update album on preferences
                                Hawk.put(Constants.CURRENT_ALBUM_KEY, album);

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

    private void showMember(String username) {
        TextView tv = new TextView(getApplicationContext());
        tv.setText(username);
        tv.setLayoutParams(layoutParams);
        tv.setTextSize(16);
        tv.setTextColor(Color.parseColor("#000000"));
        userOwnListLayout.addView(tv);
    }

}
