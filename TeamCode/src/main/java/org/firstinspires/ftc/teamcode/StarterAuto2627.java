package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.constants.RobotConstants;
import org.firstinspires.ftc.teamcode.subsystems.PollenManipulator;
import org.firstinspires.ftc.teamcode.subsystems.TankDriveTrain;

@Autonomous
public class StarterAuto2627 extends LinearOpMode {
    // Starter bot Auto for the 2026-2027 season

    // Devices and systems
    private TankDriveTrain driveTrain;
    private PollenManipulator pollenManipulator;

    // Declare pollenPusher in OpMode class to properly initialize at start
    private Servo pollenPusher;

    // Auto control timer
    ElapsedTime autoTimer = new ElapsedTime();

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

            autoTimer.reset(); // Reset autoTimer

            // Autonomous code
            driveAuto(0.25, 0, 700); // Move off the wall

            // Launch all preloaded pollen
            while (opModeIsActive() && autoTimer.seconds() <= 15) {
                pollenManipulator.setState(PollenManipulator.State.LAUNCH_MANUAL_HALT);
                pollenManipulator.run(); // Run pollen manipulator
                updateDriveTelemetry();
            }

            // Stop pollen launching
            pollenManipulator.setState(PollenManipulator.State.HOLD);
        }
    }

    // Autonomous drive control helper method (with sleep time in ms)
    private void driveAuto(double driveY, double turn, int timeMs) {
        driveTrain.drive(driveY, turn);
        sleep(timeMs);
        driveTrain.drive(0, 0);
    }

    // Telemetry
    private void updateDriveTelemetry() {
        // Retrieve rotational angles and velocities
        YawPitchRollAngles orientation = driveTrain.getRobotYawPitchRollAngles();
        AngularVelocity angularVelocity = driveTrain.getRobotAngularVelocity();

        telemetry.addLine("Autonomous Mode");
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
