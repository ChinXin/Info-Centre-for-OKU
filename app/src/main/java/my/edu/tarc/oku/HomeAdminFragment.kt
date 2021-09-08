package my.edu.tarc.oku

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class HomeAdminFragment : Fragment() {

    private lateinit var map: GoogleMap
    //private val TAG = HomeAdminFragment::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1

    private val callback = OnMapReadyCallback { googleMap ->
        val sydney = LatLng(4.5447595, 101.1049368)
        map = googleMap

        //optional style map
        //setMapStyle(map)
        enableMyLocation()
        setMapLongClick(map)
        map.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this.requireContext()))

        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

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
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            val test = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            test.showInfoWindow()

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

}
