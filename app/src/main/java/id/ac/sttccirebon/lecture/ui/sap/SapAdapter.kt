package id.ac.sttccirebon.lecture.ui.sap

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.ac.sttccirebon.lecture.R
import id.ac.sttccirebon.lecture.ui.helper.DataManager

class SapAdapter(val sapList: MutableList<Sap>) : RecyclerView.Adapter<SapAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_mengajar, parent, false)
        return ListViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = DataManager(holder.itemView.context)
        val sap = sapList[position]
        holder.textViewTanggal.text = sap.tanggal
        holder.textViewMatkul.text = sap.mata_kuliah
        holder.textViewKelas.text = sap.kelas
        val id = sap.id_detail_jadwal_kuliah

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, PengisianSapActvity::class.java)
            data.putId(id.toString())
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return sapList.size
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTanggal = itemView.findViewById<TextView>(R.id.tanggal)
        val textViewMatkul = itemView.findViewById<TextView>(R.id.matakuliah)
        val textViewKelas = itemView.findViewById<TextView>(R.id.namakelas)
    }

}