package com.example.talk

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {

    var googleSignInClient : GoogleSignInClient? = null //구글 로그인구현에 필요한 변수들
    val RC_SIGN_IN = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_email_layout.setOnClickListener {     //emailLoginActivity로 연결.
            startActivity(Intent(this, EmailLoginActivity::class.java))
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)  //구글 로그인구현에 필요한 변수들
            .requestIdToken("42975930063-5n5rhahit49t2pd7lcp6kkr8up9rm8d1.apps.googleusercontent.com")              //firebase documentation 예제 기반으로 구현했음.
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        login_google_layout.setOnClickListener { //구글 로그인버튼
            var signInIntent = googleSignInClient?.signInIntent
            startActivityForResult(signInIntent,RC_SIGN_IN)
        }

    }

    fun firebaseAuthWithGoogle(idToken: String){    //구글로그인이 성공적인지 확인하는 함수
        var credential = GoogleAuthProvider.getCredential(idToken, null)
        toast("회원정보 확인중")
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    var currentUser = FirebaseAuth.getInstance().currentUser
                    val user = FirebaseAuth.getInstance().currentUser?.uid

                    if (currentUser != null) {

                        var doc = FirebaseFirestore.getInstance().collection("users").document(user.toString())

                        doc.get().addOnSuccessListener { document ->
                            if (document.data != null) {                //내 uid에 해당하는 정보가 존재한다면 main으로, 없다면 profileEdit으로 넘어가서 계정생성과젇을 거친다.
                                startActivity(Intent(this, MainActivity::class.java))
                                this.finish()
                            }else{
                                intent = Intent(this,profileEditActivity::class.java)
                                intent.putExtra("mode","MAKE_ACCOUNT")
                                intent.putExtra("email",currentUser.email.toString())
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {   //구글로그인을 구현하는 함수.
        super.onActivityResult(requestCode, resultCode, data)

        // Google Sign-In Methods
        if(requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)   // 원래 참고하던 자료에서는 account 자체를 넘겼엇는데
                                                            // documentation보고 account.idToken을 넘겨주는 방식으로 바꿨더니 오류안났음.
            } catch (e : ApiException){ }
        }
    }
}