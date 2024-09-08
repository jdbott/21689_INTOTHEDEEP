package org.firstinspires.ftc.teamcode.OpenCV;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

public class YellowObjectDetectionPipeline extends OpenCvPipeline {
    private Mat hsvFrame = new Mat();
    private Mat yellowMask = new Mat();
    private Mat hierarchy = new Mat();
    private List<MatOfPoint> contours = new ArrayList<>();
    private Rect largestYellowRect = null;

    @Override
    public Mat processFrame(Mat input) {
        // Convert the input frame from RGB to HSV color space
        Imgproc.cvtColor(input, hsvFrame, Imgproc.COLOR_RGB2HSV);

        // Define the range for yellow in HSV color space
        Scalar lowerYellow = new Scalar(20, 100, 100);
        Scalar upperYellow = new Scalar(30, 255, 255);

        // Create a mask for yellow areas in the frame
        Core.inRange(hsvFrame, lowerYellow, upperYellow, yellowMask);

        // Find contours of the yellow areas
        contours.clear();
        Imgproc.findContours(yellowMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find the largest contour based on the area
        double maxArea = 0;
        largestYellowRect = null;
        for (MatOfPoint contour : contours) {
            Rect boundingRect = Imgproc.boundingRect(contour);
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                largestYellowRect = boundingRect;
            }
        }

        // If a significant yellow area is found, draw a rectangle around it
        if (largestYellowRect != null && maxArea > 600) {  // You can adjust the area threshold as needed
            Imgproc.rectangle(input, largestYellowRect, new Scalar(0, 255, 255), 4);
        }

        // Return the processed frame
        return input;
    }

    public Rect getLargestYellowRect() {
        return largestYellowRect;
    }
}