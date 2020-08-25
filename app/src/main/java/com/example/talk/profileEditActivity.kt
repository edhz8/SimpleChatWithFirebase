package com.example.talk

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_profile_edit.*
import org.jetbrains.anko.indeterminateProgressDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

/* 프로필 변경 엑티비티 fire store 로 구현예정. */
@Suppress("DEPRECATION")
class profileEditActivity : AppCompatActivity() {
    val GALLERY = 0
    private var profilePicUri = "not_changed"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }

        val profilePicPath = intent.getStringExtra("profilePicPath")
        if(profilePicPath != "default"){
            Glide.with(this)
                .load(profilePicPath)
                .into(profilePic)
        }

        profilePic.setOnClickListener {
            val option = arrayOf("앨범에서 사진 선택", "기본 이미지")
            AlertDialog.Builder(this@profileEditActivity)
                .setItems(option) { _, which ->
                    when (option[which]) {
                        "앨범에서 사진 선택" -> {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            startActivityForResult(intent,GALLERY)
                        }
                        "기본 이미지" -> {
                            profilePic.setImageResource(R.drawable.users)
                        }
                    }
                }
                .show()
        }

        editStatusMessage.setText(intent.getStringExtra("statusMessage"))
        editName.setText(intent.getStringExtra("name"))


        profile_confirm.setOnClickListener{
            //openAlbum()
            saveData()
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GALLERY && resultCode == Activity.RESULT_OK && data != null){
            var photoUri = data.data
            val destinationUri = Uri.fromFile(File(applicationContext.cacheDir, "IMG_" + System.currentTimeMillis()))

            if (photoUri != null) {
                UCrop.of(photoUri, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(450, 450)
                    .start(this)
            }
        }
        if(requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK){
            val croppedUri = UCrop.getOutput(data!!)
            val progressDialog = indeterminateProgressDialog("프로필 사진 변경중")
            progressDialog.setCancelable(false)

            if(croppedUri != null){
                val bmp : Bitmap
                try {
                    bmp = MediaStore.Images.Media.getBitmap(contentResolver, croppedUri)
                    val outputStream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                    val byteArray = outputStream.toByteArray()

                    val user = FirebaseAuth.getInstance().currentUser?.uid
                    val fileName = user.toString() + ".png"

                    var profilePicRef = FirebaseStorage.getInstance().reference
                        .child("profilePic").child(fileName)
                    profilePicRef.putBytes(byteArray).addOnSuccessListener {
                        profilePicRef.downloadUrl.addOnSuccessListener {
                            profilePicUri = it.toString()
                            progressDialog.dismiss()
                        }
                    }

                    Glide.with(this)
                        .load(bmp)
                        .into(profilePic)

                } catch (e: IOException) {
                    e.printStackTrace()

                }

            }
        }
    }
    fun saveData(){
        val user = FirebaseAuth.getInstance().currentUser?.uid.toString()
        var setEditName=editName.text.toString()
        var setEditStatusMessage=editStatusMessage.text.toString()
        var map = mutableMapOf<String,Any>()
        map["name"] = setEditName
        map["statusmessage"] = setEditStatusMessage
        map["uid"] = user
        if(profilePicUri == "not_changed") {
            map["profilePicPath"] = getString(R.string.default_profilePic_url)
        }else{
            map["profilePicPath"] = profilePicUri
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user)
            .set(map)
    }
}