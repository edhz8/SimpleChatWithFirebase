package com.example.talk

data class FriendModel(
                val type: Int,
                var name : String = "",
                var statusMessage : String = "",
                var profilePicPath : String = "",
                var phoneNum : String = "",
                var uid : String = ""
                ) {
    companion object{
        const val VIEWTYPE_FRIEND_WITH_SM = 0
        const val VIEWTYPE_FRIEND_WITHOUT_SM = 1
        const val VIEWTYPE_MY_PROFILE_WITH_SM = 2
        const val VIEWTYPE_MY_PROFILE_WITHOUT_SM = 3
        const val VIEWTYPE_LINE = 4
        //const val VIEWTYPE_COUNT = 5
    }
}