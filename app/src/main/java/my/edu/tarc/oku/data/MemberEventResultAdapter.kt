package my.edu.tarc.oku.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.edu.tarc.oku.R
import android.util.Base64
import android.widget.*

class MemberEventResultAdapter( val eventList: List<Event>, val listener: OnItemClickListener ) :
    RecyclerView.Adapter<MemberEventResultAdapter.myViewHolder>(){

    inner class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val eventTitle: TextView = itemView.findViewById(R.id.eTitle)
        val eventDateTime: TextView = itemView.findViewById(R.id.eDateTime)
        val eventAddress: TextView = itemView.findViewById(R.id.eAddress)
        val eventImage: ImageView = itemView.findViewById(R.id.eImage)

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
            R.layout.search_event_list, parent, false
        )

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
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}