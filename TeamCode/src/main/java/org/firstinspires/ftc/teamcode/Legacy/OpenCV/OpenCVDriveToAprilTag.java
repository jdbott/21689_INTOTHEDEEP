package org.firstinspires.ftc.teamcode.Legacy.OpenCV;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.OpenCV.AprilTagDetectionPipeline;
import org.openftc.apriltag.AprilTagDetection;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;

@TeleOp
public class OpenCVDriveToAprilTag extends LinearOpMode {

    OpenCvCamera camera;
    AprilTagDetectionPipeline aprilTagDetectionPipeline;

    static final double FEET_PER_METER = 3.28084;

    // Lens intrinsics
    // UNITS ARE PIXELS
    // NOTE: this calibration is for the C920 webcam at 800x448.
    // You will need to do your own calibration for other configurations!
    double fx = 578.272;
    double fy = 578.272;
    double cx = 402.145;
    double cy = 221.506;

    // UNITS ARE METERS
    double tagsize = 0.051; // 2 inches

    int numFramesWithoutDetection = 0;

    final float DECIMATION_HIGH = 3;
    final float DECIMATION_LOW = 2;
    final float THRESHOLD_HIGH_DECIMATION_RANGE_METERS = 1.0f;

    private static int DESIRED_TAG_ID = -1;          // Choose the tag you want to approach or set to -1 for ANY tag.
    double DESIRED_DISTANCE = 6.0;                   //  this is how close the camera should get to the target (inches)
    private boolean isAutoAligned = false;

    private DcMotor leftFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor rightBackDrive = null;

    @Override
    public void runOpMode() {

        initializeSystem();

        waitForStart();

        telemetry.setMsTransmissionInterval(50);

        while (opModeIsActive()) {
            driveToAprilTag(1, 10);
        }
    }

    public void initializeSystem() {

        leftFrontDrive = hardwareMap.get(DcMotor.class, "leftFront");
        leftBackDrive = hardwareMap.get(DcMotor.class, "leftBack");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "rightFront");
        rightBackDrive = hardwareMap.get(DcMotor.class, "rightBack");

        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        leftFrontDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFrontDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        aprilTagDetectionPipeline = new AprilTagDetectionPipeline(tagsize, fx, fy, cx, cy);

        camera.setPipeline(aprilTagDetectionPipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(800, 448, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {

            }
        });

        telemetry.addData("Camera preview on/off", "3 dots, Camera Stream");
        telemetry.update();
    }

    public void driveToAprilTag(int desiredTagID, double targetDistanceAlignment) {
        DESIRED_TAG_ID = desiredTagID;
        DESIRED_DISTANCE = targetDistanceAlignment;

        do {
            iterateAprilTagCycle();
        } while (opModeIsActive() && !isAutoAligned);
    }

    public void iterateAprilTagCycle() {
        telemetry.addData("AprilTag Status:", "Starting Cycle");
        telemetry.update();

        isAutoAligned = false;

        final double SPEED_GAIN = 0.025;
        final double STRAFE_GAIN = 0.023;
        final double TURN_GAIN = 0.02;

        final double MAX_AUTO_SPEED = 0.7;
        final double MAX_AUTO_STRAFE = 0.7;
        final double MAX_AUTO_TURN = 0.7;

        boolean targetFound = false;
        double drive = 0;
        double strafe = 0;
        double turn = 0;

        double rangeError = 0;
        double headingError = 0;
        double yawError = 0;

        AprilTagDetection desiredTag = null;

        ArrayList<AprilTagDetection> detections = aprilTagDetectionPipeline.getDetectionsUpdate();

        if (detections != null) {
            for (AprilTagDetection detection : detections) {
                if ((DESIRED_TAG_ID < 0) || (detection.id == DESIRED_TAG_ID)) {
                    targetFound = true;
                    desiredTag = detection;
                    telemetry.addData("AprilTag Status:", "Target Tag Found");
                    telemetry.update();
                    break;
                } else {
                    telemetry.addData("Skipping", "Tag ID %d is not desired", detection.id);
                }
            }
        } else {
            telemetry.addData("AprilTag Status:", "No new detections");
        }

        if (targetFound) {
            if (desiredTag.pose.z < THRESHOLD_HIGH_DECIMATION_RANGE_METERS) {
                aprilTagDetectionPipeline.setDecimation(DECIMATION_HIGH);
            } else {
                aprilTagDetectionPipeline.setDecimation(DECIMATION_LOW);
            }

            Orientation rot = Orientation.getOrientation(desiredTag.pose.R, AxesReference.INTRINSIC, AxesOrder.YXZ, AngleUnit.DEGREES);

            rangeError = -(desiredTag.pose.z * FEET_PER_METER) - DESIRED_DISTANCE;
            headingError = rot.firstAngle;
            yawError = desiredTag.pose.x;

            telemetry.addData("Errors", "Range %5.2f, Heading %5.2f, Yaw %5.2f ", rangeError, headingError, yawError);
            telemetry.update();

            drive = Range.clip(rangeError * SPEED_GAIN, -MAX_AUTO_SPEED, MAX_AUTO_SPEED);
            turn = Range.clip(headingError * TURN_GAIN, -MAX_AUTO_TURN, MAX_AUTO_TURN);
            strafe = Range.clip(-yawError * STRAFE_GAIN, -MAX_AUTO_STRAFE, MAX_AUTO_STRAFE);

            if (drive < 0.17 && rangeError > 0.5) {
                drive = 0.13;
            } else if (turn < 0.17 && headingError > 10) {
                turn = 0.13;
            } else if (strafe < 0.14 && yawError > 10) {
                strafe = 0.12;
            }

        } else {
            telemetry.addData("AprilTag Status:", "Target Not Found");
            telemetry.update();
        }

        telemetry.update();

        telemetry.addData("AprilTag Status:", "Moving Towards Tag");
        telemetry.update();

        moveRobot(drive, strafe, -turn);

        if ((rangeError < 0.75 && rangeError > -0.75) && (headingError < 4 && headingError > -4) && (yawError < 5 && yawError > -5)) {
            telemetry.addData("AprilTag Status:", "Breaking, Properly Aligned");
            telemetry.update();

            isAutoAligned = true;
        }
    }

    public void moveRobot(double x, double y, double yaw) {

        // Calculate wheel powers.
        double leftFrontPower = x - y - yaw;
        double rightFrontPower = x + y + yaw;
        double leftBackPower = x + y - yaw;
        double rightBackPower = x - y + yaw;

        // Normalize wheel powers to be less than 1.0
        double max = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
        max = Math.max(max, Math.abs(leftBackPower));
        max = Math.max(max, Math.abs(rightBackPower));

        if (max > 1.0) {
            leftFrontPower /= max;
            rightFrontPower /= max;
            leftBackPower /= max;
            rightBackPower /= max;
        }

        // Send powers to the wheels.
        leftFrontDrive.setPower(leftFrontPower);
        rightFrontDrive.setPower(rightFrontPower);
        leftBackDrive.setPower(leftBackPower);
        rightBackDrive.setPower(rightBackPower);
    }
}