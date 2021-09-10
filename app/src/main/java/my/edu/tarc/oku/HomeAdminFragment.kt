package my.edu.tarc.oku

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Exclude
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.oku.data.Marker
import java.util.*
import kotlin.jvm.internal.AdaptedFunctionReference
import my.edu.tarc.oku.R as R

class HomeAdminFragment : Fragment() {

    private lateinit var map: GoogleMap
    lateinit var customView: View
    //private val TAG = HomeAdminFragment::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1
    //val markerList: MutableList<String?> = ArrayList()

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        val database = Firebase.database
        val myRef = database.getReference("markers")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for(x in snapshot.children){
                    val id = x.key
                    val lat = x.child("latitude").value.toString().toDouble()
                    val long = x.child("longitude").value.toString().toDouble()
                    val title = x.child("title").value.toString()
                    val marker = LatLng(lat,long)

                    googleMap.addMarker(MarkerOptions().position(marker).snippet(id).title(title))
                    //googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
                    //googleMap.animateCamera
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })
        //Log.i("test123",getCurrentLocation().toString())

        //optional style map
        //setMapStyle(map)
        //map.uiSettings.isZoomControlsEnabled = true
        //map.uiSettings.isZoomGesturesEnabled = true

        enableMyLocation()
        setMapLongClick(map)
        map.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this.requireContext()))


        //correct version
        map.setOnMarkerClickListener(object: GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: com.google.android.gms.maps.model.Marker): Boolean {
                //Toast.makeText(context,p0.snippet,Toast.LENGTH_SHORT).show()
                //Log.i("test123",p0.toString())
                p0.showInfoWindow()
                map.setOnInfoWindowClickListener {

                    var markerId = p0.snippet
                    basicAlert(p0,markerId.toString(),p0.position.latitude.toString(),p0.position.longitude.toString())
                    //Log.i("test123","lat = " + p0.position.latitude.toString())
                    //Log.i("test123","lat = " + p0.position.longitude.toString())
                }
                return true
            }
        })

        //try version
//        map.setOnMarkerClickListener(object: GoogleMap.OnMarkerClickListener {
//            override fun onMarkerClick(p0: com.google.android.gms.maps.model.Marker): Boolean {
//                //val gmmIntentUri = Uri.parse("geo:" + p0.position.latitude.toString() + "," + p0.position.longitude.toString())
//                val gmmIntentUri = Uri.parse("google.navigation:q=" + p0.position.latitude.toString() + "," + p0.position.longitude.toString())
//                val mapIntent = Intent(Intent.ACTION_VIEW,gmmIntentUri)
//                mapIntent.setPackage("com.google.android.apps.maps")
//                startActivity(mapIntent)
//                return true
//            }
//        })


        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,5f))
    }

//    private fun getCurrentLocation():LatLng {
//        val request = LocationRequest()
//        var geoPoint:LatLng
//
//        request.setInterval(10000)
//
//        val client = LocationServices.getFusedLocationProviderClient(this.activity)
//        val permission = ContextCompat.checkSelfPermission(
//            this.requireContext(),
//            Manifest.permission.ACCESS_FINE_LOCATION
//        )
//
//        if (permission == PackageManager.PERMISSION_GRANTED) {
//            client.requestLocationUpdates(request, object : LocationCallback() {
//                override fun onLocationResult(locationResult: LocationResult) {
//                    val location = locationResult.lastLocation
//                    geoPoint = LatLng(location.latitude, location.longitude)
//
//                }
//            }, null)
//        }
//        return geoPoint
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home_admin, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        //return long press action
        val v:View = this.requireActivity().findViewById(android.R.id.content)
        Snackbar.make(v,"Long press to add a marker!",Snackbar.LENGTH_INDEFINITE)
            .setAction("OK",{})
            .setActionTextColor(ContextCompat.getColor(this.requireContext(),android.R.color.white))
            .show()
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


    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
//            val snippet = String.format(
//                Locale.getDefault(),
//                "Lat: %1$.5f, Long: %2$.5f",
//                latLng.latitude,
//                latLng.longitude
//            )

            val test = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("New Pin")
                    .snippet("")
                    //.draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            //Log.i("test123",test.toString())
            //Log.i("test123",test.id)
            //Toast.makeText(context,test.tag.toString(),Toast.LENGTH_LONG).show()
            //getMarker(test)
            //test.showInfoWindow()
//            map.setOnInfoWindowClickListener {
//                basicAlert(latLng.latitude.toString(),latLng.longitude.toString())
//            }
        }
    }

//    private fun getMarker(marker:com.google.android.gms.maps.model.Marker): com.google.android.gms.maps.model.Marker {
//        val abc = marker.
//        return marker
//    }

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

