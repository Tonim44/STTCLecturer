package id.ac.sttccirebon.lecture.ui.logout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import id.ac.sttccirebon.lecture.LoginActivity
import id.ac.sttccirebon.lecture.databinding.FragmentLogoutBinding
import id.ac.sttccirebon.lecture.ui.helper.PrefHelper
import id.ac.sttccirebon.lecture.ui.uitel.LoadingDialogFragment

class LogoutFragment : Fragment() {

    private var _binding: FragmentLogoutBinding? = null
    lateinit var prefHelper: PrefHelper
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLogoutBinding.inflate(inflater, container, false)
        val root: View = binding.root
        prefHelper = PrefHelper(root.context)

        val Yalogout = binding.yaLogout
        Yalogout.setOnClickListener{
            val loading = LoadingDialogFragment(this)
            loading.startLoading()
            (object :Runnable{
                override fun run() {
                    loading.isDismiss()
                }
            })

            prefHelper.clear()
            startActivity(Intent(this.context, LoginActivity::class.java))
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}