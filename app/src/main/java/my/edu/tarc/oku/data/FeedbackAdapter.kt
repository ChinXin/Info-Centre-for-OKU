package my.edu.tarc.oku.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.oku.R

class FeedbackAdapter (val feedbackList: List<Feedback>): RecyclerView.Adapter<FeedbackAdapter.myViewHolder>(){
    class myViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val fUsername: TextView = itemView.findViewById(R.id.memberUserName)
        val fFeedback: TextView = itemView.findViewById(R.id.feedback)
        val fDateTime: TextView = itemView.findViewById(R.id.feedDateTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.feedback_list,parent,false)
        return myViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentFeedback = feedbackList[position]

        holder.fUsername.text = currentFeedback.username
        holder.fFeedback.text = currentFeedback.feedback
        holder.fDateTime.text = currentFeedback.date + "" + currentFeedback.time
    }

    override fun getItemCount(): Int {
        return feedbackList.size
    }
}