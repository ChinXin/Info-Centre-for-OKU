package my.edu.tarc.oku

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.databinding.FragmentLoginBinding
import my.edu.tarc.oku.databinding.FragmentRegisterBinding
import java.lang.StringBuilder


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        binding.btnLogin.setOnClickListener{

            val database = Firebase.database
            val myRef = database.getReference("users")

            val username = binding.username.text.toString().trim()
            val password = convertedPassword(binding.password.text.toString().toByteArray())

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val usernameList : MutableList<String?> = ArrayList()
                    val adminList    : MutableList<String?> = ArrayList()

                    for (d in dataSnapshot.child("member").children) {
                        val un = d.child("username").value
                        usernameList.add(un.toString())
                    }

                    for (e in dataSnapshot.child("admin").children){
                        val an = e.child("username").value
                        adminList.add(an.toString())
                    }

                    if (usernameList.contains(username)) {
                        myRef.child("member").child(username).get().addOnSuccessListener {
                            var getPassword = ""

                            getPassword = it.child("password").value.toString()

                            if(password == getPassword){
                                // Navigate to Home Page
                                Toast.makeText(context, "Welcome Member", Toast.LENGTH_LONG).show()
                            }else{
                                Toast.makeText(context, "Incorrect Password. Please Try Again", Toast.LENGTH_LONG).show()
                                binding.password.requestFocus()
                            }
                        }
                    } else if(adminList.contains(username)){
                        myRef.child("admin").child(username).get().addOnSuccessListener {
                            var getPassword = ""

                            getPassword = it.child("password").value.toString()

                            if(password == getPassword){
                                // Navigate to Home Page
                                Toast.makeText(context, "Welcome Admin", Toast.LENGTH_LONG).show()
                            }else{
                                Toast.makeText(context, "Incorrect Password. Please Try Again", Toast.LENGTH_LONG).show()
                                binding.password.requestFocus()
                            }
                        }
                    }else {
                        Toast.makeText(context, "Invalid username and password", Toast.LENGTH_LONG).show()
                        binding.username.requestFocus()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        }

        binding.btnGoRegister.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return binding.root
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
