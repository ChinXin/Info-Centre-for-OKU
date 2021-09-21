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
import androidx.navigation.Navigation
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.databinding.FragmentInsertNewPasswordBinding
import java.lang.StringBuilder

class InsertNewPasswordFragment : Fragment() {

    private lateinit var binding : FragmentInsertNewPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_insert_new_password, container, false)

        val args = InsertNewPasswordFragmentArgs.fromBundle(requireArguments())
        val username = args.username

        setupListener()

        val database = Firebase.database
        val myRef = database.getReference("users")

        binding.btnConfirm.setOnClickListener{
            if(isValidate()){
                val password = binding.passwordN.text.toString().toByteArray()

                myRef.child("member").child(username.lowercase()).child("password").setValue(convertedPassword(password))
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Password Update Successfully!!!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                Navigation.findNavController(it).navigate(R.id.action_insertNewPasswordFragment_to_loginFragment)
            }
        }

        return binding.root
    }

    private fun isValidate(): Boolean =
        validatePassword() && validateConfirmPassword()

    private fun setupListener() {
        binding.passwordN.addTextChangedListener(TextFieldValidation(binding.passwordN))
        binding.confirmPasswordN.addTextChangedListener(TextFieldValidation(binding.confirmPasswordN))
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
        if (binding.passwordN.text.toString().trim().isEmpty()) {
            binding.passwordNLayout.error = "Required Field!"
            binding.passwordN.requestFocus()
            return false
        } else if (binding.passwordN.text.toString().length < 6) {
            binding.passwordNLayout.error = "Password must more than 5"
            binding.passwordN.requestFocus()
            return false
        } else {
            binding.passwordNLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateConfirmPassword(): Boolean {
        if (binding.confirmPasswordN.text.toString().trim().isEmpty()) {
            binding.confirmpasswordNLayout.error = "Required Field!"
            binding.confirmPasswordN.requestFocus()
            return false
        } else if (binding.confirmPasswordN.text.toString() != binding.passwordN.text.toString()) {
            binding.confirmpasswordNLayout.error = "Password not match"
            binding.confirmPasswordN.requestFocus()
            return false
        } else {
            binding.confirmpasswordNLayout.isErrorEnabled = false
        }
        return true
    }

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable?) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
                R.id.passwordN -> {
                    validatePassword()
                }
                R.id.confirmPasswordN -> {
                    validateConfirmPassword()
                }
            }
        }
    }
}