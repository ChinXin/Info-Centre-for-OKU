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
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import my.edu.tarc.oku.data.Marker
import my.edu.tarc.oku.data.UserSessionManager
import my.edu.tarc.oku.databinding.FragmentHomeAdminBinding
import my.edu.tarc.oku.databinding.FragmentHomeMemberBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.ArrayList
import java.util.regex.Pattern

class HomeMemberFragment : Fragment() {

    private lateinit var binding: FragmentHomeMemberBinding
    lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var name: String

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        val database = Firebase.database
        val myRef = database.getReference("state")

        enableMyLocation()
        //display all

        Toast.makeText(context, "username = $name", Toast.LENGTH_LONG).show()
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

//                for(x in snapshot.children){
//                    val id = x.key
//                    val lat = x.child("latitude").value.toString().toDouble()
//                    val long = x.child("longitude").value.toString().toDouble()
//                    val title = x.child("title").value.toString()
//                    val marker = LatLng(lat,long)
//
//                    googleMap.addMarker(MarkerOptions().position(marker).snippet(id).title(title))
//                    //googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
//                    //googleMap.animateCamera
//                }
                for (s in snapshot.children) {
                    //Log.i("test123","Line 78 = ${s.value}" )
                    for (t in s.children) {
                        if (t.key == "Services" || t.key == "Facilities") {
                            //Log.i("test123","Line 80 = ${t.k}" )
                            for (m in t.children) {
                                //Log.i("test123","Line 80 = ${m.value}" )
                                val id = m.key
                                val lat = m.child("latitude").value.toString().toDouble()
                                val long = m.child("longitude").value.toString().toDouble()
                                val title = m.child("title").value.toString()
                                val type = m.child("type").value.toString()
                                val marker = LatLng(lat, long)

                                if (type == "Services") {
                                    googleMap.addMarker(
                                        MarkerOptions()
                                            .position(marker)
                                            .snippet(id)
                                            .title(title)
                                            .icon(
                                                BitmapDescriptorFactory.defaultMarker(
                                                    BitmapDescriptorFactory.HUE_GREEN
                                                )
                                            )
                                    )
                                } else {
                                    googleMap.addMarker(
                                        MarkerOptions()
                                            .position(marker)
                                            .snippet(id)
                                            .title(title)
                                            .icon(
                                                BitmapDescriptorFactory.defaultMarker(
                                                    BitmapDescriptorFactory.HUE_CYAN
                                                )
                                            )
                                    )
                                }

                            }
                        }

                        if (t.key == "Individual") {
                            for (a in t.children) {
                                if (a.key == name) {
                                    Log.i("test123", "Line 122 = ${a.key}")
                                    for (b in a.children) {
                                        val id = b.key
                                        val lat = b.child("latitude").value.toString().toDouble()
                                        val long = b.child("longitude").value.toString().toDouble()
                                        val title = b.child("title").value.toString()
                                        val type = b.child("type").value.toString()
                                        val marker = LatLng(lat, long)

                                        googleMap.addMarker(
                                            MarkerOptions()
                                                .position(marker)
                                                .snippet(id)
                                                .title(title)
                                                .icon(
                                                    BitmapDescriptorFactory.defaultMarker(
                                                        BitmapDescriptorFactory.HUE_YELLOW
                                                    )
                                                )
                                        )
                                    }
                                } else {
                                    for (b in a.children) {
                                        val id = b.key
                                        val lat = b.child("latitude").value.toString().toDouble()
                                        val long = b.child("longitude").value.toString().toDouble()
                                        val title = b.child("title").value.toString()
                                        val type = b.child("type").value.toString()
                                        val marker = LatLng(lat, long)

                                        googleMap.addMarker(
                                            MarkerOptions()
                                                .position(marker)
                                                .snippet(id)
                                                .title(title)
                                                .icon(
                                                    BitmapDescriptorFactory.defaultMarker(
                                                        BitmapDescriptorFactory.HUE_ROSE
                                                    )
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        //optional style map
        //setMapStyle(map)
        //map.uiSettings.isZoomControlsEnabled = true
        //map.uiSettings.isZoomGesturesEnabled = true
        setMapLongClick(map)
        map.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this.requireContext()))

        //correct version
        map.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: com.google.android.gms.maps.model.Marker): Boolean {
                var currentLat = ""
                var currentLong = ""
                p0.showInfoWindow()

                //navigate
                binding.routeFab3.setOnClickListener {
                    clear()

                    fusedLocationClient =
                        LocationServices.getFusedLocationProviderClient(requireContext())
                    fusedLocationClient.lastLocation.addOnSuccessListener {
                        currentLat = it.latitude.toString()
                        currentLong = it.longitude.toString()
                        val origin = LatLng(currentLat.toDouble(), currentLong.toDouble())
                        val destination = LatLng(p0.position.latitude, p0.position.longitude)
                        //Log.i("test123","Line 148 = $origin")
                        //Log.i("test123","Line 148 = $destination")
                        val URL = getDirectionURL(origin, destination)

                        GetDirection(URL).execute()
                        callRoute(p0, map)
                    }
                }

                map.setOnInfoWindowClickListener {
                    var markerId = p0.snippet

                    myRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var check = false
                            var count = 0
                            for (x in snapshot.children) {
                                val individual =
                                    x.child("Individual").child(name).hasChild(markerId)
                                val facilities = x.child("Facilities").hasChild(markerId)
                                val services = x.child("Services").hasChild(markerId)

                                if (facilities || services) {
                                    viewAlert(p0, markerId.toString())
                                    check = true
                                    Log.i("test1234", "Line 211")
                                    break
                                } else if (individual) {
                                    basicAlert(
                                        p0,
                                        markerId.toString(),
                                        p0.position.latitude.toString(),
                                        p0.position.longitude.toString()
                                    )
                                    check = true
                                    Log.i("test1234", "Line 215")
                                    break
                                }
                                count++
                            }
                            if (count == snapshot.childrenCount.toInt() && !check) { //other member marker cannot display

                                viewAlert(p0, markerId.toString())
                                Log.i("test1234", "Line 219")
                            }


//                            for(x in snapshot.children){
//                                for(y in x.children){
//
//                                    if(y.key == "Facilities" || y.key == "Services"){
//                                        if(y.hasChild(markerId)){
//                                            for(a in y.children){ //marker id
//                                                for(j in y.children){
//                                                    if(a.key == markerId){
//                                                        viewAlert(p0,markerId.toString())
//                                                        Log.i("test1234","Line 212")
//                                                        break
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    if(y.key == "Individual"){
//                                        if(markerId == y.child(name).child(markerId).key){
//                                            basicAlert(p0,markerId.toString(),p0.position.latitude.toString(),p0.position.longitude.toString())
//                                            Log.i("test1234","Line 226")
//                                            break
//                                        }else{
//                                            viewAlert(p0,markerId.toString())
//                                            Log.i("test1234","Line 230")
//                                            break
//                                        }
////                                        for(a in y.children){ //member ID
////                                            if(y.hasChild(name)){ //check whether got this username or not
////                                                //for(x in y.children){
////                                                if(a.key == name){
////                                                    //if(x.hasChild(markerId)){
////                                                        for(m in a.children){
////                                                            if(m.key == markerId){
////                                                                basicAlert(p0,markerId.toString(),p0.position.latitude.toString(),p0.position.longitude.toString())
////                                                                break
////                                                            }
////                                                        }
////                                                    //}
////                                                }
////                                                //}
////                                            }else{
////                                                viewAlert(p0,markerId.toString())
////                                                Log.i("test1234","Line 237")
////                                                break
////                                            }
////                                        }
//                                    }
//                                }
//                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })

                    //basicAlert(p0,markerId.toString(),p0.position.latitude.toString(),p0.position.longitude.toString())
                }
                return true
            }
        })
    }

    private fun clear() {
        map.clear()
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.memberMap) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_member, container, false)
//        val rootView = binding.root.rootView.findViewById<NavigationView>(R.id.navView)
//        val headerView = rootView.getHeaderView(0)
//        val btnSul: Button = headerView.findViewById(R.id.btnLoginSignUp)
//        name = btnSul.text.toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)


        var session = UserSessionManager(requireContext().applicationContext)
        val user = session.userDetails
        name = user[UserSessionManager.KEY_NAME].toString()
        val status = user[UserSessionManager.KEY_STATUS]

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.memberMap) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        //return long press action
        val v: View = this.requireActivity().findViewById(android.R.id.content)
        Snackbar.make(v, "Long press to add a marker!", Snackbar.LENGTH_SHORT)
            .setAction("OK", {})
            .setActionTextColor(
                ContextCompat.getColor(
                    this.requireContext(),
                    android.R.color.white
                )
            )
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.normal_map) {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        } else if (id == R.id.hybrid_map) {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
        } else if (id == R.id.satellite_map) {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        } else {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
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
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val test = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("New Pin")
                    .snippet("")
                    //.draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            //map.setOnInfoWindowClickListener {
            basicAlert(
                test,
                test.snippet.toString(),
                latLng.latitude.toString(),
                latLng.longitude.toString()
            )
            //}
        }
    }

    //Direction
    fun getDirectionURL(origin: LatLng, dest: LatLng): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyBaPkxWQObtSkv-qUNUTsIqnbrAb8jYbS4"
    }

    private inner class GetDirection(val url: String) :
        AsyncTask<Void, Void, List<List<LatLng>>>() {
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
            for (i in result.indices) {
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

            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

    //Call route
    fun callRoute(p0: com.google.android.gms.maps.model.Marker, map: GoogleMap) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Google Maps")
        builder.setMessage("Start to Go?")

        builder.setPositiveButton("Start") { which, dialog ->
            //val gmmIntentUri = Uri.parse("geo:" + p0.position.latitude.toString() + "," + p0.position.longitude.toString())
            val gmmIntentUri =
                Uri.parse("google.navigation:q=" + p0.position.latitude.toString() + "," + p0.position.longitude.toString())
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
        builder.setNegativeButton("Cancel") { which, dialog -> }
        builder.show()
    }

    fun basicAlert(
        defaultId: com.google.android.gms.maps.model.Marker,
        markerId: String,
        latitude: String,
        longitude: String
    ) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        //builder.setTitle("Androidly Alert")

        val content = LayoutInflater.from(this.requireContext()).inflate(R.layout.marker_form, null)

        val items = resources.getStringArray(R.array.types)
        val stateList = resources.getStringArray(R.array.stateList)
        val adapter = ArrayAdapter(this.requireContext(), R.layout.dropdown_type, items)
        val adapter2 = ArrayAdapter(this.requireContext(), R.layout.dropdown_state, stateList)
        val autoId = content.findViewById<AutoCompleteTextView>(R.id.autoCompleteList)
        val autoId2 = content.findViewById<AutoCompleteTextView>(R.id.autoCompleteList2)

        autoId2.setAdapter(adapter2)

        val database = Firebase.database
        val myRef = database.getReference("state")

        val title = content.findViewById<TextInputEditText>(R.id.infoTitle)
        val description = content.findViewById<TextInputEditText>(R.id.description)
        val phoneNo = content.findViewById<TextInputEditText>(R.id.phoneNo)
        val address = content.findViewById<TextInputEditText>(R.id.address)
        var getId = ""
        var oldType = ""
        var oldState = ""

        if (markerId != "") {
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (s in snapshot.children) {
                        for (t in s.children) {
                            if (t.hasChild(markerId)) {
                                getId = t.child(markerId).key.toString()
                                oldType = t.child(markerId).child("type").value.toString()
                                autoId.setText("Individual")
                                title.setText(t.child(markerId).child("title").value.toString())
                                description.setText(
                                    t.child(markerId).child("description").value.toString()
                                )
                                phoneNo.setText(t.child(markerId).child("phoneNo").value.toString())
                                address.setText(t.child(markerId).child("address").value.toString())
                                oldState = t.child(markerId).child("state").value.toString()
                                autoId2.setText(oldState, false)
                                break
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } else {
            autoId.setText("Individual")
            title.setText("")
            description.setText("")
            phoneNo.setText("")
            address.setText("")
            autoId2.setText("")
        }

        builder.setView(content)
        builder.setCancelable(false)

        val dialog = builder.show()
        val btnSave = content.findViewById<Button>(R.id.btnSave)
        val btnCancel = content.findViewById<Button>(R.id.btnCancel)
        val btnDelete = content.findViewById<Button>(R.id.btnDelete)

        btnSave.setOnClickListener {
            val lat = latitude.toDouble()
            val long = longitude.toDouble()
            val type = autoId.text.toString()
            val title = content.findViewById<TextInputEditText>(R.id.infoTitle).text.toString()
            val description =
                content.findViewById<TextInputEditText>(R.id.description).text.toString()
            val phoneNo = content.findViewById<TextInputEditText>(R.id.phoneNo).text.toString()
            val address = content.findViewById<TextInputEditText>(R.id.address).text.toString()
            val state = autoId2.text.toString()

            val newMarker = Marker(
                lat.toString(),
                long.toString(),
                type,
                title,
                description,
                phoneNo,
                address,
                state
            )

            if (type.isNotEmpty() && title.isNotEmpty() && description.isNotEmpty() && phoneNo.isNotEmpty() && address.isNotEmpty() && state.isNotEmpty()) {
                val REG = "^(01)([0-9]{8,9})\$"
                if (!Pattern.compile(REG).matcher(phoneNo).matches()) {
                    content.findViewById<TextInputLayout>(R.id.phoneNoLayout).error =
                        "Invalid Phone Number! e.g 01XXXXXXXXX"
                    content.findViewById<TextInputEditText>(R.id.phoneNo).requestFocus()
                } else {
                    if (getId != "") {
                        myRef.child(oldState).child(oldType).child(markerId).removeValue()
                            .addOnSuccessListener {
                                myRef.child(state).child(type).child(markerId).setValue(newMarker)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Update Successfully!!!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        getId = ""
                                        dialog.dismiss()
                                    }
                            }
                    } else {
                        myRef.child(state).child(type).child(name).push().setValue(newMarker)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Added Successfully!!!", Toast.LENGTH_SHORT)
                                    .show()
                                dialog.dismiss()
                            }
                    }
                    map.clear()
                    val mapFragment =
                        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                    mapFragment?.getMapAsync(callback)
                }
            } else {
                if (type.isEmpty()) {
                    content.findViewById<TextInputLayout>(R.id.typeLayout).error = "Required Field!"
                    autoId.requestFocus()
                } else {
                    content.findViewById<TextInputLayout>(R.id.typeLayout).isErrorEnabled = false
                }

                if (title.isEmpty()) {
                    content.findViewById<TextInputLayout>(R.id.TitleLayout).error =
                        "Required Field!"
                    content.findViewById<TextInputEditText>(R.id.infoTitle).requestFocus()
                } else {
                    content.findViewById<TextInputLayout>(R.id.TitleLayout).isErrorEnabled = false
                }

                if (description.isEmpty()) {
                    content.findViewById<TextInputLayout>(R.id.descriptionLayout).error =
                        "Required Field!"
                    content.findViewById<TextInputEditText>(R.id.description).requestFocus()
                } else {
                    content.findViewById<TextInputLayout>(R.id.descriptionLayout).isErrorEnabled =
                        false
                }

                if (phoneNo.isEmpty()) {
                    content.findViewById<TextInputLayout>(R.id.phoneNoLayout).error =
                        "Required Field!"
                    content.findViewById<TextInputEditText>(R.id.phoneNo).requestFocus()
                } else {
                    val REG = "^(01)([0-9]{8,9})\$"
                    if (!Pattern.compile(REG).matcher(phoneNo).matches()) {
                        content.findViewById<TextInputLayout>(R.id.phoneNoLayout).error =
                            "Invalid Phone Number! e.g 01XXXXXXXXX"
                        content.findViewById<TextInputEditText>(R.id.phoneNo).requestFocus()
                    } else {
                        content.findViewById<TextInputLayout>(R.id.phoneNoLayout).isErrorEnabled =
                            false
                    }
                }

                if (address.isEmpty()) {
                    content.findViewById<TextInputLayout>(R.id.addressLayout).error =
                        "Required Field!"
                    content.findViewById<TextInputEditText>(R.id.address).requestFocus()
                } else {
                    content.findViewById<TextInputLayout>(R.id.addressLayout).isErrorEnabled = false
                }

                if (state.isEmpty()) {
                    content.findViewById<TextInputLayout>(R.id.stateLayout).error =
                        "Required Field!"
                    autoId2.requestFocus()
                } else {
                    content.findViewById<TextInputLayout>(R.id.stateLayout).isErrorEnabled = false
                }
            }
        }

        btnDelete.setOnClickListener {
            if (markerId != "") {
                myRef.child(oldState).child(oldType).child(markerId).get().addOnSuccessListener {
                    val markId = it.key
                    val markLat = it.child("latitude").value.toString()
                    val markLong = it.child("longitude").value.toString()
                    val markTitle = it.child("title").value.toString()
                    val markDesc = it.child("description").value.toString()
                    val markType = it.child("type").value.toString()
                    val phone = it.child("phoneNo").value.toString()
                    val add = it.child("address").value.toString()
                    val state = it.child("state").value.toString()

                    val undoMarker =
                        Marker(markLat, markLong, markType, markTitle, markDesc, phone, add, state)


                    myRef.child(oldState).child(oldType).child(markerId).removeValue()
                        .addOnSuccessListener {
                            dialog.dismiss()
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
                                    val setMark = LatLng(markLat.toDouble(), markLong.toDouble())
                                    if (oldType == "Services") {
                                        map.addMarker(
                                            MarkerOptions()
                                                .position(setMark)
                                                .title(markTitle)
                                                .snippet(markId)
                                                .icon(
                                                    BitmapDescriptorFactory.defaultMarker(
                                                        BitmapDescriptorFactory.HUE_GREEN
                                                    )
                                                )
                                        )
                                    } else {
                                        map.addMarker(
                                            MarkerOptions()
                                                .position(setMark)
                                                .title(markTitle)
                                                .snippet(markId)
                                                .icon(
                                                    BitmapDescriptorFactory.defaultMarker(
                                                        BitmapDescriptorFactory.HUE_CYAN
                                                    )
                                                )
                                        )
                                    }

                                    myRef.child(state).child(markType).child(markId.toString())
                                        .setValue(undoMarker).addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Undo Successfully!!!",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
            } else {
                defaultId.remove()
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener {
            if (markerId == "") {
                defaultId.remove()
                dialog.dismiss()
            } else {
                defaultId.hideInfoWindow()
                dialog.dismiss()
            }
        }
    }

    fun viewAlert(defaultId: com.google.android.gms.maps.model.Marker, markerId: String) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())

        val content =
            LayoutInflater.from(this.requireContext()).inflate(R.layout.marker_details, null)

        val database = Firebase.database
        val myRef = database.getReference("state")

        val type = content.findViewById<TextView>(R.id.markerType)
        val title = content.findViewById<TextView>(R.id.markerTitle)
        val description = content.findViewById<TextView>(R.id.markerDescription)
        val phoneNo = content.findViewById<TextView>(R.id.markerPhoneNo)
        val address = content.findViewById<TextView>(R.id.markerAddress)
        val state = content.findViewById<TextView>(R.id.markerState)
        var getId = ""

        if (markerId != "") {
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (s in snapshot.children) { //state list
                        for (t in s.children) { //type

                            if (t.key == "Services" || t.key == "Facilities") {
                                if (t.hasChild(markerId)) {
                                    for (a in t.children) {
                                        if (a.key == markerId) {
                                            getId = a.key.toString()
                                            type.text = a.child("type").value.toString()
                                            title.text = a.child("title").value.toString()
                                            description.text =
                                                a.child("description").value.toString()
                                            phoneNo.text = a.child("phoneNo").value.toString()
                                            address.text = a.child("address").value.toString()
                                            state.text = a.child("state").value.toString()
                                            break
                                        }
                                    }
                                }
                            }

                            if (t.key == "Individual") {
                                for (a in t.children) { //member ID
                                    if (a.hasChild(markerId)) {
                                        for (v in a.children) {
                                            if (v.key == markerId) {
                                                getId = a.child(markerId).key.toString()
                                                type.text =
                                                    a.child(markerId).child("type").value.toString()
                                                title.text = a.child(markerId)
                                                    .child("title").value.toString()
                                                description.text = a.child(markerId)
                                                    .child("description").value.toString()
                                                phoneNo.text = a.child(markerId)
                                                    .child("phoneNo").value.toString()
                                                address.text = a.child(markerId)
                                                    .child("address").value.toString()
                                                state.text = a.child(markerId)
                                                    .child("state").value.toString()
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
//        else{
//            type.text = ""
//            title.text = ""
//            description.text = ""
//            phoneNo.text = ""
//            address.text = ""
//            state.text = ""
//        }

        builder.setView(content)
        builder.setCancelable(false)

        val dialog = builder.show()

        val btnClose = content.findViewById<Button>(R.id.btnClose)

        btnClose.setOnClickListener {
            defaultId.hideInfoWindow()
            dialog.dismiss()
        }
    }
}