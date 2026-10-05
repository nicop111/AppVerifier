package dev.soupslurpr.appverifier.ui

import android.app.ActivityOptions
import android.content.ClipData
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.soupslurpr.appverifier.data.Hashes
import dev.soupslurpr.appverifier.data.InternalDatabaseInfo
import dev.soupslurpr.appverifier.data.InternalDatabaseStatus
import dev.soupslurpr.appverifier.data.VerificationStatus

@Composable
fun VerifyAppScreen(
    icon: Drawable?,
    name: String,
    packageName: String,
    hashes: Hashes,
    verificationStatus: VerificationStatus,
    appNotFound: Boolean,
    onLaunchedEffectHashEmpty: () -> Unit,
    internalDatabaseInfo: InternalDatabaseInfo,
    apkFailedToParse: Boolean,
    showHasMultipleSigners: Boolean,
) {
    val context = LocalContext.current

    val clipboardManager = LocalClipboardManager.current

    val verticalScroll = rememberScrollState()

    var showMoreInfoAboutVerificationStatusDialog by rememberSaveable { mutableStateOf(false) }

    var showMoreInfoAboutInternalDatabaseStatusDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (hashes.hashes.isEmpty()) {
            onLaunchedEffectHashEmpty()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(verticalScroll),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (apkFailedToParse) {
            Text("APK FAILED TO PARSE")
            Text(
                "Make sure you provided a valid apk file."
            )
        } else if (appNotFound) {
            Text("APP NOT INSTALLED OR INVALID FORMAT")
            Text(
                "The package name doesn't seem to correspond to any installed user app." +
                        "\nPlease note system apps are not included in the search."
            )
            Text(
                "Also please make sure the provided text is in the correct format, like the " +
                        "following:\n\ncom.example" +
                        ".app\n96:C0:2C:55:75:5C:17:1C:68:13:70:29:3B:37:11:2B:4A:5D:F7:B9:82:C2:C5:58:05:4C:45:51:AD:F5:50:DC" +
                        "\n\nThere may be multiple hashes, which is normal."
            )
        } else {
            if (icon != null) {
                Image(
                    rememberDrawablePainter(drawable = icon),
                    null,
                    Modifier.size(96.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = name,
                style = typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = packageName,
                style = typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = hashes.hashes.joinToString("\n"),
                style = typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
            )
            if (showHasMultipleSigners) {
                Text(
                    "hasMultipleSigners: "
                )
                Text(
                    hashes.hasMultipleSigners.toString(),
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.height(16.dp))
            val verificationData = "$packageName\n${hashes.hashes.joinToString("\n")}"
            val mimeType = "text/plain"
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, verificationData)
                        type = mimeType
                    }

                    val shareIntent = Intent.createChooser(
                        sendIntent,
                        null,
                    )

                    startActivity(context, shareIntent, ActivityOptions.makeBasic().toBundle())
                }) {
                    Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share")
                }
                FilledTonalButton(onClick = {
                    val clip: ClipData = ClipData.newPlainText(mimeType, verificationData)
                    clipboardManager.setClip(ClipEntry(clip))
                }) {
                    Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Copy")
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            Text(
                "Internal Database Status:"
            )
            FilledTonalButton(
                onClick = { showMoreInfoAboutInternalDatabaseStatusDialog = true },
            ) {
                InternalDatabaseStatusIcon(internalDatabaseInfo.internalDatabaseStatus)
                Spacer(Modifier.width(8.dp))
                Text(
                    internalDatabaseInfo.internalDatabaseStatus.simpleInternalDatabaseStatus.name.replace('_', ' '),
                    style = typography.titleLarge
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.Info,
                    "More info about internal database status",
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Sources:",
                style = typography.labelLarge,
            )
            Text(
                if (internalDatabaseInfo.internalDatabaseStatus == InternalDatabaseStatus.NOT_FOUND) {
                    "-"
                } else {
                    internalDatabaseInfo.sources.joinToString(" · ") { it.displayName }
                },
                style = typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            // Only set when verification info was shared to AppVerifier, so hide it otherwise.
            if (verificationStatus != VerificationStatus.UNKNOWN) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Verification Status:",
                )
                FilledTonalButton(
                    onClick = { showMoreInfoAboutVerificationStatusDialog = true },
                ) {
                    Text(
                        verificationStatus.simpleVerificationStatus.name,
                        style = typography.titleLarge
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Info,
                        "More info about verification status",
                        tint = verificationStatus.simpleVerificationStatus.color,
                    )
                }
            }
        }

        Spacer(Modifier.padding(WindowInsets.navigationBars.asPaddingValues()))
    }

    if (showMoreInfoAboutInternalDatabaseStatusDialog) {
        AlertDialog(
            onDismissRequest = { showMoreInfoAboutInternalDatabaseStatusDialog = false },
            confirmButton = {
                TextButton(
                    { showMoreInfoAboutInternalDatabaseStatusDialog = false }
                ) {
                    Text(stringResource(id = android.R.string.ok))
                }
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        internalDatabaseInfo.internalDatabaseStatus.name,
                        style = typography.headlineSmall,
                        color = internalDatabaseInfo.internalDatabaseStatus.simpleInternalDatabaseStatus.color,
                    )
                }
            },
            text = {
                LazyColumn {
                    item {
                        Text(internalDatabaseInfo.internalDatabaseStatus.info)
                    }
                    item {
                        if (internalDatabaseInfo.internalDatabaseStatus == InternalDatabaseStatus.MATCH) {
                            Text("\nThe matched database entry for this app is from the following sources:\n")
                            Text(
                                text = internalDatabaseInfo.sources.joinToString("\n") { it.displayName },
                                style = typography.headlineSmall,
                            )
                            Text(
                                "\nThis information can be useful if you distrust a specific source and want to make" +
                                        " sure the app isn't from them."
                            )
                        }
                    }
                }
            }
        )
    }

    if (showMoreInfoAboutVerificationStatusDialog) {
        AlertDialog(
            onDismissRequest = { showMoreInfoAboutVerificationStatusDialog = false },
            confirmButton = {
                TextButton(
                    { showMoreInfoAboutVerificationStatusDialog = false }
                ) {
                    Text(stringResource(id = android.R.string.ok))
                }
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        verificationStatus.name,
                        style = typography.headlineSmall,
                        color = verificationStatus.simpleVerificationStatus.color,
                    )
                }
            },
            text = {
                LazyColumn {
                    item {
                        Text(verificationStatus.info)
                    }
                }
            }
        )
    }
}