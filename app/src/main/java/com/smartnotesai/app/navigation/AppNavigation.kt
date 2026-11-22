package com.smartnotesai.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartnotesai.feature.chat.ChatRoute
import com.smartnotesai.feature.editor.EditorRoute
import com.smartnotesai.feature.home.HomeScreen

private object AppRoutes {
    const val HOME = "home"
    const val EDITOR = "editor"
    const val EDITOR_WITH_ID = "editor/{noteId}"
    const val CHAT = "chat"

    fun editor(noteId: String): String {
        return "editor/$noteId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen(
                onAddNoteClick = {
                    navController.navigate(AppRoutes.EDITOR)
                },
                onNoteClick = { noteId ->
                    navController.navigate("${AppRoutes.EDITOR}/$noteId")
                },
                onChatClick = {
                    navController.navigate(AppRoutes.CHAT)
                }
            )
        }

        composable(AppRoutes.EDITOR) {
            EditorRoute(
                noteId = null,
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppRoutes.EDITOR_WITH_ID,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val noteId = backStackEntry.arguments?.getString("noteId")

            EditorRoute(
                noteId = noteId,
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.CHAT) {
            ChatRoute()
        }
    }
}