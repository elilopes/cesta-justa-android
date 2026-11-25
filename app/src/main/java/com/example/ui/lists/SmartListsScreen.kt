package com.example.ui.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Establishment
import com.example.model.PriceEntry
import com.example.model.Product
import com.example.model.ShoppingList
import com.example.model.ShoppingListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartListsScreen(
    shoppingLists: List<ShoppingList>,
    products: List<Product>,
    establishments: List<Establishment>,
    prices: List<PriceEntry>,
    isLoggedIn: Boolean,
    onNavigateToLogin: () -> Unit,
    onSaveList: (ShoppingList) -> Unit,
    onDeleteList: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var listNameInput by remember { mutableStateOf("") }
    var selectedListForComparison by remember { mutableStateOf<ShoppingList?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Listas de Compras Inteligentes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sincronizadas na nuvem • Encontre o mercado mais barato",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isLoggedIn) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("create_list_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nova Lista")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Login Necessário para Listas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Para criar e sincronizar suas listas de compras na nuvem, faça login com sua conta Google.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToLogin) {
                            Text("Entrar com Google")
                        }
                    }
                }
            }
        } else if (shoppingLists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhuma lista de compras criada ainda.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(shoppingLists) { list ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = list.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { onDeleteList(list.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = MaterialTheme.colorScheme.error)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            list.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "• ${item.productName} (${item.quantity} ${item.unit})")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { selectedListForComparison = list },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Encontrar melhor mercado para esta lista")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var selectedProductIds by remember { mutableStateOf(setOf<String>()) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nova Lista Inteligente") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    OutlinedTextField(
                        value = listNameInput,
                        onValueChange = { listNameInput = it },
                        label = { Text("Nome da Lista") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Selecione os itens da cesta básica:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(products) { prod ->
                            val isSelected = selectedProductIds.contains(prod.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(prod.name, fontWeight = FontWeight.Medium)
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedProductIds = if (checked) {
                                            selectedProductIds + prod.id
                                        } else {
                                            selectedProductIds - prod.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (listNameInput.isNotBlank() && selectedProductIds.isNotEmpty()) {
                            val items = selectedProductIds.mapNotNull { id ->
                                val p = products.find { it.id == id }
                                p?.let { ShoppingListItem(productId = it.id, productName = it.name, quantity = 1.0, unit = it.unit) }
                            }
                            val newList = ShoppingList(name = listNameInput, items = items)
                            onSaveList(newList)
                            listNameInput = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Salvar Lista")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (selectedListForComparison != null) {
        val list = selectedListForComparison!!
        val estTotals = establishments.map { est ->
            var total = 0.0
            for (li in list.items) {
                val matchPrice = prices.find { it.establishmentId == est.id && it.productId == li.productId }
                if (matchPrice != null) {
                    total += matchPrice.price * li.quantity
                } else {
                    val pObj = products.find { it.id == li.productId }
                    total += (pObj?.averagePrice ?: 10.0) * li.quantity
                }
            }
            est to total
        }.sortedBy { it.second }

        AlertDialog(
            onDismissRequest = { selectedListForComparison = null },
            title = { Text("Melhor Mercado para: ${list.name}") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("O supermercado ou mercadinho com o menor custo total para sua lista:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    estTotals.forEachIndexed { index, (est, total) ->
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
                                Column {
                                    Text(est.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(est.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "R$ %.2f".format(total),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedListForComparison = null }) {
                    Text("Fechar")
                }
            }
        )
    }
}
