package com.example.talk


import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when(p0.itemId) {
            R.id.home->{
                val transaction =supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_Layout,FriendFragment())
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

        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        /*logout.setOnClickListener(){
            logout()
            realm.beginTransaction()
            realm.deleteAll()
            realm.commitTransaction()
            startActivity(Intent(this, LoginActivity::class.java))
        }*/
    }

/*
    fun logout(){
        var googleSignInClient : GoogleSignInClient? = null
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("42975930063-5n5rhahit49t2pd7lcp6kkr8up9rm8d1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        FirebaseAuth.getInstance().signOut()
        googleSignInClient?.signOut()
    }

    fun toolbarProfileImage(context : Context,
                            profileImage : CircleImageView,
                            onComplete: (String) -> Unit){

        db.collection("users")
            .document(user)
            .addSnapshotListener { snapshot, firestoreException ->
                if(firestoreException != null) return@addSnapshotListener
                if(snapshot != null){
                    val imagePath = snapshot["profilePicPath"].toString()
                    if(imagePath != null && !(context as Activity).isFinishing){
                        Glide.with(context)
                            .load(imagePath)
                            .into(profileImage)
                        onComplete(imagePath)
                    }
                }
            }

    }

    fun makeChatRoom(){
        var map = mutableMapOf<String,Any>()
        map["people"] = arrayOf(user)
        map["notice"] = -1
        map["lastChatNumber"] = -1

        db
            .collection("users")
            .document(user.toString())
            .set(map)
    }*/

}