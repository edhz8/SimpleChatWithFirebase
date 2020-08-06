package com.example.talk

import java.io.Serializable

class UserInfo(
                val name : String,
                val statusMessage : String,
                val profilePicPath : String,
                val FCMtoken : String):Serializable {
    constructor(): this("", "", "",  "")
}