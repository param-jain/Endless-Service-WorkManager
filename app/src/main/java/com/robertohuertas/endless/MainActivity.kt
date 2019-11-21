package com.robertohuertas.endless

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var mTextView: TextView? = null

        title = "Endless Service"

        findViewById<Button>(R.id.btnStartService).let {
            it.setOnClickListener {
                log("START THE FOREGROUND SERVICE ON DEMAND")
                actionOnService(Actions.START)
            }
        }

        findViewById<Button>(R.id.btnStopService).let {
            it.setOnClickListener {
                log("STOP THE FOREGROUND SERVICE ON DEMAND")
                actionOnService(Actions.STOP)
            }
        }

        mTextView = findViewById(R.id.textView)

        val data = Data.Builder()
            .putString(MyWorker.EXTRA_TITLE, "Message from Activity!")
            .putString(MyWorker.EXTRA_TEXT, "Hi! I have come from activity.")
            .build()

        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

        val simpleRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setInputData(data)
            .setConstraints(constraints)
            .addTag("simple_work")
            .build()

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(MyWorker::class.java, 12, TimeUnit.HOURS)
                .addTag("periodic_work")
                .build()

        val workId = simpleRequest.id

        findViewById<Button>(R.id.simpleWorkButton).setOnClickListener(View.OnClickListener {
            WorkManager.getInstance()!!.enqueue(
                simpleRequest
            )
        });

        findViewById<Button>(R.id.cancelWorkButton).setOnClickListener(View.OnClickListener {
            //WorkManager.getInstance().cancelAllWorkByTag("simple_work");
            WorkManager.getInstance()!!.cancelWorkById(workId)
        });

        findViewById<Button>(R.id.periodicWorkButton).setOnClickListener(View.OnClickListener {
            WorkManager.getInstance()!!.enqueue(
                periodicWorkRequest
            )
        });

        findViewById<Button>(R.id.cancelPeriodicWorkButton).setOnClickListener(View.OnClickListener {
            WorkManager.getInstance()!!.cancelWorkById(
                periodicWorkRequest.id
            )
        });

        WorkManager.getInstance()!!.getStatusById(simpleRequest.id)
            .observe(this, Observer { workStatus ->
                if (workStatus != null) {
                    mTextView.append("SimpleWorkRequest: " + workStatus.state.name + "\n")
                }

                if (workStatus != null && workStatus.state.isFinished) {
                    val message = workStatus.outputData.getString(
                        MyWorker.EXTRA_OUTPUT_MESSAGE,
                        "Default message"
                    )
                    mTextView.append("SimpleWorkRequest (Data): " + message!!)
                }
            })
    }

    private fun actionOnService(action: Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(this, EndlessService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
            log("Starting the service in < 26 Mode")
            startService(it)
        }
    }
}
