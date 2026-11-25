package com.example.ui.price

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.Establishment
import com.example.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPriceScreen(
    products: List<Product>,
    establishments: List<Establishment>,
    city: String,
    neighborhood: String,
    isLoggedIn: Boolean,
    onNavigateToLogin: () -> Unit,
    onAddPrice: (productId: String, establishmentId: String, price: Double, (Boolean, String?) -> Unit) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var selectedEstablishment by remember { mutableStateOf<Establishment?>(null) }
    var priceText by remember { mutableStateOf("") }
    var expandedProductDropdown by remember { mutableStateOf(false) }
    var expandedEstDropdown by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Cadastrar Preço",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Ajude a comunidade compartilhando preços atualizados de supermercados e mercadinhos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!isLoggedIn) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Login Necessário",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Para cadastrar ou atualizar preços, faça login com sua conta Google.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateToLogin) {
                        Text("Entrar com Google")
                    }
                }
            }
        } else {
            // Product dropdown selection
            ExposedDropdownMenuBox(
                expanded = expandedProductDropdown,
                onExpandedChange = { expandedProductDropdown = !expandedProductDropdown }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.name ?: "Selecione o produto...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProductDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("select_product_dropdown"),
                    shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedProductDropdown,
                    onDismissRequest = { expandedProductDropdown = false }
                ) {
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = { Text(product.name) },
                            onClick = {
                                selectedProduct = product
                                expandedProductDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Establishment dropdown selection
            ExposedDropdownMenuBox(
                expanded = expandedEstDropdown,
                onExpandedChange = { expandedEstDropdown = !expandedEstDropdown }
            ) {
                OutlinedTextField(
                    value = selectedEstablishment?.name ?: "Selecione o estabelecimento...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("select_establishment_dropdown"),
                    shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedEstDropdown,
                    onDismissRequest = { expandedEstDropdown = false }
                ) {
                    establishments.forEach { est ->
                        DropdownMenuItem(
                            text = { Text("${est.name} (${est.neighborhood})") },
                            onClick = {
                                selectedEstablishment = est
                                expandedEstDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Preço (R$)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("price_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val p = priceText.replace(",", ".").toDoubleOrNull()
                    if (selectedProduct == null || selectedEstablishment == null || p == null || p <= 0) {
                        errorMessage = "Preencha todos os campos corretamente."
                        return@Button
                    }
                    isSubmitting = true
                    errorMessage = null
                    successMessage = null

                    onAddPrice(selectedProduct!!.id, selectedEstablishment!!.id, p) { success, err ->
                        isSubmitting = false
                        if (success) {
                            successMessage = "Preço cadastrado com sucesso!"
                            priceText = ""
                            selectedProduct = null
                            selectedEstablishment = null
                        } else {
                            errorMessage = err ?: "Erro ao cadastrar preço."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_price_btn"),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Cadastrar Preço", fontWeight = FontWeight.Bold)
                }
            }

            if (successMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = successMessage!!, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
    }
}
