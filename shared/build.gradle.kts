plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    // ➕ 新增這行：引入 Kotlin 官方序列化插件
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24" // 註：請換成跟你專案相同的 Kotlin 版本，或者如果 libs 有定義也可以用 alias
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // 引入官方序列化 JSON 套件，讓 DTO 可以自動轉成 JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}