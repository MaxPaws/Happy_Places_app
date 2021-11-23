package com.coldkitchen.happyplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.coldkitchen.happyplaces.R
import com.coldkitchen.happyplaces.models.PlaceModel
import kotlinx.android.synthetic.main.activity_add_happy_place.*
import kotlinx.android.synthetic.main.activity_happy_place_details.*

class HappyPlaceDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_details)

        var happyPlaceDetailModel: PlaceModel? = null

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS))
            // Get extra in our custom model format:
            happyPlaceDetailModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS)
                as PlaceModel?

        if(happyPlaceDetailModel != null){
            setSupportActionBar(toolbar_place_details)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.title
            toolbar_place_details.setNavigationOnClickListener {
                onBackPressed()
            }

            iv_details_place_image.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            tv_details_description.text = happyPlaceDetailModel.description
            tv_details_location.text = happyPlaceDetailModel.location

            btn_view_on_map.setOnClickListener {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)
                startActivity(intent)
            }
        }
    }
}