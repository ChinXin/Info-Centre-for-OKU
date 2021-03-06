package my.edu.tarc.oku

import android.app.*
import android.content.Context.ALARM_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.data.Event
import my.edu.tarc.oku.data.EventRegistration
import my.edu.tarc.oku.data.UserSessionManager
import my.edu.tarc.oku.databinding.EventInfoBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import android.graphics.Color
import android.util.Base64


class MemberEventInfo : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var binding: EventInfoBinding

    private val myRef = Firebase.database.getReference("state")
    private val myReg = Firebase.database.getReference("register")

    private lateinit var session: UserSessionManager
    private lateinit var user: HashMap<String?, String?>
    private lateinit var username: String
    private lateinit var status: String

    //Event
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

    private var tts: TextToSpeech? = null
    private val MY_DATA_CHECK_CODE = 1234
    private lateinit var alarmManager: AlarmManager
    private var registerEventListener: ValueEventListener? = null
    private var infoValueEventListener: ValueEventListener? = null
    private lateinit var args:MemberEventInfoArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.event_info, container, false)
        createNotificationChannel()

        session = UserSessionManager(requireContext().applicationContext)
        user = session.userDetails
        username = user[UserSessionManager.KEY_NAME].toString()
        status = user[UserSessionManager.KEY_STATUS].toString()

        alarmManager = requireActivity().getSystemService(ALARM_SERVICE) as AlarmManager

        args = MemberEventInfoArgs.fromBundle(requireArguments())

        binding.btnTTS.visibility = View.VISIBLE

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
                            val bitmap = Base64.decode(img, Base64.DEFAULT)
                            Glide.with(requireContext().applicationContext)
                                .asBitmap()
                                .load(bitmap)
                                .fitCenter()// scale to fit entire image within ImageView
                                .into(binding.imageView3)
                            binding.tvTitle.text = title
                            binding.tvTimeDate.text =
                                "Date/Time: $date, $time"
                            binding.tvPhoneNo.text =
                                "Contact: $phone"
                            binding.tvAddress.text =
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

        binding.btnTTS.setOnClickListener {
            tts!!.speak(description, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        binding.tvLink.setOnClickListener {
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                link = "http://" + link
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(browserIntent)
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                tts = TextToSpeech(context, this)
                tts!!.language = Locale.US
                tts!!.setPitch(0.9f)
                tts!!.setSpeechRate(0.8f)
                binding.btnTTS.isEnabled = true
            } else {
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                tts!!.language = Locale.getDefault()
                binding.btnTTS.isEnabled = true
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        registerEventListener = myReg.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (e in snapshot.children) {
                        if (e.key.toString() == args.eventId) {
                            if (e.hasChild(username)) {
                                setHasOptionsMenu(false)
                            }
                            else {
                                setHasOptionsMenu(true)
                            }
                            break
                        } else {
                            setHasOptionsMenu(true)
                        }
                    }
                }
                else {
                    setHasOptionsMenu(true)
                }

            }

            override fun onCancelled(error: DatabaseError) {}
        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.event, menu)
        menu.findItem(R.id.btnAdd).isEnabled = false
        menu.findItem(R.id.btnDelete).isVisible = false
        menu.findItem(R.id.btnRegisterE)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.findItem(R.id.btnRegisterE).isVisible = true

        super.onCreateOptionsMenu(menu, inflater)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnRegisterE -> {
                val currentDateTime = LocalDateTime.now()
                val currentDate = currentDateTime.format(DateTimeFormatter.ISO_DATE)
                val currentTime =
                    currentDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                val registerE = EventRegistration(username, currentDate, currentTime)

                val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
                builder.setTitle("Register Event")
                builder.setMessage("Are you sure you want to register for this event?")

                builder.setPositiveButton("Yes") { which, dialog ->
                    myReg.child(eventId).child(username).setValue(registerE).addOnSuccessListener {
                        Toast.makeText(context, "Register successfully!", Toast.LENGTH_LONG).show()
                        setHasOptionsMenu(false)
                        val reminderMessage =
                            "Hey! Your upcoming events $title\nWhen: $date, $time\nWhere: $address"
                        val reminderImage = Base64.decode(img, Base64.DEFAULT)
                        val reminderRequestCode = (Math.random() * (999999 - 1 + 1)).toInt() + 1

                        val dateArray = date.split("-").toTypedArray()
                        val timeArray = time.split(Regex(":| ")).toTypedArray()
                        if (timeArray[2] == "PM") {
                            timeArray[0] = (timeArray[0].toInt() + 12).toString()
                        }
                        val dateTime = LocalDateTime.of(
                            dateArray[2].toInt(),
                            dateArray[1].toInt(),
                            dateArray[0].toInt(),
                            timeArray[0].toInt(),
                            timeArray[1].toInt(),
                            0
                        )
                        val newDateTime = dateTime.minusDays(3).minusMinutes(1)
                        val cal = Calendar.getInstance()
                        cal.set(
                            newDateTime.year,
                            newDateTime.monthValue - 1,
                            newDateTime.dayOfMonth,
                            newDateTime.hour,
                            newDateTime.minute
                        )

                        val intent = Intent(context, ReminderBroadcast::class.java)
                        intent.putExtra("remindMessage", reminderMessage)
                        intent.putExtra("remindImage", reminderImage)
                        intent.putExtra("remindRequestCode", reminderRequestCode)

                        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                            context,
                            reminderRequestCode,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        val alarmManager: AlarmManager =
                            requireContext().getSystemService(ALARM_SERVICE) as AlarmManager

                        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                    }
                }

                builder.setNegativeButton("No") { which, dialog -> }

                builder.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        // Fire off an intent to check if a TTS engine is installed
        val checkIntent = Intent()
        checkIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE)
        super.onStart()
    }

    override fun onPause() {
        if (registerEventListener != null) {
            myReg.removeEventListener(registerEventListener!!)
        }
        if (infoValueEventListener != null) {
            myRef.removeEventListener(infoValueEventListener!!)
        }
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onPause()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "OKUEventReminderChannel"
            val description = "Channel for OKU Event Reminder"
            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("notifyEvent", name, importance)
            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.GREEN
            channel.enableVibration(true)

            val notificationManager: NotificationManager =
                requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}