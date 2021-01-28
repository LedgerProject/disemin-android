package gr.exm.agroxm.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.android.material.snackbar.Snackbar
import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.R
import gr.exm.agroxm.data.Credentials
import gr.exm.agroxm.data.io.ApiService
import gr.exm.agroxm.databinding.ActivityLoginBinding
import gr.exm.agroxm.data.AuthHelper
import gr.exm.agroxm.util.onTextChanged
import kotlinx.coroutines.*
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.username.onTextChanged {
            binding.usernameContainer.error = null
            binding.login.isEnabled =
                !binding.username.text.isNullOrEmpty() && !binding.password.text.isNullOrEmpty()
        }

        binding.password.onTextChanged {
            binding.passwordContainer.error = null
            binding.login.isEnabled =
                !binding.username.text.isNullOrEmpty() && !binding.password.text.isNullOrEmpty()
        }

        binding.signupPrompt.text = HtmlCompat.fromHtml(
            getString(R.string.prompt_signup),
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        binding.signupPrompt.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SignupActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }

        binding.login.setOnClickListener {
            if (binding.username.text.isNullOrEmpty() || binding.username.text?.contains("@") == false) {
                binding.usernameContainer.error = "Invalid username"
                return@setOnClickListener
            }

            if (binding.password.text.isNullOrEmpty() || binding.password.text.toString().length < 6) {
                binding.passwordContainer.error = "Invalid password"
                return@setOnClickListener
            }

            binding.username.isEnabled = false
            binding.password.isEnabled = false
            binding.loading.visibility = View.VISIBLE

            val credentials = Credentials(
                username = binding.username.text.toString(),
                password = binding.password.text.toString()
            )

            login(credentials)
        }
    }

    private fun login(credentials: Credentials) {
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = ApiService.get().login(credentials)) {
                is NetworkResponse.Success -> {
                    Timber.d("Login success")

                    // Save credentials
                    AuthHelper.setCredentials(credentials)

                    Timber.d("Starting main app flow.")
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
                is NetworkResponse.ServerError -> {
                    val error = response.body
                    Timber.d("Server Error ${error?.status}. ${error?.error}. ${error?.message}.")
                    showError("Server Error ${error?.status}. ${error?.error}. ${error?.message}.")
                }
                is NetworkResponse.NetworkError -> {
                    Timber.d(response.error, "Network Error")
                    showError("Network Error. Please try again.")
                }
            }
            withContext(Dispatchers.Main) {
                Timber.d("Updating the UI")
                binding.username.isEnabled = true
                binding.password.isEnabled = true
                binding.loading.visibility = View.INVISIBLE
            }
        }
    }

    private fun showError(message: String) {
        if (snackbar?.isShown == true) {
            snackbar?.dismiss()
        }
        snackbar = Snackbar.make(findViewById(R.id.root), message, Snackbar.LENGTH_LONG)
        snackbar?.show()
    }
}