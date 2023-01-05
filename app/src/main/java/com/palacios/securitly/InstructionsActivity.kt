package com.palacios.securitly

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class InstructionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)
    }

    fun goBackCall(v: View){
        goBack()
    }
    private fun goBack(){
        finish()
    }
}