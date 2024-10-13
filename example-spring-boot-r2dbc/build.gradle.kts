plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.plugin.spring)
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    ksp(project(":komapper-processor"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(project(":komapper-spring-boot-starter-r2dbc"))
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}

springBoot {
    mainClass.set("example.spring.boot.r2dbc.R2dbcSpringApplicationKt")
}
