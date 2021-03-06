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
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.data.Event
import android.util.Patterns
import androidx.navigation.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import my.edu.tarc.oku.databinding.FragmentAdminEditEventBinding
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern


class AdminEditEvent : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentAdminEditEventBinding
    private val myRef = Firebase.database.getReference("state")

    //Image
    private var newImg: Boolean = false
    private var imgUri: Uri? = null
    private lateinit var strImg: String

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

    //event
    private lateinit var eventId: String
    private lateinit var title: String
    private lateinit var date: String
    private lateinit var time: String
    private lateinit var address: String
    private lateinit var img: String
    private lateinit var description: String
    private lateinit var state: String
    private lateinit var phone: String
    private lateinit var link: String

    private var addValueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin_edit_event, container, false)

        val args = AdminEditEventArgs.fromBundle(requireArguments())

        val stateList = resources.getStringArray(R.array.stateList)
        val adapter = ArrayAdapter(this.requireContext(), R.layout.dropdown_state, stateList)

        addValueEventListener = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (s in snapshot.children) {
                    for (e in s.child("Events").children) {
                        if (args.eventId == e.key.toString()) {
                            eventId = e.key.toString()
                            img = e.child("image").value.toString()
                            val bitmap = Base64.decode(img, Base64.DEFAULT)
                            Glide.with(requireView())
                                .asBitmap()
                                .load(bitmap)
                                .override(356, 100)
                                .fitCenter()
                                .into(binding.imgEventE)
                            binding.eTitleE.setText(e.child("title").value.toString())
                            binding.btnDateE.text = e.child("date").value.toString()
                            binding.btnTimeE.text = e.child("time").value.toString()
                            binding.eAddressE.setText(e.child("address").value.toString())

                            state = e.child("state").value.toString()
                            binding.eStateListE.setText(state, false)

                            binding.descriptionE.setText(e.child("description").value.toString())
                            binding.eLinkE.setText(e.child("link").value.toString())
                            binding.ePhoneE.setText(e.child("phone").value.toString())
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })

        binding.eStateListE.setAdapter(adapter)
        setupListener()

        binding.btnDateE.setOnClickListener(this)
        binding.btnTimeE.setOnClickListener(this)

        binding.imgEventE.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            launchGallery.launch(intent)
        }

        binding.btnSaveE.setOnClickListener {
            if (addValueEventListener != null) {
                myRef.removeEventListener(addValueEventListener!!)
            }

            title = binding.eTitleE.text.toString()
            date = binding.btnDateE.text.toString()
            time = binding.btnTimeE.text.toString()
            address = binding.eAddressE.text.toString()
            val newState = binding.eStateListE.text.toString()
            description = binding.descriptionE.text.toString()
            phone = binding.ePhoneE.text.toString()

            if (isValidate()) {
                binding.imgEventE.isEnabled = false
                binding.eTitleE.isEnabled = false
                binding.btnDateE.isEnabled = false
                binding.btnTimeE.isEnabled = false
                binding.eAddressE.isEnabled = false
                binding.eStateListE.isEnabled = false
                binding.descriptionE.isEnabled = false
                binding.eLinkE.isEnabled = false
                binding.ePhoneE.isEnabled = false
                binding.progressBarHolderE.visibility = View.VISIBLE

                val action = AdminEditEventDirections.actionAdminEditEventToAdminEventInfo(eventId)
                if (state == newState) {
                    if (newImg) {
                        val event = Event(eventId, strImg, title, date, time, address, state, description, link, phone)
                        myRef.child(state).child("Events").child(eventId)
                            .setValue(event)
                            .addOnSuccessListener { _ ->
                                binding.progressBarHolderE.visibility = View.INVISIBLE
                                Toast.makeText( context,"Event updated successfully!!", Toast.LENGTH_LONG).show()
                                binding.root.findNavController().navigate(action)
                            }.addOnFailureListener {
                                Toast.makeText(context,"Unable to update event.", Toast.LENGTH_LONG).show()
                            }

                    } else {
                        val event = Event(eventId, img, title, date, time, address, state, description, link, phone )
                        myRef.child(state).child("Events").child(eventId)
                            .setValue(event)
                            .addOnSuccessListener { _ ->
                                binding.progressBarHolderE.visibility = View.INVISIBLE
                                Toast.makeText( context, "Event updated successfully!!", Toast.LENGTH_LONG ).show()
                                binding.root.findNavController().navigate(action)
                            }.addOnFailureListener {
                                Toast.makeText(context,"Unable to update event.",Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    if (newImg) {
                        val event = Event(eventId, strImg, title, date, time, address, newState, description, link, phone)
                        myRef.child(state).child("Events").child(eventId).removeValue()
                            .addOnSuccessListener {
                                myRef.child(newState).child("Events").child(eventId)
                                    .setValue(event)
                                    .addOnSuccessListener { _ ->
                                        binding.progressBarHolderE.visibility =
                                            View.INVISIBLE
                                        Toast.makeText(context,"Event update successfully!!", Toast.LENGTH_LONG).show()
                                        binding.root.findNavController().navigate(action)
                                    }.addOnFailureListener {
                                        Toast.makeText(context,"Unable to update event.", Toast.LENGTH_LONG).show()
                                    }
                            }
                    } else {
                        val event = Event(eventId, img, title, date, time, address, newState, description, link, phone)
                        myRef.child(state).child("Events").child(eventId).removeValue()
                            .addOnSuccessListener {
                                myRef.child(newState).child("Events").child(eventId)
                                    .setValue(event)
                                    .addOnSuccessListener { _ ->
                                        binding.progressBarHolderE.visibility = View.INVISIBLE
                                        Toast.makeText( context,"Event updated successfully!!", Toast.LENGTH_LONG).show()
                                        binding.root.findNavController().navigate(action)
                                    }.addOnFailureListener {
                                        Toast.makeText(context,"Unable to update event.", Toast.LENGTH_LONG).show()
                                    }
                            }
                    }
                }


            }
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        if (v === binding.btnDateE) {

            // Get Current Date
            c = Calendar.getInstance()
            mYear = c.get(Calendar.YEAR)
            mMonth = c.get(Calendar.MONTH)
            mDay = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    day = dayOfMonth
                    month = monthOfYear + 1
                    binding.btnDateE.text = "$day-$month-$year"
                },
                mYear, mMonth, mDay,
            )
            datePickerDialog.datePicker.minDate = c.timeInMillis
            c.add(Calendar.DATE, 90)
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
        if (v === binding.btnTimeE) {

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
                            hour = hourOfDay
                            timeSet = "PM"
                        }
                    }
                    minutes = minute
                    if (minute < 10) {
                        binding.btnTimeE.text = "$hour:0$minutes $timeSet"
                    } else {
                        binding.btnTimeE.text = "$hour:$minutes $timeSet"
                    }

                }, mHour, mMinute, false
            )

            timePickerDialog.show()
        }
    }

    private fun isValidate(): Boolean =
        validateImage() && validateTitle() && validateDate() && validateTime() && validatePhone() && validateAddress() && validateState() && validateDescription() && validateURL()

    private fun setupListener() {
        binding.eTitleE.addTextChangedListener(TextFieldValidation(binding.eTitleE))
        binding.eAddressE.addTextChangedListener(TextFieldValidation(binding.eAddressE))
        binding.eStateListE.addTextChangedListener(TextFieldValidation(binding.eStateListE))
        binding.descriptionE.addTextChangedListener(TextFieldValidation(binding.descriptionE))
        binding.eLinkE.addTextChangedListener(TextFieldValidation(binding.eLinkE))
        binding.ePhoneE.addTextChangedListener(TextFieldValidation(binding.ePhoneE))
    }

    private fun validateImage(): Boolean {
        if (newImg){
            //convert bitmap to string
            val bitmap = (binding.imgEventE.drawable as BitmapDrawable).bitmap
            val byteArray = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArray)
            strImg = Base64.encodeToString(byteArray.toByteArray(), Base64.DEFAULT)
        }
        return true
    }

    private fun validateTitle(): Boolean {
        if (binding.eTitleE.text.toString().trim().isEmpty()) {
            binding.titleELayout.error = "Required Field!"
            binding.eTitleE.requestFocus()
            return false
        } else {
            binding.titleELayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateDate(): Boolean {
        if (binding.btnDateE.text.toString() == "Select Date") {
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

        if (binding.btnTimeE.text.toString() == "Select Time") {
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

    private fun validatePhone(): Boolean {
        val REG = "^(01)([0-9]{8,9})\$"
        val REG1 = "^(03)([0-9]{8})\$"

        if (binding.ePhoneE.text.toString().trim().isEmpty()) {
            binding.ePhoneELayout.error = "Required Field!"
            binding.ePhoneE.requestFocus()
            return false
        } else if (!Pattern.compile(REG).matcher(binding.ePhoneE.text.toString()).matches()) {
            return if (!Pattern.compile(REG1).matcher(binding.ePhoneE.text.toString()).matches()) {
                binding.ePhoneELayout.error = "Invalid Phone Number! e.g 0123456789 / 0358764321"
                binding.ePhoneE.requestFocus()
                false
            } else {
                binding.ePhoneELayout.isErrorEnabled = false
                true
            }
        } else {
            binding.ePhoneELayout.isErrorEnabled = false
            return true
        }
    }

    private fun validateAddress(): Boolean {
        if (binding.eAddressE.text.toString().trim().isEmpty()) {
            binding.eAddressELayout.error = "Required Field!"
            binding.eAddressE.requestFocus()
            return false
        } else {
            binding.eAddressELayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateState(): Boolean {
        if (binding.eStateListE.text.toString().trim().isEmpty()) {
            binding.eStateELayout.error = "Required Field!"
            binding.eStateListE.requestFocus()
            return false
        } else {
            binding.eStateELayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateDescription(): Boolean {
        if (binding.descriptionE.text.toString().trim().isEmpty()) {
            binding.descriptionELayout.error = "Required Field!"
            binding.descriptionE.requestFocus()
            return false
        } else {
            binding.descriptionELayout.isErrorEnabled = false
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
                    .override(356, 100)
                    .fitCenter() // scale to fit entire image within ImageView
                    .into(binding.imgEventE)
                newImg = true
            }

        }

    private fun validateURL(): Boolean {
        if (binding.eLinkE.text.toString().trim() =="N/A" ) {
            link = "N/A"
            return true
        }
        if (binding.eLinkE.text.toString().trim().isNotEmpty()) {
            if (Patterns.WEB_URL.matcher(binding.eLinkE.text.toString()).matches()) {
                binding.eLinkELayout.isErrorEnabled = false
                link = binding.eLinkE.text.toString()
            } else {
                binding.eLinkELayout.error = "Invalid Website Link"
                binding.eLinkE.requestFocus()
                return false
            }
        } else {
            binding.eLinkELayout.isErrorEnabled = false
        }
        return true
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
                R.id.ePhoneE -> {
                    validatePhone()
                }
            }
        }
    }
}
