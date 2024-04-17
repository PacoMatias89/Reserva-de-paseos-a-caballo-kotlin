package com.example.reservapaseoacaballo

interface ReservaRepository {
    suspend fun obtenerReservas(): List<Reserva>
    suspend fun insertarReserva(reserva: Reserva)
    suspend fun buscarReservasPorFecha(fechaHora: String): List<Reserva>
    suspend fun buscarReservaPorCaballoYFecha(caballo: String, fechaHora: String): List<Reserva>
    suspend fun actualizarReserva(reserva: Reserva)
    suspend fun eliminarReserva(reserva: Reserva)



}