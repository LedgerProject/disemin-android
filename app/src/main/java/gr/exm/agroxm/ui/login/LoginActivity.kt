package gr.exm.agroxm.ui.login

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.android.material.snackbar.Snackbar
import gr.exm.agroxm.R
import gr.exm.agroxm.data.Resource
import gr.exm.agroxm.data.Status
import gr.exm.agroxm.databinding.ActivityLoginBinding
import gr.exm.agroxm.ui.Navigator
import gr.exm.agroxm.util.Validator
import gr.exm.agroxm.util.onTextChanged
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class LoginActivity : AppCompatActivity(), KoinComponent {

    private val navigator: Navigator by inject()
    private val validator: Validator by inject()
    private val model: LoginViewModel by viewModels()
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
            navigator.showSignup(this)
            finish()
        }

        binding.login.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            if (validator.validateUsername(username)) {
                binding.usernameContainer.error = "Invalid username"
                return@setOnClickListener
            }

            if (validator.validatePassword(password)) {
                binding.passwordContainer.error = "Invalid password"
                return@setOnClickListener
            }

            binding.username.isEnabled = false
            binding.password.isEnabled = false
            binding.loading.visibility = View.VISIBLE

            model.login(username, password)
        }

        // Listen for login state change
        model.isLoggedIn().observe(this) { result ->
            onLoginResult(result)
        }
    }

    private fun onLoginResult(result: Resource<Unit>) {
        when (result.status) {
            Status.SUCCESS -> {
                Timber.d("Login success. Starting main app flow.")
                navigator.showHome(this)
                finish()
            }
            Status.ERROR -> {
                binding.username.isEnabled = true
                binding.password.isEnabled = true
                binding.loading.visibility = View.INVISIBLE
                showError("Could not login. ${result.message}.")
            }
            Status.LOADING -> {
                binding.username.isEnabled = false
                binding.password.isEnabled = false
                binding.loading.visibility = View.VISIBLE
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