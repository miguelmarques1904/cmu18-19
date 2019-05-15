from django.db import models
from django.contrib.auth.models import User

class Album(models.Model):
    name = models.CharField(max_length = 50, unique = True)

class Membership(models.Model):
    album = models.ForeignKey(Album, on_delete = models.CASCADE)
    user = models.ForeignKey(User, on_delete = models.CASCADE)
    catalog = models.CharField(max_length = 200)
    key = models.CharField(max_length = 64)
