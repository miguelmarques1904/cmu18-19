from django.contrib.auth.models import User, AnonymousUser
from django.contrib.auth import authenticate
from django.db import IntegrityError
from django.core.files import File

import random
import os

from django.core.validators import URLValidator, slug_re
from django.core.exceptions import ValidationError, ObjectDoesNotExist

from .models import Album, Membership

from rest_framework import generics, status, viewsets
from rest_framework.views import APIView
from rest_framework.permissions import AllowAny
from rest_framework.authtoken.models import Token
from rest_framework.response import Response

from .serializers import *

ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz"

### USERS ###

# POST /users/register
class RegisterUserView(generics.CreateAPIView):
    # custom permissions
    permission_classes = (AllowAny,)
    serializer_class = UserSerializer

    def post(self, request, *args, **kwargs):
        # get username and password
        username = request.data.get("username")
        password = request.data.get("password")

        # set email
        email = username + "@p2photo.com"

        if not username or len(username) > 30 or not slug_re.match(username):
            return Response({"error": "Username is too big or has invalid characters."}, status = status.HTTP_400_BAD_REQUEST)
        elif not password or len(password) > 30:
            return Response({"error": "Password is invalid."}, status = status.HTTP_400_BAD_REQUEST)

        try:
            user = User.objects.create_user(username = username, password = password, email = email)
        except IntegrityError:
            return Response({"error": "Username is already in use."}, status = status.HTTP_409_CONFLICT)
        except Exception as e:
            return Response({"error": str(e)}, status = status.HTTP_500_INTERNAL_SERVER_ERROR)

        return Response(status = status.HTTP_201_CREATED)


# POST /users/login
class LoginUserView(generics.CreateAPIView):
    # custom permissions
    permission_classes = (AllowAny,)
    serializer_class = UserSerializer

    def post(self, request, *args, **kwargs):
        # get username and password
        username = request.data.get("username")
        password = request.data.get("password")

        ### sanitize POST fields ###

        if username is None or password is None:
            return Response({'error': 'Please provide both username and password'}, status = status.HTTP_400_BAD_REQUEST)

        user = authenticate(username = username, password = password)
        if not user:
            return Response({'error': 'Invalid Credentials.'}, status = status.HTTP_401_UNAUTHORIZED)

        token, _ = Token.objects.get_or_create(user = user)
        return Response({'token': token.key}, status = status.HTTP_200_OK)


