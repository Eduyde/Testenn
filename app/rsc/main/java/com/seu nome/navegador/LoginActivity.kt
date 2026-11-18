package com.seunome.navegador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Jรก logado? Vai direto para o navegador.
        if (auth.currentUser != null) {
            abrirNavegador()
            return
        }

        setContentView(R.layout.activity_login)
        val email = findViewById<EditText>(R.id.email)
        val senha = findViewById<EditText>(R.id.senha)

        findViewById<Button>(R.id.btnEntrar).setOnClickListener {
            val e = email.text.toString().trim()
            val s = senha.text.toString()
            if (!valido(e, s)) return@setOnClickListener
            auth.signInWithEmailAndPassword(e, s)
                .addOnSuccessListener { abrirNavegador() }
                .addOnFailureListener { aviso(it.localizedMessage) }
        }

        findViewById<Button>(R.id.btnCriar).setOnClickListener {
            val e = email.text.toString().trim()
            val s = senha.text.toString()
            if (!valido(e, s)) return@setOnClickListener
            auth.createUserWithEmailAndPassword(e, s)
                .addOnSuccessListener { abrirNavegador() }
                .addOnFailureListener { aviso(it.localizedMessage) }
        }
    }

    private fun valido(email: String, senha: String): Boolean {
        if (email.isEmpty() || senha.length < 6) {
            aviso("Informe e-mail e senha com pelo menos 6 caracteres")
            return false
        }
        return true
    }

    private fun aviso(msg: String?) =
        Toast.makeText(this, msg ?: "Erro", Toast.LENGTH_LONG).show()

    private fun abrirNavegador() {
        startActivity(Intent(this, BrowserActivity::class.java))
        finish()
    }
}

