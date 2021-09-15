package my.edu.tarc.oku.data

import android.content.Intent
import android.net.Uri

data class Event (
    val id:String,
    val image:String,
    val title:String,
    val date:String,
    val time:String,
    val address: String,
    val state:String,
    val description:String,
    val link:String,
    val phone:String
)