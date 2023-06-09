package id.ac.sttccirebon.lecture.ui.riwayat

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.ac.sttccirebon.lecture.R
import id.ac.sttccirebon.lecture.ui.helper.DataManager
import id.ac.sttccirebon.lecture.ui.sap.Absen
import id.ac.sttccirebon.lecture.ui.sap.MahasiswaActivity

class AbsenriwayatAdapter(val absenList: ArrayList<Absen>) : RecyclerView.Adapter<AbsenriwayatAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_kehadiranmahasiswa, parent, false)
        return ListViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = DataManager(holder.itemView.context)
        val absen = absenList[position]
        holder.textViewNama.text = absen.nama
        holder.Edit.visibility = View.GONE

        if ( absen.status.equals("Alpa") ){
            holder.textViewTidakHadir.text=absen.status
            holder.Edit.visibility = View.GONE
        }else{
            holder.textViewHadir.text=absen.status
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, MahasiswaActivity::class.java)
                intent.putExtra(MahasiswaActivity.EXTRA_MAHASISWA, absen)
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return absenList.size
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNama = itemView.findViewById<TextView>(R.id.nama)
        val textViewHadir = itemView.findViewById<TextView>(R.id.hadir)
        val textViewTidakHadir = itemView.findViewById<TextView>(R.id.tidak_hadir)
        val Edit = itemView.findViewById<ImageView>(R.id.edit)
    }

}
