package dev.soupslurpr.appverifier.ui

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.soupslurpr.appverifier.data.Hashes
import dev.soupslurpr.appverifier.internalVerificationInfoDatabase
import java.text.Collator

private class SigningKeyEntry(
    /** The app's name if it's installed, otherwise null. */
    val appName: String?,
    val packageName: String,
    val hashes: Hashes,
)

/**
 * Lists every signing key in the internal verification info database, one entry per signing configuration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SigningKeysScreen() {
    val packageManager = LocalContext.current.packageManager

    // Sorted by app name (installed apps) or package name, from A to Z.
    val signingKeys = remember {
        internalVerificationInfoDatabase
            .flatMap { verificationInfo ->
                val appName = try {
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(verificationInfo.packageName, 0)
                    ).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
                verificationInfo.hashesList.map { SigningKeyEntry(appName, verificationInfo.packageName, it) }
            }
            .sortedWith(compareBy(Collator.getInstance()) { it.appName ?: it.packageName })
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }

    val shownSigningKeys = signingKeys.filter { entry ->
        searchQuery.isBlank() ||
                entry.appName?.contains(searchQuery, true) == true ||
                entry.packageName.contains(searchQuery, true) ||
                entry.hashes.hashes.any { it.contains(searchQuery.trim(), true) } ||
                entry.hashes.sources.any { it.displayName.contains(searchQuery, true) }
    }

    Column(Modifier.fillMaxSize()) {
        val searchBarColors = SearchBarDefaults.colors()
        DockedSearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {},
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text("Search app, package, hash or source") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = searchBarColors.inputFieldColors,
                )
            },
            expanded = false,
            onExpandedChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp),
            colors = searchBarColors,
        ) {}
        Text(
            "${shownSigningKeys.size} of ${signingKeys.size} signing keys",
            style = typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyColumn {
            items(shownSigningKeys) {
                SigningKeyItem(it)
                HorizontalDivider()
            }
            item {
                Spacer(Modifier.padding(WindowInsets.navigationBars.asPaddingValues()))
            }
        }
    }
}

@Composable
private fun SigningKeyItem(entry: SigningKeyEntry) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            entry.appName ?: entry.packageName,
            style = typography.titleMedium,
        )
        if (entry.appName != null) {
            Text(
                entry.packageName,
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            entry.hashes.hashes.joinToString("\n"),
            style = typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            entry.hashes.sources.joinToString(" · ") { it.displayName },
            style = typography.labelMedium,
            color = colorScheme.primary,
        )
    }
}
