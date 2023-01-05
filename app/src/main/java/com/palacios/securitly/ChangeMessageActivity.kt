package com.palacios.securitly

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class ChangeMessageActivity : AppCompatActivity() {
    private lateinit var messageET: EditText
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_message)
        prefs = Prefs(applicationContext)

        messageET = findViewById(R.id.messageEdit)
        if(prefs.getMessage() != ""){
            messageET.setText(prefs.getMessage())
        }

    }
    fun goBackCall(v:View){
        goBack()
    }
    private fun goBack(){
        finish()
    }
    fun saveMessageCall(v: View){
        saveMessage()
    }
    private fun saveMessage(){
        prefs.saveMessage(messageET.text.toString())
        Toast.makeText(this, resources.getString(R.string.message_saved), Toast.LENGTH_SHORT).show()
        goBack()
    }
}