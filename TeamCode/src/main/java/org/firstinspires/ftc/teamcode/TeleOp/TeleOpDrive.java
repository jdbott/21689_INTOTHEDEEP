package org.firstinspires.ftc.teamcode.TeleOp;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Rotation2d;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.PedroPathing.follower.Follower;
import org.firstinspires.ftc.teamcode.PedroPathing.pathGeneration.MathFunctions;
import org.firstinspires.ftc.teamcode.PedroPathing.pathGeneration.Vector;

@TeleOp(name = "A TeleOp")
public class TeleOpDrive extends OpMode {
    Rotation2d targetHeading = Rotation2d.exp(Math.toRadians(0));
    private Follower follower;
    private Vector driveVector;
    private Vector headingVector;
    private boolean headingLock = false;
    private boolean rightStickPressed = false;

    @Override
    public void init() {
        follower = new Follower(hardwareMap, false);
        follower.setStartingPose(new Pose2d(0, 0, Math.toRadians(0)));

        driveVector = new Vector();
        headingVector = new Vector();
    }

    @Override
    public void loop() {
        updateDrive();
        follower.update();
    }

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
}