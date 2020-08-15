package com.example.talk

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ChatModel(@PrimaryKey var id: Long = -1,
                     var uid: String = "",
                     var cartegoryOfChat: Int = -1,
                     var chat: String = "",
                     var time: Long = -1,
                     var NotRead: Int = -1): RealmObject(){}