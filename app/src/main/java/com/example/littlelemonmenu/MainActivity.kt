package com.example.littlelemonmenu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.littlelemonmenu.api.Menu
import com.example.littlelemonmenu.data.MenuItem
import com.example.littlelemonmenu.data.MenuItemNetwork
import com.example.littlelemonmenu.database.MenuDao
import com.example.littlelemonmenu.database.MenuDatabase
import com.example.littlelemonmenu.ui.theme.ImageLogo
import com.example.littlelemonmenu.ui.theme.LittleLemonMenuTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var menuDao: MenuDao
    private var menuItems: List<MenuItem> by mutableStateOf(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            MenuDatabase::class.java, "menu_database"
        ).build()

        menuDao = db.menuDao()
        lifecycleScope.launch(Dispatchers.IO) {
            val fetchData: List<MenuItemNetwork> = Menu().fetchMenu()

            // Save the default data from API if database is empty
            if (menuDao.isEmpty()) {
                saveMenuToDatabase(fetchData.ifEmpty { fakeMenuList() })
            }
        }

        setContent {
            LittleLemonMenuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val databaseMenuItems = menuDao.getAll().observeAsState(initial = emptyList())

                    var orderMenuItems by remember { mutableStateOf(false) }
                    var searchPhrase by remember { mutableStateOf("") }
                    val dishName by remember { mutableStateOf("") }
                    val priceInput by remember { mutableStateOf("") }

                    Column {
                        ImageLogo()

                        CreateDishPanel(dishName, priceInput)

                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchPhrase,
                                onValueChange = { searchPhrase = it },
                                label = { Text("Search") },
                                modifier = Modifier
                                    .weight(1f),
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            Button(onClick = { orderMenuItems = !orderMenuItems }) {
                                val icon = when {
                                    orderMenuItems -> R.drawable.az to "Sorted_A_Z"
                                    else -> R.drawable.za to "Sorted_Z_A"
                                }
                                Icon(painter = painterResource(id = icon.first), contentDescription = icon.second)
                            }
                        }

                        if (searchPhrase.isNotEmpty()) {
                            databaseMenuItems.value.filter {
                                it.name.contains(searchPhrase, ignoreCase = true)
                            }.let { filteredList ->
                                menuItems = sortedMenuItems(filteredList, orderMenuItems)
                            }
                        } else {
                            menuItems = sortedMenuItems(databaseMenuItems.value, orderMenuItems)
                        }

                        ItemsList(menuItems)
                    }

                }
            }
        }
    }


    private fun sortedMenuItems(items: List<MenuItem>, ordered: Boolean): List<MenuItem> {
        return if (ordered) {
            items.sortedBy { it.name }
        } else {
            items.sortedByDescending { it.name }
        }
    }

    private fun saveMenuToDatabase(menuItemsNetwork: List<MenuItemNetwork>) {
        val menuItemsRoom = menuItemsNetwork.map { it.toMenuItem() }
        menuDao.insertAll(*menuItemsRoom.toTypedArray())
    }

    // when failed fetching api
    private fun fakeMenuList(): List<MenuItemNetwork> {
        return listOf(
            MenuItemNetwork(id = 1, title = "Apple", price = 1.2),
            MenuItemNetwork(id = 2, title = "Banana", price = 2.2)
        )
    }

    @Composable
    private fun CreateDishPanel(dishName: String, priceInput: String) {
        var newDishName by remember { mutableStateOf(dishName) }
        var newPriceInput by remember { mutableStateOf(priceInput) }
        var showError by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier.weight(.4f),
                value = newDishName,
                onValueChange = { value -> newDishName = value },
                label = { Text("Dish name") }
            )
            Spacer(modifier = Modifier.width(16.dp))
            TextField(
                modifier = Modifier.weight(.2f),
                value = newPriceInput,
                onValueChange = { value -> newPriceInput = value },
                label = { Text("Price") }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                if (newDishName.isBlank()
                    || newPriceInput.isBlank()
                    || newPriceInput.toDoubleOrNull() == null
                ) {
                    showError = true
                } else {
                    showError = false
                    val newItem = MenuItem(
                        id = UUID.randomUUID().toString(),
                        name = newDishName,
                        price = newPriceInput.toDouble()
                    )

                    lifecycleScope.launch(Dispatchers.IO) {
                        menuDao.insert(newItem)
                    }

                    newDishName = ""
                    newPriceInput = ""
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "Add"
                )
            }
        }

        Text(
            modifier = Modifier.padding(start = 16.dp, bottom = 2.dp),
            text = if (showError) "Invalid name or price values" else "",
            color = if (showError) Color.Red else Color.Transparent
        )
    }

    @Composable
    private fun ItemsList(menuItems: List<MenuItem>) {
        if (menuItems.isEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp),
                text = "The menu is empty"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                items(
                    items = menuItems,
                    itemContent = { menuItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(menuItem.name)
                            Text(
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Right,
                                text = "%.2f".format(menuItem.price)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(onClick = {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    menuDao.delete(menuItem)

                                    val updatedMenuItems = menuDao.getAll().value
                                    withContext(Dispatchers.Main) {
                                        if (updatedMenuItems != null) {
                                            this@MainActivity.menuItems = updatedMenuItems
                                        }
                                    }
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.delete_icon),
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}