package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Establishment
import com.example.model.PriceEntry
import com.example.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectorHeader(
    city: String,
    neighborhood: String,
    onLocationClick: () -> Unit
) {
    Surface(
        onClick = onLocationClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Sua Região",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$neighborhood, $city",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            TextButton(onClick = onLocationClick) {
                Text("Alterar")
            }
        }
    }
}

@Composable
fun LocationDialog(
    currentCity: String,
    currentNeighborhood: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var city by remember { mutableStateOf(currentCity) }
    var neighborhood by remember { mutableStateOf(currentNeighborhood) }

    val cities = listOf("São Paulo", "Rio de Janeiro", "Belo Horizonte", "Curitiba", "Brasília", "Salvador")
    val neighborhoods = listOf("Centro", "Liberdade", "Pinheiros", "Copacabana", "Savassi", "Batel", "Asa Sul", "Pituba")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Cidade e Bairro") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Cidade", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Bairro", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = { neighborhood = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(city, neighborhood) }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceComparisonBottomSheet(
    product: Product,
    prices: List<PriceEntry>,
    establishments: List<Establishment>,
    onDismiss: () -> Unit
) {
    val productPrices = prices.filter { it.productId == product.id }
        .sortedBy { it.price }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Preços em tempo real nos supermercados e mercadinhos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (productPrices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum preço cadastrado para este produto ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                productPrices.forEachIndexed { index, entry ->
                    val est = establishments.find { it.id == entry.establishmentId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.establishmentName.ifEmpty { est?.name ?: "Estabelecimento" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = est?.address ?: "Bairro: ${entry.neighborhood}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "R$ %.2f".format(entry.price),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
