package id.ac.sttccirebon.lecture

import android.os.Bundle
import android.os.Process
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import id.ac.sttccirebon.lecture.databinding.ActivityDashboardBinding
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.helper.HPI
import id.ac.sttccirebon.lecture.ui.uitel.LoadingDialog
import org.json.JSONException
import org.json.JSONObject

class DashboardActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var data : DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        data = DataManager(baseContext)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val loading = LoadingDialog(this)
        loading.startLoading()
        val token = data.getString("token")
        val queue = Volley.newRequestQueue(this)
        val url = "${HPI.API_URL}/api/dosen/get-profile"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                loading.isDismiss()
                try {
                    val responseJson = JSONObject(response)
                    val user = responseJson.getJSONObject("result")

                    val User : String = user.getString("nama_dosen")
                    val Name = navView.findViewById<TextView>(R.id.userName_bar)
                    Name.text = User.toString()
                    data.putUsername(User)

                    val Nik = navView.findViewById<TextView>(R.id.nip_bar)
                    Nik.text = user.getString("nik")

                    val PhotoProfil = navView.findViewById<ImageView>(R.id.fotoProfile_bar)
                    val fotoProfil = user.getString("link_foto_profil")
                    Picasso.get().load(fotoProfil).into(PhotoProfil)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            },
            Response.ErrorListener {
                Toast.makeText(this@DashboardActivity, "Gagal ditampilkan", Toast.LENGTH_LONG).show()
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}