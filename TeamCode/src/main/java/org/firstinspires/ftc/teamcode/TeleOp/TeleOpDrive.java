package org.firstinspires.ftc.teamcode.TeleOp;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Rotation2d;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.HardwareClasses.KickoffArm;
import org.firstinspires.ftc.teamcode.OpenCV.CameraManagerYellowObject;
import org.firstinspires.ftc.teamcode.PedroPathing.follower.Follower;
import org.firstinspires.ftc.teamcode.PedroPathing.pathGeneration.MathFunctions;
import org.firstinspires.ftc.teamcode.PedroPathing.pathGeneration.Vector;
import org.opencv.core.Rect;

@TeleOp (name = "A TeleOp")
public class TeleOpDrive extends OpMode {
    private Follower follower;
    private KickoffArm kickoffArm;
    //private CameraManagerYellowObject cameraManager;

    private Vector driveVector;
    private Vector headingVector;

    private boolean headingLock = false;
    private boolean rightStickPressed = false;
    private boolean gripperClosed = true;  // Track the gripper state
    private boolean rightBumperPressed = false;  // Track the right bumper press
    private boolean dpadLeftPressed = false;  // Track the D-pad left press
    private boolean dpadRightPressed = false; // Track the D-pad right press
    Rotation2d targetHeading = Rotation2d.exp(Math.toRadians(0));

    @Override
    public void init() {
        follower = new Follower(hardwareMap, false);
        follower.setStartingPose(new Pose2d(0, 0, Math.toRadians(0)));
        kickoffArm = new KickoffArm(hardwareMap);

//        cameraManager = new CameraManagerYellowObject(hardwareMap);
//        cameraManager.initializeCamera();

        driveVector = new Vector();
        headingVector = new Vector();

        kickoffArm.CloseGripperCompletely();
    }

    @Override
    public void loop() {
//        if (gamepad1.left_bumper) {
//            alignToYellowObject();
//        } else {
//            updateDrive();
//            updateKickoffArm();
//        }
        updateDrive();
        updateKickoffArm();

        follower.update();
    }

//    public void alignToYellowObject() {
//        // Assuming you have a method to get the largest yellow rectangle from the camera
//        Rect largestYellowRect = cameraManager.getLargestYellowRect();
//
//        if (largestYellowRect != null) {
//            // Define the target position (X: 100, Y: 160)
//            int targetX = 100;
//            int targetY = 170;
//
//            // Calculate the error between the current and target positions
//            double errorX = targetX - largestYellowRect.x;
//            double errorY = targetY - largestYellowRect.y;
//
//            // Check if both errors are less than 10
//            if (Math.abs(errorX) < 15 && Math.abs(errorY) < 15) {
//                if (!gripperClosed) {  // Only close the gripper if it is currently open
//                    kickoffArm.CloseGripperCompletely();
//                    gripperClosed = true;
//                    telemetry.addData("Gripper", "Automatically Closed");
//                }
//            }
//
//            // Apply proportional control to calculate the necessary adjustments
//            double kP = 0.005;  // Proportional gain for position adjustment, tune this value
//            double kPHeading = 0.005;  // Proportional gain for heading adjustment, tune this value
//
//            // Adjust heading to point towards the yellow object
//            double adjustedHeading = kPHeading * errorX;
//
//            // Calculate the drive vector components in the field's coordinate system
//            double adjustedX = kP * errorX;
//            double adjustedY = kP * errorY;
//
//            // Get the robot's heading in radians
//            double robotHeading = follower.getPose().heading.toDouble(); // Already in radians
//
//            // Convert the field-centric drive vector to robot-centric
//            double cosHeading = Math.cos(robotHeading);
//            double sinHeading = Math.sin(robotHeading);
//
//            double robotX = adjustedX * cosHeading + adjustedY * sinHeading;
//            double robotY = -adjustedX * sinHeading + adjustedY * cosHeading;
//
//            // Set the drive vector to move based on the robot-centric coordinates
//            driveVector.setOrthogonalComponents(robotY, robotX);
//
//            // Adjust heading vector to point towards the object
//            headingVector.setComponents(adjustedHeading, robotHeading);
//
//            // Update the follower with the adjusted vectors
//            follower.setMovementVectors(follower.getCentripetalForceCorrection(), headingVector, driveVector);
//
//            telemetry.addData("Aligning", "In Progress");
//        } else {
//            telemetry.addData("Aligning", "No Yellow Object Detected");
//        }
//
//        telemetry.update();
//    }

