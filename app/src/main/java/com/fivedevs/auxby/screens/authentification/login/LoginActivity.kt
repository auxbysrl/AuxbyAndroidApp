package com.fivedevs.auxby.screens.authentification.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityLoginBinding
import com.fivedevs.auxby.domain.utils.Constants.RESPONSE_CODE_400
import com.fivedevs.auxby.domain.utils.Utils
import com.fivedevs.auxby.domain.utils.Validator.validatePassword
import com.fivedevs.auxby.domain.utils.buttonAnimator.TransitionButton
import com.fivedevs.auxby.domain.utils.extensions.*
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.screens.authentification.base.CheckEmailFragment
import com.fivedevs.auxby.screens.authentification.register.RegisterActivity
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.DashboardActivity
import com.fivedevs.auxby.screens.forgotPassword.ForgotPassActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.tapadoo.alerter.Alerter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initListeners()
        initObservers()
        initInputFieldListeners()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.apply {
            viewModel = this@LoginActivity.viewModel
            lifecycleOwner = this@LoginActivity
        }
    }

    private fun initObservers() {
        viewModel.loginClickEvent.observe(this) {
            binding.clLoginContainer.hideKeyboard()
            binding.btnLogin.startAnimation()
            Alerter.hide()
            viewModel.loginUser()
        }

        viewModel.loginEvent.observe(this) { loginEventMsg ->
            handleLogin(loginEventMsg)
        }

        viewModel.forgotPasswordClickEvent.observe(this) {
            launchActivity<ForgotPassActivity>()
        }

        viewModel.loginGoogleClickEvent.observe(this) {
            onGoogleAuthClicked()
        }

        viewModel.signUpClickEvent.observe(this) {
            launchActivity<RegisterActivity>()
        }

        viewModel.checkEmailEvent.observe(this) {
            showCheckEmailFragment()
        }
    }

    private fun handleLogin(loginEventMsg: String?) {
        if (loginEventMsg.isNullOrEmpty()) {
            binding.btnLogin.stopAnimation(
                TransitionButton.StopAnimationStyle.EXPAND,
                object : TransitionButton.OnAnimationStopEndListener {
                    override fun onAnimationStopEnd() {
                        val intent = Intent(baseContext, DashboardActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intent)
                        finishAffinity()
                    }
                })
        } else {
            showErrorMessage(loginEventMsg)
        }
    }

    private fun showErrorMessage(errorMessage: String) {
        val error =
            if (errorMessage == RESPONSE_CODE_400.toString()) resources.getString(R.string.wrong_login_credentials) else resources.getString(
                R.string.something_went_wrong
            )
        AlerterUtils.showErrorAlert(this, error)
        binding.btnLogin.stopAnimation(TransitionButton.StopAnimationStyle.SHAKE)
    }

    private fun onGoogleAuthClicked() {
        if (binding.checkBoxTermsConditions.isChecked) {
            signIn()
        } else {
            binding.checkBoxTermsConditions.buttonDrawable?.setTint(getColor(R.color.red))
            binding.llAgreeTermsConditions.makeMeShake()
        }
    }

    private fun initListeners() {
        binding.ivClose.setOnClickListenerWithDelay {
            redirectToDashboard()
        }

        binding.checkBoxTermsConditions.setOnCheckedChangeListener { _, checked ->
            handleTermsAndCondCheckBox(checked)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                redirectToDashboard()
            }
        })

        binding.tvTermsAndConditions.setOnClickListenerWithDelay {
            Utils.redirectToBrowserLink(this, resources.getString(R.string.link_terms_conditions))
        }
    }

    private fun redirectToDashboard() {
        finishAffinity()
        launchActivity<DashboardActivity>()
    }

    private fun handleTermsAndCondCheckBox(checked: Boolean) {
        val color = if (checked) {
            R.color.colorPrimary
        } else {
            R.color.red
        }

        binding.checkBoxTermsConditions.buttonDrawable?.setTint(getColor(color))
    }

    private fun initInputFieldListeners() {
        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            if (text.toString().isEmpty()) {
                binding.tilEmail.error = getString(R.string.cannot_be_empty)
                viewModel.enableLogin.value = false
                binding.btnLogin.isEnabled = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches()) {
                binding.tilEmail.error = getString(R.string.invalid_email_address)
                viewModel.enableLogin.value = false
                binding.btnLogin.isEnabled = false
            } else {
                binding.tilEmail.error = null
                viewModel.enableLogin.value = validatePassword(binding.etPassword.text.toString())
                binding.btnLogin.isEnabled = validatePassword(binding.etPassword.text.toString())
            }
        }

        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            if (text.toString().isEmpty()) {
                binding.tilPassword.error = getString(R.string.invalid_password)
                binding.tilPassword.errorIconDrawable = null
                viewModel.enableLogin.value = false
                binding.btnLogin.isEnabled = false
            } else if (!validatePassword(text.toString())) {
                binding.tilPassword.error = getString(R.string.invalid_password)
                binding.tilPassword.errorIconDrawable = null
                viewModel.enableLogin.value = false
                binding.btnLogin.isEnabled = false
            } else {
                binding.tilPassword.error = null
                viewModel.enableLogin.value =
                    Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()
                binding.btnLogin.isEnabled =
                    Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()
            }
        }
    }

    private fun signIn() {
        val signInIntent: Intent = viewModel.googleAuthClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account.idToken?.let {
                binding.btnLogin.startAnimation()
                Alerter.hide()
                viewModel.googleAuth(it)
            } ?: run {
                Timber.e("GoogleAuth: idToken is null")
                viewModel.loginEvent.value = getString(R.string.google_auth_error)
            }
        } catch (e: ApiException) {
            Timber.e("GoogleAuth: ${e.message}")
            viewModel.loginEvent.value = "GoogleAuth: ${e.message}"
        }
    }

    private fun showCheckEmailFragment() {
        replace(CheckEmailFragment(viewModel), binding.fragmentContainerView.id)
    }
}