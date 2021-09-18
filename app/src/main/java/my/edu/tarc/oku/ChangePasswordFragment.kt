package my.edu.tarc.oku

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.data.UserSessionManager
import my.edu.tarc.oku.databinding.FragmentChangePasswordBinding
import my.edu.tarc.oku.databinding.FragmentEditProfileBinding
import java.lang.StringBuilder


class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var session = UserSessionManager(requireContext().applicationContext)
        val user = session.userDetails
        val name = user[UserSessionManager.KEY_NAME].toString()
        val status = user[UserSessionManager.KEY_STATUS].toString()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_password, container, false)

        setupListener()

        val database = Firebase.database
        val myRef = database.getReference("users")


        binding.btnUpdate.setOnClickListener{
            val password = binding.password.text.toString().toByteArray()
            if(isValidate()){

                val inputManager: InputMethodManager =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(binding.root.rootView.windowToken, 0)

                myRef.child(status).child(name).child("password").setValue(convertedPassword(password))
                    .addOnSuccessListener { _ ->
                        Toast.makeText(
                            context,
                            "Password Update Successfully!!!",
                            Toast.LENGTH_LONG
                        ).show()
                        if(status == "admin"){
                            binding.root.findNavController().navigate(R.id.action_changePasswordFragment2_to_homeAdminFragment)
                        }else{
                            binding.root.findNavController().navigate(R.id.action_changePasswordFragment_to_homeMemberFragment)
                        }
                    }
            }
        }

        return binding.root
    }

    private fun isValidate(): Boolean =
        validatePassword() && validateConfirmPassword()

    private fun setupListener() {
        binding.password.addTextChangedListener(TextFieldValidation(binding.password))
        binding.confirmPassword.addTextChangedListener(TextFieldValidation(binding.confirmPassword))
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

    private fun validatePassword(): Boolean {
        if (binding.password.text.toString().trim().isEmpty()) {
            binding.passwordLayout.error = "Required Field!"
            binding.password.requestFocus()
            return false
        } else if (binding.password.text.toString().length < 6) {
            binding.passwordLayout.error = "Password must more than 5"
            binding.password.requestFocus()
            return false
        } else {
            binding.passwordLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateConfirmPassword(): Boolean {
        if (binding.confirmPassword.text.toString().trim().isEmpty()) {
            binding.confirmpasswordLayout.error = "Required Field!"
            binding.confirmPassword.requestFocus()
            return false
        } else if (binding.confirmPassword.text.toString() != binding.password.text.toString()) {
            binding.confirmpasswordLayout.error = "Password not match"
            binding.confirmPassword.requestFocus()
            return false
        } else {
            binding.confirmpasswordLayout.isErrorEnabled = false
        }
        return true
    }

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable?) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
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