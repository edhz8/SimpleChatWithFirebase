package com.example.talk

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.talk.myutils.makeChatId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_friend.view.*
import java.lang.RuntimeException

class FriendFragment : Fragment() {

    var db: FirebaseFirestore? = null
    var user: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_friend, container, false)
        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser?.uid


        view.friend_recyclerview.adapter = friendRecyclerViewAdapter()
        view.friend_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class friendRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var friendList: ArrayList<FriendModel> = arrayListOf()
        private lateinit var dialog: showProfileDialog

        init {          //친구목록을 구성하는 정보를 받아온다.
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
                                            val statusMessage =
                                                if (it["statusmessage"] != "") it["statusmessage"].toString() else ""
                                            val profilePicPath =
                                                if (it["profilePicPath"] != "") it["profilePicPath"].toString() else getString(
                                                    R.string.default_profilePic_url
                                                )
                                            val uid =
                                                if (it["uid"] != null) it["uid"].toString() else ""


                                            var type: Int = -1
                                            if (statusMessage != "" && uid != user) type = 0
                                            else if (statusMessage == "" && uid != user) type = 1
                                            else if (statusMessage != "" && uid == user) type = 2
                                            else if (statusMessage == "" && uid == user) type = 3


                                                friendList.add(FriendModel(
                                                    type = type,
                                                    name = name,
                                                    statusMessage = statusMessage,
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view: View?

            return when (viewType) {
                0 -> {
                    view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.friend_with_statusmessage, parent, false)
                    friendWithSMViewHolder(view)
                }

                1 -> {
                    view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.friend_without_statusmessage, parent, false)
                    friendWithoutSMViewHolder(view)
                }

                2 -> {
                    view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.myprofile_with_statusmessage, parent, false)
                    myprofileWithSMViewHolder(view)
                }

                3 -> {
                    view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.myprofile_without_statusmessage, parent, false)
                    myprofileWithoutSMViewHolder(view)
                }

                4 -> {
                    view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.friend_line, parent, false)
                    lineViewHolder(view)
                }
                else -> throw RuntimeException("알 수 없는 뷰 타입 에러")
            }
        }

        override fun getItemCount(): Int = friendList.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val item = friendList[position]
            when (item.type) { //뷰홀더에 어떤값들을 넣어줄건지 지정하는부분
                0 -> {
                    (holder as friendRecyclerViewAdapter.friendWithSMViewHolder).nameView.text =
                        item.name
                    Glide.with(holder.itemView.context)
                        .load(item.profilePicPath)
                        .into(holder.profilePicView)
                    holder.statusmessageView.text = item.statusMessage
                }

                1 -> {
                    (holder as friendRecyclerViewAdapter.friendWithoutSMViewHolder).nameView.text =
                        item.name
                    Glide.with(holder.itemView.context)
                        .load(item.profilePicPath)
                        .into(holder.profilePicView)
                }

                2 -> {
                    (holder as friendRecyclerViewAdapter.myprofileWithSMViewHolder).nameView.text =
                        item.name
                    Glide.with(holder.itemView.context)
                        .load(item.profilePicPath)
                        .into(holder.profilePicView)
                    holder.statusmessageView.text = item.statusMessage
                }

                3 -> {
                    (holder as friendRecyclerViewAdapter.myprofileWithoutSMViewHolder).nameView.text =
                        item.name
                    Glide.with(holder.itemView.context)
                        .load(item.profilePicPath)
                        .into(holder.profilePicView)
                }
                else -> {
                    println("알수없는 뷰홀더")
                }
            }

            holder.itemView.setOnClickListener {
                showDialog(it.context, item, user)
            }
        }

        override fun getItemViewType(position: Int): Int = friendList[position].type

        fun showDialog(context: Context, friendModel: FriendModel, user: String?) {     //친구를 클릭하면 친구의 프로필을 보여주는 다이얼로그를 부르는 함수.
            val name = friendModel.name
            val profilePicPath = friendModel.profilePicPath
            val statusMessage = friendModel.statusMessage
            val uid = friendModel.uid

            val editbuttonListener = View.OnClickListener {//클릭리스너를 미리 만들어서 넘겨준다.
                val intent = Intent(context, profileEditActivity::class.java)
                intent.putExtra("name", name)
                intent.putExtra("profilePicPath", profilePicPath)
                intent.putExtra("statusMessage", statusMessage)
                startActivity(intent)
            }

            val setnickbuttonListener = View.OnClickListener {
                val intent = Intent(context, SetNicknameActivity::class.java)
                intent.putExtra("friendNickName", name)
                intent.putExtra("user", user)
                intent.putExtra("uid", uid)
                startActivity(intent)
            }

            val chatbuttonListener = View.OnClickListener {     //개인채팅을 불러오거나 기존채팅이 없다면 새로 생성하는 클릭리스너.
                db?.collection("users")
                    ?.document(user!!)
                    ?.collection("friends")
                    ?.document(uid)
                    ?.get()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var chatid: String = task.result?.get("chatid").toString()

                            if (chatid == "") {

                                chatid = makeChatId()

                                val roominit = mutableMapOf<String, Any>(   //개인채팅 초기화
                                    "lastchat" to "대화를 나눠보세요!",
                                    "lastchattime" to System.currentTimeMillis(),
                                    "chatid" to chatid
                                )

                                db!!.collection("privatechat")
                                    .document(chatid)
                                    .set(roominit)

                                db!!.collection("users")
                                    .document(user)
                                    .collection("friends")
                                    .document(uid)
                                    .update("chatid", chatid)

                                db!!.collection("users")
                                    .document(uid)
                                    .collection("friends")
                                    .document(user)
                                    .update("chatid", chatid)
                            }
                            val uidList : ArrayList<String> = arrayListOf(
                                user,
                                uid
                            )
                            val intent = Intent(context, ChatActivity::class.java)
                            intent.putStringArrayListExtra("uidList",uidList)
                            intent.putExtra("roomName",name)
                            intent.putExtra("chatid",chatid)
                            intent.putExtra("mode","privatechat")
                            startActivity(intent)
                        }
                    }
            }



            dialog = showProfileDialog(
                context = context,
                profilePicPath = friendModel.profilePicPath,
                name = friendModel.name,
                statusMessage = friendModel.statusMessage,
                uid = friendModel.uid,
                user = user,
                editbtnListener = editbuttonListener,
                setnickbtnListener = setnickbuttonListener,
                chatbtnListener = chatbuttonListener
            )
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }


        inner class friendWithSMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameView: TextView = itemView.findViewById(R.id.item_withSM_name)
            val profilePicView: ImageView = itemView.findViewById(R.id.item_withSM_profilePic)
            val statusmessageView: TextView =
                itemView.findViewById(R.id.item_withSM_statusmessage)
        }

        inner class friendWithoutSMViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            val nameView: TextView = itemView.findViewById(R.id.item_withoutSM_name)
            val profilePicView: ImageView =
                itemView.findViewById(R.id.item_withoutSM_profilePic)
        }

        inner class myprofileWithSMViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            val nameView: TextView = itemView.findViewById(R.id.item_myprofile_withSM_name)
            val profilePicView: ImageView =
                itemView.findViewById(R.id.item_myprofile_withSM_profilePic)
            val statusmessageView: TextView =
                itemView.findViewById(R.id.item_myprofile_withSM_statusmessage)
        }

        inner class myprofileWithoutSMViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            val nameView: TextView = itemView.findViewById(R.id.item_myprofile_withoutSM_name)
            val profilePicView: ImageView =
                itemView.findViewById(R.id.item_myprofile_withoutSM_profilePic)
        }

        inner class lineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}