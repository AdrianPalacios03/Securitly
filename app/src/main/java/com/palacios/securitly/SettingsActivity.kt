package com.palacios.securitly

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import android.widget.Switch
import androidx.appcompat.app.AlertDialog

class SettingsActivity : AppCompatActivity() {
    private lateinit var batteryPicker: NumberPicker
    private lateinit var dontAskSwitch: Switch
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val prefs = Prefs(applicationContext)

        batteryPicker = findViewById(R.id.batteryPicker)

        batteryPicker.minValue = 1
        batteryPicker.maxValue = 100
        batteryPicker.value = prefs.getBatteryPercentajeAction()

        dontAskSwitch = findViewById(R.id.dontAskSwitch)
        dontAskSwitch.isChecked = prefs.getDontAskLocation()

        dontAskSwitch.setOnCheckedChangeListener { _, _ ->
            dontAskSwitchPressed()
        }

        batteryPicker.setOnScrollListener { numberPicker, _ ->
            val idx = numberPicker.value
            prefs.saveBatteryPercentajeAction(idx)
        }

    }
    fun goBackCall(v: View){
        goBack()
    }
    private fun goBack(){
        finish()
    }
    private fun dontAskSwitchPressed(){
        val prefs = Prefs(applicationContext)
        prefs.saveDontAskLocation(dontAskSwitch.isChecked)
    }
    fun toInstructionsCall(v:View){
        toInstructions()
    }
    private fun toInstructions(){
        startActivity(Intent(this, InstructionsActivity::class.java))
    }
    fun toAboutCall(v:View){
        toAbout()
    }
    private fun toAbout(){
        startActivity(Intent(this, AboutActivity::class.java))
    }
}