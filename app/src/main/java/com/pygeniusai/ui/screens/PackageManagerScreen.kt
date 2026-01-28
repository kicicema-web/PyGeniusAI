package com.pygeniusai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pygeniusai.python.PyPackage
import com.pygeniusai.ui.theme.PyGeniusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageManagerScreen(
    installedPackages: List<PyPackage>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onInstall: (String) -> Unit,
    onUninstall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var installPackageName by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = PyGeniusColors.Surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = PyGeniusColors.Secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Package Manager",
                            style = MaterialTheme.typography.titleMedium,
                            color = PyGeniusColors.OnSurface
                        )
                        Text(
                            text = "${installedPackages.size} packages installed",
                            style = MaterialTheme.typography.bodySmall,
                            color = PyGeniusColors.OnSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Install new package
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = installPackageName,
                        onValueChange = { installPackageName = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Package name (e.g., requests)") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (installPackageName.isNotBlank()) {
                                onInstall(installPackageName)
                                installPackageName = ""
                            }
                        },
                        enabled = installPackageName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PyGeniusColors.Secondary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Install")
                    }
                }
            }
        }
        
        // Search installed packages
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search installed packages...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            }
        )
        
        // Package list
        val filteredPackages = if (searchQuery.isBlank()) {
            installedPackages
        } else {
            installedPackages.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredPackages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) 
                                "No packages installed" 
                            else 
                                "No packages match your search",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PyGeniusColors.OnSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredPackages) { pkg ->
                    PackageItem(
                        pkg = pkg,
                        onUninstall = { onUninstall(pkg.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PackageItem(
    pkg: PyPackage,
    onUninstall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PyGeniusColors.SurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                tint = PyGeniusColors.OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pkg.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PyGeniusColors.OnSurface
                )
                Text(
                    text = "v${pkg.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PyGeniusColors.OnSurfaceVariant
                )
            }
            
            IconButton(onClick = onUninstall) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Uninstall",
                    tint = PyGeniusColors.Error
                )
            }
        }
    }
}
