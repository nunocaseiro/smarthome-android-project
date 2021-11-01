package com.ipleiria.estg.meicm.arcismarthome.models

class UserRegister (
        val username: String,
        var email: String,
        var first_name: String,
        var last_name: String,
        var groups: Any,
        val password: String,
        val password2: String,
        val home: Int
)