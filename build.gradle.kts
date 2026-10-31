import java.util.Properties

// Читаем local.properties и превращаем его строки в свойства проекта,
// чтобы project.findProperty("MAILRU_EMAIL") в app/build.gradle.kts их видел.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) {
        f.inputStream().use { load(it) }
    }
}
localProps.forEach { key, value ->
    project.ext.set(key.toString(), value)
}

// Отладочный вывод — увидите в Build Output при Sync
println("MAILRU_EMAIL from local.properties: '${localProps.getProperty("MAILRU_EMAIL")}'")
println("MAILRU_PASSWORD length: ${localProps.getProperty("MAILRU_PASSWORD")?.length ?: 0}")

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}