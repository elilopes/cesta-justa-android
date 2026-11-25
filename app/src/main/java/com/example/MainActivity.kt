package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.auth.AuthScreen
import com.example.ui.charts.ChartsHistoryScreen
import com.example.ui.components.LocationDialog
import com.example.ui.home.HomeScreen
import com.example.ui.lists.SmartListsScreen
import com.example.ui.map.EstablishmentsMapScreen
import com.example.ui.price.AddPriceScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CestaJustaViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CestaJustaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val city by viewModel.city.collectAsStateWithLifecycle()
                val neighborhood by viewModel.neighborhood.collectAsStateWithLifecycle()
                val products by viewModel.products.collectAsStateWithLifecycle()
                val establishments by viewModel.establishments.collectAsStateWithLifecycle()
                val prices by viewModel.prices.collectAsStateWithLifecycle()
                val shoppingLists by viewModel.shoppingLists.collectAsStateWithLifecycle()
                val promotions by viewModel.promotions.collectAsStateWithLifecycle()

                var currentTab by remember { mutableIntStateOf(0) }
                var showLocationDialog by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    viewModel.refreshAuth()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                                label = { Text("Início") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.AddCircle, contentDescription = "Cadastrar") },
                                label = { Text("Preço") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.List, contentDescription = "Listas") },
                                label = { Text("Listas") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = { Icon(Icons.Default.ShowChart, contentDescription = "Gráficos") },
                                label = { Text("Análise") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 4,
                                onClick = { currentTab = 4 },
                                icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") },
                                label = { Text("Mercados") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 5,
                                onClick = { currentTab = 5 },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                                label = { Text("Perfil") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            0 -> HomeScreen(
                                city = city,
                                neighborhood = neighborhood,
                                products = products,
                                establishments = establishments,
                                prices = prices,
                                promotions = promotions,
                                isLoggedIn = currentUser != null,
                                onNavigateToLogin = { currentTab = 5 },
                                onNavigateToEditPrice = { product -> currentTab = 1 },
                                onLocationClick = { showLocationDialog = true }
                            )
                            1 -> AddPriceScreen(
                                products = products,
                                establishments = establishments,
                                city = city,
                                neighborhood = neighborhood,
                                isLoggedIn = currentUser != null,
                                onNavigateToLogin = { currentTab = 5 },
                                onAddPrice = { pid, eid, price, callback ->
                                    viewModel.addPrice(pid, eid, price, callback)
                                }
                            )
                            2 -> SmartListsScreen(
                                shoppingLists = shoppingLists,
                                products = products,
                                establishments = establishments,
                                prices = prices,
                                isLoggedIn = currentUser != null,
                                onNavigateToLogin = { currentTab = 5 },
                                onSaveList = { list -> viewModel.saveShoppingList(list) {} },
                                onDeleteList = { id -> viewModel.deleteShoppingList(id) }
                            )
                            3 -> ChartsHistoryScreen(prices = prices)
                            4 -> EstablishmentsMapScreen(
                                establishments = establishments,
                                city = city,
                                neighborhood = neighborhood
                            )
                            5 -> {
                                if (currentUser == null) {
                                    AuthScreen(onAuthSuccess = {
                                        viewModel.refreshAuth()
                                    })
                                } else {
                                    ProfileScreen(
                                        user = currentUser,
                                        city = city,
                                        neighborhood = neighborhood,
                                        onLocationClick = { showLocationDialog = true },
                                        onSignOut = {
                                            viewModel.signOut()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (showLocationDialog) {
                    LocationDialog(
                        currentCity = city,
                        currentNeighborhood = neighborhood,
                        onDismiss = { showLocationDialog = false },
                        onSave = { c, n ->
                            viewModel.updateLocation(c, n)
                            showLocationDialog = false
                        }
                    )
                }
            }
        }
    }
}
