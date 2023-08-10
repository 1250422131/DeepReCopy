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
import com.imcys.deeprecopy.da.MTest
import com.imcys.deeprecopy.demo.AData
import com.imcys.deeprecopy.demo.deepCopy
import com.imcys.deeprecopy.ui.theme.DeepReCopyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dep = AData("", MTest("123"))

        val copyDep = dep.copy()

        val deepCopy = dep.deepCopy {
            aaa = "a"
            mTest = MTest("")
        }



        Log.d("拷贝1", (dep === copyDep).toString())
        Log.d("拷贝2", (dep === deepCopy).toString())
        Log.d("拷贝3", ((dep.mTest === copyDep.mTest).toString()))
        Log.d("拷贝4", ((dep.mTest === deepCopy.mTest).toString()))




        setContent {
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
