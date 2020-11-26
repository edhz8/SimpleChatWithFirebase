package com.example.talk

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_friend.*
import kotlinx.android.synthetic.main.activity_set_nickname.*
import org.jetbrains.anko.toast

class AddFriendActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        val user : String = intent.getStringExtra("user").toString()
        var myname : Any? = null
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user)
            .get()
            .addOnCompleteListener{
                if(it.isSuccessful){
                    myname = it.result?.get("name").toString()
                }
            }

        addFriend_edittext.hint = "이메일주소로 친구를 찾아보세요!"

        addFriend_xbutton.setOnClickListener {
            addFriend_edittext.setText("")
        }

        addFriend_toolbar_backArrow.setOnClickListener {
            finish()
        }

        addFriend_toolbar_search.setOnClickListener {
            val email: String = addFriend_edittext.text.toString()
            FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("email",email)
                .get()
                .addOnSuccessListener { querysnapshot ->
                    if(querysnapshot != null){
                        for(dc in querysnapshot.documents){
                            val name = dc["name"].toString()
                            val statusMessage = dc["statusmessage"].toString()
                            val profilePicPath = dc["profilePicPath"].toString()
                            val uid = dc["uid"].toString()

                            val addbuttonListener = View.OnClickListener {
                                val map = mutableMapOf<String,Any>()
                                map["chatid"]=""
                                map["nickname"]=name
                                map["status"]=0
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(user)
                                    .collection("friends")
                                    .document(uid)
                                    .set(map)

                                val fmap = mutableMapOf<String,Any>()
                                fmap["chatid"]=""
                                fmap["nickname"]=myname.toString()
                                fmap["status"]=0

                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .collection("friends")
                                    .document(user)
                                    .set(fmap)
                                finish()
                            }

                            val dialog = AddFriendDialog(
                                context = this,
                                profilePicPath = profilePicPath,
                                name = name,
                                statusMessage = statusMessage,
                                addbtnListener = addbuttonListener
                            )

                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            dialog.show()

                        }
                    } else{
                        toast("검색결과가 존재하지 않습니다.")
                    }

                }

        }
    }
}