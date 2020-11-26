package com.example.talk

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_add_friend_dialog.*
import kotlinx.android.synthetic.main.dialog_show_profile.*

class AddFriendDialog(
    context: Context,
    val profilePicPath: String,
    val name: String,
    val statusMessage: String,
    val addbtnListener: View.OnClickListener
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.apply {
            this.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            this.dimAmount = 0.8f
        }
        window!!.attributes = layoutParams

        setContentView(R.layout.activity_add_friend_dialog)

        Glide.with(context)
            .load(profilePicPath)
            .into(afdialog_profilepic)

        afdialog_name.text = name
        afdialog_statusmessage.text = statusMessage
        afdialog_addbutton.setOnClickListener(addbtnListener)

    }
}