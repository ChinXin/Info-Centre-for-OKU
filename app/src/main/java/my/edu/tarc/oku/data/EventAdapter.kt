package my.edu.tarc.oku.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.edu.tarc.oku.AdminEventDirections
import my.edu.tarc.oku.R

class EventAdapter (val eventList: List<Event>): RecyclerView.Adapter<EventAdapter.myViewHolder>() {


    class myViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val eventTitle: TextView = itemView.findViewById(R.id.tvTitleE)
        val eventDateTime: TextView = itemView.findViewById(R.id.tvDateTimeE)
        val eventAddress: TextView = itemView.findViewById(R.id.tvAddressE)
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
        Glide.with(holder.eventImage.context)
            .load(currentEvent.image)
            .fitCenter()
            .into(holder.eventImage)
        holder.itemView.setOnClickListener {
            val eventId = currentEvent.id
            Toast.makeText(holder.itemView.context, "$eventId", Toast.LENGTH_SHORT).show()
            val action = AdminEventDirections.actionAdminEventToAdminEventInfo(eventId)
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }
}