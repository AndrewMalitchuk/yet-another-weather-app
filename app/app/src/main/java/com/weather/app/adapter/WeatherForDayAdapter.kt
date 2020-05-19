package com.weather.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.weather.app.R
import com.weather.app.entity.detail.WeatherList
import java.text.SimpleDateFormat
import java.util.*

class WeatherForDayAdapter(private val list: List<WeatherList>)
    : RecyclerView.Adapter<WeatherForDayAdapter.ViewHolder>() {

    private val TAG="WeatherForDayAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherForDayAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: WeatherForDayAdapter.ViewHolder, position: Int) {
        val weatherList:WeatherList=list[position]
        holder.bind(weatherList)
    }

    override fun getItemCount(): Int = list.size

    /**
     * Entity for RecyclerView
     */
    inner class ViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.weather_for_day_item, parent, false)) {
        private var time:TextView?=null
        private var temperature: TextView? = null
        private var main: TextView? = null
        private var description: TextView? = null

        init {
            time = itemView.findViewById(R.id.time)
            temperature = itemView.findViewById(R.id.temperature)
            main = itemView.findViewById(R.id.main)
            description = itemView.findViewById(R.id.description)
        }

        fun bind(weatherList: WeatherList) {
            time?.text= SimpleDateFormat("HH:mm").format(Date(weatherList.dt.toLong() * 1000))
            temperature?.text="\uD83C\uDF21️ "+weatherList.main.temp.toString()+ " °С"
            main?.text=weatherList.weather[0].main
            description?.text="("+weatherList.weather[0].description+")"
        }
    }

}