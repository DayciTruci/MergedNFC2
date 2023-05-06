package com.example.fragmentpractice3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fragmentpractice3.databinding.ActivitySingInAcvivityBinding

class SingInAcvivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingInAcvivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingInAcvivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}