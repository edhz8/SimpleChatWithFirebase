package com.example.talk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.toast

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        MoveNextPage()
    }

    fun MoveNextPage() {     //로그인에 성공했는지 확인 후, 성공했다면 MainActivity로 이동한다.
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
        } else{
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }

    override fun onResume() {   //자동로그인
        super.onResume()
        MoveNextPage()
    }
}