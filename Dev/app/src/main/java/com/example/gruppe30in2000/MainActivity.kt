package com.example.gruppe30in2000


import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity


import android.widget.Toast

import com.example.gruppe30in2000.API.AirQualityStation
import com.example.gruppe30in2000.API.AsyncApiGetter
import com.example.gruppe30in2000.API.OnTaskCompleted
import com.example.gruppe30in2000.FavCity.FavoriteCityFragment
import com.example.gruppe30in2000.Map.MapFragment
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.DateTime
import com.google.gson.GsonBuilder
import com.fatboyindustrial.gsonjodatime.Converters
import org.joda.time.Seconds
import com.example.gruppe30in2000.Settings.LocationPermission
import com.example.gruppe30in2000.Settings.Notification
import com.example.gruppe30in2000.Settings.PreferenceFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.util.*
import android.net.ConnectivityManager





class MainActivity : AppCompatActivity(), OnTaskCompleted {

    // duration in seconds between updates

    lateinit var notificationManager : NotificationManager

    companion object {
        var staticAirQualityStationsList = ArrayList<AirQualityStation>()
        val updateTime = 3600
        // name of shared preferences
        val preference = "station preferences"
        // key for arrayList of measurements
        val stations = "station measurements"
        // key for datetime of last measurement
        val lastCheck = "last measurements"

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.initial_welcome_view)
        loadStations()

        // Creates LocationPermission object and asks user to allow location
        val lp = LocationPermission(this)
        lp.enableMyLocation()
    }


    override fun onTaskCompletedApiGetter(list: ArrayList<AirQualityStation>, saveData: Boolean){
        if(list.isEmpty()){
            Toast.makeText(this, "Kunne ikke hente data", Toast.LENGTH_LONG).show()
        } else {
            staticAirQualityStationsList = list
        }

        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        GlobalScope.launch {
            if (saveData)
                save()
        }

        replaceFragment(FavoriteCityFragment())


    }



    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                replaceFragment(FavoriteCityFragment())
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_map -> {
                val mf = MapFragment()
                replaceFragment(mf)

                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_notifications -> {
                val mf = PreferenceFragment()
                replaceFragment(mf)
                notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val n = Notification(this, notificationManager)
                n.notifyer()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }



    /**
     * Checks if time difference (hours) is greater than a specified
     * number, for a given date object.
     */
    private fun checkTimePassed(lastCheck : DateTime, seconds : Int): Boolean {
        val currentTime = DateTime()
        val difference : Int = Seconds.secondsBetween(lastCheck, currentTime).seconds

        return seconds < difference

    }





    /**
     * Metoden skal kalles naar main vinduet loades.
     */
    fun loadStations() {

        val sharedPreferences = getSharedPreferences(preference, Context.MODE_PRIVATE)
        var lastCheckJson : String? = sharedPreferences.getString(lastCheck, null)

        val gson = Gson()
        // custom gson parser for joda-time objects
        val dateGson = Converters.registerDateTime(GsonBuilder()).create()

        if (networkConnected() && (lastCheckJson == null || checkTimePassed(dateGson.fromJson(lastCheckJson, DateTime::class.java), updateTime))) {
            apiRequest()
        } else {

            var stationsJson : String? = sharedPreferences.getString(stations, null)

            if (stationsJson == null)
                apiRequest()

            else {
                // load already saved
                val stationList = gson.fromJson<ArrayList<AirQualityStation>>(stationsJson)
                onTaskCompletedApiGetter(stationList, false)
            }
        }

    }

    fun apiRequest() {
        // new get request is neccesary
        //gets data from api - runs in async thread

        val asyncApiGetter = AsyncApiGetter(this)
        asyncApiGetter.execute()
    }


    private fun save() {
        // save data in shared prefs
        val sharedPreferences = getSharedPreferences(preference, Context.MODE_PRIVATE)
        val gson = Gson()
        val dateGson = Converters.registerDateTime(GsonBuilder()).create()
        val editor = sharedPreferences?.edit()

        val stationsJson = gson.toJson(staticAirQualityStationsList)
        val lastCheckJson = dateGson.toJson(DateTime())

        editor?.putString(stations, stationsJson)
        editor?.putString(lastCheck, lastCheckJson)
        editor?.apply()
    }


    private fun networkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

}
