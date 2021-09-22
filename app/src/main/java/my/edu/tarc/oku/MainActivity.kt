package my.edu.tarc.oku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import my.edu.tarc.oku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("UNUSED_VARIABLE")
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        val headerView = navView.getHeaderView(0)
        val btnSul: Button = headerView.findViewById(R.id.btnLoginSignUp)

        btnSul.setOnClickListener {
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.loginFragment)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        val btnObjRecog = navView.menu.findItem(R.id.objectRecognize)

        btnObjRecog.setOnMenuItemClickListener {
            val intent = Intent(applicationContext, ObjectRecognition::class.java)
            startActivity(intent)
            true
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.loginFragment, R.id.registerFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}