package my.edu.tarc.oku

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import my.edu.tarc.oku.databinding.FragmentForgotPasswordBinding
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class ForgotPasswordFragment : Fragment() {

    private lateinit var binding : FragmentForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_forgot_password, container, false)

        val database = Firebase.database
        val myRef = database.getReference("users")

        binding.btnNext.setOnClickListener{
            val username = binding.username

            if(username.text.toString() == ""){
                binding.usernameLayout.error = "Required Field!"
            }else{
                var checkMember: Boolean

                myRef.get().addOnSuccessListener {
                    checkMember = it.child("member").hasChild(username.text.toString().lowercase())

                    if (checkMember) {
                        binding.usernameLayout.isErrorEnabled = false

                        myRef.child("member").child(username.text.toString().lowercase()).get().addOnSuccessListener {
                            val email = it.child("email").value.toString()

                            if(binding.emailF.text.toString() != email){
                                binding.emailFLayout.error = "Email is incorrect with your account!"
                            }else{
                                GlobalScope.launch(IO){
                                    Transport.send(plainMail(email))
                                }
                                val action = ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToValidateNumberFragment(username.text.toString())
                                binding.root.findNavController().navigate(action)
                                Toast.makeText(context,"Please Check Your Email",Toast.LENGTH_LONG).show()
                            }
                        }
                    }else{
                        binding.usernameLayout.error = "No such users!"
                    }
                }
            }
        }

        return binding.root
    }

    private fun plainMail(receiver:String): MimeMessage {
        val from = "okuapplication@gmail.com" //Sender email

        val properties = System.getProperties()

        with(properties) {
            put("mail.smtp.host", "smtp.gmail.com") //Configure smtp host
            put("mail.smtp.port", "587") //Configure port
            put("mail.smtp.starttls.enable", "true") //Enable TLS
            put("mail.smtp.auth", "true") //Enable authentication
        }

        val auth = object : Authenticator() {
            override fun getPasswordAuthentication() =
                PasswordAuthentication(from, "S3cre7P@55") //Credentials of the sender email
        }

        val session = Session.getDefaultInstance(properties, auth)
        val message = MimeMessage(session)
        val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val validateNumber: String = List(6) { alphabet.random() }.joinToString("")

        val database = Firebase.database
        val myRef = database.getReference("password")

        myRef.child(binding.username.text.toString()).child("validateNumber").setValue(validateNumber).addOnSuccessListener {}

        with(message) {
            setFrom(InternetAddress(from))

            addRecipient(Message.RecipientType.TO, InternetAddress(receiver))
            subject = "Change Account Password" //Email subject
            setContent(
                "<html><body><h1>$validateNumber</h1></body></html>",
                "text/html; charset=utf-8"
            ) //Sending html message, you may change to send text here.
        }
        return message
    }
}