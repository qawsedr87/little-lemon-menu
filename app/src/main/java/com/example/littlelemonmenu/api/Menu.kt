package com.example.littlelemonmenu.api

import com.example.littlelemonmenu.data.MenuItemNetwork
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

class Menu {
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(contentType = ContentType("text", "plain"))
        }
    }

    suspend fun fetchMenu(): List<MenuItemNetwork> {
        try {
            val response = httpClient.get("https://raw.githubusercontent.com/Meta-Mobile-Developer-PC/Working-With-Data-API/main/littleLemonSimpleMenu.json").bodyAsText()

            val json = Json.decodeFromString<JsonObject>(response)
            val menuItemsJson = json["menu"]?.jsonArray
            val menuItems = mutableListOf<MenuItemNetwork>()
            if (menuItemsJson != null) {
                for (menuItemJson in menuItemsJson) {
                    val menuItem = Json.decodeFromJsonElement<MenuItemNetwork>(menuItemJson)
                    menuItems.add(menuItem)
                }
            }
            return menuItems
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}