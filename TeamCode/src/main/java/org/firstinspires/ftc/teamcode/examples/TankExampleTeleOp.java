package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.subsystems.TankDriveTrain;

@Disabled
@TeleOp
public class TankExampleTeleOp extends LinearOpMode {
    // The purpose of this file is to control the robot systems with inputs.
    // NO hardware maps should be here, they are ALREADY SET UP.

    // Enum for drive modes
    public enum DriveControlMode {
        STANDARD,
        LEFT_HANDED,
        ARCADE_LEFT,
        TANK_TRADITIONAL
    }

    // Set default drive mode
    private DriveControlMode gamepadStickMode = DriveControlMode.STANDARD;

    private TankDriveTrain driveTrain;

    // Prevent holding down switch button from cycling list very fast
    private boolean lastDpadUp = false;

    @Override
    public void runOpMode() {
        // Map drivetrain
        driveTrain = new TankDriveTrain(hardwareMap);

        // This code runs after pushing INIT on driver station
        telemetry.addData("Status", "Running");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Switch drive modes
            if (gamepad1.dpad_up && !lastDpadUp) {
                switch (gamepadStickMode) {
                    case STANDARD: gamepadStickMode = DriveControlMode.LEFT_HANDED; break;
                    case LEFT_HANDED: gamepadStickMode = DriveControlMode.ARCADE_LEFT; break;
                    case ARCADE_LEFT: gamepadStickMode = DriveControlMode.TANK_TRADITIONAL; break;
                    case TANK_TRADITIONAL: gamepadStickMode = DriveControlMode.STANDARD; break;
                }
            }
            lastDpadUp = gamepad1.dpad_up;

            double driveY = 0;
            double turn = 0;

            switch(gamepadStickMode) {
                case STANDARD:
                    driveY = -gamepad1.left_stick_y;
                    turn = gamepad1.right_stick_x;
                    break;

                case LEFT_HANDED:
                    driveY = -gamepad1.right_stick_y;
                    turn = gamepad1.left_stick_x;
                    break;

                case ARCADE_LEFT:
                    // Single stick control on the left side
                    driveY = -gamepad1.left_stick_y;
                    turn = gamepad1.left_stick_x;
                    break;

                case TANK_TRADITIONAL:
                    double leftPower = -gamepad1.left_stick_y;
                    double rightPower = -gamepad1.right_stick_y;

                    // Turn tank stick drive into driveTrain format
                    driveY = (leftPower + rightPower) / 2.0;
                    turn = (leftPower - rightPower) / 2.0;
                    break;
            }

			// Reset imu direction on button press
            if (gamepad1.options) {
                driveTrain.imuResetYaw();
            }

            // Drive robot
            driveTrain.drive(driveY, turn);

            // Update telemetry
            updateDriveTelemetry();
        }
    }

    // Telemetry
    private void updateDriveTelemetry() {
        // Retrieve rotational angles and velocities
        YawPitchRollAngles orientation = driveTrain.getRobotYawPitchRollAngles();
        AngularVelocity angularVelocity = driveTrain.getRobotAngularVelocity();

        telemetry.addData("Current drive mode", gamepadStickMode);
        telemetry.addLine();
        telemetry.addData("Left motor power", "%.2f", driveTrain.getLeftPower());
        telemetry.addData("Right motor power", "%.2f", driveTrain.getRightPower());
        telemetry.addLine();
        telemetry.addData("Yaw (Z)", "%.2f Deg. (Heading)", orientation.getYaw(AngleUnit.DEGREES));
		telemetry.update();
	}
}