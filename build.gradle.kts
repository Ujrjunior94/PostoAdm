// Root build.gradle.kts
tasks.register("assembleDebug") {
    doLast {
        println("Simulating assembleDebug task for Vercel/TypeScript only build.")
    }
}
