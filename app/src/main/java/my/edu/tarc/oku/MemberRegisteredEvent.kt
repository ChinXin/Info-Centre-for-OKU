package my.edu.tarc.oku

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
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
import my.edu.tarc.oku.data.*
import my.edu.tarc.oku.databinding.FragmentAdminEventBinding


class MemberRegisteredEvent : Fragment() {
    private lateinit var binding: FragmentAdminEventBinding

    private val myRef = Firebase.database.getReference("state")
    private val myReg = Firebase.database.getReference("register")
    private lateinit var session: UserSessionManager
    private lateinit var user: HashMap<String?, String?>
    private lateinit var username:String
    private lateinit var status: String
    private var registeredList: MutableList<String> = ArrayList()
    private var eventList: MutableList<Event> = ArrayList()
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin_event, container, false )

        session = UserSessionManager(requireContext().applicationContext)
        user = session.userDetails
        username = user[UserSessionManager.KEY_NAME].toString()
        status = user[UserSessionManager.KEY_STATUS].toString()
        eventList.clear()
        CoroutineScope(IO).launch {
            valueEventListener = myReg.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    eventList.clear()
                    for (r in snapshot.children) {
                        if(r.hasChild(username)){
                            myRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (s in snapshot.children) {
                                        for (e in s.child("Events").children) {
                                            if (e.key.toString() == r.key.toString()){
                                                binding.tvNotFound.visibility = View.INVISIBLE
                                                val getId = e.key.toString()
                                                val title = e.child("title").value.toString()
                                                val date = e.child("date").value.toString()
                                                val time = e.child("time").value.toString()
                                                val address = e.child("address").value.toString()
                                                val state = e.child("state").value.toString()
                                                val description = e.child("description").value.toString()
                                                val image = e.child("image").value.toString()
                                                val link = e.child("link").value.toString()
                                                val phone = e.child("phone").value.toString()
                                                val event = Event(getId, image, title, date, time, address, state, description, link, phone)
                                                eventList.add(event)
                                            }
                                        }
                                    }
                                    if(eventList.isEmpty()){
                                        binding.tvNotFound.visibility = View.VISIBLE
                                    }
                                    CoroutineScope(Main).launch {
                                        val myRecyclerView: RecyclerView = binding.eventRecycleView
                                        myRecyclerView.adapter = MemberRegisteredEventAdapter(eventList)
                                        myRecyclerView.setHasFixedSize(true)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        binding.searchE.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(valueEventListener != null){
                    myRef.removeEventListener(valueEventListener!!)
                }
                var search = ""
                if (newText != null && newText.trim().isNotEmpty()) {
                    eventList.clear()
                    val letters: CharArray = newText.toCharArray()
                    val firstLetter = letters[0].toString().lowercase()
                    val remainingLetters: String = newText.substring(1)
                    search = "$firstLetter$remainingLetters"

                    myReg.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            eventList.clear()
                            for (r in snapshot.children) {
                                if(r.hasChild(username)){
                                    myRef.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (s in snapshot.children) {
                                                for (e in s.child("Events").children) {
                                                    if (e.child("title").value.toString().lowercase().contains(search) && (e.key.toString() == r.key.toString())) {
                                                        binding.tvNotFound.visibility = View.INVISIBLE
                                                        val getId = e.key.toString()
                                                        val title = e.child("title").value.toString()
                                                        val date = e.child("date").value.toString()
                                                        val time = e.child("time").value.toString()
                                                        val address = e.child("address").value.toString()
                                                        val state = e.child("state").value.toString()
                                                        val description = e.child("description").value.toString()
                                                        val image = e.child("image").value.toString()
                                                        val link = e.child("link").value.toString()
                                                        val phone = e.child("phone").value.toString()
                                                        val event = Event(getId, image, title, date, time, address, state, description, link, phone)
                                                        eventList.add(event)
                                                    }
                                                }
                                            }
                                            if(eventList.isEmpty()){
                                                binding.tvNotFound.visibility = View.VISIBLE
                                            }
                                            val myRecyclerView: RecyclerView = binding.eventRecycleView
                                            myRecyclerView.adapter = MemberRegisteredEventAdapter(eventList)
                                            myRecyclerView.setHasFixedSize(true)
                                        }

                                        override fun onCancelled(error: DatabaseError) {}

                                    })
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
                else{
                    eventList.clear()
                    binding.tvNotFound.visibility = View.INVISIBLE
                }
                return false
            }
        })

        return binding.root
    }
    override fun onPause() {
        eventList.clear()
        if (valueEventListener != null) {
            myRef.removeEventListener(valueEventListener!!)
        }
        super.onPause()
    }

}