from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path('p2photo/api/', include('api.urls')),
    path('admin/', admin.site.urls),
]
