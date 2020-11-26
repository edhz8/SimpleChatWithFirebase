package com.example.talk

data class ChatModel(
    var uid : String? = "",
    var kind : Int? = -1,
    var timestamp : Long? = -1,
    var message : String? = ""
)