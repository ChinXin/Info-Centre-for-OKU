package my.edu.tarc.oku

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.databinding.FragmentValidateNumberBinding

class ValidateNumberFragment : Fragment() {

    private lateinit var binding : FragmentValidateNumberBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_validate_number, container, false)

        val args = ValidateNumberFragmentArgs.fromBundle(requireArguments())
        val username = args.username

        binding.btnGoNext.setOnClickListener{
            val value = binding.validateNumber.text.toString()

            if(value == ""){
                binding.validateNumberLayout.error = "Required Field!"
            }else{
                val database = Firebase.database
                val myRef = database.getReference("password")

                myRef.child(username).get().addOnSuccessListener {
                    val digit = it.child("validateNumber").value.toString()

                    if(value == digit){
                        Toast.makeText(context,"Verify Successfully!",Toast.LENGTH_LONG).show()
                        val action = ValidateNumberFragmentDirections.actionValidateNumberFragmentToInsertNewPasswordFragment(username)
                        binding.root.findNavController().navigate(action)
                    }else{
                        binding.validateNumberLayout.error = "Invalid Alphabet!"
                    }
                }
            }
        }

        return binding.root
    }
}