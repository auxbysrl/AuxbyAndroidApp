package com.fivedevs.auxby.domain.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fivedevs.auxby.domain.utils.PermissionUtils.hasStoragePermission
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    fun getRealPathFromURI(context: Context, uri: Uri): String {
        return context.contentResolver.query(uri, null, null, null, null)?.let { cursor ->
            val indexedName = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.moveToFirst()
            val filename = cursor.getString(indexedName)
            cursor.close()
            filename.orEmpty()
        } ?: ""
    }

    fun getImageUri(context: Context, bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): Uri {
        if (!hasStoragePermission(context)) {
            // Permission is not granted, request it
            return Uri.EMPTY
        }

        val filename = "${System.currentTimeMillis()}.jpg"
        val contentValues = getContentValues(filename)
        val contentResolver = context.contentResolver

        contentResolver.also { resolver ->
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            val fos = imageUri?.let { resolver.openOutputStream(it) }


            fos?.use { bitmap.compress(format, 70, it) }

            contentValues.clear()
            imageUri?.let { resolver.update(it, contentValues, null, null) }

            return imageUri ?: Uri.EMPTY
        }
    }

    private fun getContentValues(filename: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
    }

    fun getImageUrlAndConvertToFile(context: Context, imageUrl: String): File? {
        val imageBitmap = Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .apply(RequestOptions().override(640)) // Adjust the size as needed
            .submit()
            .get()
        return saveBitmapToFile(context, imageBitmap)
    }

    // Function to save the Bitmap as a File
    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): File? {
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val directory =
            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "AuxbyMedia")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, fileName)

        return try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.WEBP, 70, fos)
            fos.flush()
            fos.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun compressImageFile(file: File, context: Context): File {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)

        val maxWidth = 1024 // Set your desired maximum width
        val maxHeight = 1024 // Set your desired maximum height
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
        val rotatedBitmap = rotateBitmapIfRequired(file.absolutePath, bitmap)

        val compressedFile = createTempImageFile(context)
        val outputStream = FileOutputStream(compressedFile)
        rotatedBitmap.compress(Bitmap.CompressFormat.WEBP, 70, outputStream) // Adjust quality as needed
        outputStream.close()

        return compressedFile
    }

    private fun rotateBitmapIfRequired(filePath: String, bitmap: Bitmap): Bitmap {
        val ei = ExifInterface(filePath)

        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun createTempImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
}