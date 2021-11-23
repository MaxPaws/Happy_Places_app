package com.coldkitchen.happyplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.lang.Exception
import java.util.*

class GetAdressFromLatLong(context: Context,
                           private val latitude: Double,
                           private val longitude: Double): AsyncTask<Void, String, String>() {

    // Coder to readable place, using passed context
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    private lateinit var mAddressListener: AddressListener

    interface AddressListener{
        fun onAddressFound(address: String?)
        fun onError()
    }

    override fun doInBackground(vararg params: Void?): String {
       try {
           val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
           if(addressList != null && addressList.isNotEmpty()){
               val address: Address = addressList[0]
               val sb = StringBuilder()
               // Taking whole address, all lines of it,
               // and converting into string by string builder value
               for(i in 0..address.maxAddressLineIndex){
                   sb.append(address.getAddressLine(i)).append(" ")
               }
               sb.deleteCharAt(sb.length - 1)
               return sb.toString()
           }
       } catch(e: Exception) {
           e.printStackTrace()
       }
        return "Address is unavailable"
    }

    override fun onPostExecute(result: String?) {
        if(result == null)
            mAddressListener.onError()
        else
            mAddressListener.onAddressFound(result)
        super.onPostExecute(result)
    }

    fun setAddressListener(addressListener: AddressListener){
        mAddressListener = addressListener
    }

    // Execute async task
    fun getAddress()
    {
        execute()
    }
                           }