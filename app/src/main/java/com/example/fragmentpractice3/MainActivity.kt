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
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.fragmentpractice3.adapters.MainAdapter
import com.example.fragmentpractice3.fragments.FirstFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_page_edit.*
import kotlinx.android.synthetic.main.fragment_first.*
import java.text.DateFormat
import java.util.*


class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {

    private lateinit var mAdapter : MainAdapter
    private var nfcAdapter: NfcAdapter? = null
    private var dbHelper: DBHelper? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 첫 메인화면에 관하여
        startMainActivity()

        mAdapter = MainAdapter(supportFragmentManager)
        mainViewPager.adapter = mAdapter
        mainTabLayout.setupWithViewPager(mainViewPager)

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
                if (FirstFragment.isDelete)
                    deleteId(id)

                else {
                    Log.v("INSERTED", "INSERTED")

                    val data = dbHelper!!.getData(id)
                    Log.v(
                        "DATA",
                        "id : ${data.id} || act : ${data.activity} || status : ${data.status}"
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
            val db: SQLiteDatabase = dbHelper!!.writableDatabase
            val values = ContentValues()
            values.put("ID", id)
            values.put("activity", textValue)
            values.put("status", 0)
            val newRowId = db.insert("activity", null, values)
            if (newRowId != -1L) {
                Toast.makeText(this@MainActivity, "등록되었습니다.", Toast.LENGTH_SHORT).show()
                timePicker.show(supportFragmentManager, "Time Picker")

            } else {
                Toast.makeText(this@MainActivity, "등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
            db.close()
            dialog.dismiss()

            textView.text = "${textView.text}$id : $textValue \n"
        }
        builder.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun deleteId(id: String) {
        try {
            val data = Data(id, null, 0)
            dbHelper!!.deleteData(data)
            resetText()
            Toast.makeText(this@MainActivity, "$id 제거에 성공했습니다.", Toast.LENGTH_SHORT).show()

        }catch (e: Exception){
            Toast.makeText(this@MainActivity, "$id 제거에 실패했습니다.\n$e", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTimeSet(timePicker: TimePicker?, hourOfDay: Int, min: Int) {
        var c = Calendar.getInstance()

        Log.v("TIMESET", "HOUR : $hourOfDay || MIN : $min")

        //시간설정
        c.set(Calendar.HOUR_OF_DAY, hourOfDay) //시
        c.set(Calendar.MINUTE, min) // 분
        c.set(Calendar.SECOND,0)    //초

        // 알람설정
        startAlarm(c)
    }

    private fun startAlarm(c: Calendar) {
        Log.v("STARTALARM", "STARTALARM")

        // 알람매니저 선언
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)

        //사용자가 선택한 알람 시간 데이터 담기
        val curTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
        intent.putExtra("time", curTime)


        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_MUTABLE)

        //설정 시간이 현재시간 이후라면 설정
        if(c.before(Calendar.getInstance())){
            c.add(Calendar.DATE, 1)
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
    }
    
    // 앱 시작시 첫 메인 화면 동작에 관하여
    fun startMainActivity(){
        val currentUser = Firebase.auth.currentUser

        // 만약 로그인이 안되있으면
        // 메인액티비티가 꺼지고 로그인 액티비티로 간다.
        if(currentUser == null){
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }
}