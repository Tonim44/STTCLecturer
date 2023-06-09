package id.ac.sttccirebon.lecture.ui.sap

import android.R
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.ac.sttccirebon.lecture.DashboardActivity
import id.ac.sttccirebon.lecture.databinding.ActivityPengisiansapBinding
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.helper.HPI
import id.ac.sttccirebon.lecture.ui.uitel.LoadingDialog
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import kotlin.concurrent.thread
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import id.ac.sttccirebon.lecture.SapActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class PengisianSapActvity : AppCompatActivity() {

    private lateinit var binding: ActivityPengisiansapBinding
    private lateinit var sap: Sap
    private lateinit var data: DataManager
    private var absenList = arrayListOf<Absen>()
    var token: String? = null
    var id: String? = null
    private val PICK_PDF_REQUEST_CODE = 1
    private var selectedFileUri: Uri? = null
    private var pdfPath: String? = null

    var items : Array<String> = arrayOf("Tatap Muka", "Praktikum", "Praktek Lapangan", "Simulasi")
    var Item : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityPengisiansapBinding.inflate(layoutInflater)

        data = DataManager(baseContext)
        val view = binding.root
        setContentView(view)

        val back = binding.back
        back.setOnClickListener{
            startActivity(Intent(this, SapActivity::class.java))
            finish()
        }

        val adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item, items
        )

        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val item = adapterView.getItemAtPosition(i).toString()

                if (item.equals("Tatap Muka")) {
                    Item = "Tatap Muka"
                    binding.pilih.text = Item
                }
                if (item.equals("Praktikum")) {
                    Item = "Praktikum"
                    binding.pilih.text = Item
                }
                if (item.equals("Praktek Lapangan")) {
                    Item = "Praktek Lapangan"
                    binding.pilih.text = Item
                }
                if (item.equals("Simulasi")) {
                    Item = "Simulasi"
                    binding.pilih.text = Item
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                binding.pilih.text = "Pilih"
            }
        }


        token = data.getString("token").toString()
        id = data.getString("id")

        binding.uploadModul.setOnClickListener {
            pickDocument()
        }

        binding.simpan.setOnClickListener {
            Toast.makeText(this@PengisianSapActvity, "Data belum lengkap", Toast.LENGTH_LONG)
        }

        detailSap()

    }

    private fun detailSap() {

        val loading = LoadingDialog(this)
        loading.startLoading()

        val queue = Volley.newRequestQueue(this@PengisianSapActvity)
        val url = "${HPI.API_URL}/api/dosen/get-detail-satuan-acara-perkuliahan"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                loading.isDismiss()
                try {
                    val responseJson = JSONObject(response)
                    val Result = responseJson.getJSONObject("result")

                    val detailJadwal = Result.getJSONArray("detail_jadwal_kuliah_dosen")
                    for (i in 0 until detailJadwal.length()){
                        val kuliahDosen = detailJadwal.getJSONObject(i)
                        id = kuliahDosen.getString("id_detail_jadwal_kuliah")
                        binding.sapMatkul.text = kuliahDosen.getString("mata_kuliah")
                        binding.sapKelas.text = kuliahDosen.getString("kelas")
                        binding.sapTanggal.text = kuliahDosen.getString("tanggal")
                        binding.sapStudi.text = kuliahDosen.getString("program_studi")
                        binding.sapJam.text = kuliahDosen.getString("jam")
                    }

                    val absenMahasiswa = Result.getJSONArray("kehadiran_mahasiswa")
                    absenList.clear()

                    for (i in 0 until absenMahasiswa.length()) {
                        val detailAbsen = absenMahasiswa.getJSONObject(i)
                        val lisAbsen = Absen(
                            detailAbsen.getString("id_absen").toString(),
                            detailAbsen.getString("nama"),
                            detailAbsen.getString("status")
                        )

                        absenList.add(lisAbsen)
                    }
                    Log.i("DATA_API", absenMahasiswa.toString())
                    showRecyclerList()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            },
            Response.ErrorListener {
                Toast.makeText(this@PengisianSapActvity, "Gagal ditampilkan", Toast.LENGTH_LONG)
                    .show()
                loading.isDismiss()
            }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                val mStatusCode = response.statusCode
                Log.i("DATA_API", Integer.toString(mStatusCode))
                return super.parseNetworkResponse(response)
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Accept"] = "application/json"
                return params
            }

            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["token"] = token.toString()
                Log.i("DATA_API", token.toString())
                params["id_detail_jadwal_kuliah"] = id.toString()
                Log.i("DATA_API", id.toString())
                return params
            }
        }

        stringRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }
        queue.add(stringRequest)
    }

    private fun pickDocument() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        startActivityForResult(intent, PICK_PDF_REQUEST_CODE)
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Mendapatkan URI dari file yang dipilih
            selectedFileUri = data.data
            pdfPath = selectedFileUri?.path

            // Mendapatkan nama file dari URI
            val cursor = contentResolver.query(selectedFileUri!!, null, null, null, null)
            cursor?.moveToFirst()
            val fileName = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            cursor?.close()

            // Menampilkan nama file pada tombol selectFileButton
            binding.namaFile.text = fileName

            binding.simpan.setOnClickListener {

                var isValid = true
                val pesan: String = binding.insertPesan.text.toString()

                if (pesan.isEmpty()) {
                    isValid = false
                    binding.insertPesan.error = "Pesan wajib diisi"
                }

                if (isValid) {

                    binding.simpan.isEnabled = false
                    val loading = LoadingDialog(this)
                    loading.startLoading()

                    thread {
                        try {
                            // Membaca file PDF menjadi byte array
                            val dataManager: DataManager = DataManager(baseContext)
                            // Membaca file PDF menjadi byte array
                            val inputStream = contentResolver.openInputStream(selectedFileUri!!)
                            val bytes = inputStream?.readBytes()
                            inputStream?.close()

                            // Mengirim file PDF ke server
                            val client = OkHttpClient()

                            val requestBody = MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("token", token.toString())
                                .addFormDataPart("id_detail_jadwal_kuliah", id.toString())
                                .addFormDataPart("materi", pesan)
                                .addFormDataPart("jenis_pertemuan", Item.toString())
                                .addFormDataPart(
                                    "modul",
                                    fileName!!,
                                    bytes!!.toRequestBody("application/pdf".toMediaType())
                                )
                                .build()

                            Log.i("DATA_API", token.toString())
                            Log.i("DATA_API", id.toString())
                            Log.i("DATA_API", pesan.toString())


                            val request = Request.Builder()
                                .url("${HPI.API_URL}/api/dosen/set-satuan-acara-perkuliahan")
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
                                        this@PengisianSapActvity,
                                        //    "SAP berhasil dikirimkan",
                                        message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    moveIntent()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@PengisianSapActvity,
                                        "Terjadi kesalahan saat mengirim data",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                        } catch (e: IOException) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@PengisianSapActvity,
                                    "Terjadi kesalahan saat mengirim data : ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } finally {
                            runOnUiThread {
                                loading.isDismiss()
                                binding.simpan.isEnabled = true
                            }
                        }
                    }
                }

            }
        }

    }

    private fun moveIntent() {
        startActivity(Intent(this, DashboardActivity::class.java))
        val loading = LoadingDialog(this)
        loading.startLoading()
        (object : Runnable {
            override fun run() {
                finish()
                loading.isDismiss()
            }
        })
    }

    private fun showRecyclerList() {
        val recyclerView: RecyclerView = binding.kehadiranMahasiswa
        recyclerView.layoutManager = LinearLayoutManager(this@PengisianSapActvity)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = AbsenAdapter(absenList)
    }

}