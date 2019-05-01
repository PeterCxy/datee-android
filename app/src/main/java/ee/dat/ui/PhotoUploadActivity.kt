package ee.dat.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import ee.dat.DateeApplication
import ee.dat.R
import ee.dat.api.DateeApi
import ee.dat.util.*
import kotlinx.android.synthetic.main.activity_upload_photos.*
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Okio
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PhotoUploadActivity: WizardActivity() {
    companion object {
        const val MIN_PHOTOS = 3 // Synchronize with backend
        private const val REQUEST_CHOOSE = 1001
    }

    private var minReached = false
    private var currentPhotoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWizardContentView(R.layout.activity_upload_photos)
        upp_upload.setOnClickListener { addPhoto() }

        GlobalScope.async(Dispatchers.Main) {
            refreshUploadedCount()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!minReached) {
            photoNotEnough()
            return false
        }
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (minReached) super.onBackPressed()
    }

    override fun onFabClick() {
        super.onFabClick()
        onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHOOSE && resultCode == RESULT_OK) {
            // Photo acquired
            val imageUri = data?.data ?: Uri.fromFile(currentPhotoFile)

            GlobalScope.async(Dispatchers.Main) {
                doUploadPhoto(imageUri)
            }
        }

        return super.onActivityResult(requestCode, resultCode, data)
    }

    private fun photoNotEnough() {
        Toast.makeText(this, getString(R.string.upp_not_enough, MIN_PHOTOS), Toast.LENGTH_SHORT).show()
    }

    private fun addPhoto() {
        // Intent for Camera
        val intentCam = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT,
                FileProvider.getUriForFile(this@PhotoUploadActivity,
                    "ee.dat.fileprovider", createImageFile()!!))
        }
        // Intent for SAF
        val intentSAF = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(Intent.createChooser(intentSAF, getString(R.string.choose_photo)).apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intentCam))
        }, REQUEST_CHOOSE)
    }

    // From Android examples <https://developer.android.com/training/camera/photobasics>
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            ).apply {
                // Save a file: path for use with ACTION_VIEW intents
                currentPhotoFile = this
            }
        } catch (e: IOException) {
            null
        }
    }

    private suspend fun doUploadPhoto(file: Uri) = coroutineScope {
        if (!working) working = true
        withContext(Dispatchers.IO) {
            contentResolver.openFileDescriptor(file, "r")!!.let { fd ->
                object : RequestBody() {
                    override fun contentType(): MediaType? {
                        return contentResolver.getType(file).let {
                            // If contentResolver cannot get the type, it is "image/jpeg" (from Camera)
                            MediaType.parse(it ?: "image/jpeg")
                        }
                    }

                    override fun contentLength(): Long {
                        return fd.statSize
                    }

                    override fun writeTo(sink: BufferedSink) {
                        FileInputStream(fd.fileDescriptor).also {
                            sink.writeAll(Okio.buffer(Okio.source(it)))
                        }
                    }
                }.let { MultipartBody.Part.createFormData("photo", "upload-file", it) }
                    .let { DateeApi.api.uploadPhoto(it).executeMaybe() }
            }
        }.onErr { showErrorToast(it); working = false; return@coroutineScope  }
        refreshUploadedCount()
    }

    private suspend fun refreshUploadedCount() = coroutineScope {
        if (!working) working = true
        val photos = withContext(Dispatchers.IO) {
            DateeApi.api.listPhotos(DateeApplication.curUser!!.uid).executeMaybe()
        }.onErr { showErrorToast(it); working = false; return@coroutineScope  }
        upp_already_uploaded.text = getString(R.string.upp_already_uploaded, photos!!.size, MIN_PHOTOS)
        minReached = photos!!.size >= MIN_PHOTOS
        working = false
    }
}