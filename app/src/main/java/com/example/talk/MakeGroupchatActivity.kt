package com.example.talk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.talk.myutils.makeChatId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_make_groupchat.*
import kotlinx.android.synthetic.main.activity_profile_edit.*
import kotlinx.android.synthetic.main.item_make_groupchat.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.RuntimeException

class MakeGroupchatActivity : AppCompatActivity() {

    var db: FirebaseFirestore? = null
    var user: String? = null
    var checkboxList = arrayListOf<checkboxData>()
    val GALLERY = 0
    var profilePicUri : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_groupchat)

        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser?.uid.toString()

        makegroupchat_recyclerview.adapter = makeGroupchatRecyclerViewAdapter()
        makegroupchat_recyclerview.layoutManager = LinearLayoutManager(this)



        makegroupchat_profilePic.setOnClickListener {
            val option = arrayOf("앨범에서 사진 선택", "기본 이미지")
            AlertDialog.Builder(this@MakeGroupchatActivity)
                .setItems(option) { _, which ->
                    when (option[which]) {
                        "앨범에서 사진 선택" -> {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            startActivityForResult(intent,GALLERY)
                        }
                        "기본 이미지" -> {
                            profilePicUri = getString(R.string.default_profilePic_url)
                            makegroupchat_profilePic.setImageResource(R.drawable.users)
                        }
                    }
                }
                .show()
        }

        makegroupchat_textInputLayout.isCounterEnabled = true
        makegroupchat_textInputLayout.counterMaxLength = 10
        makegroupchat_textInputLayout.isErrorEnabled = true
        makegroupchat_edittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if(makegroupchat_edittext.length() > 10) {
                    makegroupchat_textInputLayout.error = "채팅방이름은 최대 10글자 입니다."
                } else if (makegroupchat_edittext.length() == 0) {
                    makegroupchat_textInputLayout.error = "채팅방이름은 1글자 이상 입니다."
                }else{
                    makegroupchat_textInputLayout.error = null
                }
            }
        })

        makegroupchat_toolbar_confirm.setOnClickListener {
            val roomName: String = makegroupchat_edittext.text.toString()
            val chatid : String = makeChatId()
            if(roomName.length > 0 && roomName.length <11){
                var uidList : ArrayList<String> = arrayListOf()
                uidList.add(user!!)
                val map  = hashMapOf<String,ArrayList<String>>("uids" to uidList)
                val roominit = hashMapOf<String,Any>(
                    "lastchat" to "대화를 나눠보세요!",
                    "lastchattime" to System.currentTimeMillis(),
                    "chatid" to chatid,
                    "roomname" to roomName,
                    "profilePicPath" to if(profilePicUri == "") getString(R.string.default_profilePic_url) else profilePicUri
                )

                for(i in checkboxList){
                    if(i.checked) uidList.add(i.uid)
                }

                for(uid in uidList){
                    db!!.collection("users")
                        .document(uid)
                        .collection("groupchats")
                        .document(chatid)
                        .set(map)
                }

                db!!.collection("groupchat")
                    .document(chatid)
                    .set(roominit)

                intent = Intent(this,ChatActivity::class.java)
                intent.putExtra("roomName",roomName)
                intent.putExtra("mode","groupchat")
                intent.putExtra("chatid",chatid)
                intent.putStringArrayListExtra("uidList",uidList)
                startActivity(intent)
            } else{
                toast("채팅방이름의 글자수를 확인해주세요.")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                        profilePicRef.downloadUrl.addOnSuccessListener {
                            profilePicUri = it.toString()
                            progressDialog.dismiss()
                        }
                    }

                    Glide.with(this)
                        .load(bmp)
                        .into(makegroupchat_profilePic)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    inner class makeGroupchatRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var friendList: ArrayList<FriendModel> = arrayListOf()
        init {
            db?.collection("users")
                ?.document(user!!)
                ?.collection("friends")
                ?.orderBy("status")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    friendList.clear()
                    if (firebaseFirestoreException != null) return@addSnapshotListener
                    if (querySnapshot != null) {
                        for (dc in querySnapshot.documents) {
                            val frienduid = dc.id

                            if(frienduid != user.toString()){
                                val nickname = dc["nickname"].toString()
                                val status = dc["status"] as Long
                                db!!.collection("users")
                                    .whereEqualTo("uid", frienduid)
                                    .addSnapshotListener { snapshot, exception ->
                                        if (exception != null) return@addSnapshotListener
                                        if (snapshot != null) {
                                            snapshot.forEach {
                                                val name =
                                                    if (nickname != "") nickname else if (it["name"] != "") it["name"].toString() else "알수없는 사용자"
                                                val profilePicPath =
                                                    if (it["profilePicPath"] != "") it["profilePicPath"].toString() else getString(
                                                        R.string.default_profilePic_url
                                                    )
                                                val uid =
                                                    if (it["uid"] != null) it["uid"].toString() else ""

                                                friendList.add(FriendModel(
                                                    type = 0,
                                                    name = name,
                                                    statusMessage = "",
                                                    profilePicPath = profilePicPath,
                                                    uid = uid,
                                                    status = status
                                                ))
                                            }
                                            notifyDataSetChanged()
                                        }
                                    }
                            }

                        }
                    }


                }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view: View?

            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_make_groupchat, parent, false)
            return friendViewHolder(view)
        }

        override fun getItemCount(): Int {
            return friendList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val item = friendList[position]

            (holder as makeGroupchatRecyclerViewAdapter.friendViewHolder).nameView.text =
                item.name

            Glide.with(holder.itemView.context)
                .load(item.profilePicPath)
                .into(holder.profilePicView)

            if(position >= checkboxList.size){
                checkboxList.add(position,checkboxData(item.uid,false))
            }

            holder.checkBoxView.isChecked = checkboxList[position].checked

            holder.checkBoxView.setOnClickListener{
                checkboxList[position].checked = item_makegroupchat_checkBox.isChecked
            }
        }

        inner class friendViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            val nameView: TextView = itemView.findViewById(R.id.item_makegroupchat_name)
            val profilePicView: ImageView =
                itemView.findViewById(R.id.item_makegroupchat_profilePic)
            val checkBoxView : CheckBox = itemView.findViewById(R.id.item_makegroupchat_checkBox)
        }

    }
}