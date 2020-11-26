package com.example.talk

data class FriendModel(
                var type: Int = 0,
                var name : String = "",
                var statusMessage : String = "",
                var profilePicPath : String = "",
                var uid: String = "",
                var status: Long = 0
                )