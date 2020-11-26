package com.example.talk

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import kotlinx.android.synthetic.main.fragment_group_chat.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GroupChatFragment : Fragment() {

    var db : FirebaseFirestore? = null
    var user : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_group_chat,container,false)
        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser?.uid.toString()

        view.group_chat_recyclerview.adapter = chatroomRecyclerViewAdapter()
        view.group_chat_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class chatroomRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var chatList : ArrayList<ChatRoomModel> = arrayListOf()
        var uidList : ArrayList<String>? = null
        init{
            db?.collection("users")
                ?.document(user!!)
                ?.collection("groupchats")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    chatList.clear()
                    if (firebaseFirestoreException != null) return@addSnapshotListener
                    if (querySnapshot != null) {
                        for (dc in querySnapshot.documents) {
                            val chatid = dc.id
                            uidList = dc["uids"] as ArrayList<String>
                            var lastchat : String = ""
                            var lastchattime : Long = -1
                            var profilePicPath : String = ""
                            var roomName : String = ""

                            db!!.collection("groupchat")
                                .whereEqualTo("chatid",chatid)
                                .addSnapshotListener{snapshot, exception ->
                                    if(exception != null) return@addSnapshotListener
                                    if(snapshot != null){
                                        snapshot.forEach{
                                            lastchat = it["lastchat"].toString()
                                            lastchattime = it["lastchattime"] as Long
                                            profilePicPath = it["profilePicPath"].toString()
                                            roomName = it["roomname"].toString()

                                            val item = ChatRoomModel(
                                                profilePicPath = profilePicPath,
                                                chatRoomName = roomName,
                                                lastChat = lastchat,
                                                lastChatTime = lastchattime,
                                                user = user!!,
                                                chatid = chatid
                                            )
                                            chatList.add(item)
                                        }
                                        notifyDataSetChanged()
                                    }
                                }
                        }

                    }
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view : View?
            view = LayoutInflater.from(parent.context).inflate(R.layout.chatroom, parent, false)
            return chatRoomViewHolder(view)
        }

        override fun getItemCount(): Int = chatList.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = chatList[position]
            println(item)
            (holder as chatroomRecyclerViewAdapter.chatRoomViewHolder).chatRoomNameView.text = item.chatRoomName
            Glide.with(holder.profilePicView.context)
                .load(item.profilePicPath)
                .into(holder.profilePicView)

            holder.lastChatView.text = item.lastChat

            val format = SimpleDateFormat("HH시 mm분")
            val date : String = format.format( Date(item.lastChatTime!!))
            holder.lastChatTime.text = date

            holder.itemView.setOnClickListener{
                val intent = Intent(activity,ChatActivity::class.java)
                intent.putStringArrayListExtra("uidList",uidList)
                intent.putExtra("roomName",item.chatRoomName)
                intent.putExtra("chatid",item.chatid)
                intent.putExtra("mode","privatechat")
                startActivity(intent)
            }
        }

        inner class chatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val chatRoomNameView : TextView = itemView.findViewById(R.id.item_chatroom_chatRoomName)
            val profilePicView : ImageView = itemView.findViewById(R.id.item_chatroom_profilePic)
            val lastChatView : TextView = itemView.findViewById(R.id.item_chatroom_lastChat)
            val lastChatTime : TextView = itemView.findViewById(R.id.item_chatroom_time)
        }
    }
}