package com.example.talk

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile_edit.*
import java.text.SimpleDateFormat
import java.util.*

/* 프로필 변경 엑티비티 fire store 로 구현예정. */
class profileEditActivity : AppCompatActivity() {
    val GALLERY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }

        profile_confirm.setOnClickListener{
            //openAlbum()
            saveData()
        }
    }

    fun uploadPhoto(photoUri: Uri) {
        var fileName = "profilePic.png"

        var storageRef = FirebaseStorage.getInstance().reference.child("images").child(fileName)

        storageRef.putFile(photoUri).addOnSuccessListener {
            Toast.makeText(this, "Upload photo completed", Toast.LENGTH_LONG).show()
        }
    }
    fun saveData(){
        val user = FirebaseAuth.getInstance().currentUser?.uid
        var setEditName=editName.text.toString()
        var setEditStatusMessage=editStatusMessage.text.toString()
        var map = mutableMapOf<String,Any>()
        map["name"] = setEditName
        map["statusmessage"] = setEditStatusMessage

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.toString())
            .set(map)
    }

}