package com.example.talk

import io.realm.RealmObject

open class FriendModel(
                var name : String = "",
                var statusMessage : String = "",
                var profilePicPath : String = "",
                var phoneNum : String = "",
                var uid : String = ""
                ):RealmObject()