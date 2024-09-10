package org.firstinspires.ftc.teamcode.TeleOp;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.PedroPathing.follower.Follower;
import org.firstinspires.ftc.teamcode.PedroPathing.pathGeneration.BezierLine;
import org.firstinspires.ftc.teamcode.PedroPathing.pathGeneration.Path;
import org.firstinspires.ftc.teamcode.PedroPathing.pathGeneration.Point;

@TeleOp
public class PedroAutoAlignTeleOp extends OpMode {

    private Follower follower;

    @Override
    public void init() {
        follower = new Follower(hardwareMap, true);
        follower.setStartingPose(new Pose2d(0, 0, Math.toRadians(0)));
        follower.initialize();
    }

    @Override
    public void loop() {
        if (gamepad1.dpad_right) {
            follower.followPath(new Path(
                    new BezierLine(
                            new Point(0, 0, Point.CARTESIAN),
                            new Point(0, 0, Point.CARTESIAN)
                    )));
        }
        follower.update();
    }
}