//    private fun setMapStyle(map: GoogleMap) {
//        try {
//            // Customize the styling of the base map using a JSON object defined
//            // in a raw resource file.
//            val success = map.setMapStyle(
//                MapStyleOptions.loadRawResourceStyle(
//                    context,
//                    R.raw.map_style
//                )
//            )
//
//            if (!success) {
//                Log.e(TAG, "Style parsing failed.")
//            }
//        } catch (e: Resources.NotFoundException) {
//            Log.e(TAG, "Can't find style. Error: ", e)
//        }
//    }

    fun basicAlert(defaultId:com.google.android.gms.maps.model.Marker,markerId:String,latitude:String,longitude:String){

        val builder:AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        //builder.setTitle("Androidly Alert")

        val content = LayoutInflater.from(this.requireContext()).inflate(R.layout.marker_form,null)

        val items = resources.getStringArray(R.array.types)
        val stateList = resources.getStringArray(R.array.stateList)
        val adapter = ArrayAdapter(this.requireContext(), R.layout.dropdown_type, items)
        val adapter2 = ArrayAdapter(this.requireContext(), R.layout.dropdown_state, stateList)
        val autoId = content.findViewById<AutoCompleteTextView>(R.id.autoCompleteList)
        val autoId2 = content.findViewById<AutoCompleteTextView>(R.id.autoCompleteList2)
        autoId.setAdapter(adapter)
        autoId2.setAdapter(adapter2)

        val database = Firebase.database
        val myRef = database.getReference("markers")

        val title = content.findViewById<TextInputEditText>(R.id.infoTitle)
        //val type = content.findViewById<AutoCompleteTextView>(R.id.autoCompleteList)
        val phoneNo = content.findViewById<TextInputEditText>(R.id.phoneNo)
        val address = content.findViewById<TextInputEditText>(R.id.address)
        var getId = ""

        if(markerId != ""){
            myRef.child(markerId).get().addOnSuccessListener {
                getId = it.key.toString()
                //autoId.text. = it.child("type").value.toString()
                if(it.child("type").value.toString() == "Services"){
                    autoId.setText(adapter.getItem(0),false)
                }else{
                    autoId.setText(adapter.getItem(1),false)
                }
                //autoId.setText(adapter.getItem(0))
                title.setText(it.child("title").value.toString())
                phoneNo.setText(it.child("phoneNo").value.toString())
                address.setText(it.child("address").value.toString())

            }
        }else{
            autoId.setText("")
            title.setText("")
            phoneNo.setText("")
            address.setText("")
        }
        builder.setView(content)

        //builder.setMessage("We have a message")
        builder.setPositiveButton("Save") { dialog, which ->
            val lat = latitude.toDouble()
            val long = longitude.toDouble()
            val type = autoId.text.toString()
            val title = content.findViewById<TextInputEditText>(R.id.infoTitle).text.toString()
            val phoneNo = content.findViewById<TextInputEditText>(R.id.phoneNo).text.toString()
            val address = content.findViewById<TextInputEditText>(R.id.address).text.toString()

            val newMarker = Marker(lat.toString(),long.toString(),type,title,phoneNo,address)

            if(getId != ""){
                myRef.child(getId).setValue(newMarker).addOnSuccessListener {
                    Toast.makeText(context,"Update Successfull!!!",Toast.LENGTH_SHORT).show()
                    getId = ""
                }
            }else{
                myRef.push().setValue(newMarker).addOnSuccessListener {
                    Toast.makeText(context,"Added Successful",Toast.LENGTH_SHORT).show()
                }
            }
            map.clear()
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
            //val marker = LatLng(lat,long)
            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker,15f))
        }

        if(markerId != ""){
            builder.setNeutralButton("Delete"){ dialog,which ->
                myRef.child(markerId).get().addOnSuccessListener {
                    val markId = it.key
                    val markLat = it.child("latitude").value.toString()
                    val markLong = it.child("longitude").value.toString()
                    val markTitle = it.child("title").value.toString()
                    val markType = it.child("type").value.toString()
                    val phone = it.child("phoneNo").value.toString()
                    val add = it.child("address").value.toString()

                    val undoMarker = Marker(markLat, markLong, markType, markTitle,phone, add)

                    myRef.child(markerId).removeValue().addOnSuccessListener {
                        //Toast.makeText(context, "Delete Successful", Toast.LENGTH_SHORT).show()
                        //defaultId.remove()
                        //remove marker
                        map.clear()
                        val mapFragment =
                            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                        mapFragment?.getMapAsync(callback)

                        val v: View = this.requireActivity().findViewById(android.R.id.content)
                        Snackbar.make(v, "Marker Deleted!", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", View.OnClickListener {
                                val setMark = LatLng(markLat.toDouble(),markLong.toDouble())
                                map.addMarker(
                                    MarkerOptions()
                                        .position(setMark)
                                        .title(markTitle)
                                        .snippet(markId)
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_RED
                                            )
                                        )
                                )

                                myRef.child(markId.toString()).setValue(undoMarker).addOnSuccessListener {
                                    Toast.makeText(context,"Undo Successfully!!!",Toast.LENGTH_SHORT).show()
                                }
                            })
                            .setActionTextColor(
                                ContextCompat.getColor(
                                    this.requireContext(),
                                    android.R.color.white
                                )
                            )
                            .show()
                    }
                }
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            //remove marker
            if(markerId == ""){
                defaultId.remove()
            }else{
                defaultId.hideInfoWindow()
            }
        }
        builder.show()
    }
}
