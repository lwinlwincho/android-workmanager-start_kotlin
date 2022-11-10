package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.R
import com.example.background.TAG_OUTPUT

private const val TAG = "BlurWorker"

class BlurWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        val appContext = applicationContext

        //to get the URI we passed in from the Data object:(createInputDataForUri) with Key_Image_Uri
        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        makeStatusNotification("Blurring image", appContext)

        //Add this to slow down the worker
        sleep()

        return try {
            //Check that resourceUri obtained from the Data that was passed in is not empty
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid Input URI")
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver

            //create Bitmap from cupcake image
            /*val picture=BitmapFactory.decodeResource(
                appContext.resources,
                R.drawable.android_cupcake)*/

            //Assign the picture variable to be the image that was passed in
            val picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))

            //get blurred version of Bitmap from WorkerUtil
            val output = blurBitmap(picture, appContext)

            // Write bitmap to a temporary file from WorkUtil
            val outputUri = writeBitmapToFile(appContext, output)

            // Make Notification Display URI from WorkUtil
            //makeStatusNotification("Output is $outputUri",appContext)

            //Output URI as an output Data to make this temporary image easily accessible to other workers for further operations
            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())

            //Return this to WorkManager
            Result.success(outputData)
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error applying blur")
            throwable.printStackTrace()
            Result.failure()
        }
    }
}