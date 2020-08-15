package com.example.talk

import android.app.Application
import io.realm.Realm

class RealmInit : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}