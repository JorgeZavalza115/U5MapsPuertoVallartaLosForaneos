package mx.edu.ittepic.maps_puertovallarta

import com.google.firebase.firestore.GeoPoint

class Data {
    var id : String = ""
    var nombre : String = ""
    var pos1 : GeoPoint = GeoPoint( 0.0, 0.0 )
    var pos2 : GeoPoint = GeoPoint( 0.0, 0.0 )

    fun estoyEn ( posActual : GeoPoint ) : Boolean {
        if ( posActual.latitude <= pos1.latitude && posActual.latitude >= pos2.latitude ) {
            if ( invertir( posActual.longitude ) <= invertir( pos1.longitude ) &&
                invertir( posActual.longitude ) >= invertir( pos2.longitude ) ) {
                return true
            }
        }
        return false
    }

    private fun invertir ( valor : Double ) : Double {
        return valor * -1
    }
}