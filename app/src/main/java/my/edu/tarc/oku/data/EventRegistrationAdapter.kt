package my.edu.tarc.oku.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.oku.R

class EventRegistrationAdapter(private val registerList: List<EventRegistration>, val listener: OnItemClickListener ) :
    RecyclerView.Adapter<EventRegistrationAdapter.myViewHolder>() {

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

        holder.card.visibility  = View.VISIBLE
        holder.mInfo.text = "${currentRegistration.username} registered on ${currentRegistration.date} ${currentRegistration.time}"
    }

    override fun getItemCount(): Int {
        return registerList.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}