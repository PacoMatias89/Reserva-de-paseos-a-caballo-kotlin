package com.example.reservapaseoacaballo

class ReservaRepositoryImpl (private val daoReserva: DaoReserva): ReservaRepository{
    override suspend fun obtenerReservas(): List<Reserva> {
        return daoReserva.obtenerReservas()
    }

    override suspend fun insertarReserva(reserva: Reserva) {
        daoReserva.insertarReserva(reserva)
    }

    override suspend fun buscarReservasPorFecha(fechaHora: String): List<Reserva> {
        return daoReserva.buscarReservasPorFecha(fechaHora)
    }

    override suspend fun buscarReservaPorCaballoYFecha(caballo: String,fechaHora: String
    ): List<Reserva> {
        return  daoReserva.buscarReservaPorCaballoYFecha(caballo,fechaHora)
    }

    override suspend fun actualizarReserva(reserva: Reserva) {
        daoReserva.actualizarReserva(
            reserva.nombreJinete,
            reserva.nombreCaballo,
            reserva.fecha,
            reserva.movil,
            reserva.observaciones,
            reserva.id
        )
    }

    override suspend fun eliminarReserva(reserva: Reserva) {
        daoReserva.eliminarReserva(reserva.id)
    }


}