package com.howl.howlstagram

import com.bumptech.glide.Glide.init
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class FcmPush(){
    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = "AAAAo_cBkfg:APA91bGice0kn_JjjDpFIqVH61PpL9ofwtZ7_zCWE7A86Zty8yHwBV6aesgQFVPG1uUkZUMmQhv_3sQGIRhQL9aLWDxeigouLJCqPeeZykZpzErB7QT8Gm8qFm_zrBJIkX-7uTwfwPNMuRJs8hbGCmMKra9NBiZl0g"

    var okHttpClient : OkHttpClient? = null
    var gson : Gson? = null
    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(destinationUid:String, title: String?,message: String?){
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get().addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                println("pushToken" + task.result["pushToken"])
                var token = task.result["pushToken"].toString()

                var pushDTO = PushDTO()
                pushDTO.to = token
                pushDTO.notification?.title = title
                pushDTO.notification?.body = message

                var body = RequestBody.create(JSON,gson?.toJson(pushDTO))
                var request = Request.Builder()
                        .addHeader("Content-Type","application/json")
                        .addHeader("Authorization", "key="+serverKey)
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