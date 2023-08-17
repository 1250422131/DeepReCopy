package com.imcys.deeprecopy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.imcys.deeprecopy.an.EnhancedData
import com.imcys.deeprecopy.demo.AData
import com.imcys.deeprecopy.demo.BData
import com.imcys.deeprecopy.demo.deepCopy
import com.imcys.deeprecopy.ui.theme.DeepReCopyTheme

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            val a = listOf("aaa", "aaa")
            //EmptyList
            val v = arrayOf("")
            val aa = mutableListOf("").toMutableList()


            DeepReCopyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DeepReCopyTheme {
        Greeting("Android")
    }
}
