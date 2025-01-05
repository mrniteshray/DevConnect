package xcom.nitesh.apps.devconnect.Home

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import xcom.nitesh.apps.devconnect.Model.User

class HomeViewModel(val context: Context) : ViewModel() {

    private val homeRepo = HomeRepo(context)

    private val _currentuserdata = MutableLiveData<User?>()
    val currentuserdata: MutableLiveData<User?>
        get()  = _currentuserdata

    fun fetchCurrentUserData() {
        homeRepo.getCurrentUserData()
        homeRepo.currentUserLiveData.observeForever { user->
            _currentuserdata.value = user

        }
    }
}