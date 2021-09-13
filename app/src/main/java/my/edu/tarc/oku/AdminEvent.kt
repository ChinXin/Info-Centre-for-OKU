package my.edu.tarc.oku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import my.edu.tarc.oku.data.Event
import my.edu.tarc.oku.data.EventAdapter
import my.edu.tarc.oku.databinding.FragmentAdminEventBinding

class AdminEvent : Fragment() {
    private lateinit var binding: FragmentAdminEventBinding
    private var eventList: MutableList<Event> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin_event, container, false)
        CoroutineScope(IO).launch {
            val database = Firebase.database
            val myRef = database.getReference("state")

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    eventList.clear()
                    for (s in snapshot.children) {//for each state
                        for (t in s.children) {//for each element in each state
                            if (t.key.toString() == "Events") {
                                for (e in s.child("Events").children) {//event in events
                                    val getId = e.key.toString()
                                    val title = e.child("title").value.toString()
                                    val date = e.child("date").value.toString()
                                    val time = e.child("time").value.toString()
                                    val address = e.child("address").value.toString()
                                    val state = e.child("state").value.toString()
                                    val description = e.child("description").value.toString()
                                    val image = e.child("image").value.toString()
                                    val link = e.child("link").value.toString()
                                    val event =
                                        Event(getId,image, title, date, time, address, state, description, link)
                                    eventList.add(event)

                                }
                            }
                        }
                    }
                    CoroutineScope(Main).launch {
                        val myRecyclerView: RecyclerView = binding.eventRecycleView
                        myRecyclerView.adapter = EventAdapter(eventList)
                        myRecyclerView.setHasFixedSize(true)
                    }

                }


                override fun onCancelled(error: DatabaseError) {}

            })
        }

//        return inflater.inflate(R.layout.fragment_admin_event, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.event_add, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnAdd -> {
                binding.root.findNavController().navigate(R.id.action_adminEvent_to_adminAddEvent)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }
}