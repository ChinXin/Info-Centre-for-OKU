package my.edu.tarc.oku

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import my.edu.tarc.oku.data.UserSessionManager
import my.edu.tarc.oku.databinding.FragmentHomeAdminBinding
import my.edu.tarc.oku.databinding.FragmentHomeBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.ArrayList


class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        val database = Firebase.database
        val myRef = database.getReference("state")

        enableMyLocation()
        //display all
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(s in snapshot.children){
                    for (t in s.children){
                        if(t.key == "Services" || t.key == "Facilities"){
                            for(m in t.children){
                                val id = m.key
                                val lat = m.child("latitude").value.toString().toDouble()
                                val long = m.child("longitude").value.toString().toDouble()
                                val title = m.child("title").value.toString()
                                val type = m.child("type").value.toString()
                                val marker = LatLng(lat,long)

                                if(type == "Services"){
                                    googleMap.addMarker(
                                        MarkerOptions()
                                        .position(marker)
                                        .snippet(id)
                                        .title(title)
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_GREEN)))
                                }else{
                                    googleMap.addMarker(
                                        MarkerOptions()
                                        .position(marker)
                                        .snippet(id)
                                        .title(title)
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_CYAN)))
                                }

                            }
                        }

                        if(t.key == "Individual"){
                            for(a in t.children){ //member id
                                for(b in a.children){ //marker id
                                    val id = b.key
                                    val lat = b.child("latitude").value.toString().toDouble()
                                    val long = b.child("longitude").value.toString().toDouble()
                                    val title = b.child("title").value.toString()
                                    val type = b.child("type").value.toString()
                                    val marker = LatLng(lat,long)

                                    googleMap.addMarker(
                                        MarkerOptions()
                                            .position(marker)
                                            .snippet(id)
                                            .title(title)
                                            .icon(
                                                BitmapDescriptorFactory.defaultMarker(
                                                    BitmapDescriptorFactory.HUE_YELLOW)))

                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })
        map.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this.requireContext()))

        //correct version
        map.setOnMarkerClickListener(object: GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker): Boolean {
                var currentLat = ""
                var currentLong = ""
                p0.showInfoWindow()

                //navigate
                binding.routeFab2.setOnClickListener{
                    clear()

                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
                    fusedLocationClient.lastLocation.addOnSuccessListener {
                        currentLat = it.latitude.toString()
                        currentLong = it.longitude.toString()
                        val origin = LatLng(currentLat.toDouble(),currentLong.toDouble())
                        val destination = LatLng(p0.position.latitude,p0.position.longitude)
                        //Log.i("test123","Line 148 = $origin")
                        //Log.i("test123","Line 148 = $destination")
                        val URL = getDirectionURL(origin,destination)

                        GetDirection(URL).execute()
                        callRoute(p0,map)
                    }
                }

                map.setOnInfoWindowClickListener {
                    var markerId = p0.snippet

//                    myRef.addValueEventListener(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            for(x in snapshot.children){
//                                for(y in x.children){
//                                    if(y.key == "Facilities" || y.key == "Services"){
//                                        for(a in y.children){ //marker id
//                                            if(y.hasChild(markerId)){
//                                                //Toast.makeText(context,"Facilities or Services",Toast.LENGTH_SHORT).show()
//                                                basicAlert(p0,markerId.toString())
//                                            }
//                                        }
//                                    }
//
//                                    if(y.key == "Individual"){
//                                        for(a in y.children){ //member ID
//                                            for(b in a.children){ //member's markers id
//                                                if(a.hasChild(markerId)){
//                                                    Toast.makeText(context,"Individual",Toast.LENGTH_SHORT).show()
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {}
//                    })
                    basicAlert(p0,markerId.toString())
                }
                return true
            }
        })
    }

    private fun clear(){
        map.clear()
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var session = UserSessionManager(requireContext().applicationContext)
        if (session.checkLogin()){
            val user = session.userDetails
            val name = user[UserSessionManager.KEY_NAME]
            val status = user[UserSessionManager.KEY_STATUS]
            if(status =="admin") {
                val intent = Intent(requireContext(), AdminActivity::class.java)
                intent.putExtra("Username", name)
                startActivity(intent)
            }else if (status == "member"){
                val intent = Intent(requireContext(), MemberActivity::class.java)
                intent.putExtra("Username", name)
                startActivity(intent)
            }
            return null
        }

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.normal_map){
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        } else if(id == R.id.hybrid_map){
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
        }else if(id == R.id.satellite_map){
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }else{
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    //Direction
    fun getDirectionURL(origin:LatLng,dest:LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyBaPkxWQObtSkv-qUNUTsIqnbrAb8jYbS4"
    }

    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()
            Log.d("GoogleMap", " data : $data")
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)

                val path = ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size - 1)) {
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(R.color.purple_700)
                lineoption.geodesic(true)
            }
            map.addPolyline(lineoption)
        }
    }


    fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }

    //Call route
    fun callRoute(p0:com.google.android.gms.maps.model.Marker,map:GoogleMap){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Google Maps")
        builder.setMessage("Start to Go?")

        builder.setPositiveButton("Start"){ which,dialog ->
            val gmmIntentUri = Uri.parse("google.navigation:q=" + p0.position.latitude.toString() + "," + p0.position.longitude.toString())
            val mapIntent = Intent(Intent.ACTION_VIEW,gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        builder.setNegativeButton("Cancel"){ which,dialog ->

        }

        builder.show()
    }

    fun basicAlert(defaultId:com.google.android.gms.maps.model.Marker,markerId:String){

        val builder:AlertDialog.Builder = AlertDialog.Builder(this.requireContext())

        val content = LayoutInflater.from(this.requireContext()).inflate(R.layout.marker_details,null)

        val database = Firebase.database
        val myRef = database.getReference("state")

        val type = content.findViewById<TextView>(R.id.markerType)
        val title = content.findViewById<TextView>(R.id.markerTitle)
        val description = content.findViewById<TextView>(R.id.markerDescription)
        val phoneNo = content.findViewById<TextView>(R.id.markerPhoneNo)
        val address = content.findViewById<TextView>(R.id.markerAddress)
        val state = content.findViewById<TextView>(R.id.markerState)
        var getId = ""

        if(markerId != ""){
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(s in snapshot.children){ //state list
                        for (t in s.children){ //type
                            if(t.key == "Services" || t.key == "Facilities"){
                                if(t.hasChild(markerId)){
                                    getId = t.child(markerId).key.toString()
                                    type.text = t.child(markerId).child("type").value.toString()
                                    title.text = t.child(markerId).child("title").value.toString()
                                    description.text = t.child(markerId).child("description").value.toString()
                                    phoneNo.text = t.child(markerId).child("phoneNo").value.toString()
                                    address.text = t.child(markerId).child("address").value.toString()
                                    state.text = t.child(markerId).child("state").value.toString()
                                    break
                                }
                            }

                            if(t.key == "Individual"){
                                for(a in t.children){ //member ID
                                    if(a.hasChild(markerId)){
                                        getId = a.child(markerId).key.toString()
                                        type.text = a.child(markerId).child("type").value.toString()
                                        title.text = a.child(markerId).child("title").value.toString()
                                        description.text = a.child(markerId).child("description").value.toString()
                                        phoneNo.text = a.child(markerId).child("phoneNo").value.toString()
                                        address.text = a.child(markerId).child("address").value.toString()
                                        state.text = a.child(markerId).child("state").value.toString()
                                        break
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }else{
            type.text = ""
            title.text = ""
            description.text = ""
            phoneNo.text = ""
            address.text = ""
            state.text = ""
        }

        builder.setView(content)
        builder.setCancelable(false)

        val dialog = builder.show()

        val btnClose = content.findViewById<Button>(R.id.btnClose)

        btnClose.setOnClickListener{
                defaultId.hideInfoWindow()
                dialog.dismiss()
        }
    }
}

