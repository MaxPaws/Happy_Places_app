package com.coldkitchen.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coldkitchen.happyplaces.DB.DBHandler
import com.coldkitchen.happyplaces.R
import com.coldkitchen.happyplaces.activities.AddHappyPlaceActivity
import com.coldkitchen.happyplaces.activities.MainActivity
import com.coldkitchen.happyplaces.models.PlaceModel
import kotlinx.android.synthetic.main.activity_add_happy_place.view.*
import kotlinx.android.synthetic.main.item_happy_place.view.*

open class TakingHappyPlacesAdapter(private val context: Context,
                                    private var items: ArrayList<PlaceModel>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_happy_place, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val wrappedModel = items[position]

        if(holder is MyViewHolder) {
            holder.itemView.civ_placeImage.setImageURI(Uri.parse(wrappedModel.image))
            holder.itemView.tv_title.text = wrappedModel.title
            holder.itemView.tv_description.text = wrappedModel.description
        }

        holder.itemView.setOnClickListener {
            if(onClickListener != null){
                onClickListener!!.OnClick(position, wrappedModel)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setCustomOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun OnClick(position: Int, model: PlaceModel)
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int){
        val intent = Intent(context, AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, items[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun notifyDeleteItem(position: Int){
        val dbHandler = DBHandler(context)
        val isDeleted = dbHandler.deleteHappyPlace(items[position])
        if(isDeleted > 0){
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}