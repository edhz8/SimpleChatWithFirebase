package com.example.talk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {

    companion object{
        const val FRIEND_CHAT_TEXT : Int = 0
        const val FRIEND_CHAT_PICTURE : Int = 1
        const val MY_CHAT_TEXT : Int = 2
        const val MY_CHAT_PICTURE : Int = 3
    }

    val GALLERY = 0
    var db: FirebaseFirestore? = null
    var user: String? = null
    var uidList : ArrayList<String>? = null
    var fcmPush : FcmPush? = null
    var mode : String = ""
    var chatid : String = ""
    var sendPicPath : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser?.uid.toString()
        fcmPush = FcmPush()
        uidList = intent.getStringArrayListExtra("uidList")
        mode = intent.getStringExtra("mode").toString()
        chatid = intent.getStringExtra("chatid").toString()

        chat_recyclerview.adapter = chatRecyclerViewAdapter()
        chat_recyclerview.layoutManager = LinearLayoutManager(this)

        chat_toolbar_chatRoomName.text = intent.getStringExtra("roomName").toString()
        chat_toolbar_chatRoomName.setTypeface(null, Typeface.BOLD)
        chat_toolbar_backArrow.setOnClickListener {
            finish()
        }
        chat_toolbar_search.setOnClickListener {
            //
        }

        chat_inputLayout_editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                chat_inputLayout_sendButton.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (chat_inputLayout_editText.length() != 0) {
                    chat_inputLayout_sendButton.setBackgroundColor(Color.parseColor("#e9da29"))
                } else{
                    chat_inputLayout_sendButton.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        chat_inputLayout_UploadPicture.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent,GALLERY)
        }

        chat_inputLayout_sendButton.setOnClickListener {
            if (chat_inputLayout_editText.length() != 0) {
                val message: String = chat_inputLayout_editText.text.toString()
                val messagetime : Long = System.currentTimeMillis()
                val chatModel: ChatModel = ChatModel(
                    uid = user,
                    kind = 0,
                    timestamp = messagetime,
                    message = message
                )

                db!!.collection(mode)
                    .document(chatid)
                    .collection("chats")
                    .document()
                    .set(chatModel)

                val lastchatmap = mutableMapOf<String,Any>()
                lastchatmap["lastchat"]=message
                lastchatmap["lastchattime"]=messagetime
                db?.collection(mode)!!
                    .document(chatid)
                    .update(lastchatmap)

                for(uid in uidList!!){
                    if( uid != user){
                        db!!.collection("users")
                            .document(uid)
                            .collection("friends")
                            .document(user!!)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    fcmPush?.sendMessage(uid, task.result?.get("nickname").toString(), message)
                                }
                            }
                    }
                }
            }
            chat_inputLayout_editText.setText("")
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GALLERY && resultCode == Activity.RESULT_OK && data != null){
            var photoUri = data.data
            val destinationUri = Uri.fromFile(File(applicationContext.cacheDir, "IMG_" + myutils.makeChatId()))

            if (photoUri != null) {
                UCrop.of(photoUri, destinationUri)
                    .start(this)
            }
        }
        if(requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK){
            val croppedUri = UCrop.getOutput(data!!)

            if(croppedUri != null){
                val bmp : Bitmap
                try {
                    bmp = MediaStore.Images.Media.getBitmap(contentResolver, croppedUri)
                    val outputStream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                    val byteArray = outputStream.toByteArray()

                    val user = FirebaseAuth.getInstance().currentUser?.uid
                    val fileName = myutils.makeChatId() + ".png"

                    var profilePicRef = FirebaseStorage.getInstance().reference
                        .child("chatPic").child(fileName)
                    profilePicRef.putBytes(byteArray).addOnSuccessListener {
                        profilePicRef.downloadUrl.addOnSuccessListener {
                            sendPicPath = it.toString()

                            if(sendPicPath !=""){
                                val messagetime : Long = System.currentTimeMillis()
                                val chatModel: ChatModel = ChatModel(
                                    uid = user,
                                    kind = 1,
                                    timestamp = messagetime,
                                    message = sendPicPath
                                )

                                db!!.collection(mode)
                                    .document(chatid)
                                    .collection("chats")
                                    .document()
                                    .set(chatModel)

                                val lastchatmap = mutableMapOf<String,Any>()
                                lastchatmap["lastchat"]="사진을 보냈습니다."
                                lastchatmap["lastchattime"]=messagetime
                                db?.collection(mode)!!
                                    .document(chatid)
                                    .update(lastchatmap)

                                for(uid in uidList!!){
                                    if( uid != user){
                                        db!!.collection("users")
                                            .document(uid)
                                            .collection("friends")
                                            .document(user!!)
                                            .get()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    fcmPush?.sendMessage(uid, task.result?.get("nickname").toString(), "사진을 보냈습니다.")
                                                }
                                            }
                                    }
                                }
                            }

                            sendPicPath = ""
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }


            }
        }
    }

    inner class chatRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var chatList: ArrayList<ChatModel> = arrayListOf()
        var friendmap = mutableMapOf<String,FriendModel>()

        init {

            for(uid in uidList!!){
                if(uid != user){
                    var name : String = ""
                    db!!.collection("users")
                        .document(user!!)
                        .collection("friends")
                        .document(uid)
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                name = task.result?.get("nickname").toString()

                                db!!.collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val profilePicPath : String = task.result?.get("profilePicPath").toString()
                                            val fitem = FriendModel(name=name,profilePicPath = profilePicPath)
                                            friendmap[uid] = fitem
                                        }
                                        notifyDataSetChanged()
                                    }
                            }
                        }

                }
            }

            db!!.collection(mode)
                .document(chatid)
                .collection("chats")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    chatList.clear()
                    if (firebaseFirestoreException != null) return@addSnapshotListener
                    if (querySnapshot != null) {
                        for (dc in querySnapshot.documents) {
                            var item = dc.toObject(ChatModel::class.java)
                            chatList.add(item!!)
                        }
                        notifyDataSetChanged()
                    }
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View?
            return when (viewType) {
                FRIEND_CHAT_TEXT -> {
                    view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.friend_chat_text, parent, false)
                    friendChatTextViewHolder(view)
                }
                FRIEND_CHAT_PICTURE -> {
                    view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.friend_chat_picture, parent, false)
                    friendChatPictureViewHolder(view)

                }
                MY_CHAT_TEXT -> {
                    view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.my_chat_text, parent, false)
                    myChatTextViewHolder(view)
                }
                MY_CHAT_PICTURE -> {
                    view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.my_chat_picture, parent, false)
                    myChatPictureViewHolder(view)
                }

                else -> throw RuntimeException("알 수 없는 뷰 타입 에러")
            }
        }

        override fun getItemCount(): Int = chatList.size

        override fun getItemViewType(position: Int): Int {
            val item = chatList[position]
            return if(item.uid != user){
                if(item.kind == 0){
                    FRIEND_CHAT_TEXT
                } else{
                    FRIEND_CHAT_PICTURE
                }
            } else{
                if(item.kind == 0){
                    MY_CHAT_TEXT
                } else {
                    MY_CHAT_PICTURE
                }
            }
        }

        @SuppressLint("SimpleDateFormat")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = chatList[position]
            val type = getItemViewType(position)
            val name = friendmap[item.uid]?.name.toString()
            val profilePicPath = friendmap[item.uid]?.profilePicPath.toString()
            when(type) {

                FRIEND_CHAT_TEXT -> {
                    (holder as ChatActivity.chatRecyclerViewAdapter.friendChatTextViewHolder).nameView.text = name
                    Glide.with(holder.itemView.context)
                        .load(profilePicPath)
                        .into(holder.profilePicView)
                    holder.messageView.text = item.message

                    val format = SimpleDateFormat("HH시 mm분")
                    val date = format.format(Date(item.timestamp!!))
                    holder.timeView.text = date
                }

                FRIEND_CHAT_PICTURE -> {
                    (holder as ChatActivity.chatRecyclerViewAdapter.friendChatPictureViewHolder).nameView.text = name
                    Glide.with(holder.itemView.context)
                        .load(profilePicPath)
                        .into(holder.profilePicView)

                    Glide.with(holder.itemView.context)
                        .load(item.message)
                        .into(holder.messageView)

                    val format = SimpleDateFormat("HH시 mm분")
                    val date = format.format(Date(item.timestamp!!))
                    holder.timeView.text = date

                }

                MY_CHAT_TEXT -> {
                    (holder as ChatActivity.chatRecyclerViewAdapter.myChatTextViewHolder).messageView.text = item.message
                    val format = SimpleDateFormat("HH시 mm분")
                    val date = format.format(Date(item.timestamp!!))
                    holder.timeView.text = date
                }

                MY_CHAT_PICTURE -> {
                    val format = SimpleDateFormat("HH시 mm분")
                    val date = format.format(Date(item.timestamp!!))
                    (holder as ChatActivity.chatRecyclerViewAdapter.myChatPictureViewHolder).timeView.text = date

                    Glide.with(holder.itemView.context)
                        .load(item.message)
                        .into(holder.messageView)
                }

            }
            // TODO: 2020-10-30 본문 롱클릭하면 복사,삭제,전달 / 프사클릭하면 다이얼로그  

        }

        inner class friendChatTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameView: TextView = itemView.findViewById(R.id.friendchat_withPP_text_name)
            val profilePicView: ImageView = itemView.findViewById(R.id.friendchat_withPP_text_profilePic)
            val messageView: TextView = itemView.findViewById(R.id.friendchat_withPP_text_message)
            val timeView: TextView = itemView.findViewById(R.id.friendchat_withPP_text_time)
        }

        inner class friendChatPictureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val nameView: TextView = itemView.findViewById(R.id.friendchat_picture_name)
            val profilePicView: ImageView = itemView.findViewById(R.id.friendchat_picture_profilePic)
            val messageView: ImageView = itemView.findViewById(R.id.friendchat_picture_messagePic)
            val timeView: TextView = itemView.findViewById(R.id.friendchat_picture_time)
        }

        inner class myChatTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val messageView: TextView = itemView.findViewById(R.id.mychat_text_message)
            val timeView: TextView = itemView.findViewById(R.id.mychat_text_time)
        }

        inner class myChatPictureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val messageView: ImageView = itemView.findViewById(R.id.mychat_picture_messagePic)
            val timeView: TextView = itemView.findViewById(R.id.mychat_picture_time)
        }
    }

}
