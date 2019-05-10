# Project of Mobile and Ubiquitous Computing, IST 2018/2019

P2Photo. Made for Android 4.0+

## Server

To configure the P2Photo server:

```
pip3 install -r requirements.txt
cd server/p2photo
python3 manage.py makemigrations
python3 manage.py migrate
```

Optionally, you can add a super user to control the operations using Django Admin:

```
python3 manage.py createsuperuser
```

Finally, to run the server (on default port 8000 or change port parameter):

```
python3 manage.py runserver <port>
```

#### P2Photo Server API Calls

All calls (except register and login) require user authentication with a token received upon login.

| API Call  | Description |
| ------------- | ------------- |
| POST /users/register  | Registers a new user  |
| POST /users/login  | Logs in an existing user and returns an auth token  |
| GET /users/logout  | Logs out an user |
| GET /users  | Returns all the existing users  |
| POST /album/create  | Creates an album and returns its ID  |
| POST /album/\<id\>/add/\<username\>  | Add user <username> to the membership of album <id>  |
| GET /album/\<id\> | Returns all the catalog data for album <id>  |
| GET /album/user/\<username\>  | Returns all the album of which user <username> is member  |
