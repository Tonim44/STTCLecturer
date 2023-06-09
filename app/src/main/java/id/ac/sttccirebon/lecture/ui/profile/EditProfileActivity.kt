package id.ac.sttccirebon.lecture.ui.profile

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import com.squareup.picasso.Picasso
import id.ac.sttccirebon.lecture.DashboardActivity
import id.ac.sttccirebon.lecture.databinding.ActivityEditprofileBinding
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.helper.HPI
import id.ac.sttccirebon.lecture.ui.helper.PrefHelper
import id.ac.sttccirebon.lecture.ui.uitel.LoadingDialog
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditprofileBinding
    private lateinit var data: DataManager
    lateinit var prefHelper: PrefHelper
    private val FILE_NAME = "photo.jpg"
    private val REQUEST_CODE = 42
    private lateinit var photoFile: File
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        data = DataManager(baseContext)
        val view = binding.root
        prefHelper = PrefHelper(this)
        setContentView(view)

        val loading = LoadingDialog(this)
        loading.startLoading()

        val PhotoProfil = binding.fotoProfile
        val FotoProfile = data.getString("photo_profile")

        if (FotoProfile.toString().isNotEmpty()) {
            Picasso.get().load(FotoProfile).into(PhotoProfil)
        }

        binding.back.setOnClickListener(View.OnClickListener { onBackPressed() })

        binding.gallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*" // ubah type menjadi "image/*" untuk memilih semua jenis gambar
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.plus.setOnClickListener {

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            val fileProvider =
                FileProvider.getUriForFile(this, "id.ac.sttccirebon.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            } else {
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }
        }

        binding.save.setOnClickListener {
            Toast.makeText(this@EditProfileActivity, "Tidak ada pergantian foto", Toast.LENGTH_LONG)
                .show()
        }

        loading.isDismiss()

    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Log.i("DATA_API", "Selected image URI: $selectedImageUri")
            try {
                val data = DataManager(baseContext)
                val imageView = binding.fotoProfile
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                val resizedBitmap = resizeBitmap(bitmap, 800)
                val file = File.createTempFile("compressed_image", ".jpg", getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES))
                val out = FileOutputStream(file)
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
                imageView.setImageBitmap(resizedBitmap)
                sendAbsen(file)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val data = DataManager(baseContext)
            val imageView = binding.fotoProfile
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            val resizedBitmap = resizeBitmap(bitmap, 800)
            val file = File.createTempFile("compressed_image", ".jpg", getExternalFilesDir(
                Environment.DIRECTORY_PICTURES))
            val out = FileOutputStream(file)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            imageView.setImageBitmap(resizedBitmap)
            sendAbsen(file)
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)

        }
    }

    fun resizeBitmap(bitmap : Bitmap, maxSize: Int) : Bitmap {

        var width : Int = bitmap.getWidth();
        var height : Int = bitmap.getHeight();
        var x : Int = 0;

        if (width >= height && width > maxSize) {
            x = width / height;
            width = maxSize;
            height = maxSize / x
        } else if (height >= width && height > maxSize) {
            x = height / width;
            height = maxSize;
            width = maxSize / x
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    fun sendAbsen(file: File) {

        //SimpanKehadiran
        binding.save.setOnClickListener {

            binding.save.isEnabled = false
            val loading = LoadingDialog(this)
            loading.startLoading()
            val token = data.getString("token")

            thread {
                try {
                    val client = OkHttpClient()
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("token", token.toString())
                        .addFormDataPart(
                            "foto_dosen",
                            file.name,
                            RequestBody.create("image/jpg".toMediaTypeOrNull(), file)
                        )   .build()

                    Log.i("DATA_API", token.toString())

                    val request = Request.Builder()
                        .url("${HPI.API_URL}/api/dosen/set-foto-profile-dosen")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonResponse = JSONObject(responseBody)
                        val results = jsonResponse.getJSONObject("result")
                        val message = results.getString("message")
                        runOnUiThread {
                            Toast.makeText(
                                this@EditProfileActivity,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                            moveIntent()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@EditProfileActivity,
                                "Terjadi kesalahan saat mengirim data ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Terjadi kesalahan saat mengirim data : ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.i("DATA_API", "${e.message}")
                    }
                } finally {
                    runOnUiThread {
                        loading.isDismiss()
                        binding.save.isEnabled = true
                    }
                }
            }
        }
    }

    private fun moveIntent(){
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}