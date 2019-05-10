from django.contrib.auth.models import User, AnonymousUser
from django.contrib.auth import authenticate
from django.db import IntegrityError
from django.core.files import File

from random import randint
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
            return Response({"error": "Username is already in use."}, status = status.HTTP_400_BAD_REQUEST)
        except Exception as e:
            return Response({"error": str(e)}, status = status.HTTP_400_BAD_REQUEST)

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
        url = request.data.get("catalog")

        if not validate_catalog(url):
            return Response({'error': 'Photo Catalog URL is invalid.'}, status = status.HTTP_400_BAD_REQUEST)

        user = request.user
        if isinstance(user, AnonymousUser):
            return Response({'error': 'User is invalid.'}, status = status.HTTP_401_UNAUTHORIZED)

        # create catalog file
        album_id = randint(1, 10000000)
        catalog = open("catalogs/catalog_" + str(album_id), 'w+')

        # add slice url
        catalog.write(user.username + "," + url)

        # save album and membership on database
        try:
            album = Album.objects.create(id = album_id, owner = user)
            membership = Membership.objects.create(album = album, user = user, catalog = url)
        except Exception as e:
            return Response({"error": str(e)}, status = status.HTTP_400_BAD_REQUEST)
        finally:
            # close file
            catalog.close()

        return Response({'album_id': album_id}, status = status.HTTP_200_OK)


# POST /album/<id>/add/<username>
class AddUserView(generics.CreateAPIView):
    def post(self, request, *args, **kwargs):
        # get username, album id and new user catalog
        username = self.kwargs.get("username")
        id = self.kwargs.get("id")
        url = request.data.get("catalog")

        if not validate_catalog(url):
            return Response({'error': 'Photo Catalog URL is invalid.'}, status = status.HTTP_400_BAD_REQUEST)

        album = None
        add_user = None

        # check if album exists
        try:
            album = Album.objects.get(id = id)
            add_user = User.objects.get(username = username)
        except ObjectDoesNotExist:
            return Response({"error": "Username or Album invalid."}, status = status.HTTP_400_BAD_REQUEST)

        # get requesting user
        user = request.user
        if isinstance(user, AnonymousUser):
            return Response({'error': 'User is invalid.'}, status = status.HTTP_401_UNAUTHORIZED)

        # check ownership
        if not album.owner == user:
            return Response({'error': 'You do not own this album.'}, status = status.HTTP_401_UNAUTHORIZED)

        # check if membership already exists
        if Membership.objects.filter(album = album, user = add_user).exists():
            return Response({'error': 'User ' + username + ' is already a member of this album.'}, status = status.HTTP_401_UNAUTHORIZED)

        # add membership
        try:
            membership = Membership.objects.create(album = album, user = add_user, catalog = url)
        except Exception as e:
            return Response({"error": str(e)}, status = status.HTTP_400_BAD_REQUEST)

        # update catalog file
        catalog = "catalogs/catalog_" + str(id)
        with open(catalog, 'a') as file:
            file.write('\n' + username + ',' + url)

        return Response(status = status.HTTP_200_OK)

# GET album/<id>
class GetAlbumView(generics.ListAPIView):
    def get(self, request, *args, **kwargs):
        id = self.kwargs.get("id")

        # check if album exists
        if not Album.objects.filter(id = id).exists():
            return Response({"error": "Album does not exist."}, status = status.HTTP_400_BAD_REQUEST)

        # get requesting user
        user = request.user
        if isinstance(user, AnonymousUser):
            return Response({'error': 'User is invalid.'}, status = status.HTTP_401_UNAUTHORIZED)

        # check membership
        if not Membership.objects.filter(user = user, album__id = id).exists():
            return Response({'error': 'You are not a member of this album.'}, status = status.HTTP_401_UNAUTHORIZED)

        # return catalog information
        catalogs = Membership.objects.filter(album__id = id)
        serializer = CatalogSerializer(catalogs, many = True)

        return Response(serializer.data, status = status.HTTP_200_OK)


# GET album/user/<username>
class ListAlbumsView(generics.ListAPIView):
    queryset = Membership.objects.all()
    serializer_class = MembershipSerializer

    def list(self, request, *args, **kwargs):
        # get user and albums
        username = self.kwargs.get("username")
        memberships = self.get_queryset().filter(user__username = username)

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