# GET /users/logout
class LogoutUserView(APIView):
    def get(self, request, *args, **kwargs):
        try:
            request.user.auth_token.delete()
        except Exception:
            return Response({'error': 'Invalid Token.'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(status = status.HTTP_200_OK)


# GET /users
class FindUsersView(viewsets.ModelViewSet):
    queryset = User.objects.all().filter(is_superuser = False).order_by("username")
    serializer_class = ListUserSerializer


### ALBUMS ###

# POST /album/create
class CreateAlbumView(generics.CreateAPIView):
    def post(self, request, *args, **kwargs):
        # get catalog URL
        name = request.data.get("name")

        if not slug_re.match(name):
            return Response({'error': 'Album Name is invalid.'}, status = status.HTTP_400_BAD_REQUEST)

        user = request.user
        if isinstance(user, AnonymousUser):
            return Response({'error': 'User is invalid.'}, status = status.HTTP_401_UNAUTHORIZED)

        # save album and membership on database
        try:
            album = Album.objects.create(name = name)
            membership = Membership.objects.create(album = album, user = user, catalog = "0", key = generate_key())

            # open and write url to album catalog file
            open("catalogs/catalog_" + name, 'a').close()

        except IntegrityError:
            return Response({"error": "Album name is already in use."}, status = status.HTTP_409_CONFLICT)
        except Exception as e:
            return Response({"error": str(e)}, status = status.HTTP_500_INTERNAL_SERVER_ERROR)

        return Response(status = status.HTTP_201_CREATED)


# GET /album/<name>/add/<username>
class AddUserView(generics.CreateAPIView):
    def get(self, request, *args, **kwargs):
        # get username, album id and new user catalog
        username = self.kwargs.get("username")
        album_name = self.kwargs.get("name")

        album = None
        add_user = None

        # check if album exists
        try:
            album = Album.objects.get(name = album_name)
            add_user = User.objects.get(username = username)
        except ObjectDoesNotExist:
            return Response({"error": "Username or Album invalid."}, status = status.HTTP_404_NOT_FOUND)

        # get requesting user
        user = request.user
        if isinstance(user, AnonymousUser):
            return Response({'error': 'User is invalid.'}, status = status.HTTP_401_UNAUTHORIZED)

        # check if membership already exists
        if Membership.objects.filter(album = album, user = add_user).exists():
            return Response({'error': 'User ' + username + ' is already a member of this album.'}, status = status.HTTP_409_CONFLICT)

        # add membership
        try:
            membership = Membership.objects.create(album = album, user = add_user, catalog = "0", key = generate_key())
        except Exception as e:
            return Response({"error": str(e)}, status = status.HTTP_500_INTERNAL_SERVER_ERROR)

        return Response(status = status.HTTP_200_OK)

# GET album/<name>
# POST album/<name>
class GetAlbumView(generics.ListCreateAPIView):
    def get(self, request, *args, **kwargs):
        album_name = self.kwargs.get("name")

        # check if album exists
        if not Album.objects.filter(name = album_name).exists():
            return Response({"error": "Album does not exist."}, status = status.HTTP_404_NOT_FOUND)

        # get requesting user
        user = request.user
        if isinstance(user, AnonymousUser):
            return Response({'error': 'User is invalid.'}, status = status.HTTP_401_UNAUTHORIZED)

        # check membership
        if not Membership.objects.filter(user = user, album__name = album_name).exists():
            return Response({'error': 'You are not a member of this album.'}, status = status.HTTP_403_FORBIDDEN)

        # return catalog information
        catalogs = Membership.objects.filter(album__name = album_name)
        serializer = CatalogSerializer(catalogs, many = True)

        return Response(serializer.data, status = status.HTTP_200_OK)

    def post(self, request, *args, **kwargs):
        catalog = request.data.get("catalog")
        album_name = self.kwargs.get("name")

        membership = None

        if not validate_catalog(catalog):
            return Response({'error': 'Catalog URL is invalid.'}, status = status.HTTP_400_BAD_REQUEST)

        # get requesting user
        user = request.user
        if isinstance(user, AnonymousUser):
            return Response({'error': 'User is invalid.'}, status = status.HTTP_401_UNAUTHORIZED)

        # get membership object
        try:
            membership = Membership.objects.get(album__name = album_name, user = user)
        except ObjectDoesNotExist:
            return Response({"error": "You are not a member of this album."}, status = status.HTTP_403_FORBIDDEN)

        # update catalog file
        catalog_file = "catalogs/catalog_" + album_name
        with open(catalog_file, 'a') as f:
            f.write(user.username + ',' + catalog + '\n')

        # update membership
        membership.catalog = catalog
        membership.save()

        return Response(status = status.HTTP_200_OK)


# GET users/albums
class ListAlbumsView(generics.ListAPIView):
    queryset = Membership.objects.all()
    serializer_class = MembershipSerializer

    def list(self, request, *args, **kwargs):
        # get requesting user
        user = request.user
        if isinstance(user, AnonymousUser):
            return Response({'error': 'User is invalid.'}, status = status.HTTP_401_UNAUTHORIZED)

        memberships = self.get_queryset().filter(user = user)
        if not memberships.exists():
            return Response(status = status.HTTP_204_NO_CONTENT)

        serializer = MembershipSerializer(memberships, many = True)
        return Response(serializer.data, status = status.HTTP_200_OK)

def validate_catalog(url):
    if url is None:
        return False

    # initialize url validator
    validator = URLValidator()

    try:
        validator(url)
    except ValidationError:
        return False

    return True

def generate_key():
    key = ''.join(random.choice(ALPHANUMERIC) for _ in range(64))
    return key
