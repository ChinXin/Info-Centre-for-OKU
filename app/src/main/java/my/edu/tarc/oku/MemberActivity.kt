package my.edu.tarc.oku

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
//    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = UserSessionManager(applicationContext)

        @Suppress("UNUSED_VARIABLE")
        val binding = DataBindingUtil.setContentView<ActivityMemberBinding>(this, R.layout.activity_member)
        val bundle = intent.extras
        val username = bundle?.getString("Username").toString()
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)

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

        val btnLogout = navView.menu.findItem(R.id.Member_Logout)
        btnLogout.setOnMenuItemClickListener {
            session.logoutUser()
            true
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeMemberFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.navdrawer_menu, menu)
//        return true
//    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_member)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}