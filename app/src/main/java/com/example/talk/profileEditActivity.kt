package com.example.talk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_profile_edit.*
/* 프로필 변경 엑티비티 fire store 로 구현예정. */
class profileEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        profile_confirm.setOnClickListener{

        }
    }
}