package my.edu.tarc.oku.data

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.R

class EventRegistrationAdapter(
    val registerList: List<EventRegistration>,
    val listener: OnItemClickListener
) :
    RecyclerView.Adapter<EventRegistrationAdapter.myViewHolder>() {
    private val myReg = Firebase.database.getReference("users/member")

    inner class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        val mInfo: TextView = itemView.findViewById(R.id.tvRMinfo)
        val card: CardView = itemView.findViewById(R.id.registeredCard)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.admin_event_registration_list, parent, false
        )

        return myViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentRegistration = registerList[position]
//        val currentUser = userList[position]
//        lateinit var username: String
//        lateinit var fullName: String
//        lateinit var email: String
//        lateinit var phoneNo: String
//        lateinit var address: String
        holder.card.visibility  = View.VISIBLE
        holder.mInfo.text =
            "${currentRegistration.username} registered on ${currentRegistration.date} ${currentRegistration.time}"


//        holder.itemView.setOnClickListener {
//        val content = LayoutInflater.from(holder.itemView.context)
//            .inflate(R.layout.registered_member_info, null)
//        val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)

//        val Username = content.findViewById<TextView>(R.id.RMUsername)
//        val Name = content.findViewById<TextView>(R.id.RMName)
//        val Email = content.findViewById<TextView>(R.id.RMEmail)
//        val PhoneNo = content.findViewById<TextView>(R.id.RMPhoneNo)
//        val Address = content.findViewById<TextView>(R.id.RMAddress)

//            Username.text = ": ${currentUser.username}"
//            Name.text = ": ${currentUser.fullName}"
//            Email.text = ": ${currentUser.email}"
//            PhoneNo.text = ": ${currentUser.phoneNo}"
//            Address.text = ": ${currentUser.address}"

//        builder.setTitle("Member Information")
//
//        builder.setView(content)
//
//        builder.setPositiveButton("Ok") { _, _ -> }
//
//        builder.show()
//        }

    }

    override fun getItemCount(): Int {
        return registerList.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}