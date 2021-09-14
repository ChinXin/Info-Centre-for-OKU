package my.edu.tarc.oku

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import java.util.*

import android.app.TimePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.Navigation

import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.data.Event
import my.edu.tarc.oku.databinding.FragmentAdminAddEventBinding

import android.content.ContentResolver
import android.util.Log
import android.util.Patterns
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.view.isVisible
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage


class AdminAddEvent : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentAdminAddEventBinding
    private val myRef = Firebase.database.getReference("state")
    private val storage = Firebase.storage.getReference("EventImage")
    private var link: String = "N/A"

    //Image
    private var savedImgUri: String? = null
    private var newImg: Boolean = false
    private var imgUri: Uri? = null

    //System time
    private lateinit var c: Calendar
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private var mHour: Int = 0
    private var mMinute: Int = 0

    //Selected time
    private var month = 0
    private var day = 0
    private var hour = 0
    private var minutes = 0
    private var timeSet = ""

    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_admin_add_event, container, false)

        val stateList = resources.getStringArray(R.array.stateList)
        val adapter = ArrayAdapter(this.requireContext(), R.layout.dropdown_state, stateList)
        binding.eStateList.setAdapter(adapter)
        setupListener()

        binding.btnDate.setOnClickListener(this)
        binding.btnTime.setOnClickListener(this)

        binding.imgEvent.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            launchGallery.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            val title = binding.eTitle.text.toString()
            val date = binding.btnDate.text.toString()
            val time = binding.btnTime.text.toString()
            val address = binding.eAddress.text.toString()
            val state = binding.eStateList.text.toString()
            val description = binding.description.text.toString()
            var totalEvents = 0

            if (isValidate()) {
                valueEventListener = myRef.addValueEventListener(object : ValueEventListener {
                    var exist = false
                    var eventId: String = ""

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (s in snapshot.children) {//for each state
                            for (e in s.child("Events").children) {//event in events
                                totalEvents++
                                if (e.key.toString() == eventId) {
                                    exist = true
                                }
                            }
                        }
                        eventId = "E${totalEvents + 1}"
                        if (!exist) {
                            val fileRef: StorageReference =
                                storage.child("$eventId.${getFileExtension(imgUri!!)}")
                            fileRef.putFile(imgUri!!).addOnSuccessListener(object:OnSuccessListener<UploadTask.TaskSnapshot> {
                                override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                                    fileRef.downloadUrl.addOnSuccessListener {
                                        savedImgUri = it.toString()
                                        val event = Event(
                                            eventId,
                                            savedImgUri.toString(),
                                            title,
                                            date,
                                            time,
                                            address,
                                            state,
                                            description,
                                            link
                                        )
                                        myRef.child(state).child("Events").child(eventId)
                                            .setValue(event)
                                            .addOnSuccessListener { _ ->
                                                binding.progressBarHolder.visibility = View.INVISIBLE
                                                Toast.makeText(
                                                    context,
                                                    "Event added successfully!!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }.addOnFailureListener {
                                                Toast.makeText(
                                                    context,
                                                    "Unable to add event.",
                                                    Toast.LENGTH_LONG
                                                )
                                                    .show()
                                            }
                                    }

                                }

                            }).addOnProgressListener {
                                binding.progressBarHolder.visibility = View.VISIBLE
                            }.addOnFailureListener {
                                binding.progressBarHolder.visibility = View.INVISIBLE
                                Toast.makeText(context, "Uploading Fail", Toast.LENGTH_LONG)
                                    .show()
                            }
                        } else {
                            Navigation.findNavController(it)
                                .navigate(R.id.action_adminAddEvent_to_adminEvent)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }

        return binding.root
//        return inflater.inflate(R.layout.fragment_admin_event, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (valueEventListener != null) {
            myRef.removeEventListener(valueEventListener!!)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        if (v === binding.btnDate) {

            // Get Current Date
            c = Calendar.getInstance()
            mYear = c.get(Calendar.YEAR)
            mMonth = c.get(Calendar.MONTH)
            mDay = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(), { _, year, monthOfYear, dayOfMonth ->
                    day = dayOfMonth
                    month = monthOfYear
                    binding.btnDate.text = day.toString() + "-" + (month + 1) + "-" + year
                }, mYear, mMonth, mDay
            )
            datePickerDialog.datePicker.minDate = c.timeInMillis
            c.add(Calendar.DATE, 90)
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
        if (v === binding.btnTime) {

            // Get Current Time
            c = Calendar.getInstance()
            mHour = c.get(Calendar.HOUR_OF_DAY)
            mMinute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(
                requireContext(), { _, hourOfDay, minute ->
                    when {
                        hourOfDay > 12 -> {
                            hour = hourOfDay - 12
                            timeSet = "PM"
                        }
                        hourOfDay == 0 -> {
                            hour = hourOfDay + 12
                            timeSet = "AM"
                        }
                        hourOfDay < 12 -> {
                            hour = hourOfDay
                            timeSet = "AM"
                        }
                        hourOfDay == 12 -> {
                            timeSet = "PM"
                        }
                    }
                    minutes = minute
                    if (minute < 10) {
                        binding.btnTime.text = "$hour:0$minutes $timeSet"
                    } else {
                        minutes = minute
                        binding.btnTime.text = "$hour:$minutes $timeSet"
                    }

                }, mHour, mMinute, false
            )

            timePickerDialog.show()
        }
    }

    private fun isValidate(): Boolean =
        validateURL() && validateImage() && validateTitle() && validateDate() && validateTime() && validateAddress() && validateState() && validateDescription()

    private fun setupListener() {
        binding.eTitle.addTextChangedListener(TextFieldValidation(binding.eTitle))
        binding.eAddress.addTextChangedListener(TextFieldValidation(binding.eAddress))
        binding.eStateList.addTextChangedListener(TextFieldValidation(binding.eStateList))
        binding.description.addTextChangedListener(TextFieldValidation(binding.description))
        binding.eLink.addTextChangedListener(TextFieldValidation(binding.eLink))
    }

    private fun validateImage(): Boolean {
        if (!newImg) {
            Toast.makeText(context, "Event Banner Is Required Field!", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun validateTitle(): Boolean {
        if (binding.eTitle.text.toString().trim().isEmpty()) {
            binding.titleLayout.error = "Required Field!"
            binding.eTitle.requestFocus()
            return false
        } else {
            binding.titleLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateDate(): Boolean {
        if (binding.btnDate.text.toString() == "Select Date") {
            Toast.makeText(context, "Event Date Is Required Field!", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun validateTime(): Boolean {
        var ap = ""
        when {
            mHour > 12 -> {
                mHour -= 12
                ap = "PM"
            }
            mHour == 0 -> {
                mHour += 12
                ap = "AM"
            }
            mHour < 12 -> {
                mHour = mHour
                ap = "AM"
            }
            mHour == 12 -> {
                ap = "PM"
            }
        }

        if (binding.btnTime.text.toString() == "Select Time") {
            Toast.makeText(context, "Event time is required field!", Toast.LENGTH_LONG).show()
            return false
        } else if (timeSet == "AM" && (hour < 6 || hour == 12) || timeSet == "PM" && hour > 9) {
            Toast.makeText(context, "Not logic event time.", Toast.LENGTH_LONG).show()
            return false
        } else if (mDay == day && mMonth == month) {
            if (((timeSet == "AM" && ap == "AM") && hour <= mHour) || (timeSet == "PM" && ap == "PM") && hour <= mHour) {
                if (minutes <= mMinute) {
                    Toast.makeText(
                        context,
                        "Event time should be later than current time.",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
            }
        }
        return true
    }

    private fun validateAddress(): Boolean {
        if (binding.eAddress.text.toString().trim().isEmpty()) {
            binding.eAddressLayout.error = "Required Field!"
            binding.eAddress.requestFocus()
            return false
        } else {
            binding.eAddressLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateState(): Boolean {
        if (binding.eStateList.text.toString().trim().isEmpty()) {
            binding.eStateLayout.error = "Required Field!"
            binding.eStateList.requestFocus()
            return false
        } else {
            binding.eStateLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateDescription(): Boolean {
        if (binding.description.text.toString().trim().isEmpty()) {
            binding.descriptionLayout.error = "Required Field!"
            binding.description.requestFocus()
            return false
        } else {
            binding.descriptionLayout.isErrorEnabled = false
        }
        return true
    }

    private var launchGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                imgUri = data?.data
                Glide.with(this)
                    .load(imgUri)
                    .override(356, 150)
                    .fitCenter() // scale to fit entire image within ImageView
                    .into(binding.imgEvent)
                newImg = true
            }

        }

    private fun validateURL(): Boolean {
        if (binding.eLink.text.toString().trim().isEmpty()) {
            link = "N/A"
            binding.eLinkLayout.error = "Required Field!"
            binding.eLink.requestFocus()
            return false
        } else {
            binding.eLinkLayout.isErrorEnabled = false
        }
        if (binding.eLink.text.toString().trim().isNotEmpty()) {
//            if (URLUtil.isValidUrl(binding.eLink.text.toString())){
            if (Patterns.WEB_URL.matcher(binding.eLink.text.toString()).matches()) {
                binding.eLinkLayout.isErrorEnabled = false
                link = binding.eLink.text.toString()
            } else {
                binding.eLinkLayout.error = "Invalid Website Link"
                binding.eLink.requestFocus()
                return false
            }
        }
        return true
    }

    private fun getFileExtension(mUri: Uri): String {
        val cr: ContentResolver = requireContext().contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(mUri)).toString()
    }

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable?) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
                R.id.eTitle -> {
                    validateTitle()
                }
                R.id.btnDate -> {
                    validateDate()
                }
                R.id.btnTime -> {
                    validateTime()
                }
                R.id.eAddress -> {
                    validateAddress()
                }
                R.id.eStateList -> {
                    validateState()
                }
                R.id.description -> {
                    validateDescription()
                }
                R.id.eLink -> {
                    validateURL()
                }
            }
        }
    }
}
