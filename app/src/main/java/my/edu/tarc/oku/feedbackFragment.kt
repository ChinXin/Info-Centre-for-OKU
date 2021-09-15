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
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.data.Event
import my.edu.tarc.oku.data.EventAdapter
import my.edu.tarc.oku.data.Feedback
import my.edu.tarc.oku.data.FeedbackAdapter
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import my.edu.tarc.oku.databinding.FragmentFeedbackBinding
import java.util.*

class feedbackFragment : Fragment() {

    private lateinit var binding : FragmentFeedbackBinding
    private var feedbackList: MutableList<Feedback> = ArrayList()

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

        myRef.child(markerId).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(x in snapshot.children){ //marker id
                    for(y in x.children){ //username
                        //for(a in y.children){
                            val markerId = y.child("marker_id").value.toString()
                            val fDate = y.child("date").value.toString()
                            val fTime = y.child("time").value.toString()
                            val user = y.child("username").value.toString()
                            val feed = y.child("feedback").value.toString()

                            val get_feedback = Feedback(markerId,user,feed,fDate,fTime)

                            feedbackList.add(get_feedback)
                            Log.i("test1234","$feedbackList")
                        //}
                    }
                }

                val myRecyclerView: RecyclerView = binding.feedbackRecycleView
                myRecyclerView.adapter = FeedbackAdapter(feedbackList)
                myRecyclerView.setHasFixedSize(true)
            }

            override fun onCancelled(error: DatabaseError) {}

        })

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