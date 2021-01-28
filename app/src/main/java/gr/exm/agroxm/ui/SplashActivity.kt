package gr.exm.agroxm.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import gr.exm.agroxm.data.AuthHelper
import timber.log.Timber

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AuthHelper.isLoggedIn()) {
            Timber.d("Already logged in as ${AuthHelper.getUsername()}")
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            Timber.d("Not logged in.")
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
