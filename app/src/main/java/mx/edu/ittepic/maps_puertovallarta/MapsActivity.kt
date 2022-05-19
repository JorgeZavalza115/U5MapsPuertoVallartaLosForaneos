package mx.edu.ittepic.maps_puertovallarta

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import mx.edu.ittepic.maps_puertovallarta.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locacion : LocationManager
    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    val baseRemota = FirebaseFirestore.getInstance()
    var posicion = ArrayList<Data>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableLocation()

        // Obtener datos desde Firestore
        baseRemota.collection("GeoPoints")
            .addSnapshotListener { query, error ->
                // Si falla
                if ( error != null ) {
                    AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage( error.message )
                        .show()
                    return@addSnapshotListener
                }

                // Limpiar lista
                var resultado = ""
                posicion.clear()

                // Datos
                for ( document in query!! ) {
                    var data = Data()
                    data.nombre = document.getString("nombre").toString()
                    data.pos1 = document.getGeoPoint("pos1")!!
                    data.pos2 = document.getGeoPoint("pos2")!!
                    posicion.add(data)
                }

            }

        // Invoca al cambio de posición
        locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var oyente = Oyente(this )
        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 01f, oyente)

    }

    // Checa los permisos, regresa un true o un false
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    // Activa la localización
    private fun enableLocation() {
        if ( !:: mMap.isInitialized ) return
        if ( isLocationPermissionGranted() ) {
            mMap.isMyLocationEnabled = true // Probablemente te marque error aquí, Android Studio está loco, se puede ejecutar bien
        } else {
            requestLocationPermission()
        }
    }

    // Solicita los permisos
    private fun requestLocationPermission() {
        if ( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_LONG)
                .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }
    }

    // Resultado de solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    this,
                    "Para activar la localización ve a ajustes y acepta los permisos",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
            else -> {}
        }
    }

    fun activarFloating() {
        binding.floating.visibility = View.VISIBLE
    }

    fun desactivarFloating() {
        binding.floating.visibility = View.GONE
    }

}

// Checa si se encuentra dentro de un area de estatua
class Oyente( puntero : MapsActivity ) : LocationListener {
    var p = puntero
    override fun onLocationChanged( location : Location ) {
        var geoPosicion = GeoPoint( location.latitude, location.longitude )
            p.desactivarFloating()
        for ( item in p.posicion ) {
            if ( item.estoyEn( geoPosicion ) ) {
                // Aquí se activaria el botoncito
                p.activarFloating()
            }
        }
    }


}