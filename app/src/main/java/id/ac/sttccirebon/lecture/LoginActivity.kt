package id.ac.sttccirebon.lecture

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.ac.sttccirebon.lecture.databinding.ActivityLoginBinding
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.helper.HPI
import id.ac.sttccirebon.lecture.ui.helper.PrefHelper
import id.ac.sttccirebon.lecture.ui.uitel.LoadingDialog
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var data : DataManager
    lateinit var prefHelper: PrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityLoginBinding.inflate(layoutInflater)
        data = DataManager(baseContext)
        val view = binding.root
        prefHelper = PrefHelper(this)
        setContentView(view)

    val txtUsername = binding.insertUsername
        val txtPassword = binding.insertPassword

        binding.login.setOnClickListener{

            val username : String = txtUsername.text.toString()
            val password : String = txtPassword.text.toString()

            loginRequest(username, password)
        }

    }

    private fun loginRequest(username: String, password: String) {

            var isValid = true

            if (username.isEmpty()) {
                isValid = false
                binding.insertUsername.error = "No Telp wajib diisi"
            }

            if (password.toString().isEmpty()) {
                isValid = false
                binding.insertPassword.error = "Password wajib diisi"
            }

            if (isValid) {
                val loading = LoadingDialog(this)
                loading.startLoading()
                val queue = Volley.newRequestQueue(this)
                val url = "${HPI.API_URL}/api/dosen/login"
                binding.login.isEnabled = false
                val stringRequest: StringRequest = object : StringRequest(
                    Method.POST, url,
                    Response.Listener<String?> { response ->
                        loading.isDismiss()
                        try {
                            val responseJson = JSONObject(response)
                            val token = responseJson.getString("token")
                            Log.i("DATA_API", token)

                            if (username.isNotEmpty() && password.toString().isNotEmpty()) {
                                saveSession(username.toString(), password.toString())
                            }

                            data.putUserData(token)
                            binding.login.isEnabled = true
                            moveIntent()

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    },
                    Response.ErrorListener {
                        binding.login.isEnabled = true
                        Toast.makeText(this@LoginActivity, "Login gagal", Toast.LENGTH_LONG).show()
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
                        params["username"] = "62${username}"
                        params["password"] = password
                        params["type"] = "teacher"
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

    override fun onStart() {
        super.onStart()
        if (prefHelper.getBoolean( HPI.PREF_IS_LOGIN )) {
            moveIntent()
        }
    }

    private fun saveSession(username: String, password: String){
        prefHelper.put( HPI.PREF_USERNAME, username )
        prefHelper.put( HPI.PREF_PASSWORD, password )
        prefHelper.put( HPI.PREF_IS_LOGIN, true)
    }

    private fun moveIntent(){
        startActivity(Intent(this, DashboardActivity::class.java))
        val loading = LoadingDialog(this)
        loading.startLoading()
        (object :Runnable{
            override fun run() {
                finish()
                loading.isDismiss()
            }
        })
    }

    override fun onBackPressed() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Keluar")
        alertDialogBuilder
            .setMessage("Apakah anda yakin untuk menutup Aplikasi ?")
            .setCancelable(false)
            .setPositiveButton(
                "Iya"
            ) { dialog, id ->
                moveTaskToBack(true)
                Process.killProcess(Process.myPid())
                System.exit(1)
            }
            .setNegativeButton(
                "Tidak"
            ) { dialog, id -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}