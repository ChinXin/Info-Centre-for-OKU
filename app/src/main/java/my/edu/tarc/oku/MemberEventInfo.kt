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
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.storage.ktx.storage
import my.edu.tarc.oku.data.Event
import my.edu.tarc.oku.data.EventRegistration
import my.edu.tarc.oku.data.UserSessionManager
import java.util.*
import kotlin.collections.HashMap
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class MemberEventInfo : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var binding: EventInfoBinding
    private lateinit var session: UserSessionManager
    private lateinit var user: HashMap<String?, String?>
    private lateinit var username:String
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
    lateinit var mTTS:TextToSpeech
    private lateinit var tts: TextToSpeech
    private val MY_DATA_CHECK_CODE = 1234

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.event_info, container, false)
        session = UserSessionManager(requireContext().applicationContext)
        user = session.userDetails
        username = user[UserSessionManager.KEY_NAME].toString()
        status = user[UserSessionManager.KEY_STATUS].toString()


        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.event_info, container, false)

        val args = MemberEventInfoArgs.fromBundle(requireArguments())

        if(status == "member"){
            binding.btnTTS.visibility = View.VISIBLE
        }

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (s in snapshot.children) {//for each state
                    for (e in s.child("Events").children) {//event in events
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

        binding.btnTTS.isEnabled = false
        // Fire off an intent to check if a TTS engine is installed
        // Fire off an intent to check if a TTS engine is installed
        val checkIntent = Intent()
        checkIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE)

//        mTTS = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
//            if (status != TextToSpeech.ERROR){
//                //if there is no error then set language
//                mTTS.language = Locale.UK
//            }
//        })

        binding.btnTTS.setOnClickListener {
            tts.speak(description, TextToSpeech.QUEUE_FLUSH, null,null);
        }

        binding.tvLink.setOnClickListener {
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                link = "http://" + link;
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(browserIntent)
        }

        return binding.root
//        return inflater.inflate(R.layout.fragment_event_info, container, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                tts = TextToSpeech(context, this)
                tts.setLanguage(Locale.US)
                tts.setPitch(0.9f)
                tts.setSpeechRate(0.8f)
                binding.btnTTS.setEnabled(true)
            } else {
                // missing data, install it
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            }
        }
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            } else {
                tts.setLanguage(Locale.getDefault())
                binding.btnTTS.isEnabled = true
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (status == "member") {
            setHasOptionsMenu(true)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.event, menu)
        menu.findItem(R.id.btnAdd).isEnabled = false
        val myReg = Firebase.database.getReference("register")
        if(status == "member"){
            myReg.addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value != null){
                        for (e in snapshot.children){
                            if(e.key.toString() == eventId ){
//                                Toast.makeText(context, "${e.key.toString()}", Toast.LENGTH_LONG).show()
//                                Toast.makeText(context, "${e.hasChild(username)}", Toast.LENGTH_LONG).show()
                                if (!e.hasChild(username)){
                                    menu.findItem(R.id.btnDelete).isEnabled = false
                                    menu.findItem(R.id.btnRegisterE).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                                    menu.findItem(R.id.btnRegisterE).isVisible = true
                                }else{
                                    setHasOptionsMenu(false)
                                }
                            }else{
                                menu.findItem(R.id.btnDelete).isEnabled = false
                                menu.findItem(R.id.btnRegisterE).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                                menu.findItem(R.id.btnRegisterE).isVisible = true
                            }

                        }
                    }else{
                        menu.findItem(R.id.btnDelete).isEnabled = false
                        menu.findItem(R.id.btnRegisterE).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                        menu.findItem(R.id.btnRegisterE).isVisible = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnRegisterE -> {
                val myReg = Firebase.database.getReference("register")

                val currentDateTime = LocalDateTime.now()
                val date = currentDateTime.format(DateTimeFormatter.ISO_DATE)
                val time = currentDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                val registerE = EventRegistration(username,date,time)
                val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
                builder.setTitle("Register Event")
                builder.setMessage("Are you sure you want to register for this event?")

                builder.setPositiveButton("Yes"){ which,dialog ->
                    myReg.child(eventId).child(username).setValue(registerE).addOnSuccessListener{
                        Toast.makeText(context,"Register successfully!", Toast.LENGTH_LONG).show()
                        setHasOptionsMenu(false)
                    }
                }

                builder.setNegativeButton("No"){ which,dialog ->

                }

                builder.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

}