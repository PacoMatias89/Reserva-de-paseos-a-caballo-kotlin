package com.example.reservapaseoacaballo

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView


class AdaptadorReservas(
    var listaReservas: MutableList<Reserva>,
    val listener: AdaptadorListener
) : RecyclerView.Adapter<AdaptadorReservas.ViewHolder>() {
    private var reservas: List<Reserva> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista =
            LayoutInflater.from(parent.context).inflate(R.layout.item_rv_reserva, parent, false)
        return ViewHolder(vista)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reserva = listaReservas[position]
        holder.tvNombreJinete.text = reserva.nombreJinete
        holder.tvNombreCaballo.text = reserva.nombreCaballo
        holder.tvFecha.text = reserva.fecha
        holder.tvMovil.text = reserva.movil
        holder.tvObservaciones.text = reserva.observaciones


        holder.cvReserva.setOnClickListener {
            listener.onEditItemClick(reserva)

        }
        holder.btnBorrar.setOnClickListener {
            listener.onDeleteItemClick(reserva)
        }
    }

    override fun getItemCount(): Int {
        return listaReservas.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val cvReserva = itemView.findViewById<CardView>(R.id.cvReserva)
        val tvNombreJinete = itemView.findViewById<TextView>(R.id.tvNombreJinete)
        val tvNombreCaballo = itemView.findViewById<TextView>(R.id.tvNombreCaballo)
        val tvFecha = itemView.findViewById<TextView>(R.id.tvFecha)
        val tvMovil = itemView.findViewById<TextView>(R.id.tvMovil)
        val tvObservaciones = itemView.findViewById<TextView>(R.id.tvObservaciones)
        val btnBorrar = itemView.findViewById<Button>(R.id.btnBorrar)

    }

    fun actualizarReservas(reservas: List<Reserva>) {
        this.listaReservas
        notifyDataSetChanged()
    }
}