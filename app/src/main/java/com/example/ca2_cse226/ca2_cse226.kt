
package com.example.ca2_cse226

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _results = MutableStateFlow<List<String>>(emptyList())
    val results = _results.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            query.debounce(500).collectLatest { text ->
                if (text.isBlank()) {
                    _results.value = emptyList()
                    _loading.value = false
                } else {
                    _loading.value = true

                    val data = withContext(Dispatchers.IO) {
                        delay(1000)
                        listOf("$text Phone", "$text Laptop", "$text Headphones")
                    }

                    _results.value = data
                    _loading.value = false
                }
            }
        }
    }

    fun search(text: String) {
        _query.value = text
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel = remember { SearchViewModel() }
            val query by viewModel.query.collectAsStateWithLifecycle()
            val results by viewModel.results.collectAsStateWithLifecycle()
            val loading by viewModel.loading.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.search(it) },
                    label = { Text("Search products") },
                    trailingIcon = {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(results) { item ->
                        Text(
                            text = item,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
