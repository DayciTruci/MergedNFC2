package com.example.fragmentpractice3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class NotificationHelper(base: Context?) : ContextWrapper(base) {
    private val channelID = "channelID"
    private val channelNm = "channelNm"

    init{
        // 안드로이드 버전이 오레오이상이면 채널생성
        if(Build.VERSION.SDK_INT >= VERSION_CODES.O){
            // 채널생성
            createChannel()
        }
    }

    //채널 생성
    @RequiresApi(VERSION_CODES.O)
    private fun createChannel(){
        var channel = NotificationChannel(channelID,channelNm,
        NotificationManager.IMPORTANCE_DEFAULT)

        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lightColor = Color.GREEN
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        getManager().createNotificationChannel(channel)
    }


    // NotificationManager생성
    fun getManager(): NotificationManager{
        return getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    
    // norification 설정
    // 탭에 종속된 알람이라 그 안에 별개로 들어가있는 NFC ID로 분리해서 시간을 등록해둬야 함
    // 이후, 해당 시간이 되었을 때 등록된 ID값을 토대로 db에서 status부분을 건드려야 함
    // ex) status == 1 : 동작 가능한 상태 || status == 0 : 정해진 시간에 대한 동작을 이미 끝내서 더이상 동작 불가능
    // status는 상시 0으로 대기하다가, 지정된 시간이 지났을 때 1로 set된 후 이후 ID를 입력해 동작했을 때 다시 0으로 set 됨.
    fun getChannelNotification(time: String?):NotificationCompat.Builder{
        var activity:String? = null

        val calendar = Calendar.getInstance()
        val time = SimpleDateFormat("HHmm").format(Date((calendar.timeInMillis)))

            Log.v("TEST", "time : $time")

            val data = MainActivity.dbHelper?.getData(null, time)
            activity = data?.activity

            Log.v("TEST", "DATA : $data")

        return NotificationCompat.Builder(applicationContext, channelID)
            //.setContentTitle(time)
            .setContentText(activity)
            .setSmallIcon(R.drawable.ic_launcher_background)
    }
}