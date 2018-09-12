package info.benjaminhill.deconcamera

import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.launch

class MainActivity : EZPermissionActivity() {
    private lateinit var cam: EZCam
    private lateinit var lotsOfPictures: SetInterval
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
        if (!hasAllRequiredPermissions()) {
            Log.w(TAG, "Halted onResume because don't yet have the required permissions.")
            return
        }


        GlobalScope.launch(Dispatchers.Main) {
            cam = EZCam(this@MainActivity, textureView)

            cam.setCaptureSetting(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY)
            cam.setCaptureSetting(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_NIGHT)
            cam.setCaptureSetting(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY)
            cam.setCaptureSetting(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_HIGH_QUALITY)

            cam.setCaptureSetting(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF)

            // setCaptureSetting(CaptureRequest.DISTORTION_CORRECTION_MODE, CaptureRequest.DISTORTION_CORRECTION_MODE_HIGH_QUALITY)
            cam.setCaptureSetting(CaptureRequest.HOT_PIXEL_MODE, CaptureRequest.HOT_PIXEL_MODE_HIGH_QUALITY)

            cam.setCaptureSetting(CaptureRequest.JPEG_QUALITY, 99.toByte())

            cam.setCaptureSetting(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
            //cam.setCaptureSetting(CaptureRequest.SENSOR_EXPOSURE_TIME, (4.0 * 1_000_000_000L).toLong()) // ns
            cam.setCaptureSettingMaxExposure()
            cam.setCaptureSetting(CaptureRequest.SENSOR_SENSITIVITY, 600) // iso

            cam.setCaptureSetting(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
            cam.setFocusDistanceMax()

            Log.i(TAG, "Finished construction, now starting preview.")
            cam.startPreview()
            Log.i(TAG, "Finished starting preview")

            lotsOfPictures = SetInterval(5_000) {
                GlobalScope.launch(Dispatchers.Main) {
                    cam.takePicture()
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause - EZCam.stopPreview")
        GlobalScope.launch(Dispatchers.Main) {
            cam.stopPreview()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy - EZCam.close")
        lotsOfPictures.stop()
        GlobalScope.launch(Dispatchers.Main) {
            cam.close()
        }
    }

    companion object {
        const val TAG = "ezcam"
    }
}


