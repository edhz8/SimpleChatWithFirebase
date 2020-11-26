package com.example.talk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_email_login.*
import org.jetbrains.anko.toast

/* 이메일계정으로 로그인 및 회원가입하는 화면.
* 로그인 과정에서 일어나는 오류에 대해서 세부적으로 처리해야됨. */
class EmailLoginActivity : AppCompatActivity() {
    val user : String by lazy{
        FirebaseAuth.getInstance().currentUser?.uid.toString()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_login)

        emaillogin_registerButton.setOnClickListener {
            createEmailId()
        }
        emaillogin_loginButton.setOnClickListener{
            loginEmail()
        }
        emaillogin_toolbar_backArrow.setOnClickListener {
            finish()
        }
    }

    fun loginEmail(){   //나중에 로그인횟수 추가..?
        var email = emaillogin_emailEdittext.text.toString()
        var password = emaillogin_passwordEdittext.text.toString()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                var currentUser = FirebaseAuth.getInstance().currentUser
                val user = FirebaseAuth.getInstance().currentUser?.uid

                if (currentUser != null) {

                    var doc = FirebaseFirestore.getInstance().collection("users").document(user.toString())

                    doc.get().addOnSuccessListener { document ->
                        if (document.data != null) {
                            startActivity(Intent(this, MainActivity::class.java))
                            this.finish()
                        }
                    }
                }
            } else{
                toast("가입정보가 확인되지 않습니다.\n가입하시기 위해서는 회원가입버튼을 눌러주세요.")
            }
        }
    }

    fun createEmailId(){
        var email = emaillogin_emailEdittext.text.toString()
        var password = emaillogin_passwordEdittext.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    intent = Intent(this,profileEditActivity::class.java)
                    intent.putExtra("mode","MAKE_ACCOUNT")
                    intent.putExtra("email",email)
                    startActivity(intent)
                } else{
                    toast("아이디 또는 비밀번호가 옳지않습니다.\n아이디는 이메일주소 이여야하고,\n비밀번호는 6자리 이상이여야합니다.")
                }
            }
    }
}