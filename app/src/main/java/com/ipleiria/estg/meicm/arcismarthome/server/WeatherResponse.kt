package com.ipleiria.estg.meicm.arcismarthome.server

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class WeatherResponse: Serializable {
    @SerializedName("weather")
    var weather: List<WeatherNestedResponse>?  = null

    @SerializedName("main")
    var main: MainNestedResponse?  = null

    @SerializedName("wind")
    var wind: WindNestedResponse?  = null
}

class WeatherNestedResponse: Serializable {
    var id: Int? = null
    var main: String? = null
    var description: String? = null
    var icon: String? = null
}

class MainNestedResponse: Serializable {
    var temp: String? = null
    var humidity: String? = null

}

class WindNestedResponse: Serializable {
    var speed: String? = null
}




