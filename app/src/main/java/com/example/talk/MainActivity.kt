package com.example.talk


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.talk.myutils.makeChatId
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.lang.Thread.sleep
import kotlin.random.Random

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    val user = FirebaseAuth.getInstance().currentUser?.uid.toString()

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {  //하단 네비게이션바에서 선택되는것에 따라 프레그먼트를 변경하고, 툴바내용을 수정해준다.
        when (p0.itemId) {
            R.id.home -> {
                main_toolbar_text.text = "친구"
                main_toolbar_search.visibility = View.VISIBLE
                main_toolbar_search.isClickable = true
                main_toolbar_addfriend.visibility = View.VISIBLE
                main_toolbar_addfriend.isClickable = true
                Glide.with(this)
                    .load(R.drawable.addfriend_fill)
                    .into(main_toolbar_addfriend)
                var friendFragment = FriendFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, friendFragment)
                    .commit()
                return true
            }
            R.id.chat -> {
                main_toolbar_text.text = "개인 채팅"
                main_toolbar_search.visibility = View.VISIBLE
                main_toolbar_search.isClickable = true
                main_toolbar_addfriend.visibility = View.INVISIBLE
                main_toolbar_addfriend.isClickable = false
                var chatFragment = ChatFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, chatFragment)
                    .commit()
                return true
            }
            R.id.groupchat -> {
                main_toolbar_text.text = "그룹 채팅"
                main_toolbar_search.visibility = View.VISIBLE
                main_toolbar_search.isClickable = true
                main_toolbar_addfriend.visibility = View.VISIBLE
                main_toolbar_addfriend.isClickable = true
                Glide.with(this)
                    .load(R.drawable.groupchat_blank)
                    .into(main_toolbar_addfriend)
                var groupchatFragment = GroupChatFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, groupchatFragment)
                    .commit()
                return true
            }
            R.id.setting -> {
                main_toolbar_addfriend.visibility = View.INVISIBLE
                main_toolbar_addfriend.isClickable = false
                main_toolbar_search.visibility = View.INVISIBLE
                main_toolbar_search.isClickable = false
                var settingFragment = SettingFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, settingFragment).commit()
                return true

            }
        }
        return false
    }

    fun registerPushToken() {           //알림수신을 위해 필요한 푸시토큰을 업데이트 해준다.
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            val token = task.result?.token

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user)
                .update("pushtoken", token)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.selectedItemId = R.id.home

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )

        registerPushToken()

        main_toolbar_addfriend.setOnClickListener { //친구목록 프레그먼트에서는 친구추가 엑티비티로 넘어가고
            when(bottomNavigationView.selectedItemId){
                R.id.home -> {
                    intent = Intent(this,AddFriendActivity::class.java)
                    intent.putExtra("user",user)
                    startActivity(intent)
                }

                R.id.groupchat -> {         //그룹채팅 프레그먼트에서는 그룹채팅생성 엑티비티로 넘어간다.
                    startActivity(Intent(this,MakeGroupchatActivity::class.java))
                }
            }

        }

    }

    override fun onBackPressed() {
        ActivityCompat.finishAffinity(this)
        System.exit(0)
    }
}
