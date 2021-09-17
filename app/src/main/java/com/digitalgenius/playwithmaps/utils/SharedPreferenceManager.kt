package com.digitalgenius.playwithmaps.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceManager private constructor() {



    companion object{

        private val MyPREFERENCES = "MyPrefs"
        private  var sharedPreference: SharedPreferences?=null
        private var sharedPreferenceManager:SharedPreferenceManager?=null

        fun getInstance(context: Context?):SharedPreferenceManager{
            return if(sharedPreferenceManager==null){
                sharedPreference=context!!.getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE)
                sharedPreferenceManager= SharedPreferenceManager()
                return sharedPreferenceManager!!
            } else{
                sharedPreferenceManager!!
            }
        }
    }

    fun putString(key:String,value:String){
        val editor=sharedPreference?.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    fun getString(key:String): String? {
        return sharedPreference?.getString(key,"default-value")
    }

    fun putInt(key:String,value:Int){
        val editor=sharedPreference?.edit()
        editor?.putInt(key, value)
        editor?.apply()
    }


    fun putFloat(key:String,value:Float){
        val editor=sharedPreference?.edit()
        editor?.putFloat(key, value)
        editor?.apply()
    }

    fun putBoolean(key: String,value: Boolean){
        val editor=sharedPreference?.edit()
        editor?.putBoolean(key, value)
        editor?.apply()
    }

}