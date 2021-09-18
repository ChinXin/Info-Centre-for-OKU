package my.edu.tarc.oku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.data.UserSessionManager
import my.edu.tarc.oku.databinding.FragmentLoginBinding
import java.lang.StringBuilder
import java.lang.reflect.Member
import kotlin.system.exitProcess


class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private var loginValueListener:ValueEventListener? = null
    val database = Firebase.database
    val myRef = database.getReference("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var session = UserSessionManager(requireContext().applicationContext)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        binding.btnLogin.setOnClickListener {
            var checkMember: Boolean
            var checkAdmin: Boolean
            val username = binding.username.text.toString().lowercase().trim()
            val password = convertedPassword(binding.password.text.toString().toByteArray())

            loginValueListener = myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    checkMember = dataSnapshot.child("member").hasChild(username)
                    checkAdmin = dataSnapshot.child("admin").hasChild(username)

                    if (checkMember) {
                        myRef.child("member").child(username).get().addOnSuccessListener {
                            val getPassword = it.child("password").value.toString()

                            if (password == getPassword) {
                                session.createUserLoginSession(username,"member")
                                val intent = Intent(activity, MemberActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.putExtra("Username", username)
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Incorrect Password. Please Try Again",
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.password.requestFocus()
                            }
                        }
                    } else if (checkAdmin) {
                        myRef.child("admin").child(username).get().addOnSuccessListener {
                            val getPassword = it.child("password").value.toString()

                            if (password == getPassword) {
                                session.createUserLoginSession(username,"admin")
                                val intent = Intent(activity, AdminActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.putExtra("Username", username)
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Incorrect Password. Please Try Again",
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.password.requestFocus()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Invalid username and password", Toast.LENGTH_LONG)
                            .show()
                        binding.username.requestFocus()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        }

        binding.btnGoRegister.setOnClickListener()
        {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_registerFragment)
        }
        return binding.root
    }

    override fun onPause() {
        if(loginValueListener != null){
            myRef.removeEventListener(loginValueListener!!)
        }
        super.onPause()
    }

    //Hash Function
    private fun convertedPassword(data: ByteArray): String {
        val HEX_CHARS = "abcdefg1234567890".toCharArray()
        val r = StringBuilder(data.size * 2)
        data.forEach { b ->
            val i = b.toInt()
            r.append(HEX_CHARS[i shr 4 and 0xF])
            r.append(HEX_CHARS[i and 0xF])
        }
        return r.toString()
    }
}
