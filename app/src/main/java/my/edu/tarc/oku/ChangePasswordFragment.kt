package my.edu.tarc.oku

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.databinding.FragmentChangePasswordBinding
import my.edu.tarc.oku.databinding.FragmentEditProfileBinding
import java.lang.StringBuilder


class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_password, container, false)

        setupListener()

        val database = Firebase.database
        val myRef = database.getReference("users")

        //need modify(get username)
        val username = "aeronchow"

        binding.btnUpdate.setOnClickListener{
            val password = binding.password.text.toString().toByteArray()
            if(isValidate()){
                myRef.child("member").child(username).child("password").setValue(convertedPassword(password))
                    .addOnSuccessListener { _ ->
                        Toast.makeText(
                            context,
                            "Update Successfully!!!",
                            Toast.LENGTH_LONG
                        ).show()
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