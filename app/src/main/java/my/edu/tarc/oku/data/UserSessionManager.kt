package my.edu.tarc.oku.data

import android.content.Context
import android.content.Intent

import android.content.SharedPreferences
import my.edu.tarc.oku.MainActivity


class UserSessionManager(context: Context) {
    var pref: SharedPreferences
    var editor: SharedPreferences.Editor
    var _context: Context
    var PRIVATE_MODE = 0

    fun createUserLoginSession(name: String?, status: String?) {
        editor.putBoolean(IS_USER_LOGIN, true)
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_STATUS, status)
        editor.commit()
    }

    fun checkLogin(): Boolean {
        if (isUserLoggedIn) {
            return true
        }
        return false
    }

    val userDetails: HashMap<String?, String?>
        get() {
            val user = HashMap<String?, String?>()

            user[KEY_NAME] =
                pref.getString(KEY_NAME, null)

                user[KEY_STATUS] =
                pref.getString(KEY_STATUS, null)
            return user
        }

    fun logoutUser() {
        editor.clear()
        editor.commit()

        val i = Intent(_context, MainActivity::class.java)

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        _context.startActivity(i)
    }

    val isUserLoggedIn: Boolean
        get() = pref.getBoolean(IS_USER_LOGIN, false)

    companion object {
        private const val PREFER_NAME = "UserPref"
        private const val IS_USER_LOGIN = "IsUserLoggedIn"

        const val KEY_NAME = "name"
        const val KEY_STATUS = "status"
    }

    init {
        _context = context
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}