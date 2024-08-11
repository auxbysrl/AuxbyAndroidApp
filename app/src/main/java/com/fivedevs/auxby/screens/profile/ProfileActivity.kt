package com.fivedevs.auxby.screens.profile

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityProfileBinding
import com.fivedevs.auxby.domain.utils.Constants.PERMISSION_CAMERA
import com.fivedevs.auxby.domain.utils.Constants.PERMISSION_STORAGE
import com.fivedevs.auxby.domain.utils.DateUtils
import com.fivedevs.auxby.domain.utils.FileUtils.getRealPathFromURI
import com.fivedevs.auxby.domain.utils.PermissionUtils.getMediaRequiredPermissions
import com.fivedevs.auxby.domain.utils.Utils
import com.fivedevs.auxby.domain.utils.extensions.getDrawableCompat
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.invisible
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.changePassword.ChangePasswordActivity
import com.fivedevs.auxby.screens.dialogs.GenericDialog
import com.fivedevs.auxby.screens.profile.userActions.UserActionsDialog
import com.permissionx.guolindev.PermissionX
import com.sangcomz.fishbun.permission.PermissionCheck
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel by viewModels<ProfileViewModel>()
    private var currentPhotoPath: String = ""
    private val permissionCheck: PermissionCheck by lazy { PermissionCheck(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initObservers()
        initListeners()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_STORAGE, PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        selectPhoto()
                    } else {
                        AlerterUtils.showErrorAlert(this, getString(R.string.permission_denied_avatar))
                    }
                }
            }
        }
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        binding.apply {
            viewModel = this@ProfileActivity.viewModel
            lifecycleOwner = this@ProfileActivity
        }
    }

    private fun initObservers() {
        viewModel.user.observe(this) { user ->
            loadProfileAvatar(user.avatar.orEmpty())
        }

        viewModel.uploadedAvatarImage.observe(this) { avatar ->
            hideAvatarProgress()
            if (avatar.isNotEmpty()) {
                loadProfileAvatar(avatar)
            } else {
                AlerterUtils.showErrorAlert(this, getString(R.string.error_avatar_uploading))
            }
        }

        viewModel.userUpdateEvent.observe(this) { isSuccess ->
            if (isSuccess) {
                AlerterUtils.showSuccessAlert(this, getString(R.string.profile_changes_saved))
            } else {
                AlerterUtils.showErrorAlert(this, getString(R.string.profile_changes_error))
            }
        }
    }

    private fun initListeners() {
        binding.flAvatarContainer.setOnClickListenerWithDelay {
            checkCameraPermission()
        }

        binding.inclToolbar.materialToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.inclToolbar.ivProfileMenu.setOnClickListener {
            val dialog = UserActionsDialog()
            dialog.show(supportFragmentManager, GenericDialog.LOGOUT_DIALOG_TAG)
        }

        binding.inclFullName.btnEditFullName.setOnClickListener {
            alphaEmailAddressField(DISABLED_ALPHA_VALUE)
            alphaPhoneNumberField(DISABLED_ALPHA_VALUE)
            alphaAddressField(DISABLED_ALPHA_VALUE)
            alphaPasswordField(DISABLED_ALPHA_VALUE)
            viewModel.showEditProfile.value?.let {
                viewModel.showEditProfile.value = !it
            }
        }

        binding.inclFullName.btnCancelFullName.setOnClickListener {
            cancelChanges()
            alphaEmailAddressField(ENABLED_ALPHA_VALUE)
            alphaPhoneNumberField(ENABLED_ALPHA_VALUE)
            alphaAddressField(ENABLED_ALPHA_VALUE)
            alphaPasswordField(ENABLED_ALPHA_VALUE)
            viewModel.showEditProfile.value?.let {
                viewModel.showEditProfile.value = !it
            }
        }

        binding.inclPhoneNumber.btnEditPhoneNumber.setOnClickListener {
            alphaEmailAddressField(DISABLED_ALPHA_VALUE)
            alphaFullNameField(DISABLED_ALPHA_VALUE)
            alphaAddressField(DISABLED_ALPHA_VALUE)
            alphaPasswordField(DISABLED_ALPHA_VALUE)
            viewModel.showEditPhoneNumber.value?.let {
                viewModel.showEditPhoneNumber.value = !it
            }
        }

        binding.inclPhoneNumber.btnCancelPhoneNumber.setOnClickListener {
            cancelChanges()
            alphaEmailAddressField(ENABLED_ALPHA_VALUE)
            alphaFullNameField(ENABLED_ALPHA_VALUE)
            alphaAddressField(ENABLED_ALPHA_VALUE)
            alphaPasswordField(ENABLED_ALPHA_VALUE)
            viewModel.showEditPhoneNumber.value = false
        }

        binding.inclAddress.btnEditAddress.setOnClickListener {
            alphaEmailAddressField(DISABLED_ALPHA_VALUE)
            alphaFullNameField(DISABLED_ALPHA_VALUE)
            alphaPhoneNumberField(DISABLED_ALPHA_VALUE)
            alphaPasswordField(DISABLED_ALPHA_VALUE)
            viewModel.showEditAddress.value?.let {
                viewModel.showEditAddress.value = !it
            }
        }

        binding.inclAddress.btnCancelEditAddress.setOnClickListener {
            cancelChanges()
            alphaEmailAddressField(ENABLED_ALPHA_VALUE)
            alphaFullNameField(ENABLED_ALPHA_VALUE)
            alphaPhoneNumberField(ENABLED_ALPHA_VALUE)
            alphaPasswordField(ENABLED_ALPHA_VALUE)
            viewModel.showEditAddress.value = false
        }

        binding.inclPassword.btnEditPassword.setOnClickListenerWithDelay {
            launchActivity<ChangePasswordActivity>()
        }

        binding.inclFullName.btnSaveFullName.setOnClickListenerWithDelay {
            viewModel.updateUser()
            alphaEmailAddressField(ENABLED_ALPHA_VALUE)
            alphaPhoneNumberField(ENABLED_ALPHA_VALUE)
            alphaAddressField(ENABLED_ALPHA_VALUE)
            alphaPasswordField(ENABLED_ALPHA_VALUE)
            viewModel.showEditProfile.value?.let {
                viewModel.showEditProfile.value = !it
            }
        }

        binding.inclPhoneNumber.btnSavePhoneNumber.setOnClickListenerWithDelay {
            viewModel.updateUser()
            alphaEmailAddressField(ENABLED_ALPHA_VALUE)
            alphaFullNameField(ENABLED_ALPHA_VALUE)
            alphaAddressField(ENABLED_ALPHA_VALUE)
            alphaPasswordField(ENABLED_ALPHA_VALUE)
            viewModel.showEditPhoneNumber.value?.let {
                viewModel.showEditPhoneNumber.value = !it
            }
        }

        binding.inclAddress.btnSaveAddress.setOnClickListenerWithDelay {
            viewModel.updateUser()
            alphaEmailAddressField(ENABLED_ALPHA_VALUE)
            alphaFullNameField(ENABLED_ALPHA_VALUE)
            alphaPhoneNumberField(ENABLED_ALPHA_VALUE)
            alphaPasswordField(ENABLED_ALPHA_VALUE)
            viewModel.showEditAddress.value?.let {
                viewModel.showEditAddress.value = !it
            }
        }

        binding.inclReferral.btnShare.setOnClickListenerWithDelay {
            handleReferralLink()
        }
    }

    private fun handleReferralLink() {
        viewModel.getReferralLink { link ->
            if (link.isNotEmpty()) {
                Utils.shareLink(this, link)
            } else {
                AlerterUtils.showErrorAlert(this, resources.getString(R.string.something_went_wrong))
            }
        }
    }

    private fun cancelChanges() {
        viewModel.cancelUserDetailsChanges()
    }

    private fun alphaPasswordField(alpha: Float) {
        binding.apply {
            inclPassword.tvPassword.alpha = alpha
            inclPassword.btnEditPassword.alpha = alpha
            inclPassword.btnEditPassword.isEnabled = alpha == ENABLED_ALPHA_VALUE
        }
    }

    private fun alphaAddressField(alpha: Float) {
        binding.apply {
            inclAddress.tvAddress.alpha = alpha
            inclAddress.tvAddressValue.alpha = alpha
            inclAddress.btnEditAddress.alpha = alpha
            inclAddress.btnEditAddress.isEnabled = alpha == ENABLED_ALPHA_VALUE
        }
    }

    private fun alphaPhoneNumberField(alpha: Float) {
        binding.apply {
            inclPhoneNumber.tvPhoneNumber.alpha = alpha
            inclPhoneNumber.tvPhoneNumberValue.alpha = alpha
            inclPhoneNumber.btnEditPhoneNumber.alpha = alpha
            inclPhoneNumber.btnEditPhoneNumber.isEnabled = alpha == ENABLED_ALPHA_VALUE
        }
    }

    private fun alphaFullNameField(alpha: Float) {
        binding.apply {
            inclFullName.tvFullName.alpha = alpha
            inclFullName.tvFullNameValue.alpha = alpha
            inclFullName.btnEditFullName.alpha = alpha
            inclFullName.btnEditFullName.isEnabled = alpha == ENABLED_ALPHA_VALUE
        }
    }

    private fun alphaEmailAddressField(alpha: Float) {
        binding.apply {
            inclEmail.tvEmail.alpha = alpha
            inclEmail.tvEmailValue.alpha = alpha
        }
    }

    private fun loadProfileAvatar(avatar: String) {
        Glide.with(this)
            .load(avatar)
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.pbAvatarProgress.hide()
                    return false
                }

                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    binding.pbAvatarProgress.hide()
                    return false
                }
            }).diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(getDrawableCompat(R.drawable.ic_profile_placeholder))
            .skipMemoryCache(true)
            .circleCrop()
            .into(binding.ivAvatar)
    }

    private fun checkForPermissions() {
        //if (hasStoragePermission(this) && checkCameraPermission()) {
        selectPhoto()
        //}
    }

    private fun checkStoragePermission(): Boolean {
        return permissionCheck.checkStoragePermission(PERMISSION_STORAGE)
    }

    private fun checkCameraPermission() {
        PermissionX.init(this)
            .permissions(getMediaRequiredPermissions())
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    selectPhoto()
                } else {
                    AlerterUtils.showErrorAlert(this, getString(R.string.permission_denied_avatar))
                }
            }
    }

    private fun selectPhoto() {
        val cameraIntent = dispatchTakePictureIntent()
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val pickTitle = getString(R.string.select_or_take_picture)
        val chooserIntent = Intent.createChooser(galleryIntent, pickTitle)
        //chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        resultLauncher.launch(chooserIntent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val attachedUri = result.data?.data ?: run { Uri.parse(currentPhotoPath) }
            handleImageUri(attachedUri)
        } else {
            finish()
        }
    }

    private fun handleImageUri(attachedUri: Uri) {
        if (attachedUri.toString().isNotEmpty()) {
            showAvatarProgress()
            //Glide.with(this).load(attachedUri).circleCrop().error(R.drawable.ic_profile_placeholder).into(binding.ivAvatar)

            val file = File(getRealPathFromURI(this@ProfileActivity, attachedUri))

            val compressedFile = compressImage(file)
            compressedFile?.let { file ->
                val requestBody: RequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val fileToUpload: MultipartBody.Part = MultipartBody.Part.createFormData("file", file.name, requestBody)
                viewModel.uploadUserAvatar(fileToUpload)
            }
        }
    }

    private fun compressImage(inputFile: File): File? {
        try {
            val options = BitmapFactory.Options()
            options.inSampleSize = 2 // You can adjust the sample size as needed
            val bitmap = BitmapFactory.decodeFile(inputFile.path, options)

            // Retrieve the original orientation from EXIF data
            val exif = ExifInterface(inputFile.absolutePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            // Apply the orientation to the bitmap
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            val outputStream = ByteArrayOutputStream()
            // Use compression format and quality settings
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val compressedBytes = outputStream.toByteArray()

            val compressedFile = File.createTempFile("compressed_", ".jpg", cacheDir)
            val fileOutputStream = FileOutputStream(compressedFile)
            fileOutputStream.write(compressedBytes)
            fileOutputStream.flush()
            fileOutputStream.close()

            return compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun dispatchTakePictureIntent(): Intent {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.fivedevs.auxby.provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    return takePictureIntent
                }
            }
        }

        return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat(DateUtils.FORMAT_yyyyMMdd_HH, Locale.getDefault()).format(Date())
        val storageDir: File? = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun showAvatarProgress() {
        binding.ivAvatar.invisible()
        binding.icEditAvatar.invisible()
        binding.pbAvatarProgress.show()
    }

    private fun hideAvatarProgress() {
        binding.ivAvatar.show()
        binding.icEditAvatar.show()
        binding.pbAvatarProgress.hide()
    }


    companion object {
        const val ENABLED_ALPHA_VALUE = 1f
        const val DISABLED_ALPHA_VALUE = 0.4f
    }
}