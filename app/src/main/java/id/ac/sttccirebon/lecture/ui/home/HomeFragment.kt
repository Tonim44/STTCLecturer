package id.ac.sttccirebon.lecture.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.ac.sttccirebon.lecture.SapActivity
import id.ac.sttccirebon.lecture.databinding.FragmentHomeBinding
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.helper.HPI
import id.ac.sttccirebon.lecture.ui.uitel.LoadingDialogFragment
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONException
import kotlin.collections.HashMap

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var data : DataManager
    private var mengajarList = arrayListOf<Mengajar>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        data = DataManager(root.context)

        val username = data.getString("user")
        val User = binding.user
        User.text = "Selamat Datang\n" + "${username}"

        binding.klikSap.setOnClickListener{
            startActivity(Intent(this.context, SapActivity::class.java))
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDateandTime: String = sdf.format(Date())
        val token = data.getString("token")

        val loading = LoadingDialogFragment(this)
        loading.startLoading()
        val queue = Volley.newRequestQueue(this.context)
        val url = "${HPI.API_URL}/api/dosen/get-jadwal-kuliah-dosen"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                loading.isDismiss()
                try {
                    val responseJson = JSONObject(response)
                    val Result = responseJson.getJSONObject("result")
                    val jadwalMengajar = Result.getJSONArray("jadwal_kuliah_dosen")
                    mengajarList.clear()

                    for(i in 0 until jadwalMengajar.length()) {
                        val detailMengajar = jadwalMengajar.getJSONObject(i)
                        val lisMatkul = Mengajar(
                            detailMengajar.getString("mata_kuliah"),
                            detailMengajar.getString("kelas"),
                            detailMengajar.getString("ruangan"),
                            detailMengajar.getString("tanggal"),
                            detailMengajar.getString("jam")
                        )
                        mengajarList.add(lisMatkul)
                    }
                    Log.i("DATA_API", jadwalMengajar.toString())
                    showRecyclerList()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            },
            Response.ErrorListener {
                Toast.makeText(this.context, "Gagal ditampilkan", Toast.LENGTH_LONG).show()
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
                params["date"] = currentDateandTime
                Log.i("DATA_API", currentDateandTime.toString())
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

        val CalendarPerkuliahan : CalendarView = binding.calendarPerkuliahan
        CalendarPerkuliahan.setOnDateChangeListener { CalendarPerkuliahan, i, i2, i3 ->
            android.util.Log.i("DATA_API", i.toString())
            android.util.Log.i("DATA_API", (i2 + 1).toString())
            android.util.Log.i("DATA_API", i3.toString())

            val loading = LoadingDialogFragment(this)
            loading.startLoading()

            val Month: Int = i2 + 1
            val currentDateandTime: String = "$i-$Month-$i3"
            val queue = Volley.newRequestQueue(this.context)
            val url = "${HPI.API_URL}/api/dosen/get-jadwal-kuliah-dosen"
           val stringRequest: StringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener<String?> { response ->
                    loading.isDismiss()
                    try {
                        val responseJson = JSONObject(response)
                        val Result = responseJson.getJSONObject("result")
                        val jadwalMengajar = Result.getJSONArray("jadwal_kuliah_dosen")
                        mengajarList.clear()

                        for(i in 0 until jadwalMengajar.length()) {
                            val detailMengajar = jadwalMengajar.getJSONObject(i)
                            val lisMatkul = Mengajar(
                                detailMengajar.getString("mata_kuliah"),
                                detailMengajar.getString("kelas"),
                                detailMengajar.getString("ruangan"),
                                detailMengajar.getString("tanggal"),
                                detailMengajar.getString("jam")
                            )
                            mengajarList.add(lisMatkul)
                        }
                        Log.i("DATA_API", jadwalMengajar.toString())
                        showRecyclerList()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                },
                Response.ErrorListener {
                    Toast.makeText(this.context, "Login Gagal", Toast.LENGTH_LONG).show()
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
                    params["date"] = currentDateandTime
                    Log.i("DATA_API", currentDateandTime.toString())
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
            return root
    }

    private fun showRecyclerList() {
        val recyclerView: RecyclerView = binding.jadwalHarini
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = MengajarAdapter(mengajarList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}