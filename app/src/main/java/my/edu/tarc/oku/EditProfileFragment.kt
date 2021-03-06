package my.edu.tarc.oku

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.data.User
import my.edu.tarc.oku.data.UserSessionManager
import my.edu.tarc.oku.databinding.FragmentEditProfileBinding
import java.util.regex.Pattern


class EditProfileFragment : Fragment() {
    
    private lateinit var binding: FragmentEditProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val session = UserSessionManager(requireContext().applicationContext)
        val user = session.userDetails
        val name = user[UserSessionManager.KEY_NAME].toString()
        val status = user[UserSessionManager.KEY_STATUS].toString()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)

        setupListener()

        val database = Firebase.database
        val myRef = database.getReference("users")
        var tvPassword = ""

        myRef.child(status).child(name).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tvUsername = dataSnapshot.child("username").value.toString()
                val tvFullName = dataSnapshot.child("fullName").value.toString()
                val tvEmail = dataSnapshot.child("email").value.toString()
                val tvPhoneNo = dataSnapshot.child("phoneNo").value.toString()
                val tvAddress = dataSnapshot.child("address").value.toString()
                tvPassword = dataSnapshot.child("password").value.toString()

                binding.username.setText(tvUsername)
                binding.fullName.setText(tvFullName)
                binding.email.setText(tvEmail)
                binding.phoneNo.setText(tvPhoneNo)
                binding.address.setText(tvAddress)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.btnUpdate.setOnClickListener{
            val username = binding.username.text.toString()
            val fullName = binding.fullName.text.toString()
            val email    = binding.email.text.toString()
            val phoneNo  = binding.phoneNo.text.toString()
            val address  = binding.address.text.toString()
            val password = tvPassword

            val update_user = User (username,fullName,email,phoneNo,address, password)

            if(isValidate()){
                val inputManager: InputMethodManager =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(binding.root.rootView.windowToken, 0)
                myRef.child(status).child(name).setValue(update_user)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Profile Update Successfully!!!",
                            Toast.LENGTH_LONG
                        ).show()
                        if(status == "admin"){
                            binding.root.findNavController().navigate(R.id.action_editProfileFragment2_to_homeAdminFragment)
                        }else{
                            binding.root.findNavController().navigate(R.id.action_editProfileFragment_to_homeMemberFragment)
                        }
                    }
            }
        }
        binding.btnChangePassword.setOnClickListener {
            if (status == "admin"){
                binding.root.findNavController().navigate(R.id.action_editProfileFragment2_to_changePasswordFragment2)
            }else{
                binding.root.findNavController().navigate(R.id.action_editProfileFragment_to_changePasswordFragment)
            }
        }

        return binding.root
    }

    private fun isValidate(): Boolean =
        validateFullName() && validateEmail() && validatePhoneNo() && validateAddress()

    private fun setupListener() {
        binding.fullName.addTextChangedListener(TextFieldValidation(binding.fullName))
        binding.email.addTextChangedListener(TextFieldValidation(binding.email))
        binding.phoneNo.addTextChangedListener(TextFieldValidation(binding.phoneNo))
        binding.address.addTextChangedListener(TextFieldValidation(binding.address))
    }

    private fun validateFullName(): Boolean {
        if (binding.fullName.text.toString().trim().isEmpty()) {
            binding.fullNameLayout.error = "Required Field!"
            binding.fullName.requestFocus()
            return false
        } else {
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

    private fun validateAddress(): Boolean {
        if (binding.address.text.toString().trim().isEmpty()) {
            binding.addressLayout.error = "Required Field!"
            binding.address.requestFocus()
            return false
        } else {
            binding.addressLayout.isErrorEnabled = false
        }
        return true
    }

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable?) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
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
            }
        }
    }
}