package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.constants.RobotConstants;
import org.firstinspires.ftc.teamcode.subsystems.PollenManipulator;
import org.firstinspires.ftc.teamcode.subsystems.TankDriveTrain;

@TeleOp
public class StarterBot2627 extends LinearOpMode {
    // Starter bot OpMode for 2026-2027 season

    // Devices and systems
    private TankDriveTrain driveTrain;
    private PollenManipulator pollenManipulator;

    // Declare pollenPusher in OpMode class to properly initialize at start
    private Servo pollenPusher;

    // Statuses
    private boolean xPressed = false;
    private double manualIntakeInput = 0.0;
    private double manualFeederInput = 0.0;
    private double manualFlywheelTarget = 0.0;


    @Override
    public void runOpMode() {
        // Create instances of systems
        driveTrain = new TankDriveTrain(hardwareMap);
        pollenManipulator = new PollenManipulator(hardwareMap);
        pollenPusher = hardwareMap.get(Servo.class, RobotConstants.SERVO_PUSHER);

        telemetry.addData("Status", "Running");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            // Code that runs once on start
            pollenPusher.setPosition(RobotConstants.PUSHER_POS_UP);

            while (opModeIsActive()) {
                // Set drive inputs
                double driveY = -gamepad1.left_stick_y;
                double turn = gamepad1.right_stick_x;

                // Update launch button status
                boolean newLaunchPress = gamepad1.x && !xPressed;
                xPressed = gamepad1.x;

                // ---------- Manual controls ----------
                // Set inputs to zero, so when input is released movement stops
                manualIntakeInput = 0.0;
                manualFeederInput = 0.0;
                manualFlywheelTarget = 0.0;

                if (gamepad1.right_bumper) {
                    manualIntakeInput = RobotConstants.INTAKE_SPEED; // Forward intake
                } else if (gamepad1.left_bumper) {
                    manualIntakeInput = -RobotConstants.INTAKE_SPEED; // Outtake
                }

                if (gamepad1.right_trigger > 0.5) {
                    manualFeederInput = RobotConstants.FEEDER_SPEED; // Forward feeder
                }

                if (gamepad1.left_trigger > 0.5) {
                    manualFlywheelTarget = RobotConstants.FLYWHEEL_TARGET_VELOCITY; // Run manual flywheel
                }

                // Check if manual controls are active
                boolean isManualInputActive = (Math.abs(manualIntakeInput) > 0.05) ||
                        (Math.abs(manualFeederInput) > 0.05) ||
                        (Math.abs(manualFlywheelTarget) > 0.05);

                // If active, set motor powers
                if (isManualInputActive) {
                    pollenManipulator.setManualControls(manualIntakeInput, manualFeederInput, manualFlywheelTarget);
                } else if (pollenManipulator.getCurrentState() == PollenManipulator.State.MANUAL_CONTROL || gamepad1.b) {
                    // If no manual controls are being given, stop overriding to allow for regular pollen manipulation
                    pollenManipulator.disableOverride();
                }

                // ---------- Switch pollen manipulator modes ----------
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

                // Servo control for poking out pollen
                if (gamepad1.dpad_down) {
                    pollenPusher.setPosition(RobotConstants.PUSHER_POS_DOWN);
                } else if (gamepad1.dpad_up) {
                    pollenPusher.setPosition(RobotConstants.PUSHER_POS_UP);
                }

                // Telemetry
                updateDriveTelemetry();
            }
        }
    }

    // Telemetry
    private void updateDriveTelemetry() {
        // Retrieve rotational angles and velocities
        YawPitchRollAngles orientation = driveTrain.getRobotYawPitchRollAngles();
        AngularVelocity angularVelocity = driveTrain.getRobotAngularVelocity();

        telemetry.addLine("Hold a for intake, tap x for launch sequence");
        telemetry.addData("Current intake state", pollenManipulator.getCurrentState());
        telemetry.addData("Intake velocity", (int) pollenManipulator.getFlywheelVelocity());
        telemetry.addLine();
        telemetry.addData("Left motor power", "%.2f", driveTrain.getLeftPower());
        telemetry.addData("Right motor power", "%.2f", driveTrain.getRightPower());
        telemetry.addLine();
        telemetry.addData("Yaw (Z)", "%.2f Deg. (Heading)", orientation.getYaw(AngleUnit.DEGREES));
        telemetry.update();
    }
}
