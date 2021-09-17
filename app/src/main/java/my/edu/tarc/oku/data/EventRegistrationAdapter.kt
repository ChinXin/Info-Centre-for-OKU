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

//class ProductAdapter (private val productList: List<Product>):RecyclerView.Adapter<ProductAdapter.myViewHolder>(){
class EventRegistrationAdapter (private val registerList: List<EventRegistration>): RecyclerView.Adapter<EventRegistrationAdapter.myViewHolder>() {
    private val myReg = Firebase.database.getReference("users/member" )

    class myViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val mInfo: TextView = itemView.findViewById(R.id.tvRMinfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.admin_event_registration_list, parent, false)

        return myViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentRegistration = registerList[position]

        lateinit var username:String
        lateinit var fullName:String
        lateinit var email:String
        lateinit var phoneNo:String
        lateinit var address:String

        myReg.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(currentRegistration.username)) {
                    username = snapshot.child(currentRegistration.username)
                        .child("username").value.toString()
                    fullName = snapshot.child(currentRegistration.username)
                        .child("fullName").value.toString()
                    email = snapshot.child(currentRegistration.username)
                        .child("email").value.toString()
                    phoneNo = snapshot.child(currentRegistration.username)
                        .child("phoneNo").value.toString()
                    address = snapshot.child(currentRegistration.username)
                        .child("address").value.toString()
                    val card = holder.itemView.rootView.findViewById<CardView>(R.id.registeredCard)
                    holder.mInfo.text = "$fullName registered on ${currentRegistration.date} ${currentRegistration.time}"
                    card.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })

        holder.itemView.setOnClickListener {
            val content = LayoutInflater.from(holder.itemView.context).inflate(R.layout.registered_member_info, null)
            val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)

            val Username = content.findViewById<TextView>(R.id.RMUsername)
            val Name = content.findViewById<TextView>(R.id.RMName)
            val Email = content.findViewById<TextView>(R.id.RMEmail)
            val PhoneNo = content.findViewById<TextView>(R.id.RMPhoneNo)
            val Address = content.findViewById<TextView>(R.id.RMAddress)

            Username.text = ": $username"
            Name.text = ": $fullName"
            Email.text = ": $email"
            PhoneNo.text = ": $phoneNo"
            Address.text = ": $address"

            builder.setTitle("Member Information")

            builder.setView(content)

            builder.setPositiveButton("Ok"){ _, _ -> }

            builder.show()
        }

    }

    override fun getItemCount(): Int {
        return registerList.size
    }
}