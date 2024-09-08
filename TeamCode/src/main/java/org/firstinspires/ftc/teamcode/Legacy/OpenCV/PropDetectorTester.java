package org.firstinspires.ftc.teamcode.Legacy.OpenCV;//package org.firstinspires.ftc.teamcode.OpenCv;
//
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//
//import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
//import org.openftc.easyopencv.OpenCvCamera;
//import org.openftc.easyopencv.OpenCvCameraFactory;
//import org.openftc.easyopencv.OpenCvCameraRotation;
//@Autonomous(name = "PropDetectorTester", group = "Auto")
//public class PropDetectorTester extends LinearOpMode {
//    OpenCvCamera camera;
//    PropDetectionPipelineRedHSV PropDetector;
//    // Declare Motors and Servos.
//    @Override
//    public void runOpMode() throws InterruptedException {
//        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
//        camera= OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
//        PropDetectionPipelineRedHSV detector = new PropDetectionPipelineRedHSV(telemetry);
//        camera.setPipeline(detector);
//        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
//        {
//            @Override
//            public void onOpened()
//            {
//                telemetry.addLine("Starting streaming..");
//                camera.startStreaming(432,240, OpenCvCameraRotation.UPRIGHT);
//                telemetry.addLine("..Done");
//                telemetry.update();
//            }
//
//            @Override
//            public void onError(int errorCode) {
//                telemetry.addLine("Camera error" + errorCode);
//                telemetry.update();
//
//            }
//        });
//        telemetry.addLine("waiting..");
//
//        waitForStart();
//        telemetry.addLine("finished waiting");
//        telemetry.update();
//        while (opModeIsActive()) {
//            switch (detector.getAnalysis()) {
//                case CENTER:
//                    telemetry.addData("Prop Position Detected:", "Zone 2");
//                    telemetry.update();
//                    break;
//                case LEFT:
//                    telemetry.addData("Prop Position Detected:", "Zone 1");
//                    telemetry.update();
//                    break;
//                case RIGHT:
//                    telemetry.addData("Prop Position Detected:", "Zone 3");
//                    telemetry.update();
//                    break;
//                case UNKNOWN:
//                    telemetry.addData("Prop Position Detected:", "Unknown");
//                    telemetry.update();
//                    break;
//                default:
//                    telemetry.addData("Prop Default:", "Default");
//                    break;
//            }
//            sleep(100);
//        }
//    }
//}
