package my.edu.tarc.oku

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import my.edu.tarc.oku.databinding.ActivityMainBinding
import android.widget.Button
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import my.edu.tarc.oku.data.UserSessionManager
import org.w3c.dom.Text
import android.text.Html






class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
//    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val session = UserSessionManager(applicationContext)

        @Suppress("UNUSED_VARIABLE")
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)

//        if (session.checkLogin()){
//            val user = session.userDetails
//            val name = user[UserSessionManager.KEY_NAME]
//            val status = user[UserSessionManager.KEY_STATUS]
//            if(status =="admin") {
//                val intent = Intent(applicationContext, AdminActivity::class.java)
//                intent.putExtra("Username", name)
//                startActivity(intent)
//            }else if (status == "member"){
//
//            }
//        }

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        val headerView = navView.getHeaderView(0)
        val btnSul: Button = headerView.findViewById(R.id.btnLoginSignUp)

        btnSul.setOnClickListener {
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.loginFragment)
            drawerLayout.closeDrawer(GravityCompat.START)
        }




//        btnLogout.setOnClickListener(object : OnClickListener() {
//            fun onClick(arg0: View?) {
//
//                // Clear the User session data
//                // and redirect user to LoginActivity
//                session.logoutUser()
//            }
//        })
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.aboutUs, R.id.loginFragment, R.id.registerFragment, R.id.add_event
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
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
//    private lateinit var drawerLayout: DrawerLayout
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        @Suppress("UNUSED_VARIABLE")
//        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
//
//        drawerLayout = binding.drawerLayout
//
//        val navController = this.findNavController(R.id.myNavHostFragment)
//
//        NavigationUI.setupActionBarWithNavController(this,navController, drawerLayout)
//
//        NavigationUI.setupWithNavController(binding.navView, navController)
//
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        val navController = this.findNavController(R.id.myNavHostFragment)
//        return NavigationUI.navigateUp(navController, drawerLayout)
//    }
}