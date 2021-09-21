package my.edu.tarc.oku

import android.app.AlertDialog
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
import my.edu.tarc.oku.databinding.EventInfoBinding
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import my.edu.tarc.oku.data.Event
import my.edu.tarc.oku.data.UserSessionManager
import kotlin.collections.HashMap


class AdminEventInfo : Fragment() {
    private lateinit var binding: EventInfoBinding
    val myRef = Firebase.database.getReference("state")

    private lateinit var session: UserSessionManager
    private lateinit var user: HashMap<String?, String?>
    private lateinit var username: String
    private lateinit var status: String

    private lateinit var event: Event
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
    private var infoValueEventListener: ValueEventListener? = null

    private val job = Job()
    private val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
    private val scopeIO = CoroutineScope(job + Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.event_info, container, false)

        session = UserSessionManager(requireContext().applicationContext)
        user = session.userDetails
        username = user[UserSessionManager.KEY_NAME].toString()
        status = user[UserSessionManager.KEY_STATUS].toString()

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    binding.root.findNavController().navigate(R.id.adminEvent)
                }
            })

        binding = DataBindingUtil.inflate(inflater, R.layout.event_info, container, false)

        val args = AdminEventInfoArgs.fromBundle(requireArguments())
        scopeIO.launch {
            infoValueEventListener = myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (s in snapshot.children) {
                        for (e in s.child("Events").children) {
                            if (e.key.toString() == args.eventId) {
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
                                scopeMainThread.launch {
                                    val bitmap = Base64.decode(img, Base64.DEFAULT)
                                    Glide.with(requireContext().applicationContext)
                                        .asBitmap()
                                        .load(bitmap)
                                        .fitCenter()
                                        .into(binding.imageView3)
                                    binding.tvTitle.text = title
                                    binding.tvTimeDate.text = "Date/Time: $date, $time"
                                    binding.tvPhoneNo.text = "Contact: $phone"
                                    binding.tvAddress.text = "Address: $address"
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
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        }
        binding.tvLink.setOnClickListener {
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                link = "http://" + link
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(browserIntent)
        }

        if (status == "admin") {
            binding.edit.visibility = View.VISIBLE
            binding.edit.setOnClickListener {
                val action = AdminEventInfoDirections.actionAdminEventInfoToAdminEditEvent(eventId)
                binding.root.findNavController().navigate(action)
            }
        }
        return binding.root
    }

    override fun onPause() {
        if (infoValueEventListener != null) {
            myRef.removeEventListener(infoValueEventListener!!)
        }
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.event, menu)
        menu.findItem(R.id.btnAdd).isEnabled = false
        menu.findItem(R.id.btnDelete).isVisible = true
        menu.findItem(R.id.btnParticipants).isVisible = true

        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnDelete -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
                builder.setTitle("Delete Event")
                builder.setMessage("Are you sure you want to delete this event?")

                builder.setPositiveButton("Yes") { which, dialog ->
                    var snackBar: Snackbar
                    myRef.child(state).child("Events").child(eventId).removeValue()
                        .addOnSuccessListener {
                            binding.root.findNavController()
                                .navigate(R.id.action_adminEventInfo_to_adminEvent)
                            snackBar = Snackbar.make(
                                binding.root,
                                "Event deleted successfully!",
                                Snackbar.LENGTH_LONG
                            )
                                .setAction("UNDO", View.OnClickListener {
                                    myRef.child(state).child("Events").child(eventId)
                                        .setValue(event)
                                        .addOnSuccessListener {
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
                                .addCallback(object :
                                    BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                    override fun onShown(transientBottomBar: Snackbar?) {
                                        super.onShown(transientBottomBar)
                                    }

                                    override fun onDismissed(
                                        transientBottomBar: Snackbar?,
                                        event: Int
                                    ) {
                                        super.onDismissed(transientBottomBar, event)
                                    }
                                })

                            snackBar.show()
                        }
                }

                builder.setNegativeButton("No") { which, dialog -> }

                builder.show()
                true
            }
            R.id.btnParticipants -> {
                val action =
                    AdminEventInfoDirections.actionAdminEventInfoToAdminEventParticipant(eventId)
                binding.root.findNavController()
                    .navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}