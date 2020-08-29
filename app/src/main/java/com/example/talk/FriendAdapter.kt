package com.example.talk

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.lang.RuntimeException

class FriendAdapter(val context: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var list = ArrayList<FriendModel>()

    private var totalTypes = list.size

    fun addItem(item : FriendModel){
        println("additem "+item.name)
        list.add(item)
        println(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view : View?

        return when (viewType) {
                FriendModel.VIEWTYPE_FRIEND_WITH_SM -> {
                    view = LayoutInflater.from(context).inflate(R.layout.friend_with_statusmessage, parent, false)
                    friendWithSMViewHolder(view)
                }

            FriendModel.VIEWTYPE_FRIEND_WITHOUT_SM -> {
                    view = LayoutInflater.from(context).inflate(R.layout.friend_without_statusmessage, parent, false)
                    friendWithoutSMViewHolder(view)
                }

            FriendModel.VIEWTYPE_MY_PROFILE_WITH_SM -> {
                    view = LayoutInflater.from(context).inflate(R.layout.myprofile_with_statusmessage, parent, false)
                    myprofileWithSMViewHolder(view)
                }

            FriendModel.VIEWTYPE_MY_PROFILE_WITHOUT_SM -> {
                    view = LayoutInflater.from(context).inflate(R.layout.myprofile_without_statusmessage, parent, false)
                    myprofileWithoutSMViewHolder(view)
                }

            FriendModel.VIEWTYPE_LINE -> {
                    view = LayoutInflater.from(context).inflate(R.layout.friend_line, parent, false)
                    lineViewHolder(view)
                }
                else -> throw RuntimeException("알 수 없는 뷰 타입 에러")
        }
    }

    override fun getItemCount(): Int {
        return totalTypes
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("MultiViewTypeAdapter", "Hi, onBindViewHolder")

        val item = list[position]
        when (item.type) { //뷰홀더에 어떤값들을 넣어줄건지 지정하는부분
            FriendModel.VIEWTYPE_FRIEND_WITH_SM -> {
                (holder as friendWithSMViewHolder).nameView!!.text = item.name
                Glide.with(context)
                    .load(item.profilePicPath)
                    .into(holder.profilePicView!!)
                holder.statusmessageView!!.text = item.statusMessage
            }

            FriendModel.VIEWTYPE_FRIEND_WITHOUT_SM -> {
                (holder as friendWithoutSMViewHolder).nameView!!.text = item.name
                Glide.with(context)
                    .load(item.profilePicPath)
                    .into(holder.profilePicView!!)
            }

            FriendModel.VIEWTYPE_MY_PROFILE_WITH_SM -> {
                (holder as myprofileWithSMViewHolder).nameView!!.text = item.name
                Glide.with(context)
                    .load(item.profilePicPath)
                    .into(holder.profilePicView!!)
                holder.statusmessageView!!.text = item.statusMessage
            }

            FriendModel.VIEWTYPE_MY_PROFILE_WITHOUT_SM -> {
                (holder as myprofileWithoutSMViewHolder).nameView!!.text = item.name
                Glide.with(context)
                    .load(item.profilePicPath)
                    .into(holder.profilePicView!!)
            }
            else -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        Log.d("MultiViewTypeAdapter", "Hi, getItemViewType")

        return list[position].type
    }

    inner class friendWithSMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView : TextView = itemView.findViewById(R.id.item_withSM_name)
        val profilePicView : ImageView = itemView.findViewById(R.id.item_withSM_profilePic)
        val statusmessageView : TextView = itemView.findViewById(R.id.item_withSM_statusmessage)
    }

    inner class friendWithoutSMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView : TextView = itemView.findViewById(R.id.item_withoutSM_name)
        val profilePicView : ImageView = itemView.findViewById(R.id.item_withoutSM_profilePic)
    }

    inner class myprofileWithSMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView : TextView = itemView.findViewById(R.id.item_myprofile_withSM_name)
        val profilePicView : ImageView = itemView.findViewById(R.id.item_myprofile_withSM_profilePic)
        val statusmessageView : TextView = itemView.findViewById(R.id.item_myprofile_withSM_statusmessage)
    }

    inner class myprofileWithoutSMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView : TextView = itemView.findViewById(R.id.item_myprofile_withoutSM_name)
        val profilePicView : ImageView = itemView.findViewById(R.id.item_myprofile_withoutSM_profilePic)
    }

    inner class lineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }
}