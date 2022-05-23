package mx.edu.ittepic.maps_puertovallarta

import android.app.ProgressDialog
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import mx.edu.ittepic.maps_puertovallarta.databinding.ActivityEstatuaBinding
import java.io.File

class EstatuaActivity : AppCompatActivity() {
    lateinit var binding: ActivityEstatuaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstatuaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener extras
        var idEstatua = intent.extras!!.getString("id")!!

        // Obtener datos del Firestore
        val baseRemota = FirebaseFirestore.getInstance()
        baseRemota.collection("GeoPoints").document(idEstatua)
            .get()
            .addOnSuccessListener {
                binding.nombre.setText( it.getString("nombre") )
                binding.fecha.setText( it.getString("fecha") )
                binding.artista.setText( it.getString("artista") )
                //binding.historia.setText( it.getString("historia") )
            }
            .addOnFailureListener {
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Se ha producido un error al intentar recuperar la información. Compruebe su conexión a internet")
                    .show()
            }

        // Obtener imagen del Storage
        val progress = ProgressDialog(this)
        progress.setMessage("Cargando información...")
        progress.setCancelable(false)
        progress.show()
        val storage = FirebaseStorage.getInstance().reference.child("estatuas/${ idEstatua }.jpg")
        val localFile = File.createTempFile("tempImage", "jpg")
        storage.getFile(localFile).addOnSuccessListener {
            if ( progress.isShowing ) {
                progress.dismiss()
            }
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            binding.imagen.setImageBitmap(bitmap)
        }.addOnFailureListener {
            if ( progress.isShowing ) {
                progress.dismiss()
            }
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Se ha producido un error al cargar la imagen")
                .show()
        }

        // Botoncito para regreesar
        binding.regresar.setOnClickListener {
            finish()
        }
    }
}