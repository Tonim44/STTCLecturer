package id.ac.sttccirebon.lecture.ui.riwayat

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.ac.sttccirebon.lecture.databinding.ActivityRiwayatBinding
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.helper.HPI
import id.ac.sttccirebon.lecture.ui.sap.Absen
import id.ac.sttccirebon.lecture.ui.sap.Sap
import id.ac.sttccirebon.lecture.ui.uitel.LoadingDialog
import org.json.JSONException
import org.json.JSONObject

class RiwayatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRiwayatBinding
    private lateinit var riwayat: Sap
    private lateinit var data : DataManager
    private var absenList = arrayListOf<Absen>()

    companion object {
        const val EXTRA_RIWAYAT = "extra_riwayat"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityRiwayatBinding.inflate(layoutInflater)

        data = DataManager(baseContext)
        val view = binding.root
        setContentView(view)

        val back = binding.back
        back.setOnClickListener(View.OnClickListener { onBackPressed() })

        riwayat = intent.getParcelableExtra<Sap>(EXTRA_RIWAYAT) as Sap

        val id : String = riwayat.id_detail_jadwal_kuliah.toString()
        binding.sapMatkul.text = riwayat.mata_kuliah
        binding.sapKelas.text = riwayat.kelas
        binding.sapTanggal.text = riwayat.tanggal
        binding.sapStudi.text = riwayat.program_studi
        binding.sapJam.text = riwayat.jam

        val loading = LoadingDialog(this)
        loading.startLoading()

        val token = data.getString("token")
        val queue = Volley.newRequestQueue(this@RiwayatActivity)
        val url = "${HPI.API_URL}/api/dosen/get-detail-riwayat-mengajar"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                loading.isDismiss()
                try {
                    val responseJson = JSONObject(response)
                    val Result = responseJson.getJSONObject("result")
                    val absenMahasiswa = Result.getJSONArray("kehadiran_mahasiswa")
                    absenList.clear()

                    for(i in 0 until absenMahasiswa.length()) {
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

                    val riwayatSap = Result.getJSONObject("riwayat_satuan_acara_perkuliahan")
                    val Materi = riwayatSap.getString("materi")
                    val Modul = riwayatSap.getString("modul")
                    val JenisPertemuan = riwayatSap.getString("jenis_pertemuan")

                    binding.isiMateri.text = Materi
                    binding.sapJenispertemuan.text = JenisPertemuan

                    binding.lihatDocument.setOnClickListener{
                        val openURL = Intent(Intent.ACTION_VIEW)
                        openURL.data = Uri.parse(Modul)
                        startActivity(openURL)
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            },
            Response.ErrorListener {
                Toast.makeText(this@RiwayatActivity, "Gagal ditampilkan", Toast.LENGTH_LONG).show()
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
                params["id_detail_jadwal_kuliah"] = id
                Log.i("DATA_API", id)
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

    private fun showRecyclerList() {
        val recyclerView : RecyclerView = binding.kehadiranMahasiswa
        recyclerView.layoutManager = LinearLayoutManager(this@RiwayatActivity)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = AbsenriwayatAdapter(absenList)
    }

}
