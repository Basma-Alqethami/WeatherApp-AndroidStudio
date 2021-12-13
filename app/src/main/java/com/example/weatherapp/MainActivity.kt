package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import kotlin.math.roundToInt
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var tvName : TextView
    lateinit var tvUpdTime : TextView
    lateinit var tvMode : TextView
    lateinit var tvTemp : TextView
    lateinit var tvLowDegree : TextView
    lateinit var tvHighDegree : TextView
    lateinit var sunriseTime : TextView
    lateinit var sunsetTime : TextView
    lateinit var windDegree : TextView
    lateinit var pressureDegree : TextView
    lateinit var humidityDegree : TextView
    lateinit var refreshData : TextView

    // form
    lateinit var addForm : LinearLayout
    lateinit var tvCityId : EditText
    lateinit var btnOk : Button
    lateinit var btnCancel: Button

    var cityId = 5128581 // default city is New York

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvName = findViewById(R.id.tvName)

        tvUpdTime = findViewById(R.id.tvUpdTime)
        tvMode = findViewById(R.id.tvMode)
        tvTemp = findViewById(R.id.tvTemp)
        tvLowDegree = findViewById(R.id.tvLowDegree)
        tvHighDegree = findViewById(R.id.tvHighDegree)
        sunriseTime = findViewById(R.id.sunriseTime)
        sunsetTime = findViewById(R.id.sunsetTime)
        windDegree = findViewById(R.id.windDegree)
        pressureDegree = findViewById(R.id.pressureDegree)
        humidityDegree = findViewById(R.id.humidityDegree)
        refreshData = findViewById(R.id.refreshData)

        // GET ALL JSON DATA
        requestData()

        // Refresh DATA
        var refreshLay = findViewById<LinearLayout>(R.id.refreshLay)
        var refreshImage = findViewById<ImageView>(R.id.refreshImage)
        refreshLay.setOnClickListener {
            requestData()
        }
        refreshImage.setOnClickListener {
            requestData()
        }


        addForm = findViewById(R.id.addForm)
        tvCityId = findViewById(R.id.tvCityId)
        btnOk = findViewById(R.id.btnOk)
        btnCancel = findViewById(R.id.btnCancel)

        tvName.setOnClickListener {
            addForm.isVisible = true
        }

        btnOk.setOnClickListener {
            try {
                var Id = tvCityId.text.toString()
                cityId = Id.toInt()
                tvCityId.clearFocus()
                tvCityId.text.clear()
                requestData()
            } catch (e: Exception){
                showAlert("please enter city id")
                Log.d("Error","$e")
            }
        }

        btnCancel.setOnClickListener {
            tvCityId.clearFocus()
            tvCityId.text.clear()
            addForm.isVisible = false
        }
    }

    private fun requestData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = async { fetchData() }.await()
                if (data.isNotEmpty()) {
                    populateData(data)
                    Log.d("MAIN", "$data")
                } else {
                    withContext(Main) {
                        showAlert("the zip code does not exist or wrong")
                    }
                }
            }catch (e: Exception)
            {
                Log.d("Error","$e")
            }
        }
    }

    fun fetchData():String {
        var response = ""
        try {
            response = URL("https://api.openweathermap.org/data/2.5/weather?id=$cityId&APPID=57bbc544e83eadc4204962a7e5907163").readText()
        }catch (e: Exception)
        {
            Log.d("Error","$e")
        }
        return response
    }

    private suspend fun populateData(result: String) {
        withContext(Dispatchers.Main)
        {
            try {

                val jsonObject = JSONObject(result)
                var name = jsonObject.getString("name") +" "+ jsonObject.getJSONObject("sys").getString("country")
                var timezone = jsonObject.getInt("timezone")
                updateTime (timezone.toString())

                var description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description")
                var temp = jsonObject.getJSONObject("main").getString("temp")
                temp= convertTemp(temp)
                var temp_min = jsonObject.getJSONObject("main").getString("temp_min")
                temp_min = convertTemp(temp_min)
                var temp_max = jsonObject.getJSONObject("main").getString("temp_max")
                temp_max = convertTemp(temp_max)

                var pressure = jsonObject.getJSONObject("main").getString("pressure")
                var humidity = jsonObject.getJSONObject("main").getString("humidity")
                var wind = jsonObject.getJSONObject("wind").getString("speed")

                var sunrise= jsonObject.getJSONObject("sys").getString("sunrise")
                var sunset= jsonObject.getJSONObject("sys").getString("sunset")
                var findSunrise = getTime(sunrise.toInt())
                var findSunset = getTime(sunset.toInt())


                tvName.text = name

                tvMode.text = description
                tvTemp.text = temp
                tvLowDegree.text = "Low: $temp_min"
                tvHighDegree.text = "High: $temp_max"

                pressureDegree.text = pressure
                humidityDegree.text = humidity
                windDegree.text = wind
                sunriseTime.text = findSunrise
                sunsetTime.text = findSunset

            }catch (e: Exception){
                Log.d("Error","$e")
            }
        }
        addForm.isVisible = false
    }

    fun updateTime (id : String) {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone(id)
        val dateF = SimpleDateFormat("dd-MM-yyyy hh:mm a")
        val currentDate: String = dateF.format(calendar.time)
        tvUpdTime.text = "Updated at: $currentDate"
    }

    fun getTime(id : Int): String {
        val dateF = SimpleDateFormat("hh:mm a")
        return dateF.format(id * 1000L)
    }

    fun convertTemp(temp: String) : String {
        var tempConvert = temp.toDouble() - 273
        return "${tempConvert.roundToInt()}°C"
    }

    private fun showAlert(message: String){
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(message)
            .setNegativeButton("Close") { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Error")
        alert.show()
    }
}