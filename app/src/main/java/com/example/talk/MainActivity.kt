package com.example.talk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),BottomNavigationView.OnNavigationItemSelectedListener {
    var googleSignInClient : GoogleSignInClient? = null

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when(p0.itemId) {
            R.id.home->{
                val transaction =supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_Layout,HomeFragment())
                transaction.commit()
                return true
        }
          R.id.chat-> {
              val transaction =supportFragmentManager.beginTransaction()
              transaction.replace(R.id.frame_Layout,ChatFragment())
              transaction.commit()
              return true
          }
    }
        return false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("42975930063-5n5rhahit49t2pd7lcp6kkr8up9rm8d1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        /*logout_button.setOnClickListener {
            logout()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        profile_edit_button.setOnClickListener {
            startActivity(Intent(this, profileEditActivity::class.java))
            this.finish()
        }*/

    }

    fun logout(){
        FirebaseAuth.getInstance().signOut()

        //Google Session out
        googleSignInClient?.signOut()

        /*Facebook Session out
        LoginManager.getInstance().logOut()

         */

        finish()
    }

}