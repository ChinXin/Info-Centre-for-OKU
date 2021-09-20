package my.edu.tarc.oku

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Address
import android.location.Geocoder
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import my.edu.tarc.oku.databinding.FragmentHomeMemberBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.ArrayList
import java.util.regex.Pattern

import my.edu.tarc.oku.data.*
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.gms.maps.CameraUpdateFactory


class HomeMemberFragment : Fragment(), MemberEventResultAdapter.OnItemClickListener {

    private lateinit var binding: FragmentHomeMemberBinding
    lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var name: String
    var infoWindowListener: ValueEventListener? = null
    val database = Firebase.database
    val myRef = database.getReference("state")
    private var eventList: MutableList<Event> = ArrayList()
    private lateinit var session: UserSessionManager
    private lateinit var user: HashMap<String?, String?>
    private lateinit var username: String
    private lateinit var status: String
    private var eventClicked = false
    private var facilitiesClicked = false
    private var servicesClicked = false
    private var search = ""
    lateinit var myRecyclerView: RecyclerView
    private var eventMarker = false
    var eventLatLng : LatLng? = null
    var eventTitle : String? = null

    companion object {
        private val strokeColor = 0xFFD8D7D7
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        enableMyLocation()

        //Go to my current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation.addOnSuccessListener{
            val currentLat = it.latitude.toString()
            val currentLong = it.longitude.toString()
            val currentLocation = LatLng(currentLat.toDouble(), currentLong.toDouble())
            val move = CameraUpdateFactory.newLatLngZoom(currentLocation, 17f)
            map.animateCamera(move)
        }

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (s in snapshot.children) {
                    for (t in s.children) {
                        if (t.key == "Services" || t.key == "Facilities") {
                            for (m in t.children) {
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

        setMapLongClick(map)
        map.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this.requireContext()))

        map.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: com.google.android.gms.maps.model.Marker): Boolean {
                var currentLat = ""
                var currentLong = ""
                p0.showInfoWindow()

                //navigate
                binding.routeFab3.setOnClickListener {
                    clear()

                    if(eventMarker == true){
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(eventLatLng)
                                .snippet("Event Marker")
                                .title(eventTitle)
                                .icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_ORANGE
                                    )
                                )
                        )
                    }

