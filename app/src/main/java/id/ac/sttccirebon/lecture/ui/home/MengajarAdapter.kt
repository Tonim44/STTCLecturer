package id.ac.sttccirebon.lecture.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.ac.sttccirebon.lecture.R
import id.ac.sttccirebon.lecture.ui.helper.DataManager

class MengajarAdapter(val mengajarList: ArrayList<Mengajar>) : RecyclerView.Adapter<MengajarAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_jadwalharini, parent, false)
        return ListViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = DataManager(holder.itemView.context)
        val mengajar = mengajarList[position]
        holder.textViewTanggal.text = mengajar.tanggal
        holder.textViewJam.text = mengajar.jam
        holder.textViewMatkul.text = mengajar.mata_kuliah
        holder.textViewKelas.text = mengajar.kelas
        holder.textViewRuangan.text = mengajar.ruangan
    }

    override fun getItemCount(): Int {
        return mengajarList.size
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTanggal = itemView.findViewById<TextView>(R.id.tanggal)
        val textViewJam = itemView.findViewById<TextView>(R.id.jam)
        val textViewMatkul = itemView.findViewById<TextView>(R.id.mata_kuliah)
        val textViewKelas = itemView.findViewById<TextView>(R.id.nama_kelas)
        val textViewRuangan = itemView.findViewById<TextView>(R.id.ruangan)
    }

}