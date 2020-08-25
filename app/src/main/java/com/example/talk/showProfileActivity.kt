package com.example.talk

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_show_profile.*

class showProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)
        var profilePicPath : String
        if(intent.hasExtra("name")) {
            showProfileActivity_name.text = intent.getStringExtra("name")
        }
        if(intent.hasExtra("profilePicPath")) {
            profilePicPath = intent.getStringExtra("profilePicPath").toString()
            Glide.with(this)
                .load(profilePicPath)
                .into(showProfileActivity_profilePic)
        } else{
            profilePicPath = "default"
        }

        Glide.with(this)
            .load(profilePicPath)
            .into(showProfileActivity_profilePic)

        if(intent.hasExtra(("statusMessage"))) {
            showProfileActivity_statusMessage.text = intent.getStringExtra("statusMessage")
        }

        if(intent.getStringExtra("user") == intent.getStringExtra("uid")) {
            Glide.with(this)
                .load(R.drawable.edit_profile)
                .into(showProfileActivity_makePhoneCall)

            showProfileActivity_makePhoneCall.setOnClickListener {
                val intent = Intent(this,profileEditActivity::class.java)
                intent.putExtra("name",showProfileActivity_name.text)
                intent.putExtra("profilePicPath",profilePicPath)
                intent.putExtra("statusMessage",showProfileActivity_statusMessage.text)
                startActivity(intent)
            }
        } else{
            showProfileActivity_makePhoneCall.setOnClickListener{
                val intent = Intent(Intent.ACTION_DIAL)
                val phonenum = "tel:"+intent.getStringExtra("phoneNum")
                intent.data = Uri.parse(phonenum)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }
        }

        showProfileActivity_makeChatRoom.setOnClickListener{

        }
    }
}