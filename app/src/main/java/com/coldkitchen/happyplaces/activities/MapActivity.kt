package com.coldkitchen.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.coldkitchen.happyplaces.R
import com.coldkitchen.happyplaces.models.PlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_happy_place_details.*
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mHappyPlaceDetail: PlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS))
            mHappyPlaceDetail = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS)
                    as PlaceModel?

        if(mHappyPlaceDetail != null) {
            setSupportActionBar(tb_map)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mHappyPlaceDetail!!.title
            tb_map.setNavigationOnClickListener {
                onBackPressed()
            }

            //  Assign fragment with map and get map async
            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }
    }

    // Going to our marker with Happy place location, on map ready
    override fun onMapReady(p0: GoogleMap?) {
        val position = LatLng(mHappyPlaceDetail!!.latitude, mHappyPlaceDetail!!.longitude)
        p0!!.addMarker(MarkerOptions().position(position).title(mHappyPlaceDetail!!.location))

        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 11f)
        p0.animateCamera(newLatLngZoom)
    }
}