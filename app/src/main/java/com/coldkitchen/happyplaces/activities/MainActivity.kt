package com.coldkitchen.happyplaces.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coldkitchen.happyplaces.DB.DBHandler
import com.coldkitchen.happyplaces.R
import com.coldkitchen.happyplaces.adapters.TakingHappyPlacesAdapter
import com.coldkitchen.happyplaces.models.PlaceModel
import im.dino.dbinspector.helpers.DatabaseHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_dialog_delete_swipe.*
import pl.kitek.rvswipetodelete.SwipeToDeleteCallback
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    companion object{
        var ADD_PLACE_ACTIVITY_REQEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab_addHappyPlace.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQEST_CODE)

        }
        getHappyPlacesListFromLocalDB()
    }

    private fun getHappyPlacesListFromLocalDB() {
        val dbHandler = DBHandler(this)
        val happyPlacesList = dbHandler.getHappyPlacesList()
        Log.e("RESULT: ", "RESULT: $happyPlacesList")

        if(happyPlacesList.size > 0){
            rv_happyPlacesList.visibility = View.VISIBLE
            tv_noPlacesText.visibility = View.GONE
            setUpRecyclerView(happyPlacesList)
        }else{
            rv_happyPlacesList.visibility = View.GONE
            tv_noPlacesText.visibility = View.VISIBLE
        }
    }

    private fun setUpRecyclerView(placesList: ArrayList<PlaceModel>){
        rv_happyPlacesList.layoutManager = LinearLayoutManager(this)
        rv_happyPlacesList.setHasFixedSize(true)

        val placesAdapter = TakingHappyPlacesAdapter(this, placesList)
        rv_happyPlacesList.adapter = placesAdapter

        placesAdapter.setCustomOnClickListener(object: TakingHappyPlacesAdapter.OnClickListener{
            override fun OnClick(position: Int, model: PlaceModel) {
                val intent = Intent(this@MainActivity,
                                    HappyPlaceDetailsActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)

                startActivity(intent)
            }

        })

        val editSwipeHandler = object: SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happyPlacesList.adapter as TakingHappyPlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQEST_CODE)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_happyPlacesList)

        val deleteSwipeHandler = object: SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                customDialogForBackButton(viewHolder)
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_happyPlacesList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQEST_CODE &&
                resultCode == Activity.RESULT_OK)
                    getHappyPlacesListFromLocalDB()

    }

    private fun customDialogForBackButton(viewHolder: RecyclerView.ViewHolder){

        val customDialog = Dialog(this)
        customDialog.setContentView(R.layout.custom_dialog_delete_swipe)

        customDialog.btn_Yes.setOnClickListener {
            val adapter = rv_happyPlacesList.adapter as TakingHappyPlacesAdapter
            adapter.notifyDeleteItem(viewHolder.adapterPosition)
            getHappyPlacesListFromLocalDB()
            customDialog.dismiss()
        }

        customDialog.btn_No.setOnClickListener {
            getHappyPlacesListFromLocalDB()
            customDialog.dismiss()
        }
        customDialog.show()
    }

}