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
import gr.exm.agroxm.data.Role
import gr.exm.agroxm.data.io.ApiService
import gr.exm.agroxm.data.io.RegistrationBody
import gr.exm.agroxm.databinding.ActivitySignupBinding
import gr.exm.agroxm.data.AuthHelper
import gr.exm.agroxm.util.onTextChanged
import kotlinx.coroutines.*
import timber.log.Timber

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.username.onTextChanged {
            binding.usernameContainer.error = null
            binding.signup.isEnabled =
                !binding.username.text.isNullOrEmpty() && !binding.password.text.isNullOrEmpty()
        }

        binding.password.onTextChanged {
            binding.passwordContainer.error = null
            binding.signup.isEnabled =
                !binding.username.text.isNullOrEmpty() && !binding.password.text.isNullOrEmpty()
        }

        binding.loginPrompt.text = HtmlCompat.fromHtml(
            getString(R.string.prompt_login),
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        binding.loginPrompt.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }

        binding.signup.setOnClickListener {
            if (binding.username.text.isNullOrEmpty() || binding.username.text?.contains("@") == false) {
                binding.usernameContainer.error = "Invalid email"
                return@setOnClickListener
            }

            if (binding.password.text.isNullOrEmpty() || binding.password.text.toString().length < 6) {
                binding.passwordContainer.error = "Invalid password"
                return@setOnClickListener
            }

            binding.firstName.isEnabled = false
            binding.lastName.isEnabled = false
            binding.username.isEnabled = false
            binding.password.isEnabled = false
            binding.loading.visibility = View.VISIBLE

            signup(
                RegistrationBody(
                    username = binding.username.text.toString(),
                    password = binding.password.text.toString(),
                    role = Role.FARMER,
                    firstName = binding.firstName.text.toString(),
                    lastName = binding.lastName.text.toString()
                )
            )
        }
    }

    private fun signup(form: RegistrationBody) {
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = ApiService.get().register(form)) {
                is NetworkResponse.Success -> {
                    Timber.d("Signup success")

                    val auth = response.body
                    Timber.d("Got token ${auth.token}")

                    // Save credentials
                    AuthHelper.setCredentials(Credentials(form.username, form.password))

                    Timber.d("Starting main app flow.")
                    startActivity(Intent(this@SignupActivity, MainActivity::class.java))
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
                binding.firstName.isEnabled = true
                binding.lastName.isEnabled = true
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