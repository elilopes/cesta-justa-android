package com.example.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.Establishment
import com.google.android.gms.location.LocationServices
        import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstablishmentsMapScreen(
    establishments: List<Establishment>,
    city: String,
    neighborhood: String
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLon by remember { mutableStateOf<Double?>(null) }
    var gpsStatus by remember { mutableStateOf("GPS Desativado") }
    var isLocating by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isLocating = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    isLocating = false
                    if (location != null) {
                        userLat = location.latitude
                        userLon = location.longitude
                        gpsStatus = "GPS Ativo (Próximos em tempo real)"
                    } else {
                        gpsStatus = "Localização indisponível. Usando região padrão."
                    }
                }.addOnFailureListener {
                    isLocating = false
                    gpsStatus = "Erro ao obter GPS"
                }
            } catch (e: SecurityException) {
                isLocating = false
                gpsStatus = "Permissão negada"
            }
        } else {
            gpsStatus = "Permissão de localização negada"
        }
    }

    val filteredEsts = establishments.filter {
        it.name.contains(query, ignoreCase = true) ||
                it.address.contains(query, ignoreCase = true) ||
                it.neighborhood.contains(query, ignoreCase = true)
    }.sortedBy { est ->
        if (userLat != null && userLon != null) {
            calculateDistance(userLat!!, userLon!!, est.latitude, est.longitude)
        } else {
            0.0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Supermercados e Mercadinhos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Encontre estabelecimentos próximos em tempo real e calcule rotas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // GPS Action Button
        Button(
            onClick = {
                val permissionCheck = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    isLocating = true
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        isLocating = false
                        if (location != null) {
                            userLat = location.latitude
                            userLon = location.longitude
                            gpsStatus = "GPS Ativo (Próximos em tempo real)"
                        } else {
                            gpsStatus = "Localização indisponível"
                        }
                    }
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.GpsFixed, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isLocating) "Obtendo GPS..." else "Ativar GPS / Buscar Próximos")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = gpsStatus,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("establishment_search_input"),
            placeholder = { Text("Pesquisar rua, bairro ou mercado...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredEsts) { est ->
                val distanceKm = if (userLat != null && userLon != null) {
                    calculateDistance(userLat!!, userLon!!, est.latitude, est.longitude)
                } else null

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                               tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = est.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = est.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Bairro: ${est.neighborhood} - ${est.city}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (distanceKm != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📍 Aprox. %.1f km de você".format(distanceKm),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${est.latitude},${est.longitude}&mode=d")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    // Fallback to browser or generic geo intent
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${est.latitude},${est.longitude}"))
                                    context.startActivity(webIntent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calcular Rota")
                        }
                    }
                }
            }
        }
    }
}

// Haversine formula for distance in km
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
