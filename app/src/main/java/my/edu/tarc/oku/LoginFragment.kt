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
import my.edu.tarc.oku.databinding.FragmentLoginBinding
import java.lang.StringBuilder
import java.lang.reflect.Member


class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        binding.btnLogin.setOnClickListener {

            val database = Firebase.database
            val myRef = database.getReference("users")

            var checkMember: Boolean
            var checkAdmin: Boolean
            val username = binding.username.text.toString().lowercase().trim()
            val password = convertedPassword(binding.password.text.toString().toByteArray())

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    checkMember = dataSnapshot.child("member").hasChild(username)
                    checkAdmin = dataSnapshot.child("admin").hasChild(username)

                    if (checkMember) {
                        myRef.child("member").child(username).get().addOnSuccessListener {
//                            var getPassword = ""

                            val getPassword = it.child("password").value.toString()

                            if (password == getPassword) {
                                // Navigate to Home Page
                                Toast.makeText(context, "Welcome Member", Toast.LENGTH_LONG).show()

                                val rootView = binding.root.rootView.findViewById<NavigationView>(R.id.navView)
                                val headerView = rootView.getHeaderView(0)
                                val btnSul: Button = headerView.findViewById(R.id.btnLoginSignUp)

                                btnSul.text = username.uppercase()

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
//                            var getPassword = ""

                            val getPassword = it.child("password").value.toString()

                            if (password == getPassword) {
                                // Navigate to Home Page
                                Toast.makeText(context, "Welcome Admin", Toast.LENGTH_LONG).show()

                                val rootView = binding.root.rootView.findViewById<NavigationView>(R.id.navView)
                                val headerView = rootView.getHeaderView(0)
                                val btnSul: Button = headerView.findViewById(R.id.btnLoginSignUp)

                                btnSul.text = username.uppercase()

                                val intent = Intent(activity, MemberActivity::class.java)
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
