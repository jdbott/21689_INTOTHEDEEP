package org.firstinspires.ftc.teamcode.PedroPathing.tuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.HardwareClasses.KickoffArm;
import org.firstinspires.ftc.teamcode.Legacy.HardwareClasses.OldRobotConstants;
import org.firstinspires.ftc.teamcode.PedroPathing.follower.Follower;
import org.firstinspires.ftc.teamcode.PedroPathing.pathGeneration.BezierCurve;
import org.firstinspires.ftc.teamcode.PedroPathing.pathGeneration.BezierLine;
import org.firstinspires.ftc.teamcode.PedroPathing.pathGeneration.Path;
import org.firstinspires.ftc.teamcode.PedroPathing.pathGeneration.Point;
import org.firstinspires.ftc.teamcode.PedroPathing.util.Timer;

@Config
@Autonomous(name = "Complex Path Tuning", group = "Pedro Auto")
public class ComplexPathTuning extends OpMode {

    private final Point startPose = OldRobotConstants.RED_BACKDROP_START_POSE;
    private final Point spikeMarkMiddlePose = OldRobotConstants.RED_TO_LEFT_SPIKE_MARK_MIDDLE_POSE;
    private final Point spikeMarkPose = OldRobotConstants.RED_LEFT_SPIKE_MARK;
    private final Point backdropPose = OldRobotConstants.RED_LEFT_BACKDROP;
    private final Point toStartMiddlePose = OldRobotConstants.RED_CORNER_PARKING;

    private Telemetry telemetryA;
    private Follower follower;
    private KickoffArm kickoffArm;
    private Timer pathTimer;
    private Path toSpikeMark, toBackdrop, backToStart;
    private int pathState;

    @Override
    public void init() {
        follower = new Follower(hardwareMap, true);
        follower.setStartingPose(new Pose2d(15, -63, Math.toRadians(90)));
        kickoffArm = new KickoffArm(hardwareMap);
        telemetryA = new MultipleTelemetry(this.telemetry, FtcDashboard.getInstance().getTelemetry());
        pathTimer = new Timer();
        buildPaths();
        pathState = 0;
        kickoffArm.CloseGripperCompletely();
        telemetryA.addData("Status:", "initialized");
        telemetryA.update();
    }

    @Override
    public void start() {
        follower.followPath(toSpikeMark, true);
        kickoffArm.moveArmToPosition(75);
        setPathState(0);
        resetRuntime();
    }

    @Override
    public void loop() {
        try {
            follower.update();
        } catch (NullPointerException e) {
            telemetryA.addData("Error", "NullPointerException in follower.update()");
            telemetryA.addData("Message", e.getMessage());
            telemetryA.update();
        }
        telemetryA.update();
        autoPathUpdate();
        kickoffArm.update();
    }

    public void autoPathUpdate() {
        switch (pathState) {
            case 0:
                if (!follower.isBusy() && !kickoffArm.isArmMotorBusy()) {
                    kickoffArm.OpenGripperAtBottom();
                    setPathState(1);
                }
                break;

            case 1:
                if (pathTimer.getElapsedTimeSeconds() > 0.25) {
                    follower.followPath(toBackdrop, true);
                    kickoffArm.CloseGripperCompletely();
                    kickoffArm.moveArmToPosition(1200);
                    setPathState(2);
                }
                break;

            case 2:
                if (!follower.isBusy() && !kickoffArm.isArmMotorBusy()) {
                    kickoffArm.OpenGripperCompletely();
                    setPathState(3);
                }
                break;

            case 3:
                if (pathTimer.getElapsedTimeSeconds() > 0.25) {
                    follower.followPath(backToStart, true);
                    kickoffArm.CloseGripperCompletely();
                    kickoffArm.moveArmToPosition(75);
                    setPathState(4);
                }
                break;

            case 4:
                if (!follower.isBusy() && !kickoffArm.isArmMotorBusy()) {
                    setPathState(5);
                }
                break;

            case 5:
                if (pathTimer.getElapsedTimeSeconds() > 1) {
                    follower.followPath(toSpikeMark, true);
                    setPathState(0);
                }
                break;

            default:
                requestOpModeStop();
                break;
        }
    }

    public void setPathState(int state) {
        pathState = state;
        pathTimer.resetTimer();
    }

    public void buildPaths() {
        toSpikeMark = new Path(new BezierCurve(
                startPose,
                spikeMarkMiddlePose,
                spikeMarkPose));
        toSpikeMark.setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(137.6));

        toBackdrop = new Path(new BezierLine(
                spikeMarkPose,
                backdropPose));
        toBackdrop.setLinearHeadingInterpolation(Math.toRadians(137.6), Math.toRadians(180));
        toBackdrop.setZeroPowerAccelerationMultiplier(0.1);

        backToStart = new Path(new BezierCurve(
                backdropPose,
                toStartMiddlePose,
                startPose));
        backToStart.setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(90));
    }
}