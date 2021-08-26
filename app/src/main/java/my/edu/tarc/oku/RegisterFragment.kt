package my.edu.tarc.oku

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.data.User
import my.edu.tarc.oku.databinding.FragmentRegisterBinding
import java.lang.StringBuilder
import java.util.regex.Pattern


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    lateinit var userList: MutableList<User>
    // var username = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_register, container, false)

        setupListener()

        binding.btnRegister.setOnClickListener {
            val database = Firebase.database
            val myRef = database.getReference("users")

            val username = binding.username.text.toString()
            val fullName = binding.fullName.text.toString()
            val email = binding.email.text.toString()
            val phoneNo = binding.phoneNo.text.toString()
            val address = binding.address.text.toString()
            val password = binding.password.text.toString().toByteArray()

            if(isValidate()){
                val new_user = User(username,fullName,email,phoneNo,address,convertedPassword(password))
                myRef.child(username).setValue(new_user).addOnSuccessListener{
                    Toast.makeText(context,"Register Successfully!!!",Toast.LENGTH_LONG).show()
                }
            }
        }
        return binding.root
    }

    //Hash Function
    private fun convertedPassword(data:ByteArray):String{
        val HEX_CHARS = "abcdefg1234567890".toCharArray()
        val r = StringBuilder(data.size*2)
        data.forEach { b ->
            val i = b.toInt()
            r.append(HEX_CHARS[i shr 4 and 0xF])
            r.append(HEX_CHARS[i and 0xF])
        }
        return r.toString()
    }

    private fun isValidate():Boolean = validateUsername() && validateFullName() && validateEmail() && validatePhoneNo() && validateAddress() && validatePassword() && validateConfirmPassword()

    private fun setupListener(){
        binding.username.addTextChangedListener(TextFieldValidation(binding.username))
        binding.fullName.addTextChangedListener(TextFieldValidation(binding.fullName))
        binding.email.addTextChangedListener(TextFieldValidation(binding.email))
        binding.phoneNo.addTextChangedListener(TextFieldValidation(binding.phoneNo))
        binding.address.addTextChangedListener(TextFieldValidation(binding.address))
        binding.password.addTextChangedListener(TextFieldValidation(binding.password))
        binding.confirmPassword.addTextChangedListener(TextFieldValidation(binding.confirmPassword))
    }

    //check duplicated username
    private fun validateUsername():Boolean{
        if(binding.username.text.toString().trim().isEmpty()){
            binding.usernameLayout.error = "Required Field!"
            binding.username.requestFocus()
            return false
        }else{
            val database = Firebase.database
            val myRef = database.getReference("users")

            myRef.child("username").addValueEventListener(object: ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    //Log.i("test_data",snapshot.children())
                    Log.i("test_data",dataSnapshot.toString())
                    if(dataSnapshot.exists()){
                        Log.i("test_data",dataSnapshot.toString())
                        binding.usernameLayout.error = "Duplicated username"
                        binding.username.requestFocus()
                    }else{
                        binding.usernameLayout.isErrorEnabled = false
                        binding.usernameLayout.error = "123"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

//            myRef.child("username").get().addOnSuccessListener {
//                binding.usernameLayout.error = "Duplicated username"
//                binding.username.requestFocus()
//            }.addOnFailureListener{
//                binding.usernameLayout.isErrorEnabled = false
//            }

        }
        return true
    }

    private fun validateFullName():Boolean{
        if(binding.fullName.text.toString().trim().isEmpty()){
            binding.fullNameLayout.error = "Required Field!"
            binding.fullName.requestFocus()
            return false
        }else{
            binding.fullNameLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validEmail(email: String): Boolean {
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun validateEmail(): Boolean {
        if (binding.email.text.toString().trim().isEmpty()) {
            binding.emailLayout.error = "Required Field!"
            binding.email.requestFocus()
            return false
        } else if (!validEmail(binding.email.text.toString())) {
            binding.emailLayout.error = "Invalid Email! e.g abc@xxx.com"
            binding.email.requestFocus()
            return false
        } else {
            binding.emailLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validatePhoneNo(): Boolean {
        val REG = "^(01)([0-9]{8,9})\$"

        if (binding.phoneNo.text.toString().trim().isEmpty()) {
            binding.phoneNoLayout.error = "Required Field!"
            binding.phoneNo.requestFocus()
            return false
        } else if (!Pattern.compile(REG).matcher(binding.phoneNo.text.toString()).matches()) {
            binding.phoneNoLayout.error = "Invalid Phone Number! e.g 01XXXXXXXXX"
            binding.phoneNo.requestFocus()
            return false
        } else {
            binding.phoneNoLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateAddress():Boolean{
        if(binding.address.text.toString().trim().isEmpty()){
            binding.addressLayout.error = "Required Field!"
            binding.address.requestFocus()
            return false
        }else{
            binding.addressLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validatePassword(): Boolean {
        if(binding.password.text.toString().trim().isEmpty()) {
            binding.passwordLayout.error = "Required Field!"
            binding.password.requestFocus()
            return false
        } else if (binding.password.text.toString().length < 6) {
            binding.passwordLayout.error = "Password must more than 5"
            binding.password.requestFocus()
            return false
        }
        else {
            binding.passwordLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateConfirmPassword(): Boolean {
        if(binding.confirmPassword.text.toString().trim().isEmpty()) {
            binding.confirmpasswordLayout.error = "Required Field!"
            binding.confirmPassword.requestFocus()
            return false
        } else if (binding.confirmPassword.text.toString() != binding.password.text.toString()) {
            binding.confirmpasswordLayout.error = "Password not match"
            binding.confirmPassword.requestFocus()
            return false
        }
        else {
            binding.confirmpasswordLayout.isErrorEnabled = false
        }
        return true
    }

    inner class TextFieldValidation(private val view:View):TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable?) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when(view.id){
                R.id.username -> {
                    validateUsername()
                }
                R.id.fullName -> {
                    validateFullName()
                }
                R.id.email -> {
                    validateEmail()
                }
                R.id.phoneNo -> {
                    validatePhoneNo()
                }
                R.id.address -> {
                    validateAddress()
                }
                R.id.password -> {
                    validatePassword()
                }
                R.id.confirmPassword -> {
                    validateConfirmPassword()
                }
            }
        }
    }
}
