package dev.soupslurpr.appverifier.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.soupslurpr.appverifier.R
import dev.soupslurpr.appverifier.data.Hashes
import dev.soupslurpr.appverifier.data.InternalDatabaseInfo
import dev.soupslurpr.appverifier.data.InternalDatabaseStatus
import dev.soupslurpr.appverifier.data.SimpleVerificationStatus
import dev.soupslurpr.appverifier.data.VerificationInfo
import java.text.Collator
import kotlinx.coroutines.launch

private class AppListEntry(
    val name: String,
    val packageInfo: PackageInfo,
    val hashes: Hashes,
    val internalDatabaseInfo: InternalDatabaseInfo,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    searchQuery: String,
    onClickAppItem: (
        name: String,
        packageName: String,
        hash: Hashes,
        icon: Drawable,
        internalDatabaseInfo: InternalDatabaseInfo,
    ) -> Unit,
    onLaunchedEffect: () -> Unit,
    onQueryChange: (query: String) -> Unit,
    onSearch: (query: String) -> Unit,
    onSearchActiveChange: (active: Boolean) -> Unit,
    getHashesFromPackageInfo: (packageInfo: PackageInfo) -> Hashes,
    getInternalDatabaseInfoFromVerificationInfo: (verification: VerificationInfo) -> InternalDatabaseInfo,
    onVerifyApkFileClicked: () -> Unit,
    onSigningKeysClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
) {
    val context = LocalContext.current

    val packageManager: PackageManager = context.packageManager

    // User installed apps with their verification info, sorted from A to Z.
    val userInstalledApps = remember {
        val systemPackageNames = packageManager.getInstalledPackages(PackageManager.MATCH_SYSTEM_ONLY)
            .map { it.packageName }
            .toSet()

        packageManager.getInstalledPackages(PackageManager.GET_SIGNING_CERTIFICATES)
            .filter { it.packageName !in systemPackageNames }
            .map { packageInfo ->
                val hashes = getHashesFromPackageInfo(packageInfo)
                AppListEntry(
                    name = packageInfo.applicationInfo?.let { packageManager.getApplicationLabel(it).toString() }
                        ?: null.toString(),
                    packageInfo = packageInfo,
                    hashes = hashes,
                    internalDatabaseInfo = getInternalDatabaseInfoFromVerificationInfo(
                        VerificationInfo(packageInfo.packageName, hashes)
                    ),
                )
            }
            .sortedWith(compareBy(Collator.getInstance()) { it.name })
    }

    LaunchedEffect(key1 = Unit) {
        onLaunchedEffect()
    }

    // Internal database statuses to show. Empty means no filter, so all apps are shown.
    var statusFilter by rememberSaveable { mutableStateOf(setOf<InternalDatabaseStatus>()) }

    val searchedApps = userInstalledApps.filter {
        searchQuery == "" || it.name.contains(searchQuery, true) ||
                it.packageInfo.packageName.contains(searchQuery, true)
    }

    // Counts follow the search query so they match what each filter would show.
    val statusCounts = searchedApps.groupingBy { it.internalDatabaseInfo.internalDatabaseStatus }.eachCount()

    val shownApps = searchedApps.filter {
        statusFilter.isEmpty() || it.internalDatabaseInfo.internalDatabaseStatus in statusFilter
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val drawerCoroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.ic_launcher_background))
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.requiredSize(144.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.app_name),
                        style = typography.headlineSmall
                    )
                }
                NavigationDrawerItem(
                    label = { Text("Verify APK File") },
                    selected = false,
                    onClick = {
                        drawerCoroutineScope.launch { drawerState.close() }
                        onVerifyApkFileClicked()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    icon = { Icon(Icons.Filled.FileOpen, null) },
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.signing_keys)) },
                    selected = false,
                    onClick = {
                        drawerCoroutineScope.launch { drawerState.close() }
                        onSigningKeysClicked()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    icon = { Icon(Icons.Filled.Key, null) },
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.settings)) },
                    selected = false,
                    onClick = {
                        drawerCoroutineScope.launch { drawerState.close() }
                        onSettingsClicked()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    icon = { Icon(Icons.Filled.Settings, null) },
                )
            }
        },
        drawerState = drawerState,
    ) {
        Scaffold(
            topBar = {
                Column {
                    val colors1 = SearchBarDefaults.colors()
                    DockedSearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = searchQuery,
                                onQueryChange = onQueryChange,
                                onSearch = onSearch,
                                expanded = false,
                                onExpandedChange = onSearchActiveChange,
                                placeholder = { Text(stringResource(android.R.string.search_go)) },
                                leadingIcon = {
                                    IconButton(onClick = { drawerCoroutineScope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Open menu")
                                    }
                                },
                                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                colors = colors1.inputFieldColors,
                            )
                        },
                        expanded = false,
                        onExpandedChange = onSearchActiveChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp),
                        colors = colors1
                    ) {}
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(16.dp, 0.dp, 16.dp, 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            InternalDatabaseStatus.MATCH to "Verified",
                            InternalDatabaseStatus.NOT_FOUND to "Unknown",
                            InternalDatabaseStatus.NOMATCH to "Mismatched",
                        ).forEach { (status, label) ->
                            val selected = status in statusFilter
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    statusFilter = if (selected) statusFilter - status else statusFilter + status
                                },
                                label = { Text("$label (${statusCounts[status] ?: 0})") },
                                leadingIcon = { InternalDatabaseStatusIcon(status, Modifier.size(18.dp)) },
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                Modifier.padding(
                    innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    innerPadding.calculateTopPadding(),
                    innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                )
            ) {
                items(shownApps, key = { it.packageInfo.packageName }) {
                    AppItem(
                        name = it.name,
                        packageName = it.packageInfo.packageName,
                        hashes = it.hashes,
                        icon = packageManager.getApplicationIcon(
                            it.packageInfo.applicationInfo ?: ApplicationInfo()
                        ),
                        onClickAppItem = onClickAppItem,
                        internalDatabaseInfo = it.internalDatabaseInfo,
                    )
                }
                item {
                    Spacer(Modifier.padding(WindowInsets.navigationBars.asPaddingValues()))
                }
            }
        }
    }
}

