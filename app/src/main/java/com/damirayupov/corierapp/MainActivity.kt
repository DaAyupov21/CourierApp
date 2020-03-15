package com.damirayupov.corierapp

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth_button_register.setOnClickListener {
            if (usename_login.text.toString().isEmpty() or password_login.text.toString().isEmpty()){
                toast_message("Введите поля")
            }
            else{
                val loginTask = LoginTask()
                loginTask.execute(usename_login.text.toString(), password_login.text.toString())
                if (loginTask.get().contains("Authorization")){
                    toast_message("Успешная авторизация")
                    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    bluetoothAdapter.name = usename_login.text.toString()
                    val gson = Gson()
                    val user = gson.fromJson(loginTask.get(), User::class.java)
                    val reportChatActivity = Intent(this, ReportChatActivity::class.java)
                    reportChatActivity.putExtra(ReportChatActivity.USER_KEY, user)
                    startActivity(reportChatActivity)
                }
                else {
                    toast_message(loginTask.get())
                }


            }
        }
    }

    private fun toast_message(message: CharSequence){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
