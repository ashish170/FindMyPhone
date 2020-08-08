package com.example.findmyphone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val bundle: Bundle? =intent.extras
        val phoneNum=bundle!!.getString("PhoneNumber")
        val myRef = FirebaseDatabase.getInstance().reference
        myRef!!.child("Users").child(phoneNum.toString()).child("location")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val td=snapshot!!.value as HashMap<String,Any>
                        val lat=td["lat"].toString()
                        val longi=td["long"].toString()
                        sydney= LatLng(lat.toDouble(),longi.toDouble())
                        ls=td["Last Seen"].toString()
                        loadMap()

                    }
                    catch (ex:Exception)
                    {

                    }

                }

            })

    }

    fun loadMap()
    {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    companion object{
        var sydney = LatLng(-34.0, 151.0)
        var ls="Not Defined"
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera

        mMap.addMarker(MarkerOptions().position(sydney).title(ls))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,10f))
    }
}