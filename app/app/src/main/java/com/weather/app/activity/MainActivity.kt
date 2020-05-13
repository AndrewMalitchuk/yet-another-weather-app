package com.weather.app.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dynamitechetan.flowinggradient.FlowingGradientClass
import com.weather.app.R
import com.weather.app.entity.detail.WeatherList
import com.weather.app.entity.summary.WeatherSummary
import com.weather.app.network.APIClient
import com.weather.app.network.APIInterface
import im.dacer.androidcharts.LineView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

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

        //
        setContent()
        //
        refresh.setOnRefreshListener {
            setContent()
        }

    }

    private fun setContent() {
        refresh.isRefreshing = true
        var res = APIClient.client?.create(APIInterface::class.java)
        res?.getSummary("Ivano-Frankivsk", APIClient.appid, "metric")
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.doOnComplete {
                chartWeatherCard.visibility = View.VISIBLE

            }
            ?.subscribe {
                Log.d(TAG, it.toString())
                setSummaryContent(it)
            }

        res?.getDetail("Ivano-Frankivsk", APIClient.appid, "metric")
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.doOnComplete {
                chartWeatherCard.visibility = View.VISIBLE
                refresh.isRefreshing = false
            }
            ?.subscribe {
                Log.d(TAG, it.toString())
                setDetailContent(it.list)
            }
    }

    private fun setSummaryContent(content: WeatherSummary) {
        toolbar.title = content.name
        weatherStatus.text = content.weather[0].main
        weatherDescription.text = "(" + content.weather[0].description + ")"
        weatherTemp.setText("\uD83C\uDF21️ " + content.main.temp + " °С")
        weatherWind.setText("\uD83D\uDCA8 " + content.wind.speed + " m/s")

        tempMax.text="⬆️ "+content.main.temp_max+" °С"
        tempMin.text="⬇️ "+content.main.temp_min+" °С"

    }

    private fun setDetailContent(content: List<WeatherList>) {

        val axisX = ArrayList<String>()
        val axisY = ArrayList<Float>()

        for (weatherItem in content) {
//            axisX.add(SimpleDateFormat("HH:mm (E)").format(Date(weatherItem.dt.toLong() * 1000)))
            axisX.add(SimpleDateFormat("HH:mm (dd.MM)").format(Date(weatherItem.dt.toLong() * 1000)))
            axisY.add(weatherItem.main.temp.toFloat())
        }

        val dataLists: ArrayList<ArrayList<Float>> = ArrayList()
        dataLists.add(axisY)


        lineView.setBottomTextList(axisX)
        lineView.setColorArray(
            intArrayOf(
                Color.RED
            )
        )
        lineView.setDrawDotLine(true)
        lineView.setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY)

//        lineView.setDataList(dataLists)
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