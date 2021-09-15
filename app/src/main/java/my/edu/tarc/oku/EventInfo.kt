package my.edu.tarc.oku

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import my.edu.tarc.oku.databinding.FragmentEventInfoBinding
import android.content.Intent
import android.net.Uri
import android.view.*
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import my.edu.tarc.oku.data.Event
import my.edu.tarc.oku.data.UserSessionManager
import java.util.*
import kotlin.collections.HashMap


class EventInfo : Fragment() {
    private lateinit var binding: FragmentEventInfoBinding
    private lateinit var session: UserSessionManager
    private lateinit var user:HashMap<String?,String?>
    private lateinit var status:String
    private lateinit var event:Event
    val myRef = Firebase.database.getReference("state")
    private lateinit var id:String
    private lateinit var title:String
    private lateinit var date:String
    private lateinit var time:String
    private lateinit var address:String
    private lateinit var img:String
    private lateinit var description:String
    private lateinit var link:String
    private lateinit var phone:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        session = UserSessionManager(requireContext().applicationContext)
        user = session.userDetails
        status = user[UserSessionManager.KEY_STATUS].toString()
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_info, container, false)


        val args = EventInfoArgs.fromBundle(requireArguments())
        var link = ""
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (s in snapshot.children) {//for each state
                    for (e in s.child("Events").children) {//event in events
                        if (e.key.toString() == args.eventID) {
                            val id = e.child("id").value.toString()
                            val img = e.child("image").value.toString()
                            val title = e.child("title").value.toString()
                            val date = e.child("date").value.toString()
                            val time = e.child("time").value.toString()
                            val address = e.child("address").value.toString()
                            val description = e.child("description").value.toString()
                            val link = e.child("link").value.toString()
                            val state = e.child("state").value.toString()
                            val phone = e.child("phone").value.toString()
                            event = Event(id, img, title, date, time, address, state, description, link,phone)
                            Glide.with(requireContext())
                                .load(img)
                                .fitCenter() // scale to fit entire image within ImageView
                                .into(binding.imageView3)
                            binding.tvTitle.text = title
                            binding.tvTimeDate.text =
                                "Date/Time: ${date}, ${time}"
                            binding.tvAddres.text =
                                "Address: $address"
                            binding.tvDescription.text = description
                            if (link != "N/A") {
                                binding.tvWebsite.visibility = View.VISIBLE
                                binding.tvLink.visibility = View.VISIBLE
                                binding.tvLink.text = link
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })

        binding.tvLink.setOnClickListener {
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                link = "http://" + link;
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(browserIntent)
        }


        if (status == "admin") {
            binding.edit.visibility = View.VISIBLE
            binding.edit.setOnClickListener { view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }
        return binding.root
//        return inflater.inflate(R.layout.fragment_event_info, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (status == "admin") {
            setHasOptionsMenu(true)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.event, menu)
        menu.findItem(R.id.btnDelete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        menu.findItem(R.id.btnDelete).isVisible = true

        super.onCreateOptionsMenu(menu, inflater)
    }
//    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
//        val item = menu.findItem(R.id.btnAdd)
//
//        menu.findItem(R.id.btnAdd).isVisible = false
////        menu.findItem(R.id.btnDelete)
//            // You can also use something like:
//            // menu.findItem(R.id.example_foobar).setEnabled(false);
//
//        return true
//    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnDelete -> {
//                binding.root.findNavController().navigate(R.id.action_adminEvent_to_adminAddEvent)
//                myRef.child(state)
                Toast.makeText(context, "delete", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

}