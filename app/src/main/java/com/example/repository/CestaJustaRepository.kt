package com.example.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CestaJustaRepository(context: Context) {
    private val dbId = context.getString(R.string.firestore_database_id)
    private val firestore = FirebaseFirestore.getInstance(dbId)

    private val productsRef = firestore.collection("products")
    private val establishmentsRef = firestore.collection("establishments")
    private val pricesRef = firestore.collection("prices")
    private val listsRef = firestore.collection("shopping_lists")
    private val promotionsRef = firestore.collection("promotions")
    private val usersRef = firestore.collection("users")

    suspend fun seedInitialDataIfNeeded() {
        try {
            val prodSnapshot = productsRef.limit(1).get().await()
            if (prodSnapshot.isEmpty) {
                val sampleProducts = listOf(
                    Product(name = "Arroz Tio João 5kg", category = "Grãos", unit = "pct", averagePrice = 28.50),
                    Product(name = "Feijão Carioca 1kg", category = "Grãos", unit = "kg", averagePrice = 8.90),
                    Product(name = "Óleo de Soja 900ml", category = "Óleos", unit = "un", averagePrice = 6.49),
                    Product(name = "Açúcar Cristal 5kg", category = "Açúcar", unit = "pct", averagePrice = 18.20),
                    Product(name = "Café Tradicional 250g", category = "Bebidas", unit = "pct", averagePrice = 11.50),
                    Product(name = "Leite Integral 1L", category = "Laticínios", unit = "L", averagePrice = 5.20),
                    Product(name = "Farinha de Trigo 1kg", category = "Farináceos", unit = "kg", averagePrice = 4.80),
                    Product(name = "Pão Francês 1kg", category = "Padaria", unit = "kg", averagePrice = 14.00),
                    Product(name = "Carne Bovina (Acém) 1kg", category = "Carnes", unit = "kg", averagePrice = 32.90),
                    Product(name = "Ovos Brancos Dúzia", category = "Ovos", unit = "dz", averagePrice = 12.50),
                    Product(name = "Tomate 1kg", category = "Hortifruti", unit = "kg", averagePrice = 9.80),
                    Product(name = "Batata Inglesa 1kg", category = "Hortifruti", unit = "kg", averagePrice = 7.50)
                )
                for (p in sampleProducts) {
                    productsRef.add(p).await()
                }
            }

            val estSnapshot = establishmentsRef.limit(1).get().await()
            if (estSnapshot.isEmpty) {
                val sampleEstablishments = listOf(
                    Establishment(name = "Supermercado Econômico", city = "São Paulo", neighborhood = "Centro", address = "Rua Direita, 123", latitude = -23.5505, longitude = -46.6333),
                    Establishment(name = "Mercadinho Bom Preço", city = "São Paulo", neighborhood = "Centro", address = "Av. São João, 456", latitude = -23.5425, longitude = -46.6433),
                    Establishment(name = "Hipermercado Central", city = "São Paulo", neighborhood = "Pinheiros", address = "Rua Teodoro Sampaio, 800", latitude = -23.5655, longitude = -46.6853),
                    Establishment(name = "Atacadão do Bairro", city = "São Paulo", neighborhood = "Liberdade", address = "Rua da Glória, 200", latitude = -23.5585, longitude = -46.6353)
                )
                for (e in sampleEstablishments) {
                    establishmentsRef.add(e).await()
                }
            }

            val promoSnapshot = promotionsRef.limit(1).get().await()
            if (promoSnapshot.isEmpty) {
                val samplePromos = listOf(
                    Promotion(title = "Feijão em Oferta Especial", establishmentName = "Supermercado Econômico", productName = "Feijão Carioca 1kg", discountPrice = 7.90, originalPrice = 8.90, city = "São Paulo", neighborhood = "Centro"),
                    Promotion(title = "Arroz Mais Barato", establishmentName = "Mercadinho Bom Preço", productName = "Arroz Tio João 5kg", discountPrice = 26.90, originalPrice = 28.50, city = "São Paulo", neighborhood = "Centro")
                )
                for (pr in samplePromos) {
                    promotionsRef.add(pr).await()
                }
            }
        } catch (e: Exception) {
            Log.e("CestaJustaRepo", "Error seeding initial data", e)
        }
    }

    fun observeProducts(): Flow<List<Product>> = callbackFlow {
        val listener = productsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.toObjects(Product::class.java) ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    fun observeEstablishments(): Flow<List<Establishment>> = callbackFlow {
        val listener = establishmentsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.toObjects(Establishment::class.java) ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    fun observePrices(city: String, neighborhood: String): Flow<List<PriceEntry>> = callbackFlow {
        val listener = pricesRef
            .whereEqualTo("city", city)
            .whereEqualTo("neighborhood", neighborhood)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    pricesRef.whereEqualTo("city", city).addSnapshotListener { innerSnap, innerErr ->
                        if (innerErr != null) {
                            trySend(emptyList())
                        } else {
                            trySend(innerSnap?.toObjects(PriceEntry::class.java) ?: emptyList())
                        }
                    }
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(PriceEntry::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun observeAllPrices(): Flow<List<PriceEntry>> = callbackFlow {
        val listener = pricesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(PriceEntry::class.java) ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    suspend fun addPriceEntry(entry: PriceEntry): Result<Unit> {
        return try {
            pricesRef.add(entry).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeShoppingLists(userId: String): Flow<List<ShoppingList>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }
        val listener = listsRef.whereEqualTo("userId", userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(ShoppingList::class.java) ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveShoppingList(list: ShoppingList): Result<Unit> {
        return try {
            if (list.id.isEmpty()) {
                listsRef.add(list).await()
            } else {
                listsRef.document(list.id).set(list).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteShoppingList(listId: String): Result<Unit> {
        return try {
            listsRef.document(listId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observePromotions(city: String): Flow<List<Promotion>> = callbackFlow {
        val listener = promotionsRef.whereEqualTo("city", city).addSnapshotListener { snapshot, error ->
            if (error != null) {
                promotionsRef.addSnapshotListener { allSnap, _ ->
                    trySend(allSnap?.toObjects(Promotion::class.java) ?: emptyList())
                }
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(Promotion::class.java) ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    suspend fun getUserConfig(userId: String): UserConfig {
        return try {
            val doc = usersRef.document(userId).get().await()
            doc.toObject(UserConfig::class.java) ?: UserConfig()
        } catch (e: Exception) {
            UserConfig()
        }
    }

    suspend fun saveUserConfig(userId: String, config: UserConfig) {
        try {
            usersRef.document(userId).set(config).await()
        } catch (e: Exception) {
            Log.e("CestaJustaRepo", "Error saving user config", e)
        }
    }
}
