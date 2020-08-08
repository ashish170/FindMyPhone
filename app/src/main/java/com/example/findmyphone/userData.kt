package com.example.findmyphone

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class userData {
    var sharedRef:SharedPreferences?=null
    var context: Context?=null
    constructor(context: Context){
        this.context=context
        this.sharedRef=context.getSharedPreferences("userData",Context.MODE_PRIVATE)

    }
    fun savePhoneNum(phoneNumber:String)
    {
        val editor=sharedRef!!.edit()
        editor.putString("phoneNumber",phoneNumber)
        editor.commit()
    }
    fun loadFirst(): String?
    {
        val phoneNumber: String? =sharedRef!!.getString("phoneNumber","empty")
        if(phoneNumber.equals("empty"))
        {
            val intent=Intent(context,Login::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(intent)
        }
        return phoneNumber
    }
    fun loadNumber(): String?
    {
        val phoneNumber: String? =sharedRef!!.getString("phoneNumber","empty")
        return phoneNumber
    }

    fun saveContact()
    {
        var listOfTrackers=""
        for((key,value) in myTracker)
        {
            if (listOfTrackers.length==0) {
                listOfTrackers = key + "%" + value
            }
            else
            {
                listOfTrackers += "%" + key + "%" + value
            }
        }
        if (listOfTrackers.length==0)
        {
            listOfTrackers="empty"
        }
        val editor=sharedRef!!.edit()
        editor.putString("TrackerList",listOfTrackers)
        editor.commit()

    }
    fun loadContact()
    {
        myTracker.clear()
        val listOfTrackers=sharedRef!!.getString("TrackerList","empty")
        if (!listOfTrackers.equals("empty"))
        {
            val users=listOfTrackers!!.split("%").toTypedArray()
            var i=0
            while (i<users.size)
            {
                myTracker.put(users[i],users[i+1])
                i += 2
            }

        }
    }

    companion object{
        var myTracker: MutableMap<String,String> = HashMap()
        fun formatnum(num:String):String
        {
            var onlynum=num.replace("[^0-9]".toRegex(),"")
           return onlynum
        }
    }
}