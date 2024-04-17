package com.example.reservapaseoacaballo

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Reserva::class], version = 1)
abstract class DBReserva: RoomDatabase(){
    abstract fun daoReserva(): DaoReserva
}