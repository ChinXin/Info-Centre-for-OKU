package my.edu.tarc.oku

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import my.edu.tarc.oku.data.Event
import my.edu.tarc.oku.data.EventAdapter
import my.edu.tarc.oku.data.UserSessionManager
import my.edu.tarc.oku.databinding.FragmentAdminEventBinding
import okhttp3.internal.buildCache
import okhttp3.internal.cacheGet
import java.util.*
import kotlin.collections.ArrayList

class AdminEvent : Fragment() {
    private lateinit var binding: FragmentAdminEventBinding
    private var eventList: MutableList<Event> = ArrayList()
    private lateinit var session: UserSessionManager
    private var valueEventListener: ValueEventListener? = null
    val database = Firebase.database
    val myRef = database.getReference("state")
    private val job = Job()
    private val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
    private val scopeIO = CoroutineScope(job + Dispatchers.IO)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        session = UserSessionManager(requireContext().applicationContext)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin_event, container, false)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.root.findNavController().navigate(R.id.homeAdminFragment)
            }
        })

        scopeIO.launch {
            valueEventListener = myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    eventList.clear()
                    for (s in snapshot.children) {
                        for (e in s.child("Events").children) {
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
                    scopeMainThread.launch {
                        val myRecyclerView: RecyclerView = binding.eventRecycleView
                        myRecyclerView.adapter = EventAdapter(eventList)
                        myRecyclerView.setHasFixedSize(true)
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
                    myRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (s in snapshot.children){
                                for(e in s.child("Events").children) {
                                    if (e.child("title").value.toString().lowercase().contains(search)) {
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
                            myRecyclerView.adapter = EventAdapter(eventList)
                            myRecyclerView.setHasFixedSize(true)

                        }

                        override fun onCancelled(error: DatabaseError) {}

                    })
                }
                else{
                    eventList.clear()
                    binding.tvNotFound.visibility = View.INVISIBLE
                    myRef.addValueEventListener(valueEventListener!!)
                }

                return false
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val user = session.userDetails
        val status = user[UserSessionManager.KEY_STATUS]
        if (status == "admin") {
            setHasOptionsMenu(true)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        if (valueEventListener != null) {
            myRef.removeEventListener(valueEventListener!!)
        }
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.event, menu)
        menu.findItem(R.id.btnAdd).isVisible = true
        menu.findItem(R.id.btnAdd).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
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