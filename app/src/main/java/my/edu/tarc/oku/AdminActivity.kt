package my.edu.tarc.oku

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
import my.edu.tarc.oku.data.UserSessionManager
import my.edu.tarc.oku.databinding.ActivityAdminBinding


class AdminActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = UserSessionManager(applicationContext)

        val binding = DataBindingUtil.setContentView<ActivityAdminBinding>(this, R.layout.activity_admin)
        val bundle = intent.extras
        val username = bundle?.getString("Username").toString()

        setSupportActionBar(binding.appBarAdmin.toolbarA)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navViewA
        val headerView = navView.getHeaderView(0)

        val btnUS: Button = headerView.findViewById(R.id.btnUsername)
        btnUS.text = username
        btnUS.setOnClickListener {
            findNavController(R.id.nav_host_fragment_content_admin).navigate(R.id.editProfileFragment2)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        val btnLogout = navView.menu.findItem(R.id.Admin_Logout)
        btnLogout.setOnMenuItemClickListener {
            session.logoutUser()
            true
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeAdminFragment, R.id.adminEvent
            ), drawerLayout
        )

        val navController = findNavController(R.id.nav_host_fragment_content_admin)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_admin)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}