@Composable
fun AppItem(
    name: String,
    packageName: String,
    hashes: Hashes,
    icon: Drawable,
    onClickAppItem: (
        name: String,
        packageName: String,
        hash: Hashes,
        icon: Drawable,
        internalDatabaseInfo: InternalDatabaseInfo
    ) -> Unit,
    internalDatabaseInfo: InternalDatabaseInfo,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClickAppItem(name, packageName, hashes, icon, internalDatabaseInfo) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            rememberDrawablePainter(drawable = icon),
            null,
            Modifier.size(40.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                name,
                style = typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                packageName,
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(16.dp))
        InternalDatabaseStatusIcon(internalDatabaseInfo.internalDatabaseStatus)
    }
}

@Composable
fun InternalDatabaseStatusIcon(status: InternalDatabaseStatus, modifier: Modifier = Modifier) {
    when (status) {
        InternalDatabaseStatus.NOT_FOUND -> Icon(
            Icons.AutoMirrored.Filled.Help,
            "Not found in internal database",
            modifier,
            SimpleVerificationStatus.UNKNOWN.color,
        )

        InternalDatabaseStatus.MATCH -> Icon(
            Icons.Filled.Verified,
            "Verified successfully with internal database",
            modifier,
            SimpleVerificationStatus.SUCCESS.color,
        )

        InternalDatabaseStatus.NOMATCH -> Icon(
            Icons.Filled.Error,
            "Verification with internal database NOT successful!",
            modifier,
            SimpleVerificationStatus.FAILURE.color,
        )
    }
}