                    fusedLocationClient =
                        LocationServices.getFusedLocationProviderClient(requireContext())
                    fusedLocationClient.lastLocation.addOnSuccessListener {
                        currentLat = it.latitude.toString()
                        currentLong = it.longitude.toString()
                        val origin = LatLng(currentLat.toDouble(), currentLong.toDouble())
                        val destination = LatLng(p0.position.latitude, p0.position.longitude)
                        val URL = getDirectionURL(origin, destination)

                        GetDirection(URL).execute()
                        callRoute(p0, map)
                    }
                }

                map.setOnInfoWindowClickListener {
                    if(p0.snippet == "Event Marker"){
                        Toast.makeText(context,"This marker is not able to view.",Toast.LENGTH_LONG).show()
                    }else{
                        var markerId = p0.snippet

                    infoWindowListener = myRef.addValueEventListener(object : ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {
                            var check = false
                            var count = 0
                            for (x in snapshot.children) {
                                val individual = x.child("Individual").child(name).hasChild(markerId)
                                val facilities = x.child("Facilities").hasChild(markerId)
                                val services = x.child("Services").hasChild(markerId)

                                if (facilities || services) {
                                    viewAlert(p0, markerId.toString())
                                    check = true
                                    break
                                } else if (individual) {
                                    basicAlert(
                                        p0,
                                        markerId.toString(),
                                        p0.position.latitude.toString(),
                                        p0.position.longitude.toString()
                                    )
                                    check = true
                                    break
                                }
                                count++
                            }
                            if (count == snapshot.childrenCount.toInt() && !check) {
                                viewAlert(p0, markerId.toString())
                                Log.i("test1234", "Line 219")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                    }
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

        session = UserSessionManager(requireContext().applicationContext)
        user = session.userDetails
        username = user[UserSessionManager.KEY_NAME].toString()
        status = user[UserSessionManager.KEY_STATUS].toString()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_member, container, false)

        myRecyclerView = binding.eventResultViewM

        if (eventClicked) {
            binding.btnEventM.setBackgroundColor(strokeColor.toInt())
            searchEvent(search)
        }

        binding.btnEventM.setOnClickListener {
            if (eventClicked) {
                eventClicked = false
                binding.btnServicesM.isClickable = true
                binding.btnFacilitiesM.isClickable = true
                if (search == "") {
                    binding.eventResultViewM.visibility = View.INVISIBLE
                } else {
                    searchEvent(search)
                }
                binding.btnEventM.setBackgroundColor(Color.WHITE)
            } else {
                eventClicked = true
                binding.btnServicesM.isClickable = false
                binding.btnFacilitiesM.isClickable = false
                eventList.clear()
                searchEvent(search)
                binding.btnEventM.setBackgroundColor(strokeColor.toInt())
            }
        }
        binding.btnFacilitiesM.setOnClickListener {
            if (!facilitiesClicked) {
                facilitiesClicked = true
                binding.btnServicesM.isClickable = false
                binding.btnEventM.isClickable = false
                it.setBackgroundColor(strokeColor.toInt())
                searchFacilities(search)
            } else {
                facilitiesClicked = false
                binding.btnServicesM.isClickable = true
                binding.btnEventM.isClickable = true
                it.setBackgroundColor(Color.WHITE)
                clear()
            }
        }
        binding.btnServicesM.setOnClickListener {
            if (!servicesClicked) {
                servicesClicked = true
                binding.btnEventM.isClickable = false
                binding.btnFacilitiesM.isClickable = false
                it.setBackgroundColor(strokeColor.toInt())
                searchServices(search)
            } else {
                servicesClicked = false
                binding.btnEventM.isClickable = true
                binding.btnFacilitiesM.isClickable = true
                it.setBackgroundColor(Color.WHITE)
                clear()
            }
        }

        binding.searchM.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText != null && newText.trim().isNotEmpty()) {
                    eventList.clear()
                    map.clear()
                    val letters: CharArray = newText.toCharArray()
                    val firstLetter = letters[0].toString().lowercase()
                    val remainingLetters: String = newText.substring(1).lowercase()
                    search = "$firstLetter$remainingLetters"

                    if (eventClicked) {
                        searchEvent(search)
                    }
                    else if (facilitiesClicked){
                        searchFacilities(search)
                    }
                    else if(servicesClicked){
                        searchServices(search)
                    }
                    else {
                        searchEvent(search)
                        searchFacilities(search)
                    }
                } else {
                    search = ""
                    binding.eventResultViewM.visibility = View.VISIBLE

                    if (facilitiesClicked) {
                        searchFacilities(search)
                    }else if(servicesClicked){
                        searchServices(search)
                    }
                }
                return false
            }
        })

        return binding.root
    }

    override fun onPause() {
        facilitiesClicked = false
        servicesClicked = false
        super.onPause()
    }

    override fun onItemClick(position: Int) {
        val clickedItem: Event = eventList[position]
        var coder = Geocoder(context)
        var address: MutableList<Address> = coder.getFromLocationName(clickedItem.address, 5)

        if (address.isEmpty()) {
            Toast.makeText(context, "Invalid address!", Toast.LENGTH_SHORT).show()
        } else {
            var location: Address = address[0]
            val latLng = LatLng(location.latitude, location.longitude)

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .snippet("Event Marker")
                    .title(clickedItem.title)
                    .icon(
                        BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_ORANGE
                        )
                    )
            )
            eventMarker = true
            eventLatLng = latLng
            eventTitle = clickedItem.title
            val move = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            map.animateCamera(move)
        }
    }

    private fun initSwipe() {
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (dY == 0.0f) {
                        val swipeItem: Event = eventList[viewHolder.adapterPosition]
                        val action = HomeMemberFragmentDirections.actionHomeMemberFragmentToMemberEventInfo(swipeItem.id)
                        binding.root.findNavController().navigate(action)
                    }
                }
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(myRecyclerView)
    }

    private fun searchEvent(searchS: String) {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventList.clear()
                for (s in snapshot.children) {
                    for (e in s.child("Events").children) {
                        if (e.child("title").value.toString().lowercase().contains(searchS)) {
                            binding.eventResultViewM.visibility = View.VISIBLE
                            val getId = e.key.toString()
                            val title = e.child("title").value.toString()
                            val date = e.child("date").value.toString()
                            val time = e.child("time").value.toString()
                            val address = e.child("address").value.toString()
                            val state = e.child("state").value.toString()
                            val description = e.child("description").value.toString()
                            val image = e.child("image").value.toString()
                            val link = e.child("link").value.toString()
                            val phone = e.child("phone").value.toString()
                            val event = Event(
                                getId,
                                image,
                                title,
                                date,
                                time,
                                address,
                                state,
                                description,
                                link,
                                phone
                            )
                            eventList.add(event)
                        } else if (searchS == "") {
                            binding.eventResultViewM.visibility = View.VISIBLE
                            val getId = e.key.toString()
                            val title = e.child("title").value.toString()
                            val date = e.child("date").value.toString()
                            val time = e.child("time").value.toString()
                            val address = e.child("address").value.toString()
                            val state = e.child("state").value.toString()
                            val description = e.child("description").value.toString()
                            val image = e.child("image").value.toString()
                            val link = e.child("link").value.toString()
                            val phone = e.child("phone").value.toString()
                            val event = Event(
                                getId,
                                image,
                                title,
                                date,
                                time,
                                address,
                                state,
                                description,
                                link,
                                phone
                            )
                            eventList.add(event)
                        }
                    }
                }
                if (eventList.isEmpty()) {
                    binding.eventResultViewM.visibility = View.INVISIBLE
                }

                myRecyclerView.adapter =
                    MemberEventResultAdapter(eventList, this@HomeMemberFragment)
                myRecyclerView.setHasFixedSize(true)
                initSwipe()
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun searchFacilities(searchS: String){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                map.clear()
                for (s in snapshot.children) {
                    for (e in s.child("Facilities").children) {
                        if(e.child("title").value.toString().lowercase().contains(searchS)){
                            val id = e.key
                            val lat = e.child("latitude").value.toString().toDouble()
                            val long = e.child("longitude").value.toString().toDouble()
                            val title = e.child("title").value.toString()
                            val type = e.child("type").value.toString()
                            val marker = LatLng(lat, long)

                            map.addMarker(
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
                        }else if(searchS == ""){
                            val id = e.key
                            val lat = e.child("latitude").value.toString().toDouble()
                            val long = e.child("longitude").value.toString().toDouble()
                            val title = e.child("title").value.toString()
                            val type = e.child("type").value.toString()
                            val marker = LatLng(lat, long)

                            map.addMarker(
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
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun searchServices(searchS: String){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                map.clear()
                for (s in snapshot.children) {
                    for (e in s.child("Services").children) {
                        if(e.child("title").value.toString().lowercase().contains(searchS)){
                            val id = e.key
                            val lat = e.child("latitude").value.toString().toDouble()
                            val long = e.child("longitude").value.toString().toDouble()
                            val title = e.child("title").value.toString()
                            val type = e.child("type").value.toString()
                            val marker = LatLng(lat, long)

                            map.addMarker(
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
                        }else if(searchS == ""){
                            val id = e.key
                            val lat = e.child("latitude").value.toString().toDouble()
                            val long = e.child("longitude").value.toString().toDouble()
                            val title = e.child("title").value.toString()
                            val type = e.child("type").value.toString()
                            val marker = LatLng(lat, long)

                            map.addMarker(
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
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
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
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            basicAlert(
                test,
                test.snippet.toString(),
                latLng.latitude.toString(),
                latLng.longitude.toString()
            )
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

        if (infoWindowListener != null) {
            myRef.removeEventListener(infoWindowListener!!)
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        val content = LayoutInflater.from(this.requireContext()).inflate(R.layout.marker_form, null)
        val items = resources.getStringArray(R.array.types)
        val stateList = resources.getStringArray(R.array.stateList)
        val adapter = ArrayAdapter(this.requireContext(), R.layout.dropdown_type, items)
        val adapter2 = ArrayAdapter(this.requireContext(), R.layout.dropdown_state, stateList)
        val autoId = content.findViewById<AutoCompleteTextView>(R.id.autoCompleteList)
        val autoId2 = content.findViewById<AutoCompleteTextView>(R.id.autoCompleteList2)

        autoId2.setAdapter(adapter2)

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
                            if (t.key == "Individual") {
                                for (a in t.children) {
                                    if (a.hasChild(markerId)) {
                                        for (v in a.children) {
                                            if (v.key == markerId) { //marker same
                                                getId = v.key.toString()
                                                oldType = v.child("type").value.toString()
                                                autoId.setText(oldType, false)
                                                title.setText(v.child("title").value.toString())
                                                description.setText(v.child("description").value.toString())
                                                phoneNo.setText(v.child("phoneNo").value.toString())
                                                address.setText(v.child("address").value.toString())
                                                oldState = v.child("state").value.toString()
                                                autoId2.setText(oldState, false)
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
        val btnFeedback = content.findViewById<Button>(R.id.btnGoFeedback)

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
                        myRef.child(oldState).child(oldType).child(name).child(markerId)
                            .removeValue()
                            .addOnSuccessListener {
                                myRef.child(state).child(type).child(name).child(markerId)
                                    .setValue(newMarker)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Update Successfully!!!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        getId = ""
                                        defaultId.hideInfoWindow()
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
                    clear()
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
                myRef.child(oldState).child(oldType).child(name).child(markerId).get()
                    .addOnSuccessListener {
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
                            Marker(
                                markLat,
                                markLong,
                                markType,
                                markTitle,
                                markDesc,
                                phone,
                                add,
                                state
                            )


                        myRef.child(oldState).child(oldType).child(name).child(markerId)
                            .removeValue()
                            .addOnSuccessListener {
                                dialog.dismiss()
                                clear()

                                val v: View =
                                    this.requireActivity().findViewById(android.R.id.content)
                                Snackbar.make(v, "Marker Deleted!", Snackbar.LENGTH_LONG)
                                    .setAction("UNDO", View.OnClickListener {
                                        val setMark =
                                            LatLng(markLat.toDouble(), markLong.toDouble())
                                        map.addMarker(
                                            MarkerOptions()
                                                .position(setMark)
                                                .title(markTitle)
                                                .snippet(markId)
                                                .icon(
                                                    BitmapDescriptorFactory.defaultMarker(
                                                        BitmapDescriptorFactory.HUE_YELLOW
                                                    )
                                                )
                                        )

                                        myRef.child(state).child(markType).child(name)
                                            .child(markId.toString())
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

        btnFeedback.setOnClickListener {
            dialog.dismiss()
            val action = HomeMemberFragmentDirections.actionHomeMemberFragmentToFeedbackFragment2(
                markerId,
                name
            )
            binding.root.findNavController().navigate(action)
        }
    }

    fun viewAlert(defaultId: com.google.android.gms.maps.model.Marker, markerId: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val content = LayoutInflater.from(this.requireContext()).inflate(R.layout.marker_details, null)
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
                    for (s in snapshot.children) {
                        for (t in s.children) {

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
                                for (a in t.children) {
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

        builder.setView(content)
        builder.setCancelable(false)

        val dialog = builder.show()
        val btnFeedback = content.findViewById<Button>(R.id.btnGoFeedback2)
        val btnClose = content.findViewById<Button>(R.id.btnClose)

        btnClose.setOnClickListener {
            defaultId.hideInfoWindow()
            dialog.dismiss()
        }

        btnFeedback.setOnClickListener {
            dialog.dismiss()
            val action = HomeMemberFragmentDirections.actionHomeMemberFragmentToFeedbackFragment2(
                markerId,
                name
            )
            binding.root.findNavController().navigate(action)
        }
    }
}