    public void updateDrive() {
        // --- Heading Lock Logic ---
        if (gamepad1.right_stick_button && !rightStickPressed) {
            headingLock = !headingLock;
            rightStickPressed = true;
        } else if (Math.abs(gamepad1.right_stick_x) > 0.5 && headingLock) {
            headingLock = false;
        } else if (!gamepad1.right_stick_button) {
            rightStickPressed = false;
        }

        // --- Speed Changer Logic ---
        boolean slowerDriving = gamepad1.left_bumper;
        double rightStickScale = slowerDriving ? 0.5 : 1.0;
        double leftStickScale = slowerDriving ? 0.5 : 1.0;

        // --- Heading Vector Update ---
        if (headingLock) {
            headingVector.setComponents(targetHeading.minus(follower.getPose().heading), follower.getPose().heading.toDouble());
        } else {
            headingVector.setComponents(-gamepad1.right_stick_x * rightStickScale, follower.getPose().heading.toDouble());
        }

        // --- Drive Vector Update ---
        driveVector.setOrthogonalComponents(-gamepad1.left_stick_y * leftStickScale, -gamepad1.left_stick_x * leftStickScale);
        driveVector.setMagnitude(MathFunctions.clamp(driveVector.getMagnitude(), 0, 1));
        driveVector.rotateVector(follower.getPose().heading.toDouble());

        // --- Update Follower with Vectors ---
        follower.setMovementVectors(follower.getCentripetalForceCorrection(), headingVector, driveVector);
    }

    public void updateKickoffArm() {
        // Arm movement controls
        if (gamepad1.a) {
            kickoffArm.moveArmToPosition(50);
        } else if (gamepad1.b) {
            kickoffArm.moveArmToPosition(0);
        } else if (gamepad1.x) {
            kickoffArm.moveArmToPosition(425);
        } else if (gamepad1.y) {
            kickoffArm.moveArmToPosition(1800);
        } else if (gamepad1.dpad_up) {
            kickoffArm.moveArmToPosition(kickoffArm.armMotorPosition() + 50);
        } else if (gamepad1.dpad_down) {
            kickoffArm.moveArmToPosition(kickoffArm.armMotorPosition() - 50);
        }

        // Gripper toggle with right bumper
        if (gamepad1.right_bumper && !rightBumperPressed) {
            if (kickoffArm.armMotorPosition() < 75) {
                gripperClosed = !gripperClosed;
                if (gripperClosed) {
                    kickoffArm.CloseGripperCompletely();
                } else {
                    kickoffArm.OpenGripperAtBottom();
                }
            } else {
                gripperClosed = !gripperClosed;
                if (gripperClosed) {
                    kickoffArm.CloseGripperCompletely();
                } else {
                    kickoffArm.OpenGripperCompletely();
                }
            }
            rightBumperPressed = true;
        } else if (!gamepad1.right_bumper) {
            rightBumperPressed = false;
        }

        // Gripper fine control with D-pad left and right
        if (gamepad1.dpad_left && !dpadLeftPressed) {
            kickoffArm.OpenGripper(0.05);  // Close gripper slightly
            dpadLeftPressed = true;
        } else if (!gamepad1.dpad_left) {
            dpadLeftPressed = false;
        }

        if (gamepad1.dpad_right && !dpadRightPressed) {
            kickoffArm.CloseGripper(0.05);  // Open gripper slightly
            dpadRightPressed = true;
        } else if (!gamepad1.dpad_right) {
            dpadRightPressed = false;
        }

        kickoffArm.update();
    }
}