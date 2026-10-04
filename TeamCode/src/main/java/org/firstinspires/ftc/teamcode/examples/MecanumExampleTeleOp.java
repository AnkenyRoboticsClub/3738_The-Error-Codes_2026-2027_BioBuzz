package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.subsystems.MecanumDriveTrain;

@Disabled
@TeleOp
public class MecanumExampleTeleOp extends LinearOpMode {
    // The purpose of this file is to control the robot systems with inputs.
    // NO hardware maps should be here, they are ALREADY SET UP.

    // Initializing systems and controller input tracking
    private MecanumDriveTrain driveTrain;
    private boolean lastDpadUp = false;

    @Override
    public void runOpMode() {
        // Map drivetrain
        driveTrain = new MecanumDriveTrain(hardwareMap);

        // This code runs after pushing INIT on driver station
        telemetry.addData("Status", "Running");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            double driveX = gamepad1.left_stick_x;
            double driveY = -gamepad1.left_stick_y;
            double turn = gamepad1.right_stick_x;

            // Swap drive mode on keypress
			if (gamepad1.dpad_up && !lastDpadUp) {
				driveTrain.toggleDriveMode();
			}
			lastDpadUp = gamepad1.dpad_up;

			// Reset imu direction on button press
            if (gamepad1.options) {
                driveTrain.imuResetYaw();
            }

			// Drive robot
            driveTrain.drive(driveX, driveY, turn);

            // Update telemetry
            updateDriveTelemetry();
        }
    }

    // Telemetry LOL
    private void updateDriveTelemetry() {
        // Retrieve rotational angles and velocities
        YawPitchRollAngles orientation = driveTrain.getRobotYawPitchRollAngles();
        AngularVelocity angularVelocity = driveTrain.getRobotAngularVelocity();

        telemetry.addData("DRIVE MODE", driveTrain.getDriveMode());
        telemetry.addLine("Press D-Pad Up to Switch");
        telemetry.addLine();
        telemetry.addData("Yaw (Z)", "%.2f Deg. (Heading)", orientation.getYaw(AngleUnit.DEGREES));
        telemetry.addData("Pitch (X)", "%.2f Deg.", orientation.getPitch(AngleUnit.DEGREES));
        telemetry.addData("Roll (Y)", "%.2f Deg.\n", orientation.getRoll(AngleUnit.DEGREES));
        telemetry.addData("Yaw (Z) velocity", "%.2f Deg/Sec", angularVelocity.zRotationRate);
        telemetry.addData("Pitch (X) velocity", "%.2f Deg/Sec", angularVelocity.xRotationRate);
        telemetry.addData("Roll (Y) velocity", "%.2f Deg/Sec", angularVelocity.yRotationRate);
        telemetry.update();
	}
}