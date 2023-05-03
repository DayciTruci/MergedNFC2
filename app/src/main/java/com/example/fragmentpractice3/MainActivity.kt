package com.example.fragmentpractice3

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_page_edit.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {

    private var nfcAdapter: NfcAdapter? = null
    private var isDelete: Boolean = false
    private var tempdata: ContentValues? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 편집 페이지로 이동
        mainPageEdit()
        
        // 메인페이지에서 유저정보 클릭시 이동
        mainPageUserInfo()

        // 삭제 버튼 활성화
        deleteBtnAct()

        // dataFlush
        dataFlush()

//        mAdapter = MainAdapter(supportFragmentManager)
//        mainViewPager.adapter = mAdapter
//        mainTabLayout.setupWithViewPager(mainViewPager)

        // NFC 어댑터 가져오기
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // NFC 기능이 꺼져있는 경우 Toast 메시지로 안내
        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "NFC 기능이 꺼져 있습니다. NFC를 켜주세요.", Toast.LENGTH_LONG).show()
        }
        dbHelper = DBHelper(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // NFC 태그를 읽었을 때 호출되는 메소드
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag != null) {

            val id = bytesToHex(tag.id)
            Log.v("READ", "READED ID = $id")

            val isInserted: Boolean = dbHelper!!.insertCheck(id)

            if (!isInserted) {
                insertID(id)

            } else {
                if (isDelete)
                    deleteId(id)

                else {
                    Log.v("INSERTED", "INSERTED")

                    val data = dbHelper!!.getData(id, null)
                    Log.v(
                        "DATA",
                        "id : ${data.id} || act : ${data.activity} || time : ${data.time} || status : ${data.status}"
                    )

                    if (data.status == 1) {
                        Toast.makeText(
                            this,
                            "${data.id}의 동작 ${data.activity}을/를 성공적으로 마쳤습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        data.status = 0
                        dbHelper!!.updateData(data)

                    } else
                        Toast.makeText(this, "${data.id}는 이미 동작을 끝냈습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Foreground Dispatch 설정
        if (nfcAdapter != null) {
            val intent = Intent(this, javaClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            // PendingIntent 생성
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

            // Foreground Dispatch 설정
            nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, null, null)
        }
    }

    override fun onPause() {
        super.onPause()

        // Foreground Dispatch 해제
        if (nfcAdapter != null) {
            nfcAdapter!!.disableForegroundDispatch(this)
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    private fun resetText() {
        val textView: TextView = findViewById(R.id.text1_1)
        textView.text = ""

        val datalist = dbHelper!!.getAllData()

        for (each in datalist) {
            textView.text = "${textView.text}${each.id} : ${each.activity}\n"
        }
    }
    @SuppressLint("ResourceType")
    private fun insertID(id: String) {
        val textView:TextView = findViewById(R.id.text1_1)

        var timePicker = TimePickerFragment()

        Log.v("INSERT", "ID : $id")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("값 입력")
        val input = EditText(this@MainActivity)
        builder.setView(input)
        builder.setPositiveButton("등록") { dialog, _ ->
            val textValue = input.text.toString()
            tempdata?.put("ID", id)
            tempdata?.put("activity", textValue)
            tempdata?.put("status", 0)

            timePicker.show(supportFragmentManager, "TIME PICKER")
            dialog.dismiss()

            textView.text = "${textView.text}$id : $textValue \n"
        }
        builder.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun deleteId(id: String) {
        try {
            val data = Data(id, null, null, 0)
            dbHelper!!.deleteData(data)
            resetText()
            Toast.makeText(this@MainActivity, "$id 제거에 성공했습니다.", Toast.LENGTH_SHORT).show()

        }catch (e: Exception){
            Toast.makeText(this@MainActivity, "$id 제거에 실패했습니다.\n$e", Toast.LENGTH_SHORT).show()
        }
    }

    private fun adjTime(hour:Int, min:Int): String{
        var adjhour = "$hour"
        var adjmin  = "$min"

        if (hour < 10)
            adjhour = "0$hour"

        if (min < 10)
            adjmin = "0$min"

        Log.v("TEST", "ADJTIME : $adjhour$adjmin")
        return "$adjhour$adjmin"
    }
    override fun onTimeSet(timePicker: TimePicker?, hourOfDay: Int, min: Int) {
        val c = Calendar.getInstance()

        Log.v("TIMESET", "HOUR : $hourOfDay || MIN : $min")

        //시간설정
        c.set(Calendar.HOUR_OF_DAY, hourOfDay) //시
        c.set(Calendar.MINUTE, min) // 분
        c.set(Calendar.SECOND,0)    //초

        // 알람설정
        startAlarm(c)

        // db 전달
        insertTime(adjTime(hourOfDay, min))
    }

    private fun startAlarm(c: Calendar) {
        Log.v("STARTALARM", "STARTALARM")

        // 알람매니저 선언
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)

        //사용자가 선택한 알람 시간 데이터 담기
        val curTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
        val id = System.currentTimeMillis().toInt()
        intent.putExtra("time", curTime)

        val pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_MUTABLE)

        //설정 시간이 현재시간 이후라면 설정
        if(c.before(Calendar.getInstance())){
            c.add(Calendar.DATE, 1)
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
    }

        private fun insertTime(time:String){
            val db: SQLiteDatabase = dbHelper!!.writableDatabase

            tempdata?.put("time", time)
            db.insert("activity", null, tempdata)
            db.close()

            dataFlush()
        }

        private fun dataFlush(){
            tempdata = ContentValues()
        }


        fun mainPageEdit(){
            mainPageEdit.setOnClickListener {
                // 다른 화면으로 이동하기
                // Intent(출발지, 도착지)
                val myIntent = Intent(this, MainPageEditActivity::class.java)
                startActivity(myIntent) // 출발지, 도착지 정보가담긴 myIntent를 넣어준다.
            }
        }

        // 메인페이지 사용자정보 클릭시
        private fun mainPageUserInfo(){
            mainPageUserInfo.setOnClickListener {
                // 다른 화면으로 이동하기
                // Intent(출발지, 도착지)
                val myIntent = Intent(this, MyInfoActivity::class.java)
                startActivity(myIntent) // 출발지, 도착지 정보가담긴 myIntent를 넣어준다.
            }
        }

        private fun deleteBtnAct(){
            deleteBtn.setOnClickListener {
                isDelete = !isDelete

                if (isDelete)
                    deleteBtn.setTextColor(Color.parseColor("#FF0000"))

                else
                    deleteBtn.setTextColor(Color.parseColor("#000000"))
            }
        }

    companion object{
        var dbHelper: DBHelper? = null
    }
}