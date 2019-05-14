package pt.ist.cmu.api;

import java.util.List;

import pt.ist.cmu.models.Album;
import pt.ist.cmu.models.Membership;
import pt.ist.cmu.models.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @FormUrlEncoded
    @POST("users/register")
    Call<Void> registerUser(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("users/login")
    Call<User> loginUser(@Field("username") String username, @Field("password") String password);

    @GET("users/logout")
    Call<Void> logoutUser(@Header("Authorization") String token);

    @GET("users")
    Call<List<User>> getUsers(@Header("Authorization") String token);

    @FormUrlEncoded
    @POST("album/create")
    Call<Void> createAlbum(@Header("Authorization") String token, @Field("name") String albumName);

    @GET("album/{name}/add/{username}")
    Call<Void> addUserToAlbum(@Header("Authorization") String token, @Path("name") String albumName, @Path("username") String username);

    @GET("album/{name}")
    Call<List<Membership>> getAlbum(@Header("Authorization") String token, @Path("name") String albumName);

    @FormUrlEncoded
    @POST("album/{name}")
    Call<Void> updateAlbum(@Header("Authorization") String token, @Path("name") String albumName, @Field("catalog") String catalog);

    @GET("users/albums")
    Call<List<Album>> getUserAlbums(@Header("Authorization") String token);

}
