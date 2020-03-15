package com.damirayupov.corierapp

import java.io.Serializable

data class User(val Authorization:String, val client_data:Map<String, Any?>) : Serializable

