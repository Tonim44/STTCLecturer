package id.ac.sttccirebon.lecture.ui.sap

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.ac.sttccirebon.lecture.databinding.FragmentSapBinding
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.helper.HPI
import id.ac.sttccirebon.lecture.ui.uitel.LoadingDialogFragment
import org.json.JSONException
import org.json.JSONObject

class SapFragment : Fragment() {

    private var _binding: FragmentSapBinding? = null
    private val binding get() = _binding!!
    private lateinit  var  data : DataManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: SapAdapter
    private lateinit var sapList: MutableList<Sap>
    private lateinit var requestQueue: RequestQueue
    private var page = 1
    var isLoading = false

        override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

      _binding = FragmentSapBinding.inflate(inflater, container, false)
      val root: View = binding.root
      data = DataManager(root.context)

            val loading = LoadingDialogFragment(this)
            loading.startLoading()

            sapList = mutableListOf()
            adapter = SapAdapter(sapList)
            recyclerView = binding.jadwalPerkuliahan
            recyclerView.layoutManager = LinearLayoutManager(this.context)
            recyclerView.adapter = adapter

            swipeRefreshLayout = binding.swipeRefreshLayout
            swipeRefreshLayout.setOnRefreshListener {
                page = 1
                sapList.clear()
                loadDataFromApi()
            }

            requestQueue = Volley.newRequestQueue(this.context)


            var isLastPage = false

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItemPosition == adapter.itemCount - 1 && !isLoading && !isLastPage) {
                        isLoading = true
                        page++
                        loadDataFromApi()
                    }
                }
            })

            loadDataFromApi()
            isLoading = false

            loading.isDismiss()

            return root
  }

    private fun loadDataFromApi() {

        val token = data.getString("token")
        swipeRefreshLayout.isRefreshing = true
        val url = "${HPI.API_URL}/api/dosen/get-satuan-acara-perkuliahan"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                try {
                    val responseJson = JSONObject(response)
                    val Result = responseJson.getJSONObject("result")
                    val jadwalMengajar = Result.getJSONArray("jadwal_kuliah_dosen")

                    for (i in 0 until jadwalMengajar.length()) {
                        val detailMengajar = jadwalMengajar.getJSONObject(i)
                        val listMatkul = Sap(
                            detailMengajar.getInt("id_detail_jadwal_kuliah"),
                            detailMengajar.getString("mata_kuliah"),
                            detailMengajar.getString("tanggal"),
                            detailMengajar.getString("jam"),
                            detailMengajar.getString("program_studi"),
                            detailMengajar.getString("kelas")
                        )
                        sapList.add(listMatkul)
                    }

                    if (page == 1) {
                        adapter = SapAdapter(sapList)
                        recyclerView.adapter = adapter
                    } else {
                        adapter.notifyDataSetChanged()
                    }

                    swipeRefreshLayout.isRefreshing = false
                    isLoading = false

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this.context, "Gagal ditampilkan", Toast.LENGTH_LONG).show()
                swipeRefreshLayout.isRefreshing = false
                isLoading = false
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
                params["page"] = page.toString()
                Log.i("DATA_API", page.toString())
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
            override fun retry(error: VolleyError) {}
        }

        requestQueue.add(stringRequest)
    }

    override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
   }