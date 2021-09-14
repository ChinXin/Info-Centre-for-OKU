package my.edu.tarc.oku

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import my.edu.tarc.oku.data.Event
import my.edu.tarc.oku.data.EventAdapter
import my.edu.tarc.oku.databinding.FragmentEventInfoBinding

class EventInfo : Fragment() {
    private lateinit var binding: FragmentEventInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_info, container, false)

        val database = Firebase.database
        val myRef = database.getReference("state")
        val args = EventInfoArgs.fromBundle(requireArguments())

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (s in snapshot.children) {//for each state
                    for (e in s.child("Events").children) {//event in events
                        if (e.key.toString() == args.eventID) {
                            Glide.with(requireContext())
                                .load(e.child("image").value.toString())
                                .fitCenter() // scale to fit entire image within ImageView
                                .into(binding.imageView3)
                            binding.tvTitle.text = e.child("title").value.toString()
                            binding.tvTimeDate.text =
                                "${e.child("date").value.toString()}, ${e.child("time").value.toString()}"
                            binding.tvAddres.text = e.child("address").value.toString()
                            binding.tvDescription.text = e.child("description").value.toString()
                            val link = e.child("link").value.toString()
                            if(link != "N/A"){
                                binding.tvLink.visibility = View.VISIBLE
                                binding.tvLink.text = link
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })


        return binding.root
//        return inflater.inflate(R.layout.fragment_event_info, container, false)
    }


}