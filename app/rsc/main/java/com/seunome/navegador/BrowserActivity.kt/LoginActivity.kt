package com.seunome.navegador

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class BrowserActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private lateinit var barra: EditText
    private lateinit var progresso: ProgressBar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        web = findViewById(R.id.webview)
        barra = findViewById(R.id.barraUrl)
        progresso = findViewById(R.id.progresso)

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportZoom(false)       // menos trabalho para o celular
            allowFileAccess = false     // segurança
        }

        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(v: WebView, url: String, f: Bitmap?) {
                progresso.visibility = View.VISIBLE
            }
            override fun onPageFinished(v: WebView, url: String) {
                progresso.visibility = View.GONE
                barra.setText(url)
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(v: WebView, p: Int) {
                progresso.progress = p
            }
        }

        barra.setOnEditorActionListener { _, acao, ev ->
            if (acao == EditorInfo.IME_ACTION_GO ||
                ev?.keyCode == KeyEvent.KEYCODE_ENTER) {
                abrir(barra.text.toString()); true
            } else false
        }

        findViewById<Button>(R.id.btnFav).setOnClickListener { favoritar() }
        findViewById<Button>(R.id.btnSair).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        abrir("https://www.google.com")
    }

    private fun abrir(entrada: String) {
        val t = entrada.trim()
        val url = when {
            t.startsWith("http://") || t.startsWith("https://") -> t
            t.contains(" ") || !t.contains(".") ->
                "https://www.google.com/search?q=" + android.net.Uri.encode(t)
            else -> "https://$t"
        }
        web.loadUrl(url)
    }

    private fun favoritar() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dados = hashMapOf(
            "url" to (web.url ?: ""),
            "titulo" to (web.title ?: ""),
            "criadoEm" to FieldValue.serverTimestamp()
        )
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("favoritos").add(dados)
            .addOnSuccessListener {
                Toast.makeText(this, "Favoritado!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        web.destroy() // libera memória
        super.onDestroy()
    }
}
