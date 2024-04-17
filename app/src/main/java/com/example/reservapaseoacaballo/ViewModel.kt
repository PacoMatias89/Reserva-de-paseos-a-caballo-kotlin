package com.example.reservapaseoacaballo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
class ViewModel: ViewModel() {

    // Obtenemos los datos para el livedata y el viewModel
    val listaReservas: MutableLiveData<List<Reserva>> = MutableLiveData()
    val nombreJinete: MutableLiveData<String> = MutableLiveData()
    val nombreCaballo: MutableLiveData<String> = MutableLiveData()
    val fecha: MutableLiveData<String> = MutableLiveData()
    val movil: MutableLiveData<String> = MutableLiveData()
    val observacionesViewModel: MutableLiveData<String> = MutableLiveData()

    fun addReserva(reserva: Reserva) {
        val reservasActuales = listaReservas.value ?: emptyList()
        val nuevasReservas = reservasActuales.toMutableList().apply {
            add(reserva)
        }
        listaReservas.value = nuevasReservas
    }

    fun actualizarReserva(reserva: Reserva) {
        val reservasActuales = listaReservas.value ?: emptyList()
        val indice = reservasActuales.indexOfFirst { it.id == reserva.id }
        if (indice != -1) {
            val nuevasReservas = reservasActuales.toMutableList().apply {
                set(indice, reserva)
            }
            listaReservas.value = nuevasReservas
        }
    }



}