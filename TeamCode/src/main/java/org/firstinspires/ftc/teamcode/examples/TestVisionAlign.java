package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.subsystems.TankDriveTrain;
import org.firstinspires.ftc.teamcode.subsystems.VisionCalibration;

@Utility // Shows up in 3 dots -- "Utility OpModes" on Driver Station
public class TestVisionAlign extends LinearOpMode {
    private TankDriveTrain drive;
    private VisionCalibration vision;
    private ElapsedTime alignTimer = new ElapsedTime();

    @Override
    public void runOpMode() {
        drive = new TankDriveTrain(hardwareMap);
        vision = new VisionCalibration(hardwareMap);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            alignToTagWithTimeout(22, 999.0);

            // Launch, do other things...
        }
    }

    /**
     * Method that loops rotateAlign and distanceAlign until aligned or time runs out.
     */
    private void alignToTagWithTimeout(int targetTagId, double maxSeconds) {
        alignTimer.reset();

        while (opModeIsActive() && alignTimer.seconds() < maxSeconds) {
            boolean isAligned = vision.rotateAndDistanceAlign(drive, targetTagId);

            telemetry.addData("Target Tag", targetTagId);
            telemetry.addData("Aligned", isAligned);
            telemetry.addData("Time Left", "%.1f s", maxSeconds - alignTimer.seconds());
            telemetry.addData("Target Forward/Back Power", vision.getVisionDriveStraightSpeed());
            telemetry.addData("Target Turn Power", vision.getVisionTurnSpeed());
            telemetry.update();

            if (isAligned) {
                //break; // Exit once aligned
            }
        }

        // Always make sure motors are stopped when leaving the loop
        drive.drive(0, 0);
    }
}
