package com.example.talk

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_show_profile.*

class showProfileDialog(
    context: Context,
    val profilePicPath: String,
    val name: String,
    val statusMessage: String,
    val uid: String,
    val user: String?,
    val editbtnListener: View.OnClickListener,
    val setnickbtnListener : View.OnClickListener,
    val chatbtnListener: View.OnClickListener
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.apply {
            this.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            this.dimAmount = 0.8f
        }
        window!!.attributes = layoutParams

        setContentView(R.layout.dialog_show_profile)

        Glide.with(context)
            .load(profilePicPath)
            .into(spdialog_profilepic)

        spdialog_name.text = name
        spdialog_statusmessage.text = statusMessage

        spdialog_chatbutton.setOnClickListener(chatbtnListener)

        if (user == uid) {
            spdialog_chatbutton_text.text = "나와의 채팅"
            spdialog_editbutton_text.text = "프로필 편집"

            spdialog_editbutton.setOnClickListener(editbtnListener)
        } else {
            spdialog_editbutton.setOnClickListener(setnickbtnListener)
        }
    }
}