package com.palacios.securitly

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class ContactsView(context: Context): AppCompatActivity() {
    var view: View = LayoutInflater.from(context).inflate(R.layout.contacts_block, null, false)
}