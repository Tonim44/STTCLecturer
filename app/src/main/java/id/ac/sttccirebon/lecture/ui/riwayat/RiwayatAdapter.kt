package id.ac.sttccirebon.lecture.ui.riwayat

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.ac.sttccirebon.lecture.R
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.sap.Sap

class RiwayatAdapter(val riwayatList: MutableList<Sap>) : RecyclerView.Adapter<RiwayatAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_mengajar, parent, false)
        return ListViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = DataManager(holder.itemView.context)
        val riwayat = riwayatList[position]
        holder.textViewTanggal.text = riwayat.tanggal
        holder.textViewMatkul.text = riwayat.mata_kuliah
        holder.textViewKelas.text = riwayat.kelas

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RiwayatActivity::class.java)
            intent.putExtra(RiwayatActivity.EXTRA_RIWAYAT, riwayat)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return riwayatList.size
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTanggal = itemView.findViewById<TextView>(R.id.tanggal)
        val textViewMatkul = itemView.findViewById<TextView>(R.id.matakuliah)
        val textViewKelas = itemView.findViewById<TextView>(R.id.namakelas)
    }

}
