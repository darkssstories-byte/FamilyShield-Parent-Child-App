package com.familyshield.parent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.familyshield.parent.ui.AuthScreen
import com.familyshield.parent.ui.FamilyShieldTheme
import com.familyshield.parent.ui.HomeScreen
import com.familyshield.parent.ui.NotConfiguredScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Backend.init(applicationContext)
        setContent {
            FamilyShieldTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val vm: AppViewModel = viewModel()
                    val ui by vm.ui.collectAsStateWithLifecycle()
                    when {
                        !ui.configured -> NotConfiguredScreen(
                            demo = ui.demo,
                            onDemo = vm::setDemo
                        )
                        !ui.signedIn -> AuthScreen(
                            busy = ui.authBusy,
                            error = ui.authError,
                            onSignIn = vm::signIn,
                            onRegister = vm::register
                        )
                        else -> HomeScreen(
                            ui = ui,
                            onGenerateCode = vm::generateCode,
                            onUnlink = vm::unlink,
                            onDemo = vm::setDemo,
                            onSignOut = vm::signOut,
                            onDeleteData = vm::deleteData,
                            onMessageShown = vm::clearMessage
                        )
                    }
                }
            }
        }
    }
}
