package com.example.reservapaseoacaballo


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.reservapaseoacaballo.databinding.ActivityRegistraJineteBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegistrarJinete : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private var selectedYear: Int = 0
    private  var selectedMonth: Int = 0
    private  var selectedDay: Int = 0
    private  var selectedHour: Int = 0
    private var selectedMinute: Int = 0
    private var selectedPosition: Int = -1
    lateinit var binding: ActivityRegistraJineteBinding
    lateinit var room: DBReserva
    lateinit var reserva: Reserva
    lateinit var viewModel: ViewModel
    lateinit var reservaRepository: ReservaRepository
    var listaReservas: MutableList<Reserva> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistraJineteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        room = Room.databaseBuilder(this, DBReserva::class.java, "reservas").build()
        reservaRepository = ReservaRepositoryImpl(room.daoReserva())
        // Obtener la posición del elemento seleccionado desde el intent
        selectedPosition = intent.getIntExtra("position", -1)

        // Configuración del spinner
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.nombre_caballos, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spNombreCaballo.adapter = adapter

        binding.tvFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val yearRegister = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val tempCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }

                val dayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK)

                // Verificar si el día es Sábado o Domingo
                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    // La fecha seleccionada es sábado o domingo, se puede proceder a elegir la hora
                    val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                        if (selectedHour in 10..11) {
                            val selectedDateTime = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth, selectedHour, selectedMinute)
                            }
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val formattedDateTime = dateFormat.format(selectedDateTime.time)
                            binding.tvFecha.setText( formattedDateTime)
                        } else {
                            Toast.makeText(this, "Hora de reserva no válida", Toast.LENGTH_SHORT).show()
                        }
                    }, 10, 0, true)
                    timePickerDialog.show()
                } else {
                    Toast.makeText(this, "Por favor, seleccione un sábado o domingo", Toast.LENGTH_LONG).show()
                }
            }, yearRegister, month, day)

            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()

        }
        binding.btnAddUpdate.setOnClickListener {
            if (binding.etNombreJinete.text.isNullOrEmpty() ||
                binding.tvFecha.text.isNullOrEmpty() || binding.etMovil.text.isNullOrEmpty() || binding.etObservaciones.text.isNullOrEmpty()
            ) {
                Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nombreJinete = binding.etNombreJinete.text.toString()
            val nombreCaballo = binding.spNombreCaballo.selectedItem.toString()
            val movil = binding.etMovil.text.toString()
            val fecha = binding.tvFecha.text.toString()
            val observaciones = binding.etObservaciones.text.toString()

            viewModel.nombreJinete.value = nombreJinete
            viewModel.nombreCaballo.value = nombreCaballo
            viewModel.fecha.value = fecha
            viewModel.movil.value = movil
            viewModel.observacionesViewModel.value = observaciones

            //Verificamos que el caballo no tenga un paseo programado en segundo plano
            lifecycleScope.launch {
                val reservaExistente = reservaRepository.buscarReservaPorCaballoYFecha(nombreCaballo, fecha)

                //verificamos si tiene o no tiene paseo
                if (reservaExistente.isNotEmpty()) {
                    Toast.makeText(this@RegistrarJinete, "Ya existe una reserva para este caballo y fecha", Toast.LENGTH_SHORT).show()
                }else{
                    //Si no tiene paseo programado, se registra la reserva
                    if (binding.btnAddUpdate.text.equals("Agregar")) {
                        // Agregar nueva reserva
                        val nuevaReserva = Reserva(
                            0,
                            nombreJinete,
                            nombreCaballo,
                            fecha,
                            movil,
                            observaciones
                        )
                        viewModel.addReserva(nuevaReserva)
                        lifecycleScope.launch {
                            reservaRepository.insertarReserva(nuevaReserva)
                        }
                        listaReservas.add(nuevaReserva)
                        actualizarRecyclerView()
                        enviarMensaje(nuevaReserva)
                        vaciarCampos()


                    }
                }
            }

            if (nombreJinete.isNullOrEmpty() || nombreCaballo.isNullOrEmpty() || fecha.isNullOrEmpty() || movil.isNullOrEmpty() || observaciones.isNullOrEmpty()) {
                Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }




        }
    }
    private fun actualizarRecyclerView() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("actualizar", true)
        startActivity(intent)
        finish()
    }



    private fun enviarMensaje(reserva: Reserva) {
        val mensaje = "¡Gracias por realizar tu reserva!\n\n" +
                "Detalles de la reserva:\n" +
                "Nombre del caballo: ${reserva.nombreCaballo}\n" +
                "Fecha del paseo: ${reserva.fecha}\n"

        val phoneNumber = reserva.movil

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data =
            Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(mensaje)}")
        startActivity(intent)
    }

    fun vaciarCampos() {
        binding.etNombreJinete.setText("")
        binding.tvFecha.setText("")
        binding.etMovil.setText("")
        binding.etObservaciones.setText("")
        binding.etMovil.setText("")

        if (binding.btnAddUpdate.text.equals("Actualizar")) {
            binding.btnAddUpdate.setText("agregar")
            binding.etMovil.isEnabled = true
            binding.spNombreCaballo.isEnabled = true
            binding.etNombreJinete.isEnabled = true
            binding.etObservaciones.isEnabled = true
        }

    }




    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.set(year, month, dayOfMonth)

        val currentCalendar = Calendar.getInstance()

        if (selectedCalendar.before(currentCalendar)) {
            Toast.makeText(this, "No se puede seleccionar una fecha pasada", Toast.LENGTH_SHORT).show()
            return
        }

        selectedYear = year
        selectedMonth = month
        selectedDay = dayOfMonth

        val availableHours = mutableListOf<String>()
        if (selectedCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)) {
            val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
            if (currentHour < 10) {
                availableHours.add(getFormattedTime(10, 0))
                availableHours.add(getFormattedTime(11, 0))

            } else if (currentHour < 11) {
                availableHours.add(getFormattedTime(11, 0))
                availableHours.add(getFormattedTime(10, 0))
            } else {
                Toast.makeText(this, "No se pueden hacer reservas para el día actual después de las 11:00", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            availableHours.add(getFormattedTime(10, 0))
            availableHours.add(getFormattedTime(11, 0))

        }

        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            val selectedHourText = getFormattedTime(hourOfDay, minute)
            if (!availableHours.contains(selectedHourText)) {
                Toast.makeText(this, "Hora no válida", Toast.LENGTH_SHORT).show()
                return@TimePickerDialog
            }

            selectedHour = hourOfDay
            selectedMinute = minute

            binding.tvFecha.setText(String.format("%02d/%02d/%04d %02d:%02d", selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute))
        }, 0, 0, true)

        val numberPickerId = Resources.getSystem().getIdentifier("hour", "id", "android")
        val numberPicker = timePickerDialog.findViewById<NumberPicker>(numberPickerId)
        numberPicker?.minValue = 0
        numberPicker?.maxValue = availableHours.size - 1
        numberPicker?.displayedValues = availableHours.toTypedArray()

        timePickerDialog.show()
    }

    private fun getFormattedTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }



}