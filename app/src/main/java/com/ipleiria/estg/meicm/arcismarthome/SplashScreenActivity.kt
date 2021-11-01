package com.ipleiria.estg.meicm.arcismarthome

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_splash.*

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val nightModeFlags = applicationContext.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        val imageView: ImageView = findViewById(R.id.logo)
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_NO -> imageView.setImageResource(R.drawable.logo_light_mode)
            Configuration.UI_MODE_NIGHT_YES -> imageView.setImageResource(R.drawable.logo_dark_mode)
            Configuration.UI_MODE_NIGHT_UNDEFINED -> imageView.setImageResource(R.drawable.logo_light_mode)
        }

        constraint_screen.alpha = 0f
        constraint_screen.animate().setDuration(4500).alpha(1f).withEndAction {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}