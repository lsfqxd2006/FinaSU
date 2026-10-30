plugins {
    alias(libs.plugins.agp.app) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.compose.compiler) apply false
}

extra["androidMinSdkVersion"] = 31
extra["androidTargetSdkVersion"] = 37
extra["androidCompileSdkVersion"] = 37
extra["androidCompileSdkVersionMinor"] = 0
extra["androidBuildToolsVersion"] = "37.0.0"
extra["androidCompileNdkVersion"] = libs.versions.ndk.get()
extra["androidSourceCompatibility"] = JavaVersion.VERSION_21
extra["androidTargetCompatibility"] = JavaVersion.VERSION_21
extra["managerVersionCode"] = getVersionCode()
extra["managerVersionName"] = getVersionName()

// Kotlin DSL 没有内置 exec，这里补一个
fun exec(command: String, workingDir: String): String {
    val parts = command.split(" ").filter { it.isNotBlank() }
    val process = ProcessBuilder(parts)
        .directory(if (workingDir.isBlank()) null else File(workingDir))
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    val exit = process.waitFor()
    if (exit != 0) {
        error("Command failed (exit $exit): $command\n$output")
    }
    return output
}

fun getGitCommitCount(): Int {
    val process = Runtime.getRuntime().exec(arrayOf("git", "rev-list", "--count", "HEAD"))
    return process.inputStream.bufferedReader().use { it.readText().trim().toInt() }
}

fun getGitDescribe(): String {
    return try {
        // --abbrev=0 只输出最近的 tag 名，不带 -<提交数>-g<hash>
        exec("git describe --tags --abbrev=0", "").trim()
    } catch (e: Exception) {
        // 仓库里一个 tag 都没有时回退到短 hash
        try {
            exec("git describe --tags --always", "").trim()
        } catch (e2: Exception) {
            "unknown"
        }
    }
}

fun getVersionCode(): Int {
    val commitCount = getGitCommitCount()
    return 30000 + commitCount
}

fun getVersionName(): String {
    return getGitDescribe()
}
