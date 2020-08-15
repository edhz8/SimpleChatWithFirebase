package com.example.talk

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile_edit.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    lateinit var profileImagePath : String
    val db = FirebaseFirestore.getInstance()
    val user : String by lazy{
        FirebaseAuth.getInstance().currentUser?.uid.toString()
    }
    val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

/*              이거 맨위에 내 프로필하고 그 밑에 친구목록사이에 그어진 선 만들어보려고 한건데 아직 어설퍼서 주석처리 해놓음.
        db.collection("users")
            .whereEqualTo("uid", user)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) return@addSnapshotListener
                else {
                    println("exception null")
                }
                if (snapshot != null) {
                    snapshot.forEach {
                        val friend = realm.where<FriendModel>().equalTo("uid", user).findFirst()
                        if (friend == null) {
                            realm.beginTransaction()
                            val newItem = realm.createObject<FriendModel>()
                            newItem.name =
                                if (it["name"] != "") it["name"].toString() else "알수없는 사용자"
                            newItem.phoneNum =
                                if (it["phoneNum"] != null) it["phoneNum"].toString() else ""
                            newItem.profilePicPath =
                                if (it["profilePicPath"] != "") it["profilePicPath"].toString() else "https://firebasestorage.googleapis.com/v0/b/talk-fc671.appspot.com/o/profilePic%2Fusers.png?alt=media&token=64c14c60-409f-4f38-982e-c65bd9c814a0"
                            newItem.statusMessage =
                                if (it["statusmessage"] != "") it["statusmessage"].toString() else ""
                            newItem.uid = if (it["uid"] != null) it["uid"].toString() else ""
                            realm.commitTransaction()
                        } else {
                            friend?.apply {
                                realm.beginTransaction()
                                name = if (it["name"] != "") it["name"].toString() else "알수없는 사용자"
                                phoneNum =
                                    if (it["phoneNum"] != null) it["phoneNum"].toString() else ""
                                profilePicPath =
                                    if (it["profilePicPath"] != "") it["profilePicPath"].toString() else "https://firebasestorage.googleapis.com/v0/b/talk-fc671.appspot.com/o/profilePic%2Fusers.png?alt=media&token=64c14c60-409f-4f38-982e-c65bd9c814a0"
                                statusMessage =
                                    if (it["statusmessage"] != "") it["statusmessage"].toString() else ""
                                uid = if (it["uid"] != null) it["uid"].toString() else ""
                                realm.commitTransaction()
                            }
                        }
                    }
                }
            }
        val isLineExists = realm.where<FriendModel>().equalTo("uid", user).findFirst()
        if(isLineExists == null) {
            realm.beginTransaction()
            val makeLine = realm.createObject<FriendModel>()
            makeLine.uid =="Line"
            realm.commitTransaction()
        }
*/

        refreshFriends()
        val realmResult = realm.where<FriendModel>().findAll().sort("name")

        val adapter = FriendAdapter(this,realmResult)
        listView.adapter = adapter

        realmResult.addChangeListener { _ -> adapter.notifyDataSetChanged() }

        logout.setOnClickListener(){
            logout()
            realm.deleteAll()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
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
    }

    fun refreshFriends(){
        db.collection("users")
            .document(user)
            .collection("friends")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) return@addSnapshotListener
                if (querySnapshot != null) {

                    for (dc in querySnapshot.documents) {
                        val frienduid = dc["uid"].toString()
                        db.collection("users")
                            .whereEqualTo("uid", frienduid)
                            .addSnapshotListener { snapshot, exception ->
                                if(exception != null) return@addSnapshotListener
                                else{
                                    println("exception null")
                                }
                                if(snapshot != null){
                                    snapshot.forEach{
                                        val friend = realm.where<FriendModel>().equalTo("uid",frienduid).findFirst()
                                        if(friend == null){
                                            realm.beginTransaction()
                                            val newItem = realm.createObject<FriendModel>()
                                            newItem.name = if(dc["nickname"] != "") dc["nickname"].toString() else if(it["name"] != "") it["name"].toString() else "알수없는 사용자"
                                            newItem.phoneNum = if(it["phoneNum"] != null) it["phoneNum"].toString() else ""
                                            newItem.profilePicPath = if(it["profilePicPath"] != "") it["profilePicPath"].toString() else "https://firebasestorage.googleapis.com/v0/b/talk-fc671.appspot.com/o/profilePic%2Fusers.png?alt=media&token=64c14c60-409f-4f38-982e-c65bd9c814a0"
                                            newItem.statusMessage = if(it["statusmessage"] != "") it["statusmessage"].toString() else ""
                                            newItem.uid = if(it["uid"] != null) it["uid"].toString() else ""
                                            realm.commitTransaction()
                                        }else {friend?.apply{
                                            realm.beginTransaction()
                                            name = if(dc["nickname"] != "") dc["nickname"].toString() else if(it["name"] != "") it["name"].toString() else "알수없는 사용자"
                                            phoneNum = if(it["phoneNum"] != null) it["phoneNum"].toString() else ""
                                            profilePicPath = if(it["profilePicPath"] != "") it["profilePicPath"].toString() else "https://firebasestorage.googleapis.com/v0/b/talk-fc671.appspot.com/o/profilePic%2Fusers.png?alt=media&token=64c14c60-409f-4f38-982e-c65bd9c814a0"
                                            statusMessage = if(it["statusmessage"] != "") it["statusmessage"].toString() else ""
                                            uid = if(it["uid"] != null) it["uid"].toString() else ""
                                            realm.commitTransaction()
                                            }}
                                    }
                                }else{
                                    println("snapshot null")
                                }
                            }
                    }
                }

            }
    }
}