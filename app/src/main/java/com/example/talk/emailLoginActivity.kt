package com.example.talk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_email_login.*
import org.jetbrains.anko.toast

/* 이메일계정으로 로그인 및 회원가입하는 화면.
* 로그인 과정에서 일어나는 오류에 대해서 세부적으로 처리해야됨. */
class emailLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_login)

        email_register_button.setOnClickListener {
            createEmailId()
        }
        email_login_button.setOnClickListener{
            loginEmail()
        }
    }

    fun loginEmail(){   //나중에 로그인횟수 추가..?
        var email = email_edittext.text.toString()
        var password = password_edittext.text.toString()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                MoveNextPage()
            } else{
                toast("ID 혹은 페스워드를 확인해주세요.")
            }
        }
    }

    fun createEmailId(){
        var email = email_edittext.text.toString()
        var password = password_edittext.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    MoveNextPage()
                }
            }
    }

    fun MoveNextPage(){
        toast("Now Loading...")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            this.finish()
        }
    }
}