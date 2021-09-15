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
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.callbackFlow
import my.edu.tarc.oku.data.Event
import my.edu.tarc.oku.data.UserSessionManager
import java.util.*
import kotlin.collections.HashMap


class EventInfo : Fragment() {
    private lateinit var binding: FragmentEventInfoBinding
    private lateinit var session: UserSessionManager
    private lateinit var user: HashMap<String?, String?>
    private lateinit var status: String
    private lateinit var event: Event
    val myRef = Firebase.database.getReference("state" )
    private val storage = Firebase.storage.getReference("EventImage")
    private var eventId: String = ""
    private var title: String = ""
    private var date: String = ""
    private var time: String = ""
    private var address: String = ""
    private var img: String = ""
    private var description: String = ""
    private var link: String = ""
    private var phone: String = ""
    var state: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
//            Navigation.findNavController().navigate()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_info, container, false)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.root.findNavController().navigate(R.id.adminEvent)
            }
        })

        session = UserSessionManager(requireContext().applicationContext)
        user = session.userDetails
        status = user[UserSessionManager.KEY_STATUS].toString()
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_info, container, false)

        val args = EventInfoArgs.fromBundle(requireArguments())

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (s in snapshot.children) {//for each state
                    for (e in s.child("Events").children) {//event in events
                        if (e.key.toString() == args.eventID) {
                            eventId = e.child("id").value.toString()
                            img = e.child("image").value.toString()
                            title = e.child("title").value.toString()
                            date = e.child("date").value.toString()
                            time = e.child("time").value.toString()
                            address = e.child("address").value.toString()
                            description = e.child("description").value.toString()
                            link = e.child("link").value.toString()
                            state = e.child("state").value.toString()
                            phone = e.child("phone").value.toString()
                            event = Event(
                                eventId,
                                img,
                                title,
                                date,
                                time,
                                address,
                                state,
                                description,
                                link,
                                phone
                            )
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
            binding.edit.setOnClickListener { _ ->
                val action = EventInfoDirections.actionEventInfoToAdminEditEvent(eventId)
                binding.root.findNavController().navigate(action)
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
        menu.findItem(R.id.btnAdd).isEnabled = false
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
                var snackBar: Snackbar
                myRef.child(state).child("Events").child(eventId).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Deleted Successfully!", Toast.LENGTH_SHORT).show()
                        binding.root.findNavController()
                            .navigate(R.id.action_eventInfo_to_adminEvent)
                        snackBar = Snackbar.make(binding.root,"Deleted Successfully!",Snackbar.LENGTH_LONG)
                            .setAction("UNDO", View.OnClickListener {
                                myRef.child(state).child("Events").child(eventId)
                                    .setValue(event)
                                    .addOnSuccessListener { _ ->
                                        Toast.makeText(
                                            context,
                                            "Event restore successfully!!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Unable to restore event.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            })
                            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onShown(transientBottomBar: Snackbar?) {
                                    super.onShown(transientBottomBar)
                                }

                                override fun onDismissed(transientBottomBar: Snackbar?,event: Int) {
                                    super.onDismissed(transientBottomBar, event)
                                    if(event != 1){ //Indicates that the Snackbar was dismissed via an action click.
                                        storage.child("$eventId.png").delete()
                                    }
                                }
                            })

                        snackBar.show()
                    }
//                binding.root.findNavController().navigate(R.id.action_adminEvent_to_adminAddEvent)
//                myRef.child(state)
//                Toast.makeText(context, "delete", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

}