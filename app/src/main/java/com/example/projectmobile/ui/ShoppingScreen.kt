package com.example.projectmobile.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    items: MutableList<Pair<String, Boolean>>,
    onBack: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Shopping List") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF3E6D8E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 1) input sempre in cima con placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "Add to your list..",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { new ->
                        if (new.endsWith("\n")) {
                            val toAdd = new.trimEnd('\n')
                            if (toAdd.isNotBlank()) {
                                items.add(toAdd to false)    // Aggiungi l'elemento
                            }
                            text = ""
                        } else {
                            text = new
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    cursorBrush = SolidColor(Color.Black)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2) lista delle voci
            items.forEachIndexed { index, (name, checked) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { isChecked ->
                            items[index] = name to isChecked
                        }
                    )
                    Text(
                        text = name,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    //Eliminare l'elemento
                    IconButton(
                        onClick = {
                            items.removeAt(index)
                        }
                    ) {
                        Text(
                            text = "X",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        //Icon(
                        //    imageVector = Icons.Default.Delete, // Icona del cestino
                        //    contentDescription = "Delete Item",
                        //    tint = Color.Red // Colore rosso per l'icona del cestino
                        //)
                    }
                }
            }
        }
    }
}
