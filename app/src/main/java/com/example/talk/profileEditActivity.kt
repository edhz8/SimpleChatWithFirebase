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
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.talk.myutils.makeChatId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_profile_edit.*
import kotlinx.android.synthetic.main.activity_set_nickname.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

/* 프로필 변경 엑티비티 fire store 로 구현예정. */
@Suppress("DEPRECATION")
class profileEditActivity : AppCompatActivity() {
    val user = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val GALLERY = 0
    private var profilePicUri = "not_changed"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        val mode : String = intent.getStringExtra("mode").toString()
        val email: String = intent.getStringExtra("email").toString()
        var profilePicPath : String = intent.getStringExtra("profilePicPath").toString()

        if(mode == "MAKE_ACCOUNT"){     ///MAKE_ACCOUNT라는 값을 넘겨받았다면 회원등록을 위한 엑티비티로서 사용된다.
            profileedit_toolbar_text.text="회원정보 등록"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }

        if(profilePicPath != "null"){   //프로필사진 주소가 null이 아니라면 해당사진을 로드하고, null이라면 기본프로필사진을 로드한다.
            Glide.with(this)
                .load(profilePicPath)
                .into(profileedit_profilePic)
        } else{
            Glide.with(this)
                .load(getString(R.string.default_profilePic_url))
                .into(profileedit_profilePic)
        }

        profileedit_profilePic.setOnClickListener {     //프로필사진을 클릭해서 프로필사진을 교체할 수 있다.
            val option = arrayOf("앨범에서 사진 선택", "기본 이미지")
            AlertDialog.Builder(this@profileEditActivity)
                .setItems(option) { _, which ->
                    when (option[which]) {
                        "앨범에서 사진 선택" -> {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            startActivityForResult(intent,GALLERY)
                        }
                        "기본 이미지" -> {
                            profilePicUri = getString(R.string.default_profilePic_url)
                            profileedit_profilePic.setImageResource(R.drawable.users)
                        }
                    }
                }
                .show()
        }

        profileedit_editName.setText(intent.getStringExtra("name"))
        profileedit_editName_textInputLayout.isCounterEnabled = true
        profileedit_editName_textInputLayout.counterMaxLength = 10
        profileedit_editName_textInputLayout.isErrorEnabled = true
        profileedit_editName.addTextChangedListener(object : TextWatcher{   //계정의 이름은 1~10글자로 지정한다.
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if(profileedit_editName.length() > 10) {
                    profileedit_editName_textInputLayout.error = "이름은 최대 10글자 입니다."
                } else if (profileedit_editName.length() == 0) {
                    profileedit_editName_textInputLayout.error = "이름은 1글자 이상 입니다."
                }else{
                    profileedit_editName_textInputLayout.error = null
                }
            }
        })

        profileedit_statusMessage.setText(intent.getStringExtra("statusMessage"))
        profileedit_statusMessage_textInputLayout.isCounterEnabled = true
        profileedit_statusMessage_textInputLayout.counterMaxLength = 30
        profileedit_statusMessage_textInputLayout.isErrorEnabled = true
        profileedit_statusMessage.addTextChangedListener(object : TextWatcher{  //계정의 상태메시지는 0~30글자로 지정한다.
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if(profileedit_statusMessage.length() > 30) {
                    profileedit_statusMessage_textInputLayout.error = "상태메시지는 최대 30글자 입니다."
                }else{
                    profileedit_statusMessage_textInputLayout.error = null
                }
            }
        })

        profileedit_toolbar_backArrow.setOnClickListener {
            finish()
        }


        profileedit_toolbar_confirm.setOnClickListener{
            val newName: String = profileedit_editName.text.toString()
            val newStatusMessage: String = profileedit_statusMessage.text.toString()
            var map = mutableMapOf<String,Any>()

            if (newName.length > 0 && newName.length < 11) {
                map["name"] = newName
                if (newStatusMessage.length < 30) {     //이름과 상태메시지 모두 조건을 만족해야 등록할 수 있다.
                                                        // 사용자에게 각각 toast메시지를 보여주기 위해 이중으로 조건문을 사용했다.
                    map["statusmessage"] = newStatusMessage
                    if(profilePicUri != "not_changed") {
                        map["profilePicPath"] = profilePicUri
                    }

                    if(mode == "MAKE_ACCOUNT"){ //회원등록일때는 계정의 필수요소들을 만들어주는 과정을 거친다.(null로 인한 오류를 최대한 방지하기 위해)
                        map["uid"]=user
                        map["email"]=email
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user)
                            .set(map)
                        var fmap = mutableMapOf<String,Any>()
                        fmap["chatid"]=""
                        fmap["nickname"]=newName
                        fmap["status"]=-1
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user)
                            .collection("friends")
                            .document(user)
                            .set(fmap)
                    } else{                 //자신의 이름을 변경할때는 자신doument의 이름뿐만 아니라, 자신이 가지고있는 친구목록에서의 자기이름도 update해준다.
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user)
                            .update(map)
                            .addOnSuccessListener {
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(user)
                                    .collection("friends")
                                    .document(user)
                                    .update("nickname",newName)
                                    .addOnSuccessListener {
                                        finish()
                                    }
                            }
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                    this.finish()
                } else{
                    toast("상태메시지의 글자수를 확인해주세요.")
                }
            } else{
                toast("이름의 글자수를 확인해주세요.")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {   //ucrop으로 전달받은 프로필사진을 450*450사이즈의 png파일로 만들어 서버에 저장한다.
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GALLERY && resultCode == Activity.RESULT_OK && data != null){
            var photoUri = data.data
            val destinationUri = Uri.fromFile(File(applicationContext.cacheDir, "IMG_" + makeChatId()))

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
                        profilePicRef.downloadUrl.addOnSuccessListener {//변환이 완료되면 서버에 사진을 올리고, 해당url을 profilePicUri에 넘긴다.
                            profilePicUri = it.toString()
                            progressDialog.dismiss()
                        }
                    }

                    Glide.with(this)                //넘겨받은주소의 프로필사진을 로드해준다.
                        .load(bmp)
                        .into(profileedit_profilePic)

                } catch (e: IOException) {
                    e.printStackTrace()

                }

            }
        }
    }

}