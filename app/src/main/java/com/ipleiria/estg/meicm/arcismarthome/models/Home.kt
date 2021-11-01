package com.ipleiria.estg.meicm.arcismarthome.models

data class Home(var id: Int?, var name: String?, var latitude: String?, var longitude: String?, var key: String?){
    init {
        if (name!= null){
            AppData.instance.housename.postValue(name)
        }
    }
}