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
    // CI 的 actions/checkout 默认是 shallow clone，直接 fetch 可能只拿到 1 个提交。
    // 先尝试 --unshallow 补全历史；非 shallow 仓库会失败，回退到普通 fetch。
    try {
        exec("git fetch --unshallow --quiet https://github.com/LyraVoid/FinaSU.git master", "")
    } catch (e: Exception) {
        exec("git fetch --quiet https://github.com/LyraVoid/FinaSU.git master", "")
    }
    val count = exec("git rev-list --count FETCH_HEAD", "").trim()
    return count.toIntOrNull()?.takeIf { it > 0 }
        ?: error("Failed to determine upstream commit count, got: '$count'")
}

fun getGitDescribe(): String {
    val process = Runtime.getRuntime().exec(arrayOf("git", "describe", "--tags", "--always"))
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}

fun getVersionCode(): Int {
    val commitCount = getGitCommitCount()
    return 30000 + commitCount
}

fun getVersionName(): String {
    return getGitDescribe()
}
