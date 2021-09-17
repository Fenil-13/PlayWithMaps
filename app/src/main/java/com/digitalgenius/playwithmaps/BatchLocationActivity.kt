package com.digitalgenius.playwithmaps

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import com.digitalgenius.playwithmaps.databinding.ActivityBatchLocationBinding
import com.digitalgenius.playwithmaps.utils.LocationResultHelper
import com.digitalgenius.playwithmaps.utils.SharedPreferenceManager
import com.google.android.gms.location.*

class BatchLocationActivity : AppCompatActivity() ,SharedPreferences.OnSharedPreferenceChangeListener{

    private lateinit var binding:ActivityBatchLocationBinding

    private lateinit var mLocationClient:FusedLocationProviderClient

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val location = locationResult.locations

            val helper=LocationResultHelper(this@BatchLocationActivity,location)

            helper.showNotification()

            helper.saveLocationResults()
            binding.tvResult.text=helper.getLocationText()



        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBatchLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mLocationClient=LocationServices.getFusedLocationProviderClient(this@BatchLocationActivity)

        binding.btnRequestLocationBatch.setOnClickListener {
            requestBatchLocationUpdates()
        }

    }

    private fun requestBatchLocationUpdates() {
        val locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000
        locationRequest.maxWaitTime= 10000

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationClient.requestLocationUpdates(locationRequest,mLocationCallback,null)
        }

    }

    override fun onPause() {
        super.onPause()
        mLocationClient.removeLocationUpdates(mLocationCallback)
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this@BatchLocationActivity)
            .registerOnSharedPreferenceChangeListener(this@BatchLocationActivity)
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this@BatchLocationActivity)
            .unregisterOnSharedPreferenceChangeListener(this@BatchLocationActivity)
    }

    override fun onResume() {
        super.onResume()
        binding.tvResult.text=LocationResultHelper.getSavedLocationResult()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key==LocationResultHelper.KEY_LOCATION_RESULT){
            binding.tvResult.text=LocationResultHelper.getSavedLocationResult()
        }
    }
}