package com.damirayupov.corierapp

import android.os.AsyncTask
import khttp.post

class LoginTask : AsyncTask<String, Void, String>(){
    override fun doInBackground(vararg params: String?): String {
        return sendPostRequest(params[0].toString(), params[1].toString())
    }

    private fun sendPostRequest(userName:String, password:String):String {
        val response = post(url = "http://filmilk.herokuapp.com/login", json = mapOf("username" to userName, "password" to password, "is_customer" to false))
        return response.jsonObject.toString()
    }
}