package my.edu.tarc.oku

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
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
    var infoWindowListener: ValueEventListener? = null

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

        if(username.isBlank()){
            binding.linear1.visibility = View.GONE
        }

        myRef.child(markerId).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                feedbackList.clear()
                for(x in snapshot.children){ //username
                    //for(y in x.children){ //username
                        //for(a in y.children){
                            val markerId = x.child("marker_id").value.toString()
                            val fDate = x.child("date").value.toString()
                            val fTime = x.child("time").value.toString()
                            val user = x.child("username").value.toString()
                            val feed = x.child("feedback").value.toString()

                            val get_feedback = Feedback(markerId,user,feed,fDate,fTime)

                            feedbackList.add(get_feedback)
                            Log.i("test12345","Line 70 = $feedbackList")
                        //}
                    //}
                }

                val myRecyclerView: RecyclerView = binding.feedbackRecycleView
                myRecyclerView.adapter = FeedbackAdapter(feedbackList)
                myRecyclerView.setHasFixedSize(true)

                if(feedbackList.isEmpty()){
                    Toast.makeText(context,"There is no feedback by other users.",Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })

        binding.btnSubmit.setOnClickListener{

            val feedback = binding.feedback.text.toString()

            if(username == ""){
                Toast.makeText(context,"Please Log in Account and submit feedback!",Toast.LENGTH_LONG).show()
            }
            else{
                if(feedback.isNotEmpty()){
                    Log.i("test12345","Line 92")
                    val date = currentDateTime.format(DateTimeFormatter.ISO_DATE)
                    val time = currentDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                    val new_feedback = Feedback(markerId,username,feedback,date,time)

                    //check whether the user has give feedback or not
                    infoWindowListener = myRef.addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(infoWindowListener != null){
                                myRef.removeEventListener(infoWindowListener!!)
                            }
                            for(x in snapshot.children){
                                if(x.key == markerId){
                                    Log.i("test12345","Line 102")
                                    if(x.hasChild(username)){ ///marker id
                                        Log.i("test12345","Line 103")
                                        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                                        builder.setTitle("Feedback")
                                        builder.setMessage("You have submit feedback in this marker before, do you want to overwrite it?")

                                        builder.setCancelable(false)

                                        builder.setPositiveButton("Yes"){ which,dialog ->
                                            myRef.child(markerId).child(username).setValue(new_feedback).addOnSuccessListener {
                                                Toast.makeText(context,"Submit Successful!",Toast.LENGTH_LONG).show()
                                            }
                                        }

                                        builder.setNegativeButton("No"){which,dialog ->

                                        }

                                        builder.show()
                                        break
                                    }else{
                                        Log.i("test1234","Line 123")
                                        myRef.child(markerId).child(username).setValue(new_feedback).addOnSuccessListener {
                                            Toast.makeText(context,"Submit Successful!",Toast.LENGTH_LONG).show()
                                        }
                                        break
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}

                    })
                }
            }

        }
        return binding.root
    }
}