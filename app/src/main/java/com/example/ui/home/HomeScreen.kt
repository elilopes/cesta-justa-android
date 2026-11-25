package com.example.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Establishment
import com.example.model.PriceEntry
import com.example.model.Product
import com.example.model.Promotion
import com.example.ui.components.LocationSelectorHeader
import com.example.ui.components.PriceComparisonBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    city: String,
    neighborhood: String,
    products: List<Product>,
    establishments: List<Establishment>,
    prices: List<PriceEntry>,
    promotions: List<Promotion>,
    isLoggedIn: Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToEditPrice: (Product) -> Unit,
    onLocationClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProductForComparison by remember { mutableStateOf<Product?>(null) }
    var selectedProductForAction by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LocationSelectorHeader(
            city = city,
            neighborhood = neighborhood,
            onLocationClick = onLocationClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_search_input"),
                    placeholder = { Text("Pesquisar produto da cesta básica...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            if (promotions.isNotEmpty() && searchQuery.isEmpty()) {
                item {
                    Text(
                        text = "Promoções na Região",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(promotions) { promo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = promo.establishmentName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = promo.productName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "R$ %.2f".format(promo.discountPrice),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (promo.originalPrice > promo.discountPrice) {
                                    Text(
                                        text = "R$ %.2f".format(promo.originalPrice),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Menores Preços da Cesta Básica",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(filteredProducts) { product ->
                val prodPrices = prices.filter { it.productId == product.id }
                val lowestPriceEntry = prodPrices.minByOrNull { it.price }
                val lowestPrice = lowestPriceEntry?.price ?: product.averagePrice
                val storeName = lowestPriceEntry?.establishmentName ?: "Média da região"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedProductForAction = product },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Categoria: ${product.category} • ${product.unit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "R$ %.2f".format(lowestPrice),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Menor preço",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📍 $storeName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = { selectedProductForComparison = product },
                                modifier = Modifier.testTag("compare_prices_btn_${product.id}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Ver outros preços")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (selectedProductForComparison != null) {
        PriceComparisonBottomSheet(
            product = selectedProductForComparison!!,
            prices = prices,
            establishments = establishments,
            onDismiss = { selectedProductForComparison = null }
        )
    }

    if (selectedProductForAction != null) {
        ProductActionBottomSheet(
            product = selectedProductForAction!!,
            prices = prices,
            establishments = establishments,
            isLoggedIn = isLoggedIn,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToEditPrice = onNavigateToEditPrice,
            onDismiss = { selectedProductForAction = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductActionBottomSheet(
    product: Product,
    prices: List<PriceEntry>,
    establishments: List<Establishment>,
    isLoggedIn: Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToEditPrice: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val prodPrices = prices.filter { it.productId == product.id }
    val lowestPriceEntry = prodPrices.minByOrNull { it.price }
    val lowestPrice = lowestPriceEntry?.price ?: product.averagePrice
    val storeName = lowestPriceEntry?.establishmentName ?: "Média da região"
    val est = establishments.find { it.id == lowestPriceEntry?.establishmentId }
    val address = est?.address ?: "Endereço não cadastrado"

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Menor Preço: R$ %.2f no(a) $storeName".format(lowestPrice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Divider()

            // 1. Abrir no mapa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val gmmIntentUri = Uri.parse("geo:${est?.latitude ?: -23.5505},${est?.longitude ?: -46.6333}?q=${Uri.encode(address)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        context.startActivity(mapIntent)
                        onDismiss()
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Abrir no mapa", style = MaterialTheme.typography.bodyLarge)
            }

            // 2. Copiar endereço
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clipboardManager.setText(AnnotatedString(address))
                        onDismiss()
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Copiar endereço", style = MaterialTheme.typography.bodyLarge)
            }

            // 3. Compartilhar endereço
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Endereço do(a) $storeName: $address")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, null))
                        onDismiss()
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Compartilhar endereço", style = MaterialTheme.typography.bodyLarge)
            }

            // 4. Copiar produto e valor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val text = "${product.name}: R$ %.2f em $storeName".format(lowestPrice)
                        clipboardManager.setText(AnnotatedString(text))
                        onDismiss()
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Copiar produto e valor", style = MaterialTheme.typography.bodyLarge)
            }

            // 5. Compartilhar produto e valor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val text = "Cesta Justa 🛒\n${product.name} por apenas R$ %.2f no(a) $storeName! Endereço: $address".format(lowestPrice)
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, null))
                        onDismiss()
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Compartilhar produto e valor", style = MaterialTheme.typography.bodyLarge)
            }

            // 6. Editar o valor (solicita login)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        if (!isLoggedIn) {
                            onNavigateToLogin()
                        } else {
                            onNavigateToEditPrice(product)
                        }
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (isLoggedIn) "Editar o valor" else "Editar o valor (Requer Login)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
