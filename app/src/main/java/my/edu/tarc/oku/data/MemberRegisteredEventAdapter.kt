package my.edu.tarc.oku.data

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.edu.tarc.oku.MemberRegisteredEventDirections
import my.edu.tarc.oku.R

class MemberRegisteredEventAdapter (val eventList: List<Event>): RecyclerView.Adapter<MemberRegisteredEventAdapter.myViewHolder>() {


    class myViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val eventTitle: TextView = itemView.findViewById(R.id.tvTitleE)
        val eventDateTime: TextView = itemView.findViewById(R.id.tvAddressE)
        val eventAddress: TextView = itemView.findViewById(R.id.tvDateTimeE)
        val eventImage: ImageView = itemView.findViewById(R.id.imageEvent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.admin_event_list, parent, false)

        return myViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentEvent = eventList[position]

        holder.eventTitle.text = currentEvent.title
        holder.eventDateTime.text = "Date/Time: ${currentEvent.date}, ${currentEvent.time}"
        holder.eventAddress.text = "Address: ${currentEvent.address}"
        val bitmap = Base64.decode(currentEvent.image, Base64.DEFAULT)
        Glide.with(holder.eventImage.context)
            .asBitmap()
            .load(bitmap)
            .fitCenter()
            .into(holder.eventImage)
        holder.itemView.setOnClickListener {
            val eventId = currentEvent.id
            Toast.makeText(holder.itemView.context, "$eventId", Toast.LENGTH_SHORT).show()
            val action = MemberRegisteredEventDirections.actionMemberRegisteredEventToMemberEventInfo(eventId)
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }
}