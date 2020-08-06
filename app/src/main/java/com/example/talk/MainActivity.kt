package com.example.talk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    lateinit var profileImagePath : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbarProfileImage(this,myProfile_imageview_mainActivity){
            profileImagePath = it
        }

        myProfile_imageview_mainActivity.setOnClickListener {
            val intent = Intent(this, profileEditActivity::class.java)
            intent.putExtra("profileImagePath", profileImagePath)
            startActivity(intent)
        }

        logout_button.setOnClickListener {
            logout()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun logout(){
        var googleSignInClient : GoogleSignInClient? = null
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("42975930063-5n5rhahit49t2pd7lcp6kkr8up9rm8d1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        FirebaseAuth.getInstance().signOut()
        googleSignInClient?.signOut()
        finish()
    }

    fun toolbarProfileImage(context : Context,
                            profileImage : CircleImageView,
                            onComplete: (String) -> Unit){

        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
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

}