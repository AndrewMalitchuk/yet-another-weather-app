package com.weather.app.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dynamitechetan.flowinggradient.FlowingGradientClass
import com.google.android.material.snackbar.Snackbar
import com.vivekkaushik.datepicker.OnDateSelectedListener
import com.weather.app.R
import com.weather.app.adapter.WeatherForDayAdapter
import com.weather.app.entity.detail.WeatherDetail
import com.weather.app.entity.detail.WeatherList
import com.weather.app.entity.summary.WeatherSummary
import com.weather.app.network.APIClient
import com.weather.app.network.APIInterface
import im.dacer.androidcharts.LineView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    val APP_PREFERENCES = "weather"
    val APP_PREFERENCES_CITY = "city"
    val APP_PREFERENCES_LAT = "lat"
    val APP_PREFERENCES_LON = "lon"

    lateinit var pref: SharedPreferences

    private val list: ArrayList<WeatherList> = ArrayList<WeatherList>()

    private val adapter: WeatherForDayAdapter = WeatherForDayAdapter(list)

    lateinit var city: String

    var lat: Float = (-1.0).toFloat()
    var lon: Float = (-1.0).toFloat()

    var isCityMode = false

    var count = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set Toolbar
        setSupportActionBar(toolbar)
        // Set background
        FlowingGradientClass()
            .setBackgroundResource(R.drawable.translate)
            .onLinearLayout(mainLayout)
            .setTransitionDuration(4000)
            .start()
        refresh.isRefreshing = true
        pref = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        if (pref.contains(APP_PREFERENCES_CITY)) {
            city = pref.getString(APP_PREFERENCES_CITY, null).toString();
            isCityMode = true
            setContent(city)
        } else if (pref.contains(APP_PREFERENCES_LAT) && pref.contains(APP_PREFERENCES_LON)) {
            lat = pref.getFloat(APP_PREFERENCES_LAT, -1.0.toFloat());
            lon = pref.getFloat(APP_PREFERENCES_LON, -1.0.toFloat());
            isCityMode = false
            setContent(lat, lon)
        }else{
            startActivity(Intent(applicationContext, LocationActivity::class.java))
        }
        refresh.setOnRefreshListener {
            if (isCityMode) {
                setContent(city)
            } else {
                setContent(lat, lon)
            }
        }
        val date: LocalDateTime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = LocalDateTime.now()
            datePickerTimeline.setInitialDate(
                date.getYear(),
                date.getMonthValue() - 1,
                date.getDayOfMonth()
            )
        }
        datePickerTimeline.setOnDateSelectedListener(object : OnDateSelectedListener {
            override fun onDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int) {
                val date = Date()
                date.year = year - 1900
                date.month = month
                date.date = day
                refresh.isRefreshing = true
                val formated = SimpleDateFormat("dd.MM.yyyy").format(date)
                var res = APIClient.client?.create(APIInterface::class.java)
                var content: Observable<WeatherDetail>
                if (isCityMode) {
                    content = res?.getDetail(city, APIClient.appid, "metric")!!
                } else {
                    content = res?.getDetail(lat, lon, APIClient.appid, "metric")!!
                }
                content?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribeOn(Schedulers.io())
                    ?.doOnComplete {
                        chartWeatherCard.visibility = View.VISIBLE
                        refresh.isRefreshing = false
                        adapter.notifyDataSetChanged()
                    }
                    ?.subscribe({
                        setWeatherForDate(it, formated)
                    }, {
                        Snackbar.make(
                            refresh,
                            resources.getText(R.string.network_error),
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    })
                adapter.notifyDataSetChanged()
            }

            override fun onDisabledDateSelected(
                year: Int,
                month: Int,
                day: Int,
                dayOfWeek: Int,
                isDisabled: Boolean
            ) {
            }
        })
        weatherIcon.setOnClickListener {
            count++
            if (count == 10) {
                Toast.makeText(this@MainActivity, R.string.easter, Toast.LENGTH_LONG).show()
                count = 0
            }
        }
    }

    private fun setWeatherForDate(content: WeatherDetail, currentDate: String) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = WeatherForDayAdapter(list)
        }
        list.clear()
        var counter = 0
        for (weatherItem in content.list) {
            val date =
                SimpleDateFormat("dd.MM.yyyy").format(Date(weatherItem.dt.toLong() * 1000))
            if (date.equals(currentDate)) {
                list.add(weatherItem)
                counter++
            }
        }
        if (counter == 0) {
            Snackbar.make(
                refresh,
                resources.getText(R.string.no_data),
                Snackbar.LENGTH_LONG
            )
                .show()
            detailWeatherCard.visibility = View.GONE
        } else {
            detailWeatherCard.visibility = View.VISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setContent(city: String) {
        refresh.isRefreshing = true
        var res = APIClient.client?.create(APIInterface::class.java)
        res?.getSummary(city, APIClient.appid, "metric")
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.doOnComplete {
                chartWeatherCard.visibility = View.VISIBLE
            }
            ?.subscribe({
                setSummaryContent(it)
            }, {
                Snackbar.make(
                    refresh,
                    resources.getText(R.string.network_error),
                    Snackbar.LENGTH_LONG
                )
                    .show()
            })
        res?.getDetail(city, APIClient.appid, "metric")
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.doOnComplete {
                chartWeatherCard.visibility = View.VISIBLE
                refresh.isRefreshing = false
            }
            ?.subscribe({
                setDetailContent(it.list)
                setWeatherForDate(
                    it,
                    DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now())
                )
            }, {
                Snackbar.make(
                    refresh,
                    resources.getText(R.string.network_error),
                    Snackbar.LENGTH_LONG
                )
                    .show()
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setContent(lat: Float, lon: Float) {
        refresh.isRefreshing = true
        var res = APIClient.client?.create(APIInterface::class.java)
        res?.getSummary(lat, lon, APIClient.appid, "metric")
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.doOnComplete {
                chartWeatherCard.visibility = View.VISIBLE
            }
            ?.subscribe({
                setSummaryContent(it)
            }, {
                Snackbar.make(
                    refresh,
                    resources.getText(R.string.network_error),
                    Snackbar.LENGTH_LONG
                )
                    .show()
            })
        res?.getDetail(lat, lon, APIClient.appid, "metric")
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.doOnComplete {
                chartWeatherCard.visibility = View.VISIBLE
                refresh.isRefreshing = false
            }
            ?.subscribe({
                setDetailContent(it.list)
                setWeatherForDate(
                    it,
                    DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now())
                )
            }, {
                Snackbar.make(
                    refresh,
                    resources.getText(R.string.network_error),
                    Snackbar.LENGTH_LONG
                )
                    .show()
            })
    }

    private fun setSummaryContent(content: WeatherSummary) {
        toolbar.title = content.name
        weatherStatus.text = content.weather[0].main
        weatherDescription.text = "(" + content.weather[0].description + ")"
        weatherTemp.setText("\uD83C\uDF21️ " + content.main.temp + " °С")
        weatherWind.setText("\uD83D\uDCA8 " + content.wind.speed + " m/s")
        tempMax.text = "⬆️ " + content.main.temp_max + " °С"
        tempMin.text = "⬇️ " + content.main.temp_min + " °С"
        val weatherId = content.weather[0].id
        if (weatherId >= 200 && weatherId <= 232) {
            weatherIcon.setImageResource(R.drawable.thunderstorm)
        } else if (weatherId >= 300 && weatherId <= 321) {
            weatherIcon.setImageResource(R.drawable.shower_rain)
        } else if (weatherId >= 500 && weatherId <= 531) {
            weatherIcon.setImageResource(R.drawable.rain)
        } else if (weatherId >= 600 && weatherId <= 622) {
            weatherIcon.setImageResource(R.drawable.snow)
        } else if (weatherId >= 701 && weatherId <= 781) {
            weatherIcon.setImageResource(R.drawable.mist)
        } else if (weatherId == 800) {
            weatherIcon.setImageResource(R.drawable.clear_sky)
        } else if (weatherId >= 801 && weatherId <= 804) {
            weatherIcon.setImageResource(R.drawable.broken_clouds)
        }
        refresh.isRefreshing = false
        mainWeatherCard.visibility = View.VISIBLE
        calendarWeatherCard.visibility = View.VISIBLE
        detailWeatherCard.visibility = View.VISIBLE
        chartWeatherCard.visibility = View.VISIBLE
        info.visibility = View.VISIBLE
    }

    private fun setDetailContent(content: List<WeatherList>) {
        val axisX = ArrayList<String>()
        val axisY = ArrayList<Float>()
        for (weatherItem in content) {
            axisX.add(SimpleDateFormat("HH:mm (dd.MM)").format(Date(weatherItem.dt.toLong() * 1000)))
            axisY.add(weatherItem.main.temp.toFloat())
        }
        val dataLists: ArrayList<ArrayList<Float>> = ArrayList()
        dataLists.add(axisY)
        lineView.setBottomTextList(axisX)
        lineView.setColorArray(
            intArrayOf(
                R.color.black
            )
        )
        lineView.setDrawDotLine(true)
        lineView.setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY)
        lineView.setFloatDataList(dataLists)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_change_city -> {
                startActivity(Intent(applicationContext, LocationActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}