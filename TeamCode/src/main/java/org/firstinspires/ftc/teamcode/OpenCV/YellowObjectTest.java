package org.firstinspires.ftc.teamcode.OpenCV;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.opencv.core.Rect;

/**
 * OpenCVTest.java
 * <p>
 * This code is an FTC (FIRST Tech Challenge) teleop mode that uses OpenCV to process images from a webcam.
 * It identifies which section of the image has the most red and blue colors and displays this information
 * on the telemetry. The image is divided into three equal horizontal sections, and the red and blue color
 * ratios are calculated for each section.
 */
@TeleOp
public class YellowObjectTest extends LinearOpMode {
    // Declare variables for the webcam and the OpenCV pipeline
    private CameraManagerYellowObject cameraManager;

    /**
     * The main method for the op mode. This method sets up and runs the OpenCV pipeline.
     */
    @Override
    public void runOpMode() {

        cameraManager = new CameraManagerYellowObject(hardwareMap);
        cameraManager.initializeCamera();

        // Notify that the op mode is waiting for the start signal
        telemetry.addLine("Waiting for start");
        telemetry.update();

        // Wait for the start signal
        waitForStart();

        // Run the op mode while it is active
        while (opModeIsActive()) {
            // Get the largest yellow rectangle from the camera manager
            Rect largestYellowRect = cameraManager.getLargestYellowRect();

            // Check if the result is not null before accessing its properties
            if (largestYellowRect != null) {
                telemetry.addData("X", largestYellowRect.x);
                telemetry.addData("Y", largestYellowRect.y);
                telemetry.addData("Area", largestYellowRect.area());
            } else {
                telemetry.addData("X", "No detection");
                telemetry.addData("Y", "No detection");
                telemetry.addData("Area", "No detection");
            }

            // Update the telemetry
            telemetry.update();

            // Sleep for a short duration to avoid excessive processing
            sleep(50);
        }
    }
}