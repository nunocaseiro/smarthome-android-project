package com.ipleiria.estg.meicm.arcismarthome.models

data class User(
    var id: Int? = null,
    val username: String? = null,
    var email: String? = null,
    var photo: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var roleGroup: Int? = null,
    val token: String,
    val profile: Int? = null
) {
    var role: String? = null

    init {
        role = when(roleGroup) {
            1 -> "admin"
            3 -> "privileged"
            else -> "user"
        }
    }

    fun updateRoleGroup() {
        roleGroup = when(role) {
            "admin" -> 1
            "privileged" -> 3
            else -> 2
        }
    }
}