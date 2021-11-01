package com.ipleiria.estg.meicm.arcismarthome.server
import java.io.Serializable

class HouseKeyResponse: Serializable  {
    var validKey = false
    var homeCreated = false
    var profilesCreated = false
}