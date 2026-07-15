package com.harsh.healthmonitor

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PermissionsRationaleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions_rationale)

        findViewById<Button>(R.id.ok_button).setOnClickListener {
            finish()
        }
    }
}
