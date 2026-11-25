package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.repository.CestaJustaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CestaJustaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CestaJustaRepository(application)
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser

    private val _city = MutableStateFlow("São Paulo")
    val city: StateFlow<String> = _city

    private val _neighborhood = MutableStateFlow("Centro")
    val neighborhood: StateFlow<String> = _neighborhood

    private val _userConfig = MutableStateFlow(UserConfig())
    val userConfig: StateFlow<UserConfig> = _userConfig

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _establishments = MutableStateFlow<List<Establishment>>(emptyList())
    val establishments: StateFlow<List<Establishment>> = _establishments

    private val _prices = MutableStateFlow<List<PriceEntry>>(emptyList())
    val prices: StateFlow<List<PriceEntry>> = _prices

    private val _allPrices = MutableStateFlow<List<PriceEntry>>(emptyList())
    val allPrices: StateFlow<List<PriceEntry>> = _allPrices

    private val _shoppingLists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val shoppingLists: StateFlow<List<ShoppingList>> = _shoppingLists

    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())
    val promotions: StateFlow<List<Promotion>> = _promotions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            loadAppData()
        }
    }

    fun refreshAuth() {
        _currentUser.value = auth.currentUser
        val uid = auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                val config = repository.getUserConfig(uid)
                _userConfig.value = config
                _city.value = config.city
                _neighborhood.value = config.neighborhood
                loadAppDataForLocation(config.city, config.neighborhood, uid)
            }
        }
    }

    private fun loadAppData() {
        val uid = auth.currentUser?.uid ?: ""
        viewModelScope.launch {
            repository.observeProducts().collect { _products.value = it }
        }
        viewModelScope.launch {
            repository.observeEstablishments().collect { _establishments.value = it }
        }
        viewModelScope.launch {
            repository.observeAllPrices().collect { _allPrices.value = it }
        }
        if (uid.isNotEmpty()) {
            viewModelScope.launch {
                repository.observeShoppingLists(uid).collect { _shoppingLists.value = it }
            }
        }
        loadAppDataForLocation(_city.value, _neighborhood.value, uid)
    }

    private fun loadAppDataForLocation(c: String, n: String, uid: String) {
        viewModelScope.launch {
            repository.observePrices(c, n).collect { _prices.value = it }
        }
        viewModelScope.launch {
            repository.observePromotions(c).collect { _promotions.value = it }
        }
    }

    fun updateLocation(newCity: String, newNeighborhood: String) {
        _city.value = newCity
        _neighborhood.value = newNeighborhood
        val uid = auth.currentUser?.uid ?: ""
        viewModelScope.launch {
            val current = _userConfig.value
            val updated = current.copy(city = newCity, neighborhood = newNeighborhood)
            _userConfig.value = updated
            if (uid.isNotEmpty()) {
                repository.saveUserConfig(uid, updated)
            }
            loadAppDataForLocation(newCity, newNeighborhood, uid)
        }
    }

    fun toggleFavorite(productId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val current = _userConfig.value
            val favs = current.favoriteProductIds.toMutableList()
            if (favs.contains(productId)) favs.remove(productId) else favs.add(productId)
            val updated = current.copy(favoriteProductIds = favs)
            _userConfig.value = updated
            repository.saveUserConfig(uid, updated)
        }
    }

    fun addPrice(productId: String, establishmentId: String, price: Double, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "Usuário não autenticado")
            return
        }
        val product = _products.value.find { it.id == productId }
        val establishment = _establishments.value.find { it.id == establishmentId }
        if (product == null || establishment == null) {
            onResult(false, "Produto ou estabelecimento inválido")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val entry = PriceEntry(
                productId = productId,
                establishmentId = establishmentId,
                productName = product.name,
                establishmentName = establishment.name,
                price = price,
                userId = user.uid,
                userName = user.displayName ?: user.email ?: "Usuário Cesta Justa",
                city = _city.value,
                neighborhood = _neighborhood.value
            )
            val result = repository.addPriceEntry(entry)
            _isLoading.value = false
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.localizedMessage ?: "Erro ao salvar preço")
            }
        }
    }

    fun saveShoppingList(list: ShoppingList, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val listToSave = list.copy(userId = uid)
            val res = repository.saveShoppingList(listToSave)
            onResult(res.isSuccess)
        }
    }

    fun deleteShoppingList(listId: String) {
        viewModelScope.launch {
            repository.deleteShoppingList(listId)
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }
}
