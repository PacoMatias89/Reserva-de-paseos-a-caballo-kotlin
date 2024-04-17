package com.example.reservapaseoacaballo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "reservas")
data class Reserva (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var nombreJinete: String,
    var nombreCaballo: String,
    var fecha: String,
    var movil: String,
    var observaciones: String

):Serializable