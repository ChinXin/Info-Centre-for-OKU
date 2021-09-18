package my.edu.tarc.oku.data

import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.GeoPoint
import my.edu.tarc.oku.R
import android.location.Address
import android.util.Log
import android.widget.*

class MemberEventResultAdapter(
    val eventList: List<Event>,
    val listener: OnItemClickListener
) :
    RecyclerView.Adapter<MemberEventResultAdapter.myViewHolder>(){

    var search = eventList as ArrayList<Event>

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

    init {
        search = eventList as ArrayList<Event>
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

        Glide.with(holder.eventImage.context)
            .load(currentEvent.image)
            .fitCenter()// scale to fit entire image within ImageView
            .into(holder.eventImage)

//        holder.itemView.set
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

//    override fun getFilter(): Filter {
//
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                val charSearch = constraint.toString()
//
//                if (charSearch.isEmpty()) {
//                    search = eventList as ArrayList<Event>
//                } else {
//                    val resultList = ArrayList<Event>()
//                    for (row in eventList) {
//                        if(row.title.lowercase().contains(charSearch)) {
//                            resultList.add(row)
//                        }
//                    }
//                    search = resultList
//                }
//                val filterResults = FilterResults()
//
//                    filterResults.values = search
//
//
//                return filterResults
//            }
//
//            override fun publishResults(p0: CharSequence?, filterResults: FilterResults?) {
//                search = filterResults!!.values as ArrayList<Event>
//                notifyDataSetChanged()
//            }
//
//        }
//    }

//    public GeoPoint getLocationFromAddress(String strAddress){
//
//        Geocoder coder = new Geocoder(this);
//        List<Address> address;
//        GeoPoint p1 = null;
//
//        try {
//            address = coder.getFromLocationName(strAddress,5);
//            if (address==null) {
//                return null;
//            }
//            Address location=address.get(0);
//            location.getLatitude();
//            location.getLongitude();
//
//            p1 = new GeoPoint((double) (location.getLatitude() * 1E6),
//            (double) (location.getLongitude() * 1E6));
//
//            return p1;
//        }
//    }
}