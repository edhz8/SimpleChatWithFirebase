package com.example.talk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_set_nickname.*

class SetNicknameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_nickname)

        val friendNickName: String = intent.getStringExtra("friendNickName").toString()
        val user: String = intent.getStringExtra("user").toString()
        val uid: String = intent.getStringExtra("uid").toString()

        setnickname_edittext.hint = friendNickName
        setnickname_textInputLayout.isCounterEnabled = true
        setnickname_textInputLayout.counterMaxLength = 10
        setnickname_textInputLayout.isErrorEnabled = true

        setnickname_edittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (setnickname_edittext.length() > 10) {
                    setnickname_textInputLayout.error = "닉네임은 최대 10글자 입니다."
                } else if (setnickname_edittext.length() == 0) {
                    setnickname_textInputLayout.error = "닉네임은 1글자 이상 입니다."
                } else{
                    setnickname_textInputLayout.error = null
                }
            }
        })

        setnickname_xbutton.setOnClickListener {
            setnickname_edittext.setText("")
        }

        setnickname_toolbar_backArrow.setOnClickListener {
            finish()
        }

        setnickname_toolbar_confirm.setOnClickListener {
            val newnickname: String = setnickname_edittext.text.toString()

            if (newnickname.length > 0 && newnickname.length<11) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user)
                    .collection("friends")
                    .document(uid)
                    .update("nickname", newnickname)
                    .addOnSuccessListener {
                        finish()
                    }
            } else {
                finish()
            }
        }
    }

}