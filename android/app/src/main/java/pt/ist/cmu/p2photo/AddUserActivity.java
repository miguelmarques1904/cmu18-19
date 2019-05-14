package pt.ist.cmu.p2photo;

import android.graphics.Color;
import android.os.Bundle;
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
import pt.ist.cmu.cloud.DropboxActivity;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.models.Album;
import pt.ist.cmu.models.Membership;
import pt.ist.cmu.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddUserActivity extends DropboxActivity {

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

        // show message in case of zero members
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
                                showUser(username);
                            }
                        }

                        if (response.body().size() <= 1) {
                            newUsers.setText("There are no users available to add.");
                            addBtn.setEnabled(false);
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

    /*
     *  Action function to add user
     */

    public void addUserClick(View v) {
        addBtn.setEnabled(false);
        addBtn.setText("Adding...");

        for (final CheckBox cb : checkboxList) {
            // get username from checkbox
            final String username = cb.getText().toString();

            // loop through checkboxes
            // check if checked and if user is not a member of the album already
            if (cb.isChecked() && !memberList.contains(username)) {
                // call addUser function
                addUser(username, cb);
            }
        }
    }

    /*
     *  GET album/<name>/user/<username>
     *
     *  Server call to add user to album
     */

    private void addUser(final String username, final CheckBox cb) {
        Call<Void> addUserCall = service.addUserToAlbum(token, album.getName(), username);

        addUserCall.enqueue(new Callback<Void>() {
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
                        newMembers.add(new Membership(username, "0"));
                        album.setCatalogs(newMembers);

                        // update album on preferences
                        Hawk.put(Constants.CURRENT_ALBUM_KEY, album);

                        // update text
                        ownUsers.setText("Users That You Share Ownership With:");

                        // update add button
                        addBtn.setEnabled(true);
                        addBtn.setText("Add");

                        Toast.makeText(getApplicationContext(), "Users were added successfully.", Toast.LENGTH_SHORT).show();
                        break;
                    case 400:
                        Toast.makeText(getApplicationContext(), "Catalog URL is invalid.", Toast.LENGTH_SHORT).show();
                        break;
                    case 401:
                        Toast.makeText(getApplicationContext(), "You were not logged in.", Toast.LENGTH_SHORT).show();
                        break;
                    case 404:
                        Toast.makeText(getApplicationContext(), "This album does not exist.", Toast.LENGTH_SHORT).show();
                        break;
                    case 409:
                        Toast.makeText(getApplicationContext(), "User '" + username + "' is already a member of this album.", Toast.LENGTH_SHORT);
                        break;
                    case 500:
                        Toast.makeText(getApplicationContext(), "Something went wrong on the server side.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Something went wrong on the server side.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(Call<Void> addUserCall, Throwable t) {
                Toast.makeText(getApplicationContext(), "Something went wrong... Network may be down...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
     *  Auxiliary functions
     */

    // add member to textview
    private void showMember(String username) {
        TextView tv = new TextView(getApplicationContext());
        tv.setText(username);
        tv.setLayoutParams(layoutParams);
        tv.setTextSize(16);
        tv.setTextColor(Color.parseColor("#000000"));
        userOwnListLayout.addView(tv);
    }

    // create checkbox to add users to album
    private void showUser(String username) {
        CheckBox cb = new CheckBox(getApplicationContext());
        cb.setText(username);
        cb.setTextSize(16);
        userAddListLayout.addView(cb);
        checkboxList.add(cb);
    }

    /*
    *  Activity change function
    */

    public void backOnClick(View v) {
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
