package my.edu.tarc.oku

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import my.edu.tarc.oku.data.Event
import my.edu.tarc.oku.data.EventAdapter
import my.edu.tarc.oku.data.EventRegistration
import my.edu.tarc.oku.data.EventRegistrationAdapter
import my.edu.tarc.oku.databinding.FragmentAdminEventParticipantBinding

class AdminEventParticipant : Fragment() {

    private lateinit var binding: FragmentAdminEventParticipantBinding
    val myReg = Firebase.database.getReference("register" )

    private var registerList: MutableList<EventRegistration> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin_event_participant, container, false )

        val args = AdminEventParticipantArgs.fromBundle(requireArguments())
        CoroutineScope(IO).launch {
            myReg.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    registerList.clear()
                    for (e in snapshot.children) {
                        if(e.key.toString() == args.eventId){
                            binding.tvNoParticipant.visibility = View.INVISIBLE
                            for (m in e.children) {
                                val username = m.key.toString()
                                val date = m.child("date").value.toString()
                                val time = m.child("time").value.toString()
                                val registeredM = EventRegistration(username, date, time)
                                registerList.add(registeredM)
                            }
                        }
                    }
                    if(registerList.isEmpty()){
                        binding.tvNoParticipant.visibility = View.VISIBLE
                    }
                    CoroutineScope(Main).launch {
                        val myRecyclerView: RecyclerView = binding.eventRecycleView
                        myRecyclerView.adapter = EventRegistrationAdapter(registerList)
                        myRecyclerView.setHasFixedSize(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        }
        return binding.root
    }

}