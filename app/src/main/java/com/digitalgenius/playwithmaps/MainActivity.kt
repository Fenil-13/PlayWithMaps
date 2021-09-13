package com.digitalgenius.playwithmaps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.digitalgenius.playwithmaps.databinding.ActivityMainBinding
import com.digitalgenius.playwithmaps.utils.Functions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private var mLocationPermissionGranted = false
    val PERMISSION_REQUEST_CODE = 123
    val PLAY_SERVICES_ERROR_CODE = 123


    private lateinit var mGoogleMap: GoogleMap
    private lateinit var supportMapFragment: SupportMapFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLocationServiceOk()

        supportMapFragment = SupportMapFragment.newInstance()

        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,supportMapFragment)
            .commit()

        supportMapFragment.getMapAsync (this@MainActivity)
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

    }
}