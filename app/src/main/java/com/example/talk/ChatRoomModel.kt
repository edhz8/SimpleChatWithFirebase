package com.example.talk

import io.realm.RealmObject

open class ChatRoomModel(
    var chatRoomName : String = "",
    var chatRoomPic : String = "",
    var noti : String = "",
    var lastChat : String = "",
    var lastChatTime : Long = System.currentTimeMillis()
):RealmObject()