from django.db import models
from django.contrib.auth.models import User

class Album(models.Model):
    id = models.IntegerField(primary_key = True)
    owner = models.ForeignKey(User, on_delete = models.CASCADE)

class Membership(models.Model):
    album = models.ForeignKey(Album, on_delete = models.CASCADE)
    user = models.ForeignKey(User, on_delete = models.CASCADE)
    catalog = models.CharField(max_length = 200)
