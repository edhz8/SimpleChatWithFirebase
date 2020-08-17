package com.example.talk

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter

class FriendAdapter(context: Context, realmResult: OrderedRealmCollection<FriendModel>) : RealmBaseAdapter<FriendModel>(realmResult) {

    inner class ViewHolder {
        var profilePicView: ImageView? = null
        var nameView: TextView? = null
        var statusmessageView : TextView? = null
    }

    companion object {
        const val VIEWTYPE_FRIEND_WITH_SM = 0
        const val VIEWTYPE_FRIEND_WITHOUT_SM = 1
        const val VIEWTYPE_MY_PROFILE_WITH_SM = 2
        const val VIEWTYPE_MY_PROFILE_WITHOUT_SM = 3
        const val VIEWTYPE_LINE = 4
        const val VIEWTYPE_COUNT = 5
    }

    val user : String by lazy{
        FirebaseAuth.getInstance().currentUser?.uid.toString()
    }
    private val mInflater = LayoutInflater.from(context)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        val type = getItemViewType(position)
        lateinit var viewHolder: ViewHolder

        if (view == null) {
            when (type) {
                VIEWTYPE_FRIEND_WITH_SM -> {
                    viewHolder = ViewHolder()
                    view = mInflater.inflate(R.layout.friend_with_statusmessage, parent, false)
                    viewHolder.nameView = view.findViewById(R.id.item_withSM_name)
                    viewHolder.profilePicView = view.findViewById(R.id.item_withSM_profilePic)
                    viewHolder.statusmessageView = view.findViewById(R.id.item_withSM_statusmessage)
                    view.tag = viewHolder
                }

                VIEWTYPE_FRIEND_WITHOUT_SM -> {
                    viewHolder = ViewHolder()
                    view = mInflater.inflate(R.layout.friend_without_statusmessage, parent, false)
                    viewHolder.nameView = view.findViewById(R.id.item_withoutSM_name)
                    viewHolder.profilePicView = view.findViewById(R.id.item_withoutSM_profilePic)
                    view.tag = viewHolder
                }

                VIEWTYPE_MY_PROFILE_WITH_SM -> {
                    viewHolder = ViewHolder()
                    view = mInflater.inflate(R.layout.myprofile_with_statusmessage, parent, false)
                    viewHolder.nameView = view.findViewById(R.id.item_myprofile_withSM_name)
                    viewHolder.profilePicView =
                        view.findViewById(R.id.item_myprofile_withSM_profilePic)
                    viewHolder.statusmessageView =
                        view.findViewById(R.id.item_myprofile_withSM_statusmessage)
                    view.tag = viewHolder
                }

                VIEWTYPE_MY_PROFILE_WITHOUT_SM -> {
                    viewHolder = ViewHolder()
                    view = mInflater.inflate(R.layout.myprofile_without_statusmessage, parent, false)
                    viewHolder.nameView = view.findViewById(R.id.item_myprofile_withoutSM_name)
                    viewHolder.profilePicView =
                        view.findViewById(R.id.item_myprofile_withoutSM_profilePic)
                    view.tag = viewHolder
                }

                VIEWTYPE_LINE -> {
                    viewHolder = ViewHolder()
                    view = mInflater.inflate(R.layout.friend_line, parent, false)
                    view.tag = viewHolder
                }
            }
        } else {
            viewHolder = view.tag as ViewHolder
        }
        adapterData?.let {
            when (type) { //뷰홀더에 어떤값들을 넣어줄건지 지정하는부분
                VIEWTYPE_FRIEND_WITH_SM -> {
                    val item = it[position]
                    viewHolder.nameView!!.text = item.name
                    Glide.with(view!!)
                        .load(item.profilePicPath)
                        .into(viewHolder.profilePicView!!)
                    viewHolder.statusmessageView!!.text = item.statusMessage
                }

                VIEWTYPE_FRIEND_WITHOUT_SM -> {
                    val item = it[position]
                    viewHolder.nameView!!.text = item.name
                    Glide.with(view!!)
                        .load(item.profilePicPath)
                        .into(viewHolder.profilePicView!!)
                }

                VIEWTYPE_MY_PROFILE_WITH_SM -> {
                    val item = it[position]
                    viewHolder.nameView!!.text = item.name
                    Glide.with(view!!)
                        .load(item.profilePicPath)
                        .into(viewHolder.profilePicView!!)
                    viewHolder.statusmessageView!!.text = item.statusMessage
                }

                VIEWTYPE_MY_PROFILE_WITHOUT_SM -> {
                    val item = it[position]
                    viewHolder.nameView!!.text = item.name
                    Glide.with(view!!)
                        .load(item.profilePicPath)
                        .into(viewHolder.profilePicView!!)
                }
                else ->{ }
            }
        }
        return view!!
    }

    override fun getItem(position: Int): FriendModel? {
        adapterData?.let{
            return it[position]
        }
        return super.getItem(position)
    }

    override fun getItemId(position: Int): Long {
        adapterData?.let {
            return position.toLong()
        }
        return super.getItemId(position)
    }

    override fun getCount(): Int {
        adapterData?.let{
            return it.size
        }
        return super.getCount()
    }

    override fun getViewTypeCount(): Int {
        return VIEWTYPE_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        var ret = -1
        adapterData?.let {
            if (it[position].uid == "Line") ret = VIEWTYPE_LINE
            else if (it[position].statusMessage != "" && it[position].uid != user) ret =
                VIEWTYPE_FRIEND_WITH_SM
            else if (it[position].statusMessage == "" && it[position].uid != user) ret =
                VIEWTYPE_FRIEND_WITHOUT_SM
            else if (it[position].statusMessage != "" && it[position].uid == user) ret =
                VIEWTYPE_MY_PROFILE_WITH_SM
            else if (it[position].statusMessage == "" && it[position].uid == user) ret =
                VIEWTYPE_MY_PROFILE_WITHOUT_SM
            else { }
        }
        return ret
    }

}