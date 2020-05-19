package com.weather.app.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dynamitechetan.flowinggradient.FlowingGradientClass
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.weather.app.R
import com.weather.app.network.APIClient
import com.weather.app.network.APIInterface
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_location.*
import java.util.concurrent.TimeUnit

class LocationActivity : AppCompatActivity() {

    private val TAG = "LocationActivity"

    private val delay: Long = 2

    private val PERMISSION_ID = 42

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isForGps = true

    // Stuff for SharedPreferences
    private val APP_PREFERENCES = "weather"
    private val APP_PREFERENCES_CITY = "city"
    private val APP_PREFERENCES_LAT = "lat"
    private val APP_PREFERENCES_LON = "lon"
    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        // Set background
        FlowingGradientClass()
            .setBackgroundResource(R.drawable.translate)
            .onRelativeLayout(relativeLayout)
            .setTransitionDuration(4000)
            .start()
        // Location via GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
        // Init SharedPreferences
        pref = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        // Change FAB icon - if text inputted - just confirm, else - use GPS
        locationText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (s.toString().trim() == "") {
                        floatingActionButton.setImageIcon(
                            Icon.createWithResource(
                                applicationContext,
                                R.drawable.ic_place_black_24dp
                            )
                        )
                        isForGps = true
                    } else {
                        floatingActionButton.setImageIcon(
                            Icon.createWithResource(
                                applicationContext,
                                R.drawable.ic_check_black_24dp
                            )
                        )
                        isForGps = false
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        floatingActionButton.setOnClickListener {
            if (isForGps) {
                fabProgressCircle.show()
                Snackbar.make(
                    relativeLayout,
                    resources.getText(R.string.searching),
                    Snackbar.LENGTH_LONG
                )
                    .show()
            } else {
                fabProgressCircle.hide()
                var res = APIClient.client?.create(APIInterface::class.java)
                res?.getSummary(locationText.text.toString(), APIClient.appid, "metric")
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribeOn(Schedulers.io())
                    ?.doOnComplete {
                        Snackbar.make(
                            relativeLayout,
                            resources.getText(R.string.success),
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
                    ?.doOnError {
                        floatingActionButton.setImageIcon(
                            Icon.createWithResource(
                                applicationContext,
                                R.drawable.ic_place_black_24dp
                            )
                        )
                        isForGps = true
                    }
                    ?.subscribe({
                        startMainActivity(city = locationText.text.toString())
                    },
                        {
                            Snackbar.make(
                                relativeLayout,
                                resources.getText(R.string.fail),
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }
                    )
            }
        }
    }

    fun startMainActivity(city: String = "", lat: Double = -1.0, lon: Double = -1.0) {
        if (!city.equals("")) {
            val editor = pref.edit()
            editor.putString(APP_PREFERENCES_CITY, city)
            editor.apply()
        } else if (lat != -1.0 && lon != -1.0) {
            val editor = pref.edit()
            editor.putFloat(APP_PREFERENCES_LAT, lat.toFloat())
            editor.putFloat(APP_PREFERENCES_LON, lon.toFloat())
            editor.apply()
        }
        Observable
            .timer(delay, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startActivity(Intent(this, MainActivity::class.java))
            }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    }
                }
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var location: Location = locationResult.lastLocation
            fabProgressCircle.hide()
            startMainActivity(lat = location.latitude, lon = location.longitude)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient!!.requestLocationUpdates(
            locationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

}
