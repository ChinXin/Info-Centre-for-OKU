package my.edu.tarc.oku

import android.content.ClipData
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
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
import my.edu.tarc.oku.databinding.ActivityAdminBinding
import my.edu.tarc.oku.databinding.ActivityMemberBinding
import android.content.Intent




class AdminActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
//    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = UserSessionManager(applicationContext)

        @Suppress("UNUSED_VARIABLE")
        val binding =
            DataBindingUtil.setContentView<ActivityAdminBinding>(this, R.layout.activity_admin)
        val bundle = intent.extras
        val username = bundle?.getString("Username").toString()
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        setSupportActionBar(binding.appBarAdmin.toolbarA)

        binding.appBarAdmin.fabA.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navViewA
        val navController = findNavController(R.id.nav_host_fragment_content_admin)

        val headerView = navView.getHeaderView(0)
        val btnUS: Button = headerView.findViewById(R.id.btnUsername)

        btnUS.text = username
        btnUS.setOnClickListener {
            findNavController(R.id.nav_host_fragment_content_admin).navigate(R.id.editProfileFragment)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        val btnLogout = navView.menu.findItem(R.id.Admin_Logout)
        btnLogout.setOnMenuItemClickListener {
            session.logoutUser()
            true
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeAdminFragment, R.id.adminEvent
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
        val navController = findNavController(R.id.nav_host_fragment_content_admin)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}