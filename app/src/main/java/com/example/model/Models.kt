package com.example.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep
@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = "Alimentação",
    val unit: String = "un",
    val imageUrl: String = "",
    val averagePrice: Double = 0.0
)

@Keep
@IgnoreExtraProperties
data class Establishment(
    @DocumentId val id: String = "",
    val name: String = "",
    val city: String = "São Paulo",
    val neighborhood: String = "Centro",
    val address: String = "",
    val latitude: Double = -23.5505,
    val longitude: Double = -46.6333
)

@Keep
@IgnoreExtraProperties
data class PriceEntry(
    @DocumentId val id: String = "",
    val productId: String = "",
    val establishmentId: String = "",
    val productName: String = "",
    val establishmentName: String = "",
    val price: Double = 0.0,
    val userId: String = "",
    val userName: String = "",
    val city: String = "São Paulo",
    val neighborhood: String = "Centro",
    @ServerTimestamp val createdAt: Date? = null
)

@Keep
@IgnoreExtraProperties
data class ShoppingListItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Double = 1.0,
    val unit: String = "un",
    val checked: Boolean = false
)

@Keep
@IgnoreExtraProperties
data class ShoppingList(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "Minha Lista",
    val items: List<ShoppingListItem> = emptyList(),
    @ServerTimestamp val createdAt: Date? = null
)

@Keep
@IgnoreExtraProperties
data class Promotion(
    @DocumentId val id: String = "",
    val establishmentId: String = "",
    val establishmentName: String = "",
    val productId: String = "",
    val productName: String = "",
    val title: String = "",
    val discountPrice: Double = 0.0,
    val originalPrice: Double = 0.0,
    val city: String = "São Paulo",
    val neighborhood: String = "Centro",
    @ServerTimestamp val validUntil: Date? = null
)

@Keep
@IgnoreExtraProperties
data class UserConfig(
    val city: String = "São Paulo",
    val neighborhood: String = "Centro",
    val favoriteProductIds: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true
)
