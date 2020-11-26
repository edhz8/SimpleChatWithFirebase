package com.example.talk

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class FcmPush {

    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = "AAAACgGQps8:APA91bF0wI-xQ4lu2l8k-E3kzOEprnZPcjB_JOMjt1Ihp_L85Lte__edLTyBR_LyP9m5BudmIJGajeXdLOxxHHj15l_I63ApWNLQzGhhsmIWT2Athpk_2FWqRGyLR9EFSDiuF6O-V3ZW"
    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null
    companion object{
        var instance = FcmPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }
    fun sendMessage(destinationUid : String, title : String, message : String){
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(destinationUid)
            .get().
            addOnCompleteListener {
                task ->
            if(task.isSuccessful){
                var token = task.result?.get("pushtoken").toString()

                var PushModel = PushModel()
                PushModel.to = token
                PushModel.notification.title = title
                PushModel.notification.body = message

                var body = RequestBody.create(JSON,gson?.toJson(PushModel))
                var request = Request.Builder()
                    .addHeader("Content-Type","application/json")
                    .addHeader("Authorization","key="+serverKey)
                    .url(url)
                    .post(body)
                    .build()

                okHttpClient?.newCall(request)?.enqueue(object : Callback{
                    override fun onFailure(call: Call?, e: IOException?) {

                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        println(response?.body()?.string())
                    }

                })
            }
        }
    }
}