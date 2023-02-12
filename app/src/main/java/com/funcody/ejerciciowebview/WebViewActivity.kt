
package com.funcody.ejerciciowebview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.funcody.ejerciciowebview.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    lateinit var binding: ActivityWebViewBinding
    lateinit var miWebView: WebView

    // La variable pulsacionLargaPulsada indica que cuando esté activa no se ejecutará ninguna opción del setOnTouchListener
    private var pulsacionLargaPulsada = false

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Asignamos el binding a una variable identificativa
        miWebView = binding.webViewMainActivity
        // Añadimos si se desea los ajustes a una variable
        val ajustes = miWebView.settings
        // Mostrar controles de zoom
        ajustes.displayZoomControls = true
        ajustes.builtInZoomControls = true
        // Activamos javascript.
        ajustes.javaScriptEnabled = true
        // Creamos un objeto de la clase MiWebView (creada más abajo anidada) y se lo incorporamos a nuestro webView
        miWebView.webViewClient = ClaseMiWebView()
        // Hacemos que el progressbar cargue a la vez que carga la web
        miWebView.webChromeClient = Navegador()
        // loadUrl asigna una. IMPORTANTE: Cargar la url debe ser la última linea del código
        miWebView.loadUrl("https://es.wikipedia.org/wiki/Wikipedia:Portada")

        // PULSACIONES LARGAR SOBRE LAS FLECHAS: (setOnLongClickListener)
        binding.flechaAnterior.setOnLongClickListener {

            val posicionActual = miWebView.copyBackForwardList().currentIndex
            val inicio = posicionActual * -1

            if (miWebView.canGoBackOrForward(inicio)) miWebView.goBackOrForward(inicio)

            pulsacionLargaPulsada = true
            true
        }

        binding.flechaPosterior.setOnLongClickListener {

            val posicionActual = miWebView.copyBackForwardList().currentIndex
            val tamanioTotal = miWebView.copyBackForwardList().size
            val final = tamanioTotal - posicionActual - 1

            if (miWebView.canGoBackOrForward(final)) miWebView.goBackOrForward(final)

            pulsacionLargaPulsada = true
            true
        }

        // Botones
        binding.botonCerrar.setOnTouchListener { _, event -> pulsacion(event, binding.botonCerrar); true }
        binding.flechaAnterior.setOnTouchListener { _, event -> pulsacion(event, binding.flechaAnterior); false }
        binding.flechaPosterior.setOnTouchListener { _, event -> pulsacion(event, binding.flechaPosterior); false }
        binding.botonRecargar.setOnTouchListener { _, event -> pulsacion(event, binding.botonRecargar); true }
        binding.botonPararCarga.setOnTouchListener { _, event -> pulsacion(event, binding.botonPararCarga); true }
        binding.botonAbrirNavegador.setOnTouchListener { _, event ->pulsacion(event,binding.botonAbrirNavegador); true}
    }

    // FUNCIONES:
    private fun pulsacion(evento: MotionEvent, vista: TextView) {
        // Declaro los dos colores que voy a utilizar al pulsar y al soltar
        val colorPulsacion = ContextCompat.getColorStateList(this, R.color.gris_medio)
        val colorSoltarPulsacion = ContextCompat.getColorStateList(this, R.color.blanco_roto)

        when (evento.action) {
            MotionEvent.ACTION_DOWN -> {
                vista.backgroundTintList = colorPulsacion
            } // Cambia color
            MotionEvent.ACTION_UP   -> {
                vista.backgroundTintList = colorSoltarPulsacion // Cambia color
                when {
                    pulsacionLargaPulsada                -> pulsacionLargaPulsada = false
                    vista == binding.botonCerrar         -> finish()
                    vista == binding.botonAbrirNavegador -> abrirNavegador()
                    vista == binding.flechaAnterior      -> miWebView.goBack()
                    vista == binding.flechaPosterior     -> miWebView.goForward()
                    vista == binding.botonRecargar       -> miWebView.reload()
                    vista == binding.botonPararCarga     -> miWebView.stopLoading()
                }
            }
        }
    }

    // Función que lanza un intent con la url actual
    private fun abrirNavegador() {
        val url = miWebView.url
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    // Botón físico hacia atrás de Android
    override fun onBackPressed() {
        if (miWebView.canGoBack()) miWebView.goBack()
        else preguntaSalida()
    }

    // AlertDialog
    private fun preguntaSalida() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Mensaje del sistema")
        builder.setMessage("¿Desea salir de la aplicación?")
        builder.setPositiveButton(R.string.si) { _, _ ->
            finish()
        }
        builder.setNegativeButton("Cancelar", null)

        val dialog = builder.create() //AlertDialog
        dialog.show()
    }

    // Clase anidada que maneja los eventos que afectan el procesamiento del contenido, como la navegación o los errores
    // en el envío de formularios.
    inner class ClaseMiWebView : WebViewClient() {

        // Detecta si al pulsar sobre un enlace se abre el explorador o si sigue en el webView
        override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
        ): Boolean {
            return false
        }

        // Detecta cuando la página empieza a cargar
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            // Se muestra cuando empiece a cargar
            binding.progressBar.visibility = View.VISIBLE
            // Oculta el botón de carga
            binding.botonRecargar.visibility = View.INVISIBLE
            // Muestra el bot´´on de parar carga
            binding.botonPararCarga.visibility = View.VISIBLE
        }

        // Detecta cuando termine de cargar
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.contentTitulo.text = miWebView.title
            // Se oculta cuando carga del toro la página
            binding.progressBar.visibility = View.GONE
            // Muestra el bot´´on de carga
            binding.botonRecargar.visibility = View.VISIBLE
            // Oculta el botón de parar carga
            binding.botonPararCarga.visibility = View.INVISIBLE
            // Desactiva los botones al llegar al principio o al fin
            validaBotonesFlechas()

        }
    }

    //  ---- DESACTIVAR BOTONES AL LLEGAR AL PRINCIPIO O AL PRINCIPIO DEL HISTORIAL ----
    private fun validaBotonesFlechas() {
        // Declaro los dos colores que voy a utilizar al pulsar y al soltar
        val colorDesabilitado = ContextCompat.getColorStateList(this, R.color.gris_medio)
        val colorHabilitado = ContextCompat.getColorStateList(this, R.color.blanco_roto)

        // Flecha hacia atrás
        if (miWebView.canGoBack()) {
            binding.flechaAnterior.backgroundTintList = colorHabilitado
            binding.flechaAnterior.isEnabled = true
        } else {
            binding.flechaAnterior.backgroundTintList = colorDesabilitado
            binding.flechaAnterior.isEnabled = false
        }

        // Flecha hacia delante
        if (miWebView.canGoForward()) {
            binding.flechaPosterior.backgroundTintList = colorHabilitado
            binding.flechaPosterior.isEnabled = true
        } else {
            binding.flechaPosterior.backgroundTintList = colorDesabilitado
            binding.flechaPosterior.isEnabled = false
        }
    }

    inner class Navegador : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            binding.progressBar.progress = newProgress
        }
    }
}