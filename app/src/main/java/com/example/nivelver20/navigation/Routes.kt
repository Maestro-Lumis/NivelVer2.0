package com.example.nivelver20.navigation

// Sealed class для навигации между экранами
sealed class Routes(val route: String) {
    object Main : Routes("main")
    object Login : Routes("login")
    object Register : Routes("register")
    object Perfil : Routes("perfil")
    object NivelSelection : Routes("nivel_selection")
    object Vocabulario : Routes("vocabulario")
    object VocabularioResults : Routes("vocabulario_results")
    object Lectura : Routes("lectura")
    object LecturaResults : Routes("lectura_results")
    object Audio : Routes ("audio")
    object AudioResults : Routes("audio_results")
    object Grammatica : Routes("grammatica")
    object GrammaticaResults : Routes("grammatica_results")
    object Nivel : Routes("nivel")
    object NivelResults: Routes("nivel_results")
    object Flujo : Routes("flujo")
    object FlujoResults: Routes("flujo_results")
}