package com.palacios.securitly

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.*
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsManager
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.math.hypot


class MainActivity : AppCompatActivity() {
    private lateinit var greetingText: TextView
    private lateinit var statusSwitch: Switch
    private lateinit var messageText: TextView
    private lateinit var statusText: TextView
    private lateinit var locationCheckBox: CheckBox
    private lateinit var contactsSV: LinearLayout
    private lateinit var addContactsButton: TextView
    private lateinit var changeMessageButton: TextView
    private lateinit var notSelectedContacts: TextView

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var canSendMessage = true

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val prefs = Prefs(applicationContext)

        greetingText = findViewById(R.id.greetingText)
        statusSwitch = findViewById(R.id.systemStatusSwitch)
        messageText = findViewById(R.id.messageText)
        statusText = findViewById(R.id.statusText)
        locationCheckBox =   findViewById(R.id.locationCheckBox)
        contactsSV = findViewById(R.id.contacts)
        notSelectedContacts = findViewById(R.id.notSelectedContacts)
        changeMessageButton = findViewById(R.id.changeMessageButton)
        addContactsButton = findViewById(R.id.addContactsButton)
        val rightNow = Calendar.getInstance()
        val currentHour: Int = rightNow.get(Calendar.HOUR_OF_DAY)
        when {
            currentHour >= 20 -> {
                greetingText.text = this.getText(R.string.evening)
            }
            currentHour >= 12 -> {
                greetingText.text = this.getText(R.string.afternoon)
            }
            currentHour >= 6 -> {
                greetingText.text = this.getText(R.string.morning)
            }
            else -> {
                greetingText.text = this.getText(R.string.evening)
            }
        }


