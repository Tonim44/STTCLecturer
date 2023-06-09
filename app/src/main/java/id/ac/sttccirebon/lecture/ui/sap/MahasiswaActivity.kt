package id.ac.sttccirebon.lecture.ui.sap

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.ac.sttccirebon.lecture.databinding.ActivityMahasiswaBinding
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.helper.HPI
import id.ac.sttccirebon.lecture.ui.helper.PrefHelper
import id.ac.sttccirebon.lecture.ui.uitel.LoadingDialog
import org.json.JSONException
import org.json.JSONObject

class MahasiswaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMahasiswaBinding
    private lateinit var data: DataManager
    lateinit var prefHelper: PrefHelper
    private lateinit var mahasiswa: Absen

    companion object {
        const val EXTRA_MAHASISWA = "extra_mahasiswa"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMahasiswaBinding.inflate(layoutInflater)
        data = DataManager(baseContext)
        val view = binding.root
        prefHelper = PrefHelper(this)
        setContentView(view)

        val back = binding.back
        back.setOnClickListener(View.OnClickListener { onBackPressed() })

        mahasiswa = intent.getParcelableExtra<Absen>(MahasiswaActivity.EXTRA_MAHASISWA) as Absen

        val loading = LoadingDialog(this)
        loading.startLoading()

        val id : String = mahasiswa.id.toString()
        val token = data.getString("token")
        val queue = Volley.newRequestQueue(this@MahasiswaActivity)
        val url = "${HPI.API_URL}/api/dosen/get-absen-mahasiswa"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                loading.isDismiss()
                try {
                    val responseJson = JSONObject(response)
                    val Result = responseJson.getJSONObject("result")
                    val Nama = Result.getString("nama")
                    val Status = Result.getString("valid")

                    binding.namaMahasiswa.text = Nama

                    if (Status.equals("yes")){
                        binding.hadir.text = "Hadir"
                    }else{
                        binding.tidakHadir.text = "Tidak Hadir"
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            },
            Response.ErrorListener {
                Toast.makeText(this@MahasiswaActivity, "Gagal ditampilkan", Toast.LENGTH_LONG).show()
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
                params["id_absen"] = id
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
}
