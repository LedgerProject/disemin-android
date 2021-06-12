package gr.exm.agroxm.ui.signup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.android.material.snackbar.Snackbar
import gr.exm.agroxm.R
import gr.exm.agroxm.data.Resource
import gr.exm.agroxm.data.Role
import gr.exm.agroxm.data.Status
import gr.exm.agroxm.databinding.ActivitySignupBinding
import gr.exm.agroxm.ui.Navigator
import gr.exm.agroxm.ui.login.LoginActivity
import gr.exm.agroxm.util.Validator
import gr.exm.agroxm.util.onTextChanged
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import timber.log.Timber

class SignupActivity : AppCompatActivity(), KoinComponent {

    private val model: SignupViewModel by viewModels()
    private val navigator: Navigator by inject()
    private val validator: Validator by inject()
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
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            if (validator.validateUsername(username)) {
                binding.usernameContainer.error = "Invalid email"
                return@setOnClickListener
            }

            if (validator.validatePassword(password)) {
                binding.passwordContainer.error = "Invalid password"
                return@setOnClickListener
            }

            binding.firstName.isEnabled = false
            binding.lastName.isEnabled = false
            binding.username.isEnabled = false
            binding.password.isEnabled = false
            binding.loading.visibility = View.VISIBLE

            model.signup(
                username = username,
                password = password,
                role = Role.FARMER,
                firstName = binding.firstName.text.toString(),
                lastName = binding.lastName.text.toString()
            )
        }

        // Listen for login state change
        model.isSignedUp().observe(this) { result ->
            onSignupResult(result)
        }
    }

    private fun onSignupResult(result: Resource<Unit>) {
        when (result.status) {
            Status.SUCCESS -> {
                Timber.d("Signup success. Starting main app flow.")
                navigator.showHome(this)
                finish()
            }
            Status.ERROR -> {
                binding.firstName.isEnabled = true
                binding.lastName.isEnabled = true
                binding.username.isEnabled = true
                binding.password.isEnabled = true
                binding.loading.visibility = View.INVISIBLE
                showError("Could not signup. ${result.message}.")
            }
            Status.LOADING -> {
                binding.firstName.isEnabled = false
                binding.lastName.isEnabled = false
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