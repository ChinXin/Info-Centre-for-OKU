package my.edu.tarc.oku

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.data.Feedback
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import my.edu.tarc.oku.databinding.FragmentFeedbackBinding
import java.util.*

class feedbackFragment : Fragment() {

    private lateinit var binding : FragmentFeedbackBinding
    @RequiresApi(Build.VERSION_CODES.O)
    val currentDateTime = LocalDateTime.now()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_feedback, container, false)

        val database = Firebase.database
        val myRef = database.getReference("feedback")

        val args = feedbackFragmentArgs.fromBundle(requireArguments())
        val markerId = args.markerId
        val username = args.username

        binding.btnSubmit.setOnClickListener{
            val feedback = binding.feedback.text.toString()
            val date = currentDateTime.format(DateTimeFormatter.ISO_DATE)
            val time = currentDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

            val new_feedback = Feedback(markerId,username,feedback,date,time)

            if(username.isNotEmpty()){
                myRef.child(markerId).child(username).push().setValue(new_feedback).addOnSuccessListener {
                    Toast.makeText(context,"Submit Successful!",Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(context,"Please Log in Account and submit feedback!",Toast.LENGTH_LONG).show()
            }
        }
        return binding.root
    }
}