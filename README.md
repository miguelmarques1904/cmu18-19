# Project of Mobile and Ubiquitous Computing, IST 2018/2019

P2Photo. Made for Android 5.0+

## App

- To run the app, go to *pt.ist.cmu.helpers.Constants* and set the IP Addess in the BASE_URL
- Also, go to *res/xml/network_security_config.xml* and add the IP Address of the API Endpoint

*Note:* Don't forget to set DNS on host computer to 8.8.8.8 / 8.8.4.4 for the emulator to be able to access the internet and to configure the firewall appropriately 

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
python3 manage.py runserver <address:port>
```

Now, the server will be running on http://127.0.0.1:8000/ by default. To use it in a network, configure it on your computer's local IP address e.g. *192.168.1.1:8000*

- To access the Django Admin Panel, go to http://127.0.0.1:8000/admin.

- To make API Calls, go to http://127.0.0.1:8000/p2photo/api/ and add the path to the API Call or use cURL.

When calling methods that require user authentication, remember to add the following to the HTTP request:

```
Authorization: Token <auth_token>
```
