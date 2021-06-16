package gr.exm.agroxm.ui

import android.content.Context
import android.content.Intent
import gr.exm.agroxm.ui.main.MainActivity
import gr.exm.agroxm.ui.login.LoginActivity
import gr.exm.agroxm.ui.signup.SignupActivity
import gr.exm.agroxm.ui.splash.SplashActivity

class Navigator {

    fun showSplash(context: Context) {
        context.startActivity(
            Intent(
                context, SplashActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    fun showLogin(context: Context) {
        context.startActivity(
            Intent(
                context, LoginActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    fun showSignup(context: Context) {
        context.startActivity(
            Intent(
                context, SignupActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    fun showHome(context: Context) {
        context.startActivity(
            Intent(
                context, MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }
}