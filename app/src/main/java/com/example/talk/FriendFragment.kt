package com.example.talk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_friend.*

class FriendFragment : Fragment() {

    val db = FirebaseFirestore.getInstance()
    val user: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid.toString()
    }
    lateinit var recyclerView: RecyclerView


    private lateinit var FriendAdapter: FriendAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var rootView = inflater.inflate(R.layout.fragment_friend,container,false)
        FriendAdapter = FriendAdapter(requireContext())


/*        friend_recyclerview.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position) as FriendModel
            val intent = Intent(getActivity(),showProfileActivity::class.java)
            intent.putExtra("name",item.name)
            intent.putExtra("profilePicPath",item.profilePicPath)
            intent.putExtra("statusMessage",item.statusMessage)
            intent.putExtra("uid",item.uid)
            intent.putExtra("user",user)
            intent.putExtra("phoneNum",item.phoneNum)
            startActivity(intent)
        }*/

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
                                if (exception != null) return@addSnapshotListener
                                else {
                                    println("exception null")
                                }
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
                                        if (statusMessage != "" && uid != user) type =
                                            FriendModel.VIEWTYPE_FRIEND_WITH_SM
                                        else if (statusMessage == "" && uid != user) type =
                                            FriendModel.VIEWTYPE_FRIEND_WITHOUT_SM
                                        else if (statusMessage != "" && uid == user) type =
                                            FriendModel.VIEWTYPE_MY_PROFILE_WITH_SM
                                        else if (statusMessage == "" && uid == user) type =
                                            FriendModel.VIEWTYPE_MY_PROFILE_WITHOUT_SM

                                        val item = FriendModel(
                                            type = type,
                                            name = name,
                                            statusMessage = statusMessage,
                                            profilePicPath = profilePicPath,
                                            uid = uid
                                        )

                                        println("추가되는아이템" + item.name)

                                        FriendAdapter.addItem(item)
                                    }
                                } else {
                                    println("snapshot null")
                                }
                            }
                    }
                }

            }
        recyclerView = rootView.findViewById(R.id.friend_recyclerview) as RecyclerView
        recyclerView.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = FriendAdapter
        }
        return rootView
    }

}

