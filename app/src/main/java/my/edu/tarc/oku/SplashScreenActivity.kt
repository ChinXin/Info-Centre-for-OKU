package my.edu.tarc.oku

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import my.edu.tarc.oku.data.UserSessionManager

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val session = UserSessionManager(applicationContext)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler().postDelayed({
            if (session.checkLogin()){
                val user = session.userDetails
                val name = user[UserSessionManager.KEY_NAME]
                val status = user[UserSessionManager.KEY_STATUS]
                if(status =="admin") {
                    val intent = Intent(applicationContext, AdminActivity::class.java)
                    intent.putExtra("Username", name)
                    startActivity(intent)
                    finish()
                }else if (status == "member"){
                    val intent = Intent(applicationContext, MemberActivity::class.java)
                    intent.putExtra("Username", name)
                    startActivity(intent)
                    finish()
                }
            }
            else{
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, 3000)
    }
}