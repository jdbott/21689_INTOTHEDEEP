package org.firstinspires.ftc.teamcode.Legacy.OpenCV;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

public class PropDetectionPipelineRedBackboardHSV extends OpenCvPipeline{
    Telemetry telemetry;
    Mat mat = new Mat();
    public enum propPosition
    {
        LEFT,
        CENTER,
        RIGHT,
        UNKNOWN
    }
    private propPosition location;
    //Define the vertices of Zone 1 (adjust coordinates if necessary
    static final Rect zone1 = new Rect(
            new Point(0, 90),
            new Point(114, 170)
    );
    // Define the vertices of Zone 2 (adjust coordinates if necessary)
    static final Rect zone2 = new Rect(
            new Point(124,45),
            new Point(300,119)
    );
    // Define the vertices of Zone 3 (adjust coordinates if necessary)
    static final Rect zone3 = new Rect(
            new Point(305, 30),
            new Point(432, 130)
    );

    static double LEFT_COLOR_THRESHOLD = 0.04; // >50% Red = Red prop
    static double CENTER_COLOR_THRESHOLD = 0.0305;
    static double RIGHT_COLOR_THRESHOLD = 0.1;
    public PropDetectionPipelineRedBackboardHSV(Telemetry t) {telemetry = t;}

    @Override
    public Mat processFrame(Mat input){
        //COLOR.RGB2HSV converts a RGB picture into HSV values
        Imgproc.cvtColor(input, mat, Imgproc.COLOR_RGB2HSV);
        //configure for the lower bound of color red (lower bound of color)
        Scalar lowHSV = new Scalar(0,75,20);
        //configure for the lower bound of color red (higher bound of color)
        Scalar highHSV = new Scalar(20,255,255);

        //Thresholding, converts areas of the picture within the HSV bounds to white, rest to black
        Core.inRange(mat, lowHSV, highHSV, mat);
        Mat left = mat.submat(zone1);
        Mat center = mat.submat(zone2);
        Mat right = mat.submat(zone3);

        double leftValue = Core.sumElems(left).val[0] / zone1.area() / 255;
        double centerValue = Core.sumElems(center).val[0] / zone2.area() / 255;
        double rightValue = Core.sumElems(right).val[0] / zone3.area() / 255;

        left.release();
        center.release();
        right.release();

        telemetry.addData("leftValue",leftValue);
        telemetry.addData("centerValue",centerValue);
        telemetry.addData("rightValue",rightValue);
        telemetry.update();

        boolean propLeft = leftValue > LEFT_COLOR_THRESHOLD;
        boolean propCenter = centerValue > CENTER_COLOR_THRESHOLD;
        boolean propRight = rightValue > RIGHT_COLOR_THRESHOLD;

        telemetry.addData("leftValue",leftValue);
        telemetry.addData("centerValue",centerValue);
        telemetry.addData("rightValue",rightValue);
        telemetry.update();
        if (propLeft && !propCenter && !propRight){
            location = propPosition.LEFT;
            telemetry.addData("Prop Position", "Left");
        } else if (!propLeft && propCenter && !propRight){
            location = propPosition.CENTER;
            telemetry.addData("Prop Position", "Center");
        } else if (!propLeft && !propCenter && propRight){
            location = propPosition.RIGHT;
            telemetry.addData("Prop Position", "Right");
        } else {
            location = propPosition.UNKNOWN;
            telemetry.addData("Prop Position", "Unknown");
        }
        telemetry.update();

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGB);

        Scalar colorZone1 = new Scalar(0, 0, 255);
        Scalar colorZone2 = new Scalar(0, 255, 0);
        Scalar colorZone3 = new Scalar(255, 0, 0);

        Imgproc.rectangle(mat, zone1, colorZone1);
        Imgproc.rectangle(mat, zone2, colorZone2);
        Imgproc.rectangle(mat, zone3, colorZone3);

        return mat;
    }
    public propPosition getAnalysis(){
        return location;
    }
}