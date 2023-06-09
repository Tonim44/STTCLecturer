package id.ac.sttccirebon.lecture.ui.sap

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.ac.sttccirebon.lecture.databinding.ActivityValidasiabsenBinding
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.helper.HPI
import id.ac.sttccirebon.lecture.ui.helper.PrefHelper
import id.ac.sttccirebon.lecture.ui.uitel.LoadingDialog
import org.json.JSONException

class ValidasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityValidasiabsenBinding
    private lateinit var data: DataManager
    lateinit var prefHelper: PrefHelper
    private lateinit var absen: Absen
    var items: Array<String> = arrayOf("Hadir", "Tidak Hadir")
    var Absen: String? = null
    var Item: String? = null

    companion object {
        const val EXTRA_ABSEN = "extra_absen"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityValidasiabsenBinding.inflate(layoutInflater)
        data = DataManager(baseContext)
        val view = binding.root
        prefHelper = PrefHelper(this)
        setContentView(view)

        val loading = LoadingDialog(this)
        loading.startLoading()

        absen = intent.getParcelableExtra<Absen>(ValidasiActivity.EXTRA_ABSEN) as Absen
        binding.namaMahasiswa.text = absen.nama
        val id : String = absen.id.toString()

        val back = binding.back
        back.setOnClickListener(View.OnClickListener { onBackPressed() })

        val adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_dropdown_item, items
        )

        val spinner = binding.spinner
        val absen : TextView = binding.absen
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val item = adapterView.getItemAtPosition(i).toString()

                if (item.equals("Hadir")) {
                    Absen = "yes"
                    Item = "Hadir"
                    absen.text = Item
                }
                if (item.equals("Tidak Hadir")) {
                    Absen = "no"
                    Item = "Tidak Hadir"
                    absen.text = Item
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                absen.text = "Absen"
            }
        }
        loading.isDismiss()

        binding.simpanAbsen.setOnClickListener{

            val loading = LoadingDialog(this)
            loading.startLoading()
            val token = data.getString("token")
            val queue = Volley.newRequestQueue(this)
            val url = "${HPI.API_URL}/api/dosen/set-validasi-kehadiran-mahasiswa"
            val stringRequest: StringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener<String?> { response ->
                    loading.isDismiss()
                    try {

                        moveIntent()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                },
                Response.ErrorListener {
                    Toast.makeText(this@ValidasiActivity, "Gagal mengirimkan validasi", Toast.LENGTH_LONG).show()
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
                    params["valid"] = Absen.toString()
                    Log.i("DATA_API", Absen.toString())

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

    private fun moveIntent() {
        startActivity(Intent(this, PengisianSapActvity::class.java))
        finish()
    }

}