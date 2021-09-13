package my.edu.tarc.oku.data

import android.content.Context
import android.content.Intent

import android.content.SharedPreferences
import my.edu.tarc.oku.MainActivity


class UserSessionManager(context: Context) {
    // Shared Preferences reference
    var pref: SharedPreferences

    // Editor reference for Shared preferences
    var editor: SharedPreferences.Editor

    // Context
    var _context: Context

    // Shared pref mode
    var PRIVATE_MODE = 0

    //Create login session
    fun createUserLoginSession(name: String?, status: String?) {
        // Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, true)

        // Storing name in pref
        editor.putString(KEY_NAME, name)

        // Storing email in pref
        editor.putString(KEY_STATUS, status)

        // commit changes
        editor.commit()
    }

    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else do anything
     */
    fun checkLogin(): Boolean {
        // Check login status
        if (isUserLoggedIn) {

//            // user is not logged in redirect him to Login Activity
//            val i = Intent(_context, MainActivity::class.java)
//
//            // Closing all the Activities from stack
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//
//            // Add new Flag to start new Activity
//            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//
//            // Staring Login Activity
//            _context.startActivity(i)
            return true
        }
        return false
    }//Use hashmap to store user credentials

    // user name

    // user email id

    // return user
    /**
     * Get stored session data
     */
    val userDetails: HashMap<String?, String?>
        get() {

            //Use hashmap to store user credentials
            val user = HashMap<String?, String?>()

            // user name
            user[KEY_NAME] =
                pref.getString(KEY_NAME, null)

            // user email id
                user[KEY_STATUS] =
                pref.getString(KEY_STATUS, null)

            // return user
            return user
        }

    /**
     * Clear session details
     */
    fun logoutUser() {

        // Clearing all user data from Shared Preferences
        editor.clear()
        editor.commit()

        // After logout redirect user to Login Activity
        val i = Intent(_context, MainActivity::class.java)

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Add new Flag to start new Activity
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        // Staring Login Activity
        _context.startActivity(i)
    }

    // Check for login
    val isUserLoggedIn: Boolean
        get() = pref.getBoolean(IS_USER_LOGIN, false)

    companion object {
        // Sharedpref file name
        private const val PREFER_NAME = "UserPref"

        // All Shared Preferences Keys
        private const val IS_USER_LOGIN = "IsUserLoggedIn"

        // User name (make variable public to access from outside)
        const val KEY_NAME = "name"

        // Email address (make variable public to access from outside)
        const val KEY_STATUS = "status"
    }

    // Constructor
    init {
        _context = context
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}