        if(prefs.getIsFirstTime()){
            prefs.saveStatus(false)
            prefs.saveContacts(":,")
            prefs.savePhoneNumberList(",")
            prefs.saveDontAskLocation(false)
            statusSwitch.isChecked = false
            statusChanged(false)
            prefs.saveIsFirstTime(false)
        }else {
            statusSwitch.isChecked = prefs.getStatus()
            if (prefs.getStatus()) {
                statusChanged(true)
            } else {
                statusChanged(false)
            }
        }
        locationCheckBox.isChecked = prefs.getSendLastLocation()
        checkSendLocationCheckbox(prefs.getSendLastLocation())

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(R.string.permission_neccesary)
                        .setPositiveButton(R.string.settings) { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        .setNegativeButton(R.string.cancel,null)
                    builder.create().show()
                }
            }
        val requestLocationPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(R.string.location_permission_neccesary)
                        .setPositiveButton(R.string.settings) { _, _ ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivity(intent)
                        }
                        .setNegativeButton(R.string.cancel,null)
                    builder.create().show()
                }
            }
        requestPermissionLauncher.launch(
            Manifest.permission.SEND_SMS)

        if(!prefs.getDontAskLocation()) {
            requestLocationPermissionLauncher.launch(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fillHome(true)
        startBatteryComprobation()

        statusSwitch.setOnCheckedChangeListener { _, _ ->
            switchPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = Prefs(applicationContext)
        fillHome(true)
        checkSendLocationCheckbox(prefs.getSendLastLocation())
    }

    private fun checkSendLocationCheckbox(up: Boolean){
        val messageScrollView = findViewById<NestedScrollView>(
            R.id.messageScrollView
        )
        var height = 120
        if(up){
            height = 300
        }
        addRemoveLocationText()
        val anim = ValueAnimator.ofInt(messageScrollView.measuredHeight, height)
        anim.duration = 300
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams: ViewGroup.LayoutParams = messageScrollView.layoutParams
            layoutParams.height = `val`
            messageScrollView.layoutParams = layoutParams
        }
        anim.start()
    }
    private fun addRemoveLocationText(){
        val prefs = Prefs(applicationContext)
        if (prefs.getSendLastLocation()) {
            if (prefs.getMessage() != "") {
                messageText.text = "${prefs.getMessage()} ${resources.getString(R.string.location_example)}"
            } else {
                messageText.text = "${resources.getString(R.string.message_placeholder)} ${resources.getString(R
                    .string
                    .location_example)}"
            }
        } else {
            messageText.text = prefs.getMessage()
        }
    }

    private fun fillHome(fromUp: Boolean){
        contactsSV.removeAllViews()
        val prefs = Prefs(applicationContext)
        val contacts = prefs.getContacts()
        if(contacts != ":,"){
            val contactsArray = contacts.substring(0, contacts.length - 1).split(",")
            var phoneNumberList = ""
            for(e in contactsArray){
            val splitString = e.split(":")
            val currentName = splitString[0]
            val currentPhone = splitString[1]
            phoneNumberList += "$currentPhone,"
            addContactsViews(currentName, currentPhone, fromUp)
            }
            prefs.savePhoneNumberList(phoneNumberList)
        }else{
            contactsSV.addView(notSelectedContacts)
            notSelectedContacts.visibility = View.VISIBLE
        }
    }

    private fun addContactsViews(name: String, phone: String, fromUp: Boolean){
        val stView = ContactsView(this)
        val nameView = stView.view.findViewById<TextView>(R.id.nameTV)
        val phoneView = stView.view.findViewById<TextView>(R.id.phoneTV)

        nameView.text = name
        phoneView.text = phone

        contactsSV.addView(stView.view)
        if(fromUp){
            stView.view.startAnimation(loadAnimation(this, R.anim.fade_down))
        }else{
            stView.view.startAnimation(loadAnimation(this, R.anim.fade_up))
        }


        setViewMargins(this, stView.view.layoutParams, 0,0,0,20, stView.view)

        stView.view.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(name)
            builder.setMessage(R.string.delete_contact)
                .setPositiveButton(R.string.delete) { _, _ ->
                    val prefs = Prefs(applicationContext)
                    // create list of contacts out of prefs
                    val contacts = prefs.getContacts()
                    val contactsList = contacts.substring(0, contacts.length-1).split(",")

                    // with a for loop, add every contacts to new list and check every pair and
                    // do not add that one that matches with name and phone
                    var newContactsList = ""

                    for(contact in contactsList){
                        if(contact != "$name:$phone"){
                            newContactsList += "$contact,"
                        }
                    }
                    if(newContactsList == ""){
                        newContactsList = ":,"
                    }
                    // create a string with the new list with the (:, sintaxis) and save it
                    // to prefs
                    prefs.saveContacts(newContactsList)

                    // animation
                    val myView = stView.view
                    // get the center for the clipping circle
                    val cx = myView.width / 2
                    val cy = myView.height / 2

                    // get the initial radius for the clipping circle
                    val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

                    // create the animation (the final radius is zero)
                    val anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0f)

                    // make the view invisible when the animation is done
                    anim.addListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            //myView.visibility = View.INVISIBLE
                            contactsSV.removeView(stView.view)
                            fillHome(false)
                        }
                    })
                    anim.duration = 400
                    // start the animation
                    anim.start()
                }
                .setNegativeButton(R.string.cancel, null)
            builder.create().show()
        }
    }

    private fun startBatteryComprobation(){
        val prefs = Prefs(applicationContext)
        val broadcastReceiverBattery: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if(prefs.getStatus()) {
                    val integerBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)

                    if (canSendMessage) {
                        if (integerBatteryLevel == prefs.getBatteryPercentajeAction()) {
                            getLocationAndSendMessageToContacts()
                            canSendMessage = false
                            //println("canSendMessage = false")
                            //println("Enviando mensaje")
                            //Toast.makeText(applicationContext, "Enviando mensaje false", Toast
                            //    .LENGTH_SHORT).show()
                        }
                    } else {
                        if (integerBatteryLevel != prefs.getBatteryPercentajeAction()) {
                            canSendMessage = true
                            //println("canSendMessage = true")
                            //Toast.makeText(applicationContext, "Cambiando a True", Toast
                            //    .LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        registerReceiver(broadcastReceiverBattery,  IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    fun changeMessageCall(v:View){
        changeMessage()
    }
    private fun changeMessage(){
        val myView = changeMessageButton
        val cx = myView.width / 2
        val cy = myView.height / 2

        val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0f)
        anim.duration = 400
        anim.start()
        startActivity(Intent(applicationContext, ChangeMessageActivity::class.java))
    }

    private fun switchPressed() {
        val prefs = Prefs(applicationContext)

        if (prefs.getStatus()) {
            prefs.saveStatus(false)
            statusChanged(false)
        } else {
            prefs.saveStatus(true)
            statusChanged(true)
        }
    }

    private fun statusChanged(boolean: Boolean) {
        val content = findViewById<LinearLayout>(R.id.content_function)
        if (boolean) {
            statusText.text = this.getString(R.string.active)
            statusText.setTextColor(this.resources.getColor(R.color.pink))

            val contentAnimation = content.animate()
                .translationY(0f)
                .alpha(1f)
            contentAnimation.duration = 300
            contentAnimation.start()
        } else {
            statusText.text = this.getString(R.string.not_active)
            statusText.setTextColor(this.resources.getColor(R.color.friendly_red))

            val contentAnimation = content.animate()
                .translationY(-200f)
                .alpha(0f)
            contentAnimation.duration = 300
            contentAnimation.start()
        }
    }

    fun locationCheckboxClickCall(v:View){
        locationCheckboxClick()
    }
    private fun locationCheckboxClick(){
        val prefs = Prefs(applicationContext)
        if(prefs.getSendLastLocation()){
            locationCheckBox.isChecked = false
            prefs.saveSendLastLocation(false)
            checkSendLocationCheckbox(false)
        }else{
            locationCheckBox.isChecked = true
            prefs.saveSendLastLocation(true)
            checkSendLocationCheckbox(true)
        }

    }
    fun toSettingsCall(v:View){
        toSettings()
    }
    private fun toSettings(){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    fun toAddContactCall(v:View){
        toAddContact()
    }
    private fun toAddContact(){
        val myView = addContactsButton
        val cx = myView.width / 2
        val cy = myView.height / 2

        val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0f)
        anim.duration = 400
        anim.start()

        startActivity(Intent(applicationContext, AddContactActivity::class.java))
    }

    private fun setViewMargins(
        con: Context, params: ViewGroup.LayoutParams,
        left: Int, top: Int, right: Int, bottom: Int, view: View
    ) {
        val scale = con.resources.displayMetrics.density
        // convert the DP into pixel
        val pixelLeft = (left * scale + 0.5f).toInt()
        val pixelTop = (top * scale + 0.5f).toInt()
        val pixelRight = (right * scale + 0.5f).toInt()
        val pixelBottom = (bottom * scale + 0.5f).toInt()
        val s = params as MarginLayoutParams
        s.setMargins(pixelLeft, pixelTop, pixelRight, pixelBottom)
        view.layoutParams = params
    }

    private fun sendSMSsCall(lastLocation: LatLng?, lastLocationString: String){
        val prefs = Prefs(applicationContext)
        val message = if(lastLocationString == "null"){
            prefs.getMessage()
        }else{
            "${prefs.getMessage()}\n${resources.getString(R.string.last_location)}\n " +
                    "$lastLocation\n$lastLocationString\nhttps://maps.google.com/?q=${lastLocation!!
                        .latitude},${lastLocation.longitude}"
        }

        // Create list of phone numbers off of prefs.getPhoneNumberList()
        val numbersList = prefs.getPhoneNumberList()
        val phoneNumbersList = numbersList.subSequence(0, numbersList.length-1).split(",")
        for(phoneNumber in phoneNumbersList){
            sendSMS(phoneNumber, message)
        }

    }
    private fun sendSMS(number: String, message: String){
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(number, "ME", message, null, null)
        //println("enviando: $message")
    }
    private fun getLocationAndSendMessageToContacts() {
        val prefs = Prefs(applicationContext)
        if(prefs.getSendLastLocation()) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                val location = task.result
                if (location != null) {
                    val geocoder = Geocoder(this)
                    val lat = task.result.latitude
                    val lon = task.result.longitude
                    val location = LatLng(lat, lon)
                    var locationString = resources.getString(R.string.error_getting_location)
                    try {
                        val list = geocoder.getFromLocation(lat, lon, 1).toTypedArray()
                        val address = list[0].toString()
                        locationString = address.substring(25, 55)
                        sendSMSsCall(location, locationString)
                    } catch (e: Exception) {
                        //println("Error con GeoCoder")
                        sendSMSsCall(location, locationString)
                    }
                }
            }
        }else{
            sendSMSsCall(null, "null")
        }
    }

}