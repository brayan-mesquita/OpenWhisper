package com.openwhisper.android.update

import org.junit.Test

class UpdatePackagePolicyTest {
    private fun check(
        packageName: String? = "com.openwhisper.android",
        versionCode: Long = 30,
        signerCount: Int = 1,
        signersMatch: Boolean = true,
    ) = UpdatePackagePolicy.requireAcceptable(
        expectedPackageName = "com.openwhisper.android",
        installedVersionCode = 29,
        archivePackageName = packageName,
        archiveVersionCode = versionCode,
        archiveSignerCount = signerCount,
        allSignersMatchInstalledApp = signersMatch,
    )

    @Test
    fun acceptsNewerSamePackageSignedBySameKey() = check()

    @Test(expected = IllegalArgumentException::class)
    fun rejectsADifferentPackage() = check(packageName = "com.attacker.fake")

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAnUnparseablePackageName() = check(packageName = null)

    @Test(expected = IllegalArgumentException::class)
    fun rejectsADowngradeOrSameVersion() = check(versionCode = 29)

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAForeignSigningKey() = check(signersMatch = false)

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAnUnsignedApk() = check(signerCount = 0)
}
