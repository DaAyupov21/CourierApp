package com.damirayupov.corierapp

import android.os.AsyncTask
import khttp.post
import org.json.JSONObject

class ReportTask : AsyncTask<List<String>, Void, JSONObject>() {
    override fun doInBackground(vararg p0: List<String>?): JSONObject? {
        return p0[0]?.let { sendPostRequest(it) }
    }
    private fun sendPostRequest(list:List<String>):JSONObject {

        val response = post(url = "http://filmilk.herokuapp.com/customer", json = mapOf("usernames" to list))
        return response.jsonObject
    }
}
