from django.contrib.auth.models import User
from rest_framework import serializers
from .models import Album, Membership

# user
class UserSerializer(serializers.Serializer):
    username = serializers.CharField(max_length = 30)
    password = serializers.CharField(max_length = 30)

# list user
class ListUserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('username',)

class UserListingField(serializers.RelatedField):
    def to_representation(self, value):
        return "%s" % value.username

# catalogs
class CatalogSerializer(serializers.ModelSerializer):
    user = UserListingField(read_only = True)

    class Meta:
        model = Membership
        fields = ('user', 'catalog',)

# membership
class MembershipSerializer(serializers.ModelSerializer):
    class Meta:
        model = Membership
        fields = ('album_id',)
