package com.palacios.securitly

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.text.isDigitsOnly

class AddContactActivity : AppCompatActivity() {

    private lateinit var nameET: EditText
    private lateinit var phoneET: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        nameET = findViewById(R.id.nameEditText)
        phoneET = findViewById(R.id.phoneEditText)
    }

    fun goBackCall(v: View){
        goBack()
    }
    private fun goBack(){
        finish()
    }
    fun doneClickedCall(v:View){
        doneClicked()
    }
    private fun doneClicked(){
        // Recoger datos de los edit text
        var name = nameET.text.toString()
        name = parseComma(name)
        val phone = phoneET.text.toString().trim().filter { !it.isWhitespace() }

        // Verificar si los datos son correctos, cambiar esto pq hay otros paises con mas de 10
        // digits
        if(name.isEmpty()){
            Toast.makeText(this, resources.getString(R.string.fill_name), Toast.LENGTH_SHORT).show()
            return
        }
        if(phone.isEmpty()){
            Toast.makeText(this, resources.getString(R.string.fill_phone), Toast.LENGTH_SHORT).show()
            return
        }

        // Guardar los datos en una base de datos local.
        val prefs = Prefs(applicationContext)
        val currentContacts = prefs.getContacts()
        if(currentContacts == ":,"){
            prefs.saveContacts("$name:$phone,")
        }else{
            prefs.saveContacts("$currentContacts$name:$phone,")
        }
        finish()
    }

    private fun parseComma(s: String): String {
        return s.replace(",", "")

    }
}
// Como guardar los datos en la base de datos
// name:phone,name:phone,name:phone