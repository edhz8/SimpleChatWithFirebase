package com.example.talk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendFragment : Fragment() {

    val db = FirebaseFirestore.getInstance()
    val user : String by lazy{
        FirebaseAuth.getInstance().currentUser?.uid.toString()
    }
    lateinit var mContext : Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_friend,container,false)
        val adapter = FriendAdapter(if(mContext != null) mContext else context!!)

        db.collection("users")
            .document(user)
            .collection("friends")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) return@addSnapshotListener
                if (querySnapshot != null) {
                    for (dc in querySnapshot.documents) {
                        val frienduid = dc["uid"].toString()
                        val nickname = dc["nickname"].toString()
                        db.collection("users")
                            .whereEqualTo("uid", frienduid)
                            .addSnapshotListener { snapshot, exception ->
                                if(exception != null) return@addSnapshotListener
                                else{
                                    println("exception null")
                                }
                                if(snapshot != null){
                                    snapshot.forEach{
                                        val name = if(nickname != "") nickname else if (it["name"] != "") it["name"].toString() else "알수없는 사용자"
                                        val statusMessage = if (it["statusmessage"] != "") it["statusmessage"].toString() else ""
                                        val profilePicPath = if (it["profilePicPath"] != "") it["profilePicPath"].toString() else getString(R.string.default_profilePic_url)
                                        val uid = if (it["uid"] != null) it["uid"].toString() else ""
                                        val item  = FriendModel(name = name,statusMessage = statusMessage,profilePicPath = profilePicPath,uid = uid)
                                        adapter.addItem(item)
                                    }
                                }else{
                                    println("snapshot null")
                                }
                            }
                    }
                }

            }

        val connect_listView = view.findViewById<ListView>(R.id.listView)
        connect_listView.adapter = adapter

        connect_listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position) as FriendModel
            val intent = Intent(getActivity(),showProfileActivity::class.java)
            intent.putExtra("name",item.name)
            intent.putExtra("profilePicPath",item.profilePicPath)
            intent.putExtra("statusMessage",item.statusMessage)
            intent.putExtra("uid",item.uid)
            intent.putExtra("user",user)
            intent.putExtra("phoneNum",item.phoneNum)
            startActivity(intent)
        }



        return view
    }

    inner class FriendAdapter(context: Context) : BaseAdapter() {

        inner class ViewHolder {
            var profilePicView: ImageView? = null
            var nameView: TextView? = null
            var statusmessageView : TextView? = null
        }


        val VIEWTYPE_FRIEND_WITH_SM = 0
        val VIEWTYPE_FRIEND_WITHOUT_SM = 1
        val VIEWTYPE_MY_PROFILE_WITH_SM = 2
        val VIEWTYPE_MY_PROFILE_WITHOUT_SM = 3
        val VIEWTYPE_LINE = 4
        val VIEWTYPE_COUNT = 5


        private val user : String by lazy{
            FirebaseAuth.getInstance().currentUser?.uid.toString()
        }

        private val mInflater = LayoutInflater.from(context)
        var mItem = ArrayList<FriendModel>()

        fun addItem(item: FriendModel){
            mItem.add(item)

            for (i in mItem){
                println(i.name+" / "+i.uid+" / "+i.profilePicPath+" / "+i.statusMessage)
            }
        }

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
                        viewHolder.profilePicView = view.findViewById(R.id.item_myprofile_withSM_profilePic)
                        viewHolder.statusmessageView = view.findViewById(R.id.item_myprofile_withSM_statusmessage)
                        view.tag = viewHolder
                    }

                    VIEWTYPE_MY_PROFILE_WITHOUT_SM -> {
                        viewHolder = ViewHolder()
                        view = mInflater.inflate(R.layout.myprofile_without_statusmessage, parent, false)
                        viewHolder.nameView = view.findViewById(R.id.item_myprofile_withoutSM_name)
                        viewHolder.profilePicView = view.findViewById(R.id.item_myprofile_withoutSM_profilePic)
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

            val item = mItem[position]

            println("type : "+type)
            println("item: "+item)
            when (type) { //뷰홀더에 어떤값들을 넣어줄건지 지정하는부분
                VIEWTYPE_FRIEND_WITH_SM -> {
                    viewHolder.nameView!!.text = item.name
                    Glide.with(view!!)
                        .load(item.profilePicPath)
                        .into(viewHolder.profilePicView!!)
                    viewHolder.statusmessageView!!.text = item.statusMessage
                }

                VIEWTYPE_FRIEND_WITHOUT_SM -> {
                    viewHolder.nameView!!.text = item.name
                    Glide.with(view!!)
                        .load(item.profilePicPath)
                        .into(viewHolder.profilePicView!!)
                }

                VIEWTYPE_MY_PROFILE_WITH_SM -> {
                    viewHolder.nameView!!.text = item.name
                    Glide.with(view!!)
                        .load(item.profilePicPath)
                        .into(viewHolder.profilePicView!!)
                    viewHolder.statusmessageView!!.text = item.statusMessage
                }

                VIEWTYPE_MY_PROFILE_WITHOUT_SM -> {
                    viewHolder.nameView!!.text = item.name
                    Glide.with(view!!)
                        .load(item.profilePicPath)
                        .into(viewHolder.profilePicView!!)
                }
                else ->{ }
            }
            return view!!
        }

        override fun getItem(position: Int) = mItem[position]


        override fun getItemId(position: Int) = position.toLong()

        override fun getCount() = mItem.size

        override fun getViewTypeCount() = VIEWTYPE_COUNT

        override fun getItemViewType(position: Int): Int {
            var ret = -1
            if (mItem[position].statusMessage != "" && mItem[position].uid != user) ret =
                VIEWTYPE_FRIEND_WITH_SM
            else if (mItem[position].statusMessage == "" && mItem[position].uid != user) ret =
                VIEWTYPE_FRIEND_WITHOUT_SM
            else if (mItem[position].statusMessage != "" && mItem[position].uid == user) ret =
                VIEWTYPE_MY_PROFILE_WITH_SM
            else if (mItem[position].statusMessage == "" && mItem[position].uid == user) ret =
                VIEWTYPE_MY_PROFILE_WITHOUT_SM
            else { }

            println("!!!!!!!!!1getItemViewType"+ret)
            return ret
        }
    }
}

