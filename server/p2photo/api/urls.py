from django.urls import include, path
from api import views

urlpatterns = [
    path('users/register', views.RegisterUserView.as_view(), name = "register-user"),
    path('users/login', views.LoginUserView.as_view(), name = "login-user"),
    path('users/logout', views.LogoutUserView.as_view(), name = "logout-user"),
    path('users', views.FindUsersView.as_view({'get': 'list'}), name = "find-user"),
    path('users/albums', views.ListAlbumsView.as_view(), name = "list-user-albums"),
    path('album/create', views.CreateAlbumView.as_view(), name = "create-album"),
    path('album/<slug:name>/add/<slug:username>', views.AddUserView.as_view(), name = "add-user-album"),
    path('album/<slug:name>', views.GetAlbumView.as_view(), name = "get-album"),
]
