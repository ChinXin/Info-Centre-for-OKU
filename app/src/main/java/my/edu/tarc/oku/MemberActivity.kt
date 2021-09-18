package my.edu.tarc.oku

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.ActionBar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import my.edu.tarc.oku.data.UserSessionManager
import my.edu.tarc.oku.databinding.ActivityMemberBinding

class MemberActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = UserSessionManager(applicationContext)

        @Suppress("UNUSED_VARIABLE")
        val binding = DataBindingUtil.setContentView<ActivityMemberBinding>(this, R.layout.activity_member)
        val bundle = intent.extras
        val username = bundle?.getString("Username").toString()

        setSupportActionBar(binding.appBarMember.toolbarM)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navViewM
        val navController = findNavController(R.id.nav_host_fragment_content_member)

        val headerView = navView.getHeaderView(0)
        val btnUS: Button = headerView.findViewById(R.id.btnUsername)

        btnUS.text = username
        btnUS.setOnClickListener {
            findNavController(R.id.nav_host_fragment_content_member).navigate(R.id.editProfileFragment)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        navView.menu.findItem(R.id.objectRecognizeM).setOnMenuItemClickListener {
            val intent = Intent(applicationContext, ObjectRecognition::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
            true
        }

        navView.menu.findItem(R.id.Member_Logout).setOnMenuItemClickListener {
            session.logoutUser()
            true
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeMemberFragment, R.id.memberRegisteredEvent
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_member)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}