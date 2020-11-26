package com.example.talk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_setting.*
import java.lang.System.exit


class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

            //logout()

        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    fun logout(){
        var googleSignInClient : GoogleSignInClient? = null
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("42975930063-5n5rhahit49t2pd7lcp6kkr8up9rm8d1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity!!,gso)
        FirebaseAuth.getInstance().signOut()
        googleSignInClient?.signOut()
        exit(0)
    }
}