package com.example.reservapaseoacaballo

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.reservapaseoacaballo.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), AdaptadorListener {


    lateinit var binding: ActivityMainBinding
    var listaReservas: MutableList<Reserva> = mutableListOf()
    lateinit var adapter: AdaptadorReservas
    lateinit var room: DBReserva
    lateinit var reserva: Reserva
    lateinit var viewModel: ViewModel
    lateinit var reservaRepository: ReservaRepository

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        binding.rvReservas.layoutManager = LinearLayoutManager(this)
        adapter = AdaptadorReservas(listaReservas, this)
        room = Room.databaseBuilder(this, DBReserva::class.java, "reservas").build()

        reservaRepository = ReservaRepositoryImpl(room.daoReserva())

        binding.floatingAgregarContacto.setOnClickListener{
            val intent = Intent(this, RegistrarJinete::class.java)
            startActivity(intent)
        }

        binding.rvReservas.adapter = adapter


        // recargamos la lista de reservas
        obtenerReservas(room)
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            obtenerReservas(room)
        }
        val actualizar = intent.getBooleanExtra("actualizar", false)
        if (actualizar) {
            obtenerReservas(room)
        }
        adapter.notifyDataSetChanged()
        buscar()

    }



    private fun buscarReservas(fechaHora: String) {
        val reservasEncontradas = mutableListOf<Reserva>()

        lifecycleScope.launch {
            val reservas = reservaRepository.buscarReservasPorFecha(fechaHora)
            reservasEncontradas.addAll(reservas)

            adapter.listaReservas.clear()
            adapter.listaReservas.addAll(reservasEncontradas)

            adapter.notifyDataSetChanged()
        }
    }

    private fun buscar() {
        binding.btnBuscar.setOnClickListener {
            val fechaHora = binding.etBuscarFecha.text.toString().trim()
            if (fechaHora.isNotEmpty()) {
                buscarReservas(fechaHora)
            } else {
                obtenerReservas(room) // Vuelve a obtener todas las reservas

            }
        }
    }


    private fun obtenerReservas(room: DBReserva) {
        lifecycleScope.launch {

            listaReservas = room.daoReserva().obtenerReservas() as MutableList<Reserva>
            adapter = AdaptadorReservas(listaReservas, this@MainActivity)
            binding.rvReservas.adapter = adapter
            adapter.notifyDataSetChanged()
            // Finalizar la actualización
            swipeRefreshLayout.isRefreshing = false
        }

}

    override fun onEditItemClick(reserva: Reserva) {
        val dialogView = layoutInflater.inflate(R.layout.actualizar_info_jinete, null)
        val nombreJinete = dialogView.findViewById<EditText>(R.id.NombreJinete)
        val nombreCaballo = dialogView.findViewById<Spinner>(R.id.spNombreCaballo)
        val fecha = dialogView.findViewById<EditText>(R.id.Fecha)
        val movil = dialogView.findViewById<EditText>(R.id.Movil)
        val observaciones = dialogView.findViewById<EditText>(R.id.Observaciones)

        // Establecer los valores de reserva en los campos del diálogo
        nombreJinete.setText(reserva.nombreJinete)

        // Configurar el adapter para el spinner
        val caballoAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.nombre_caballos,
            android.R.layout.simple_spinner_item
        )
        caballoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        nombreCaballo.adapter = caballoAdapter


        fecha.setText(reserva.fecha)
        movil.setText(reserva.movil)
        observaciones.setText(reserva.observaciones)

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Actualizar información de jinete")
            .setPositiveButton("Actualizar") { dialog, which ->
                // Obtener los valores actualizados de los campos del diálogo
                val nuevoNombreJinete = nombreJinete.text.toString()
                val nuevoNombreCaballo = nombreCaballo.selectedItem.toString()
                val nuevaFecha = fecha.text.toString()
                val nuevoMovil = movil.text.toString()
                val nuevasObservaciones = observaciones.text.toString()


                // Actualizar los valores en la reserva
                reserva.nombreJinete = nuevoNombreJinete
                reserva.nombreCaballo = nuevoNombreCaballo
                reserva.fecha = nuevaFecha
                reserva.movil = nuevoMovil
                reserva.observaciones = nuevasObservaciones
                val reservaExistente = Reserva(
                    reserva.id,
                    nuevoNombreJinete,
                    nuevoNombreCaballo,
                    nuevaFecha,
                    nuevoMovil,
                    nuevasObservaciones
                )


                lifecycleScope.launch {
                    reservaRepository.actualizarReserva(reservaExistente)
                }
               
                viewModel.actualizarReserva(reservaExistente)
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged()

                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onDeleteItemClick(reserva: Reserva) {
        lifecycleScope.launch {
            room.daoReserva().eliminarReserva(reserva.id)
            listaReservas.remove(reserva)
            obtenerReservas(room)
            adapter.notifyDataSetChanged()

        }

    }


}