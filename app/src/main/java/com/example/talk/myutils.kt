package com.example.talk

import kotlin.random.Random

object myutils {
     fun makeChatId() : String {
        var buffer : StringBuffer = StringBuffer()
        var random : Random = Random(System.currentTimeMillis())
        val chars = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z".split(",")
        for(i in 1..10){
            buffer.append(chars[random.nextInt(chars.size)])
        }
        return System.currentTimeMillis().toString()+"_"+buffer.toString()
    }
}