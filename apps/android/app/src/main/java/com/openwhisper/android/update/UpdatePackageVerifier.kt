package com.openwhisper.android.update

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

/** Pure decision rules for accepting a downloaded update APK. */
object UpdatePackagePolicy {
    fun requireAcceptable(
        expectedPackageName: String,
        installedVersionCode: Long,
        archivePackageName: String?,
        archiveVersionCode: Long,
        archiveSignerCount: Int,
        allSignersMatchInstalledApp: Boolean,
    ) {
        require(archivePackageName == expectedPackageName) {
            "Downloaded update is for a different app"
        }
        require(archiveVersionCode > installedVersionCode) {
            "Downloaded update is not newer than the installed app"
        }
        require(archiveSignerCount > 0 && allSignersMatchInstalledApp) {
            "Downloaded update is not signed by the installed app's key"
        }
    }
}

/**
 * Checks the downloaded APK before Android's installer is opened: it must be this
 * same package, a newer version, and signed with the certificate of the app that
 * is already installed. The SHA-256 in the feed only proves the download matches
 * the feed; this proves the APK actually comes from the same publisher.
 */
object UpdatePackageVerifier {
    fun requireTrustedUpdate(context: Context, apk: File, installedVersionCode: Int) {
        val packageManager = context.packageManager
        val archive = packageManager.getPackageArchiveInfo(
            apk.absolutePath,
            PackageManager.GET_SIGNING_CERTIFICATES,
        ) ?: throw IllegalArgumentException("Downloaded update is not a valid APK")
        val signingInfo = archive.signingInfo
        val signers = when {
            signingInfo == null -> emptyArray()
            signingInfo.hasMultipleSigners() -> signingInfo.apkContentsSigners
            else -> signingInfo.signingCertificateHistory
        }.orEmpty()
        val ownPackage = context.packageName
        val allMatch = signers.all { signature ->
            packageManager.hasSigningCertificate(
                ownPackage,
                signature.toByteArray(),
                PackageManager.CERT_INPUT_RAW_X509,
            )
        }
        UpdatePackagePolicy.requireAcceptable(
            expectedPackageName = ownPackage,
            installedVersionCode = installedVersionCode.toLong(),
            archivePackageName = archive.packageName,
            archiveVersionCode = archive.longVersionCode,
            archiveSignerCount = signers.size,
            allSignersMatchInstalledApp = allMatch,
        )
    }
}
