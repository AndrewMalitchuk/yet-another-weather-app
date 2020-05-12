package com.weather.app.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dynamitechetan.flowinggradient.FlowingGradientClass
import com.google.android.material.snackbar.Snackbar
import com.weather.app.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class LocationActivity : AppCompatActivity() {

    private val delay: Long = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        // Set background
        FlowingGradientClass()
            .setBackgroundResource(R.drawable.translate)
            .onRelativeLayout(relativeLayout)
            .setTransitionDuration(4000)
            .start()
    }

    fun onConfirmButtonClick(view: View) {
        Snackbar.make(relativeLayout, resources.getText(R.string.success), Snackbar.LENGTH_LONG)
            .show()

        Observable
            .timer(delay, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startActivity(Intent(this, MainActivity::class.java))
            }
    }

}
