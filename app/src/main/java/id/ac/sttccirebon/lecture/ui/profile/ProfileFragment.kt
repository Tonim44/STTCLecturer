package id.ac.sttccirebon.lecture.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import id.ac.sttccirebon.lecture.databinding.FragmentProfileBinding
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.helper.HPI
import id.ac.sttccirebon.lecture.ui.uitel.LoadingDialogFragment
import org.json.JSONException
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var data : DataManager
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        data = DataManager(root.context)

        binding.edit.setOnClickListener {
            startActivity(Intent(this.context, EditProfileActivity::class.java))
        }

        val loading = LoadingDialogFragment(this)
        loading.startLoading()

        val token = data.getString("token")
        val queue = Volley.newRequestQueue(this.context)
        val url = "${HPI.API_URL}/api/dosen/get-profile"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                loading.isDismiss()
                try {
                    val responseJson = JSONObject(response)
                    val user = responseJson.getJSONObject("result")

                    val User : String = user.getString("nama_dosen")
                    binding.namaDosen.text = User.toString()
                    data.putUsername(User)

                    binding.detailNip.text = user.getString("nik")
                    binding.detailNomertelepon.text = user.getString("nomor_telepon")

                    val fotoProfil = user.getString("link_foto_profil")
                    Picasso.get().load(fotoProfil).into(binding.fotoProfile)
                    data.putProfile(fotoProfil)

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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}