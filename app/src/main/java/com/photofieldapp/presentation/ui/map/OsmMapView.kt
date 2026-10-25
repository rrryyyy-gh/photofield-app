package com.photofieldapp.presentation.ui.map

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    markers: List<PhotoMarker> = emptyList(),
    onMarkerClick: (PhotoMarker) -> Unit = {},
    cameraPosition: CameraPosition? = null,
    onCameraChange: (CameraPosition) -> Unit = {}
) {
    val context = LocalContext.current
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val myLocationOverlayRef = remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    val infoWindowRef = remember { mutableStateOf<InfoWindow?>(null) }

    // Initialize osmdroid configuration once
    DisposableEffect(Unit) {
        Configuration.getInstance().apply {
            setUserAgentValue("PhotoField/1.0")
        }
        onDispose {
            // Cleanup if needed
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val mapView = MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK) // Default OpenStreetMap tile source
                setBuiltInZoomControls(true)
                setMultiTouchControls(true)
                // Disable hardware acceleration for better compatibility
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            }

            // My Location overlay
            val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), mapView)
            myLocationOverlay.enableMyLocation()
            myLocationOverlay.enableFollowLocation()
            mapView.overlays.add(myLocationOverlay)
            myLocationOverlayRef.value = myLocationOverlay

            // Custom info window layout
            val infoWindow = CustomInfoWindow(mapView)
            infoWindowRef.value = infoWindow

            mapViewRef.value = mapView
            mapView
        },
        update = { mapView ->
            // Update markers
            updateMarkers(mapView, markers, onMarkerClick, infoWindowRef.value)

            // Update camera position
            cameraPosition?.let { pos ->
                val controller = mapView.controller
                val newCenter = GeoPoint(pos.target.latitude, pos.target.longitude)
                if (mapView.mapCenter != newCenter || mapView.zoomLevel != pos.zoom) {
                    controller.animateTo(newCenter)
                    controller.setZoom(pos.zoom)
                }
            }

            // Set map change listener for camera updates
            mapView.addOnMapListener { _, _, _, _ ->
                val controller = mapView.controller
                val center = mapView.mapCenter
                val zoom = mapView.zoomLevel
                onCameraChange(CameraPosition(
                    target = LatLng(center.latitude, center.longitude),
                    zoom = zoom
                ))
            }
        }
    )
}

private fun updateMarkers(
    mapView: MapView,
    markers: List<PhotoMarker>,
    onMarkerClick: (PhotoMarker) -> Unit,
    infoWindow: InfoWindow?
) {
    // Remove old markers (keep my location overlay and other non-marker overlays)
    mapView.overlays.removeAll { it is Marker }

    markers.forEach { marker ->
        val osMarker = Marker(mapView).apply {
            position = GeoPoint(marker.position.latitude, marker.position.longitude)
            title = marker.title
            snippet = marker.snippet
            // Use default marker icon, can be customized
            // icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_marker)
            
            // Set info window
            infoWindow?.let { setInfoWindow(it) }
            
            // Handle click
            onMarkerClickListener = { _, m ->
                onMarkerClick(marker)
                true
            }
            
            // Tag for identification
            tag = marker.id
        }
        mapView.overlays.add(osMarker)
    }
    mapView.invalidate()
}

// Custom InfoWindow for better styling
class CustomInfoWindow(mapView: MapView) : BasicInfoWindow(R.layout.osm_bonuspack_bubble, mapView) {
    
    override fun onOpen(marker: Marker) {
        super.onOpen(marker)
        // Customize appearance if needed
    }
}

// Simple marker layout for info window (fallback if layout not found)
private class SimpleInfoWindow(context: Context, mapView: MapView) : InfoWindow(R.layout.osm_bonuspack_bubble, mapView) {
    private val titleView: TextView
    private val snippetView: TextView
    
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.osm_bonuspack_bubble, null)
        titleView = view.findViewById(android.R.id.title)
        snippetView = view.findViewById(android.R.id.snippet)
        this.view = view
    }
    
    override fun onOpen(marker: Marker?) {
        marker?.let {
            titleView.text = it.title
            snippetView.text = it.snippet
        }
        super.onOpen(marker)
    }
}

// Data classes for map
data class PhotoMarker(
    val id: Long,
    val position: LatLng,
    val title: String,
    val snippet: String,
    val photo: Photo
)

data class LatLng(
    val latitude: Double,
    val longitude: Double
)

data class CameraPosition(
    val target: LatLng,
    val zoom: Double = 15.0
)

// Domain model reference (import from domain)
typealias Photo = com.photofieldapp.domain.model.Photo