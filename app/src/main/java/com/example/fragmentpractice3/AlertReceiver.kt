package com.example.fragmentpractice3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlertReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        var notificationHelper: NotificationHelper = NotificationHelper(context)

        //메인페이지 에딧 액티비티에서 넘어온 사용자가 설정한 시간데이터를 받는다.
        var time = intent?.extras?.getString("time")
        var nb: NotificationCompat.Builder = notificationHelper.getChannelNotification(time)

        // 알람 호출
        notificationHelper.getManager().notify(1, nb.build())
    }
}