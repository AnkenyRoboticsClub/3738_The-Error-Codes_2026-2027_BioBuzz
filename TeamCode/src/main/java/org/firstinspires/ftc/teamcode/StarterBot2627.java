package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.subsystems.PollenManipulator;
import org.firstinspires.ftc.teamcode.subsystems.TankDriveTrain;

@TeleOp
public class StarterBot2627 extends LinearOpMode {
    // Starter bot OpMode for 2026-2027 season

    // Devices and systems
    private TankDriveTrain driveTrain;
    private PollenManipulator pollenManipulator;

    // Statuses
    private boolean xPressed = false;

    @Override
    public void runOpMode() {
        // Create instances of systems
        driveTrain = new TankDriveTrain(hardwareMap);
        pollenManipulator = new PollenManipulator(hardwareMap);

        telemetry.addData("Status", "Running");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Set drive inputs
            double driveY = -gamepad1.left_stick_y;
            double turn = gamepad1.right_stick_x;

            // Update launch button status
            boolean newLaunchPress = gamepad1.x && !xPressed;
            xPressed = gamepad1.x;

            // Switch pollen manipulator modes
            if (gamepad1.a) {
                // Intake pollen
                pollenManipulator.setState(PollenManipulator.State.INTAKING);
            } else if (newLaunchPress) {
                // Spin up and launch if hold or intaking
                if (pollenManipulator.getCurrentState() == PollenManipulator.State.HOLD
                        || pollenManipulator.getCurrentState() == PollenManipulator.State.INTAKING) {
                    pollenManipulator.setState(PollenManipulator.State.FLY_SPINUP);
                }
            } else if (pollenManipulator.getCurrentState() == PollenManipulator.State.INTAKING) {
                // Drop back to HOLD if not intaking or in launch sequence
                pollenManipulator.setState(PollenManipulator.State.HOLD);
            }

            // Reset imu direction on button press
            if (gamepad1.options) {
                driveTrain.imuResetYaw();
            }

            // Start system loops
            driveTrain.drive(driveY, turn);
            pollenManipulator.run();

            // Telemetry
            updateDriveTelemetry();
        }
    }

    // Telemetry
    private void updateDriveTelemetry() {
        // Retrieve rotational angles and velocities
        YawPitchRollAngles orientation = driveTrain.getRobotYawPitchRollAngles();
        AngularVelocity angularVelocity = driveTrain.getRobotAngularVelocity();

        telemetry.addLine("Hold a for intake, tap x for launch sequence");
        telemetry.addData("Current intake state", pollenManipulator.getCurrentState());
        telemetry.addLine();
        telemetry.addData("Left motor power", "%.2f", driveTrain.getLeftPower());
        telemetry.addData("Right motor power", "%.2f", driveTrain.getRightPower());
        telemetry.addLine();
        telemetry.addData("Yaw (Z)", "%.2f Deg. (Heading)", orientation.getYaw(AngleUnit.DEGREES));
        telemetry.update();
    }
}
