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
import kotlinx.android.synthetic.main.activity_login.*

/* 로그인방법을 선택하는 화면 , 앱의 시작화면이다.
   페이스북 로그인 아직 미구현. 로그인오류일때 세부적으로 처리예정.
 */
class LoginActivity : AppCompatActivity() {

    var googleSignInClient : GoogleSignInClient? = null //구글 로그인구현에 필요한 변수들
    val RC_SIGN_IN = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        goto_emailPage.setOnClickListener {     //emailLoginActivity로 연결.
            startActivity(Intent(this, emailLoginActivity::class.java))
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)  //구글 로그인구현에 필요한 변수들
            .requestIdToken(getString(R.string.default_web_client_id))              //firebase documentation 예제 기반으로 구현했음.
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        google_register_button.setOnClickListener { //구글 로그인버튼
            var signInIntent = googleSignInClient?.signInIntent
            startActivityForResult(signInIntent,RC_SIGN_IN)
        }

    }

    fun MoveNextPage(){     //로그인에 성공했는지 확인 후, 성공했다면 MainActivity로 이동한다.
        var currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            this.finish()
        }
    }

    fun firebaseAuthWithGoogle(idToken: String){    //구글로그인이 성공적인지 확인하는 함수
        var credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
            if(task.isSuccessful){
                MoveNextPage()
            }
        }
    }

    override fun onResume() {   //자동로그인
        super.onResume()
        MoveNextPage()
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
            }
            catch (e : ApiException){
                                                            // 로그인 실패처리에 대해서 추가할 예정.
            }
        }
        else{

        }
    }
}