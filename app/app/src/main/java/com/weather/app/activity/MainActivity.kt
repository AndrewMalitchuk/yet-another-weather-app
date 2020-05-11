package com.weather.app.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.dynamitechetan.flowinggradient.FlowingGradientClass
import com.weather.app.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set Toolbar
        toolbar.setTitle("sdfsdf")
        setSupportActionBar(toolbar)
//        toolbar.setTitle("df")

        val tb=findViewById<Toolbar>(R.id.toolbar)
        tb.setTitle("Sdf")
//        toolbar.title = "./weather.py"

        // Set background
        FlowingGradientClass()
            .setBackgroundResource(R.drawable.translate)
            .onLinearLayout(mainLayout)
            .setTransitionDuration(4000)
            .start()
    }

    private fun setContent(){

    }
}
