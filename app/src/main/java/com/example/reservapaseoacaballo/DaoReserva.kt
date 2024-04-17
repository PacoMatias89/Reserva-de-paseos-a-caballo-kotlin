package com.example.reservapaseoacaballo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DaoReserva {
    @Query("SELECT * FROM reservas ORDER BY fecha DESC")
    suspend fun obtenerReservas(): MutableList<Reserva>

    @Insert
    suspend fun insertarReserva(reserva: Reserva)

    @Query("SELECT * FROM reservas WHERE fecha = :fechaHora ORDER BY fecha ASC")
    suspend fun buscarReservasPorFecha(fechaHora: String): MutableList<Reserva>

    @Query("SELECT * FROM reservas WHERE nombreCaballo = :nombreCaballo AND fecha = :fechaHora LIMIT 1")
    suspend fun buscarReservaPorCaballoYFecha(nombreCaballo: String, fechaHora: String): MutableList<Reserva>


    @Query("UPDATE reservas SET nombreJinete = :nombreJinete, nombreCaballo = :nombreCaballo, fecha = :fecha, movil= :movil, observaciones = :observaciones WHERE id = :id")
    suspend fun actualizarReserva(nombreJinete: String, nombreCaballo: String, fecha: String, movil:String,  observaciones: String, id: Int)

    @Query("DELETE FROM reservas WHERE id = :id")
    suspend fun eliminarReserva(id: Int)

}