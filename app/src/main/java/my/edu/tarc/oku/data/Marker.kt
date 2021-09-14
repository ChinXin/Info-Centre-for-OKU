package my.edu.tarc.oku.data

import com.google.firebase.database.Exclude

data class Marker(
    //@Exclude val id:String,
    val latitude:String,
    val longitude:String,
    val type:String,
    val title:String,
    val description:String,
    val phoneNo:String,
    val address:String,
    val state:String

)
