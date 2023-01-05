package com.palacios.securitly

import android.content.Context

class Prefs(val context: Context) {
    val SHARED_NAME = "mydatabase"
    val SHARED_STATUS = "STATUS"
    val SHARED_MESSAGE = "MESSAGE"
    val SHARED_SEND_LOCATION = "SEND_LOCATION"
    val SHARED_BATTERY_PERCENTAJE_ACTION = "BATTERY_PERCENTAJE_ACTION"
    val SHARED_CONTACTS = "CONTACTS"
    val SHARED_FIRST_TIME = "FIRST_TIME"
    val SHARED_PHONE_NUMBER_LIST = "PHONE_NUMBER_LIST"
    val SHARED_DONT_ASK_LOCATION = "DONT_ASK_LOCATION"

    val storage = context.getSharedPreferences(SHARED_NAME, 0)

    fun saveStatus(bool: Boolean){
        storage.edit().putBoolean(SHARED_STATUS,bool).apply()
    }
    fun getStatus():Boolean{
        return storage.getBoolean(SHARED_STATUS, false)
    }
    fun saveMessage(message: String){
        storage.edit().putString(SHARED_MESSAGE,message).apply()
    }
    fun getMessage():String{
        return storage.getString(SHARED_MESSAGE, "")!!
    }
    fun saveSendLastLocation(bool: Boolean){
        storage.edit().putBoolean(SHARED_SEND_LOCATION,bool).apply()
    }
    fun getSendLastLocation():Boolean{
        return storage.getBoolean(SHARED_SEND_LOCATION, true)
    }
    fun saveBatteryPercentajeAction(b:Int){
        storage.edit().putInt(SHARED_BATTERY_PERCENTAJE_ACTION, b).apply()
    }
    fun getBatteryPercentajeAction():Int{
        return storage.getInt(SHARED_BATTERY_PERCENTAJE_ACTION, 5)
    }
    fun saveContacts(contact: String){
        storage.edit().putString(SHARED_CONTACTS, contact).apply()
    }
    fun getContacts(): String{
        return storage.getString(SHARED_CONTACTS, ":,")!!
    }
    fun saveIsFirstTime(bool: Boolean){
        storage.edit().putBoolean(SHARED_FIRST_TIME,bool).apply()
    }
    fun getIsFirstTime():Boolean{
        return storage.getBoolean(SHARED_FIRST_TIME, true)
    }
    fun savePhoneNumberList(number: String){
        storage.edit().putString(SHARED_PHONE_NUMBER_LIST, number).apply()
    }
    fun getPhoneNumberList(): String{
        return storage.getString(SHARED_PHONE_NUMBER_LIST, ",")!!
    }
    fun saveDontAskLocation(bool: Boolean){
        storage.edit().putBoolean(SHARED_DONT_ASK_LOCATION,bool).apply()
    }
    fun getDontAskLocation():Boolean{
        return storage.getBoolean(SHARED_DONT_ASK_LOCATION, false)
    }
}