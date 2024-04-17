# App de registro de reserva para club hípico

Esta aplicación trata sobre cómo poder reservar un paseo a caballo.
La aplicación consta de una entrada de datos desde el nombre del jinete (En el ejercicio dice que se pueden agregar más de uno. me se pasó, pero eso)
con agregar unos campos más y agregándolo a la base de datos estáría todo corregido, también tiene un campo para agregar el nombre del caballo, otro
para la fecha y hora del paseo, en mi caso he agregado tanto la fecha como la hora en el mismo EditText, entonce esto quiere decir que a la hora de buscar la reserva tendrá que ser tanto por fecha como por hora, ya que puede hacer programadas más de un paseo, y así poder tener controlados los paseos de ese día y esa hora, por último a camposo de introducción de campos se refiere tiene un campo para agregar una obsersación, en mi app es obligatorio rellenar todos los campos si queremos reservar el paseo. 
Las horas de paseo son de 17:00 a 20:00, está bien a la hora de seleccionar la hora ya que no te va a dejar reservar en otra franja horaria, pero lo suyo hubiera sido que a la hora de elegir las horas sólo te diera esas opciones, pero aún así, queremos hacer el paseo a las 2 de la tarde, no nos va a dejar y nos salta un Toast diciendo que la hora no es válida. La fecha en cambio nunca nos va a dejar elegior una fecha en el pasado.


## Código significativo (Kotlin).

En esta parte voy a hacer una breve explicación del código más significativa y más nuevo que hemos visto hasta la fecha.


lo primero que tenemos que tener en cuenta para crear una pequeña aplicación de este tipo, la cual está conectada a una base de datos interna, crearnos el archivo correspondiente que hará de base de datos, en nuestro caso y como en otros muchos es una clase abstracta que se llama DBReserva:

```
@Database(entities = [Reserva::class], version = 1)
abstract class DBReserva: RoomDatabase(){
    abstract fun daoReserva(): DaoReserva
}
``` 
Cómo podremos observar es una clase en la cual hace referencia a la clase que se encargará de construir la tabla en nuestra base de datos. la cual como bien pone ahí se llama reserva. Es sumamente importante tener las anotaciones bien puestas sino tendremos problema. En este caso hemos usado liveData, que lo que va hacer es que cuando haya un cambio en el código que afecte a la base de datos "avisará" a la room diciendo que ha habido un cambio.

Normalmente el cambio se va a crear o a realizar mediante una clase de tipo interface donde colocaremos todas las querys que necesite. Si por lo que tenemos que cambiar o agregar una query normalemente es el liveDate el que avisa que tal cosa se ha cambiado.

### Clase Reservar (Tabla):

```
@Entity(tableName = "reservas")
data class Reserva(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var nombreJinete: String,
    var nombreCaballo: String,
    var fecha: String,
    var movil: String,
    var observaciones: String

)
``` 
Como podremos ver es una data class, esta clase es similar a un pojo en java, lo que va a guardar son los datos de nuestro objeto.

### Clase DaoReserva (Donde se ubica el CRUD)

```
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

```

Como he mencionado anteriormente, es muy importante tener las anotaciones bien puestas. Como podremos ver tenemos varias anotaciones que sirven para cadao cosa

```
@Query => este tipo de anotaciones hará mención a la creación de una consulta a la base de datos

@Insert => Como su nombre indica, inserta valores a la baes de datos y no hace falta crearse una query, aunque si lo deseamos, lo habría problema.


```

Otro código que creo que es interesante de mencionar de nuestra app, es que en el ejercicio nos pedía que se enviara un mensaje vía WhatsApp con cierto mensaje de confirmación. En mi caso no tengo instalado whatsApp en el emulador, pero en el terminal físico si funciona correctamente.

### Código WhatsApp

```
private fun enviarMensaje(reserva: Reserva) {
        val mensaje = "¡Gracias por realizar tu reserva!\n\n" +
                "Detalles de la reserva:\n" +
                "Nombre del caballo: ${reserva.nombreCaballo}\n" +
                "Fecha del paseo: ${reserva.fecha}\n"

        val phoneNumber = reserva.movil

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(mensaje)}")
        startActivity(intent)
    }

```
Este código va introducido en el método de agregar usuario, cuando agregamos el usuario le pasamos la reserva y le mandamos el mensaje.
También puedes observar que he obtenido la api de whatsApp, que lo que hace es que cuando una vez se agrega escribe la app ese mensaje al número de teléfono que hayamos puesto y somos nosotros los que le tenemos que dar a enviar, ya que si queríamos que la app hiciera todo eso, tendríamos que haver hecho una solicitud a WhatsApp Bussines y que nos la acepten como empresa registrada, total un "follón" y vale pasta.


### Errores
El único error que he contemplado, ya que los inputs son todos string a excepción de la fecha y hora, ha sido que 
si quiero escoger un caballo que ese mismo caballo no tenga un paseo ya programado en una misma fecha y hora. 
