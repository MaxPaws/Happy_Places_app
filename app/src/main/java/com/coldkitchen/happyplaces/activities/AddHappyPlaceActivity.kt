package com.coldkitchen.happyplaces.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.karumi.dexter.Dexter
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.coldkitchen.happyplaces.DB.DBHandler
import com.coldkitchen.happyplaces.R
import com.coldkitchen.happyplaces.models.PlaceModel
import com.coldkitchen.happyplaces.utils.GetAdressFromLatLong
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_happy_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private val cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var savedImageUri: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var mHappyPlaceDetails: PlaceModel? = null
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setSupportActionBar(toolbar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }

        if(!Places.isInitialized())
            Places.initialize(this@AddHappyPlaceActivity, resources.getString(R.string.API_GM))

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getSerializableExtra(
                MainActivity.EXTRA_PLACE_DETAILS) as PlaceModel
        }

        dateSetListener = DatePickerDialog.OnDateSetListener {
                view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        if(mHappyPlaceDetails != null){
            supportActionBar?.title = "Edit Happy Place"
            btn_save.text = "UPDATE"
            et_title.setText(mHappyPlaceDetails!!.title)
            et_description.setText(mHappyPlaceDetails!!.description)
            et_date.setText(mHappyPlaceDetails!!.date)
            et_location.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude
            savedImageUri = Uri.parse(mHappyPlaceDetails!!.image)
            iv_place_image.setImageURI(savedImageUri)
        }

        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        et_location.setOnClickListener(this)
        tv_selectCurrentLocation.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){

            R.id.et_date -> {
                DatePickerDialog(this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                val pictureDialogItems = arrayOf("Select photo from gallery", "Take photo")
                pictureDialog.setTitle("Select Action")
                pictureDialog.setItems(pictureDialogItems){
                        _, which ->
                    when(which){
                        0 -> addPhotoFromGallery()
                        1 -> addPhotoFromCamera()
                    }
                }.show()
            }

            R.id.btn_save -> {
                when{
                    TextUtils.isEmpty(et_title.text.toString()) ->{
                        Toast.makeText(this, "Please, enter a title",
                            Toast.LENGTH_LONG).show()
                    }
                    TextUtils.isEmpty(et_description.text.toString()) ->{
                        Toast.makeText(this, "Please, enter a description",
                            Toast.LENGTH_LONG).show()
                    }
                    TextUtils.isEmpty(et_location.text.toString()) ->{
                        Toast.makeText(this, "Please, enter a location",
                            Toast.LENGTH_LONG).show()
                    }
                    savedImageUri == null ->{
                        Toast.makeText(this, "Please, select an image",
                            Toast.LENGTH_LONG).show()
                    } else ->{
                        val happyPlace = PlaceModel(
                            if(mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                            et_title.text.toString(),
                            savedImageUri.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dbHandler = DBHandler(this)
                        if(mHappyPlaceDetails == null) {
                            val addHappyPlaceResult = dbHandler.addHappyPlace(happyPlace)
                            if (addHappyPlaceResult > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else{
                            val updateHappyPlaceResult = dbHandler.updateHappyPlace(happyPlace)
                            if(updateHappyPlaceResult > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }

            }

            R.id.et_location -> {
                try{
                    val fields = listOf(
                        Place.Field.LAT_LNG, Place.Field.NAME,
                        Place.Field.LAT_LNG, Place.Field.ADDRESS
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields)
                            .build(this@AddHappyPlaceActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                }
                catch (e: Exception){
                    e.printStackTrace()
                }
            }

            R.id.tv_selectCurrentLocation -> {
                //if(!isLocationEnabled()){
                //    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                //    startActivity(intent)

                Dexter.withContext(this)
                    .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                    .withListener(object: MultiplePermissionsListener{
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?)
                        {
                            if(report!!.areAllPermissionsGranted()){
                                requestNewLocationData()
                            }else showDeniedPermissionDialog()
                        }
                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?)
                        {
                            showDeniedPermissionDialog()
                            token?.continuePermissionRequest()
                        }
                    }).onSameThread().check()
            }
        }
    }

    private fun addPhotoFromGallery() {

        Dexter.withContext(this)
            .withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted()){

                    val galleryIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)

                }else showDeniedPermissionDialog()
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?)
            {
                showDeniedPermissionDialog()
                token?.continuePermissionRequest()
            }
        }).onSameThread().check()
    }

    // Dexter Single permission camera use:
    /*
    private fun addPhotoFromCamera() {

        Dexter.withContext(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?)
                {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    showDeniedPermissionDialog()
                }

                override fun onPermissionRationaleShouldBeShown(
                    request: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    showDeniedPermissionDialog()
                    token?.continuePermissionRequest()
                }
            }).onSameThread().check()
    }
    */

    // Dexter Multiple permissions camera use:

    private fun addPhotoFromCamera() {

        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)
            .withListener(object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?)
                {
                    if(report!!.areAllPermissionsGranted()){

                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA)

                    }else showDeniedPermissionDialog()
                }
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?)
                {
                    showDeniedPermissionDialog()
                    token?.continuePermissionRequest()
                }
            }).onSameThread().check()
    }


    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }

    private fun showDeniedPermissionDialog(){
        AlertDialog.Builder(this@AddHappyPlaceActivity).setMessage("" +
                "It looks like you have turned off permission required for this feature. " +
                "It can be enabled under the Applications Settings.")
            .setPositiveButton("GO TO SETTINGS"){
                    _, _ ->
                try{
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                GALLERY -> if(data != null){
                    val contentURI = data.data
                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver, contentURI)
                        iv_place_image.setImageBitmap(selectedImageBitmap)

                        savedImageUri = saveImageToInternalStorage(selectedImageBitmap)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity, "Failed to load image!",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                CAMERA -> if(data != null){
                    val thumbNail: Bitmap = data.extras!!.get("data") as Bitmap
                    iv_place_image.setImageBitmap(thumbNail)

                    savedImageUri = saveImageToInternalStorage(thumbNail)
                }

                PLACE_AUTOCOMPLETE_REQUEST_CODE -> {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    Log.e("AddHappyPlace", "onActivityResult: place=${place}")
                    et_location.setText(place.address)
                    if (et_title.text.isNullOrEmpty()) {
                        et_title.setText(place.name)
                    }
                    mLatitude = place.latLng!!.latitude
                    mLongitude = place.latLng!!.longitude
                }
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            var status: Status? = Autocomplete.getStatusFromIntent(data!!)
            Log.e("AddHappyPlace", "onResult[PLACE] error: ${status?.statusMessage}")
        }
    }

    private fun saveImageToInternalStorage(passedBitmap: Bitmap): Uri{
        val wrapper = ContextWrapper(applicationContext)
        // Get the image in a file
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            passedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

        // Parsing file path to Uri format and return
        return Uri.parse(file.absolutePath)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE)
                as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // Creating request and requesting location updates,
    // with a callback that takes last location latitude and longitude
    // Suppressing missing permission error,
    // because we checked its permission before this function execution
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.numUpdates = 1

        mFusedLocationProviderClient.requestLocationUpdates(locationRequest,
            mLocationCallback, Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult?) {
            val mLastLocation: Location = p0!!.lastLocation
            mLatitude = mLastLocation.latitude
            mLongitude = mLastLocation.longitude

            val addressTask = GetAdressFromLatLong(this@AddHappyPlaceActivity, mLatitude, mLongitude)
            addressTask.setAddressListener(object: GetAdressFromLatLong.AddressListener{
                override fun onAddressFound(address: String?){
                    et_location.setText(address)
                }
                override fun onError(){
                    Log.e("Get Address:: ", "Error")
                }
            })
            addressTask.getAddress()
        }
    }
}