package com.weather.app.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dynamitechetan.flowinggradient.FlowingGradientClass
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.weather.app.R
import com.weather.app.entity.summary.WeatherSummary
import com.weather.app.network.APIClient
import com.weather.app.network.APIInterface
import im.dacer.androidcharts.LineView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set Toolbar
        toolbar.setTitle("sdfsdf")
        setSupportActionBar(toolbar)
        // Set background
        FlowingGradientClass()
            .setBackgroundResource(R.drawable.translate)
            .onLinearLayout(mainLayout)
            .setTransitionDuration(4000)
            .start()
        // Get summary weather info
        var res = APIClient.client?.create(APIInterface::class.java)
        res?.getSummary("Ivano-Frankivsk", APIClient.appid, "metric")
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.doOnComplete {
                chartWeatherCard.visibility = View.VISIBLE
            }
            ?.subscribe {
                Log.d(TAG, it.toString())
                setContent(it)
            }



    }

    private fun setContent(content: WeatherSummary) {
        toolbar.title = content.name
//        weatherDate.text= SimpleDateFormat("dd.MM.yyyy").format(Date(content.dt.toLong() * 1000))
        weatherStatus.text = content.weather[0].main
        weatherDescription.text = "(" + content.weather[0].description + ")"
        weatherTemp.setText("\uD83C\uDF21️ " + content.main.temp + " °С")
        weatherWind.setText("\uD83D\uDCA8 " + content.wind.speed + " m/s")

        setChart()
    }

    private fun setChart() {

        val test = ArrayList<String>()
        for (i in 0 until 9) {
            test.add((i + 1).toString())
        }

        val dataList: ArrayList<Int> = ArrayList()
        var random = (Math.random() * 9 + 1).toFloat()
        for (i in 0 until 9) {
            dataList.add((Math.random() * random).toInt())
        }


        val dataLists: ArrayList<ArrayList<Int>> = ArrayList()
        dataLists.add(dataList)


        lineView.setBottomTextList(test)
        lineView.setColorArray(
            intArrayOf(
                Color.RED
            )
        )
        lineView.setDrawDotLine(true)
        lineView.setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY)

        lineView.setDataList(dataLists)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_change_city->{
                startActivity(Intent(applicationContext,LocationActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

}