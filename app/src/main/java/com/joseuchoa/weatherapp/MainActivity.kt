package com.joseuchoa.weatherapp

// Importações necessárias
import android.os.AsyncTask
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Classe principal da atividade
class MainActivity : ComponentActivity() {

    // Chave da API do OpenWeatherMap
    private val API: String = "8118ed6ee68db2debfaaa5a44c832918"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialização dos elementos de UI
        val editTextCity = findViewById<EditText>(R.id.editTextCity)
        val loader = findViewById<ProgressBar>(R.id.loader)
        val mainContainer = findViewById<RelativeLayout>(R.id.mainContainer)
        val errorText = findViewById<TextView>(R.id.errortext)

        // Configuração do listener do EditText para iniciar a busca ao pressionar "Enter"
        editTextCity.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                event != null && event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                val cityName = editTextCity.text.toString()
                if (cityName.isNotEmpty()) {
                    WeatherTask(cityName, loader, mainContainer, errorText).execute()
                } else {
                    Toast.makeText(this, "Digite o nome da cidade", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        // Inicia a busca com a cidade de São Paulo ao abrir o aplicativo
        performSearch("Sao Paulo", loader, mainContainer, errorText)
    }

    // Método para realizar a busca
    private fun performSearch(
        cityName: String,
        loader: ProgressBar,
        mainContainer: RelativeLayout,
        errorText: TextView
    ) {
        if (cityName.isNotEmpty()) {
            WeatherTask(cityName, loader, mainContainer, errorText).execute()
        } else {
            Toast.makeText(this, "Digite o nome da cidade", Toast.LENGTH_SHORT).show()
        }
    }

    // Classe interna para executar a tarefa de busca em background
    inner class WeatherTask(
        private val cityName: String,
        private val loader: ProgressBar,
        private val mainContainer: RelativeLayout,
        private val errorText: TextView
    ) : AsyncTask<String, Void, String>() {

        // Antes da execução da tarefa
        override fun onPreExecute() {
            super.onPreExecute()
            // Mostra o loader e esconde outros elementos de UI
            loader.visibility = View.VISIBLE
            mainContainer.visibility = View.GONE
            errorText.visibility = View.GONE
        }

        // Tarefa em background para buscar dados da API
        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                // Realiza a requisição à API do OpenWeatherMap para buscar dados da cidade
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$cityName&units=metric&appid=$API&lang=pt")
                    .readText(Charsets.UTF_8)
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        // Após a execução da tarefa em background
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                // Processa os dados obtidos da API
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                // Extrai informações dos dados obtidos
                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Atualizado em: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
                val temp = main.getString("temp")+"°C"
                val tempMin = "Temp Mín: " + main.getString("temp_min")+"°C"
                val tempMax = "Temp Máx: " + main.getString("temp_max")+"°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")
                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")
                val address = jsonObj.getString("name")+", "+sys.getString("country")

                // Atualiza os elementos de UI com as informações obtidas
                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text =  updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity
                // ... (atualiza outras views)

                // Mostra os elementos de UI atualizados e esconde o loader
                loader.visibility = View.GONE
                mainContainer.visibility = View.VISIBLE
            } catch (e: Exception) {
                // Em caso de erro, esconde o loader e mostra mensagem de erro
                loader.visibility = View.GONE
                errorText.visibility = View.VISIBLE
            }
        }
    }
}
