package com.digitalgenius.playwithmaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.ListAdapter
import com.digitalgenius.playwithmaps.databinding.ActivityMainBinding
import com.digitalgenius.playwithmaps.utils.Functions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private var mLocationPermissionGranted = false
    val PERMISSION_REQUEST_CODE = 123
    val PLAY_SERVICES_ERROR_CODE = 123


    private lateinit var mGoogleMap: GoogleMap
    private lateinit var supportMapFragment: SupportMapFragment

    private lateinit var gpsLauncher: ActivityResultLauncher<Intent>

    private lateinit var mLocationClient: FusedLocationProviderClient

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val location = locationResult.lastLocation
            Log.d("MainActivity", "onLocationResult: ${location.latitude}  ${location.longitude}")

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gpsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (checkGPSService()) {
                Toast.makeText(this@MainActivity, "PERMISSION GRANDTED", Toast.LENGTH_SHORT).show()
            } else {
                takeGPSPermission()
            }
        }


        mLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

        checkLocationServiceOk()


//        val googleMapOptions=GoogleMapOptions()
//            .zoomControlsEnabled(true)

        supportMapFragment = SupportMapFragment.newInstance()

        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, supportMapFragment)
            .commit()

        supportMapFragment.getMapAsync(this@MainActivity)


        binding.ivSearchLocation.setOnClickListener {
            Functions.hideSoftKeyboard(this@MainActivity, it!!)

            val locationName = binding.etLocation.text.toString()
            if (locationName.isBlank()) {
                Toast.makeText(this@MainActivity, "Please Enter Something", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
            try {
                val addressList = geocoder.getFromLocationName(locationName, 5)

                Log.d("MainActivity", "onCreate: ${addressList.size}")

                if (addressList.size > 0) {
                    val singleItems = Array(addressList.size) {
                        addressList[it].featureName
                    }

                    for (i in 0 until addressList.size) {
                        singleItems[i] = addressList[i].featureName
                    }

                    var checkedItem = 1

                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Choose One Location")
                        .setSingleChoiceItems(
                            singleItems, checkedItem
                        ) { dialog, which ->
                            dialog.dismiss()
                            checkedItem = which
                        }
                        .setPositiveButton(R.string.choose) { dialog, which ->
                            binding.etLocation.setText(addressList[checkedItem].featureName)
                            goToLocation(
                                addressList[checkedItem].latitude,
                                addressList[which].longitude
                            )
                            setMarkerForLocation(
                                addressList[checkedItem].featureName,
                                LatLng(
                                    addressList[checkedItem].latitude,
                                    addressList[checkedItem].longitude
                                )
                            )
                        }.setNegativeButton(R.string.cancel) { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()

                }
            } catch (e: Exception) {
                Log.d("MainActivity", "onCreate: ${e.message}")
            }


        }


        binding.btnMyLocation.setOnClickListener {
            if (checkGPSService()) {


                val choices = arrayOf("Current Location", "Start Current Location Update","Stop Current Location Update")
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Choose One")
                    .setSingleChoiceItems(choices, 0) { dialog, which ->
                        dialog.dismiss()
                        when (which) {
                            0 -> getOwnCurrentLocation()
                            1 -> getLocationUpdates()
                            2-> stopLocationUpdates()
                        }
                    }.show()
            } else {
                takeGPSPermission()
            }
        }
    }


    private fun getOwnCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                checkLocationServiceOk()
                return
            }
            mLocationClient.lastLocation.addOnCompleteListener {
                Log.d("MainActivity", "getOwnCurrentLocation: ${it.isSuccessful}")
                if (it.isSuccessful) {
                    val location = it.result
                    setMarkerForLocation(
                        "Your Location",
                        LatLng(location.latitude, location.longitude)
                    )
                }
            }.addOnFailureListener {
                Toast.makeText(this@MainActivity, "${it.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun takeGPSPermission() {
        AlertDialog.Builder(this@MainActivity)
            .setTitle("GPS Permission")
            .setMessage("GPS is required for this services")
            .setPositiveButton("Ok") { dialog, which ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                gpsLauncher.launch(intent)
            }
            .setCancelable(false)
            .show()
    }


    private fun initGoogleMap() {
        if (checkServicesOk()) {
            Toast.makeText(this@MainActivity, "Ready to Map", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "Can't Use Map", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationServiceOk() {
        if (mLocationPermissionGranted) {
            initGoogleMap()
        } else {
            if (!Functions.checkPermission(this@MainActivity)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    private fun checkServicesOk(): Boolean {
        val googleApi = GoogleApiAvailability.getInstance()
        val isPlayServiceAvailable = googleApi.isGooglePlayServicesAvailable(this@MainActivity)

        when (isPlayServiceAvailable) {
            ConnectionResult.SUCCESS -> {
                return true
            }
            else -> {
                if (googleApi.isUserResolvableError(isPlayServiceAvailable)) {
                    val dialog = googleApi.getErrorDialog(
                        this@MainActivity,
                        isPlayServiceAvailable,
                        PLAY_SERVICES_ERROR_CODE
                    ) {
                        it?.dismiss()
                    }
                    dialog.show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Play Service are Required for this Application",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return false
    }

    private fun checkGPSService(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
            Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_SHORT).show()
            initGoogleMap()
        } else {
            Toast.makeText(this@MainActivity, "Permission Not Granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        goToLocation(21.269487, 72.958216)
        setMarkerForLocation("Default Location", LatLng(21.269487, 72.958216))
        mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

    }

    private fun goToLocation(lat: Double, lng: Double) {
        val latLng = LatLng(lat, lng)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 7.0f)
        mGoogleMap.moveCamera(cameraUpdate)
    }

    private fun setMarkerForLocation(title: String, latLng: LatLng) {

        val markerOption = MarkerOptions()
            .title(title)
            .position(latLng)
        mGoogleMap.addMarker(markerOption)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_type_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mapTypeNon -> {
                mGoogleMap.mapType = GoogleMap.MAP_TYPE_NONE
            }
            R.id.mapTypeNormal -> {
                mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            R.id.mapTypeHybrid -> {
                mGoogleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            R.id.mapTypeSatelite -> {
                mGoogleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
            R.id.mapTypeTerrain -> {
                mGoogleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getLocationUpdates() {
        val locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationServiceOk()
            return
        }
        mLocationClient.requestLocationUpdates(
            locationRequest,
            mLocationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates(){
        mLocationClient.removeLocationUpdates(mLocationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
}