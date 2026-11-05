package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.constants.RobotConstants;
import org.firstinspires.ftc.teamcode.subsystems.PollenManipulator;

@TeleOp
public class LimelightTestWithServo extends LinearOpMode {
    // Limelight test with servo output

    // Pollen manipulator system
    private PollenManipulator pollenManipulator;

    // Initialize hardware variables
    private Limelight3A limelight;
    private double manualFlywheelTarget = 0.0; // Use flywheel for visual confirmation
    private final int TARGET_ID = 22; // Set to see sample AprilTag and nothing else

    @Override
    public void runOpMode() {
        // Set up systems and hardware
        pollenManipulator = new PollenManipulator(hardwareMap);

        limelight = hardwareMap.get(Limelight3A.class, RobotConstants.LIMELIGHT_NAME);

        // Initialize Limelight
        limelight.setPollRateHz(100); // This sets how often we ask Limelight for data (100 times per second)
        limelight.start(); // This tells Limelight to start looking

        limelight.pipelineSwitch(0); // Pipeline number - "preset" of settings on limelight config

        telemetry.addData("Status", "Running");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            // Code that runs once on start

            while (opModeIsActive()) {
                // Set flywheel spin speed based off y offset (degrees) from the limelight center
                double degreeOffset = 0.0; // Set to zero just in case that limelight does not see

                // Limelight magic
                LLResult result = limelight.getLatestResult();
                if (result != null && result.isValid()) {
                    // Only focus on one id
                    for (LLResultTypes.FiducialResult res : result.getFiducialResults()) {
                        if (res.getFiducialId() == TARGET_ID) {
                            // Found it!
                            telemetry.addData("Limelight", "Target found! ID: " + res.getFiducialId());
                            telemetry.addData("Target X", res.getTargetXDegrees());
                            telemetry.addData("Target Y", res.getTargetYDegrees());

                            // Now set degree offset to x degrees
                            degreeOffset = res.getTargetXDegrees();

                            break; // Get out! Stop checking for other tags!
                        }
                    }
                } else {
                    telemetry.addData("Limelight", "Can't find target");
                }

                manualFlywheelTarget = (degreeOffset*22.0);

                telemetry.addData("Flywheel output", manualFlywheelTarget);

                pollenManipulator.setManualControls(0.0, 0.0, manualFlywheelTarget);

                // Run pollen manipulator system
                pollenManipulator.run();

                telemetry.update();
            }
        }
    }
}
