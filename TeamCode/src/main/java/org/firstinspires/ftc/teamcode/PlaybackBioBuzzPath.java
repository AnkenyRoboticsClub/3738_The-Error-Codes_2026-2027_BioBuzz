package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.constants.RobotConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

@Autonomous
public class PlaybackBioBuzzPath extends LinearOpMode {

    // BioBuzz Starter Bot Configuration
    private DcMotor leftDrive;
    private DcMotor rightDrive;
    private DcMotorEx flywheelMotor;
    private CRServo feederServo;
    private DcMotor intakeMotor;
    private Servo pusherServo;

    // Structure to hold recorded frame details
    private static class DriveFrame {
        double timestamp;
        double leftPower;
        double rightPower;
        double flywheelPower;
        double feederPower;
        double pusherPosition;

        DriveFrame(double timestamp, double leftPower, double rightPower, double flywheelPower, double feederPower, double pusherPosition) {
            this.timestamp = timestamp;
            this.leftPower = leftPower;
            this.rightPower = rightPower;
            this.flywheelPower = flywheelPower;
            this.feederPower = feederPower;
            this.pusherPosition = pusherPosition;
        }
    }

    // Playback Buffer
    private final ArrayList<DriveFrame> playbackBuffer = new ArrayList<>();
    private final ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        // Hardware maps
        leftDrive = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_TANK_LEFT);
        rightDrive = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_TANK_RIGHT);
        flywheelMotor = hardwareMap.get(DcMotorEx.class, RobotConstants.MOTOR_FLY);
        feederServo = hardwareMap.get(CRServo.class, RobotConstants.SERVO_FEEDER);
        intakeMotor = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_INTAKE);
        pusherServo = hardwareMap.get(Servo.class, RobotConstants.SERVO_PUSHER);

        // Starter bot basic direction setup
        leftDrive.setDirection(DcMotor.Direction.REVERSE);
        rightDrive.setDirection(DcMotor.Direction.FORWARD);

        // Track path location
        File logFile = AppUtil.getInstance().getSettingsFile("biobuzz_path.csv");

        // Load and Parse the CSV during initialization to avoid loop lag
        telemetry.addData("Status", "Loading recording file...");
        telemetry.update();

        if (logFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String[] tokens = line.split(",");
                    if (tokens.length >= 6) {
                        double timestamp = Double.parseDouble(tokens[0]);
                        double leftPower = Double.parseDouble(tokens[1]);
                        double rightPower = Double.parseDouble(tokens[2]);
                        double flywheelPower = Double.parseDouble(tokens[3]);
                        double feederPower = Double.parseDouble(tokens[4]);
                        double pusherPosition = Double.parseDouble(tokens[5]);

                        playbackBuffer.add(new DriveFrame(timestamp, leftPower, rightPower, flywheelPower, feederPower, pusherPosition));
                    }
                }
                telemetry.addData("Status", "Successfully loaded path!");
                telemetry.addData("Total Frames", playbackBuffer.size());
            } catch (IOException | NumberFormatException e) {
                telemetry.addData("Error", "Failed to parse CSV: " + e.getMessage());
            }
        } else {
            telemetry.addData("Error", "No recording file found at " + logFile.getName());
        }
        telemetry.update();

        waitForStart();
        runtime.reset();

        int currentFrameIndex = 0;

        // Playback Loop
        while (opModeIsActive() && currentFrameIndex < playbackBuffer.size()) {
            double currentElapsedTime = runtime.seconds();
            DriveFrame currentFrame = playbackBuffer.get(currentFrameIndex);

            // Wait until the system time catches up to the recorded timestamp
            if (currentElapsedTime >= currentFrame.timestamp) {
                // Apply the recorded hardware instructions
                leftDrive.setPower(currentFrame.leftPower);
                rightDrive.setPower(currentFrame.rightPower);
                flywheelMotor.setVelocity(currentFrame.flywheelPower);
                feederServo.setPower(currentFrame.feederPower);
                intakeMotor.setPower(currentFrame.feederPower);
                pusherServo.setPosition(currentFrame.pusherPosition);

                // Advance to the next instruction frame
                currentFrameIndex++;
            }

            // Keep drivers updated on progress
            telemetry.addData("Status", "Playing Back Path");
            telemetry.addData("Progress", "%d / %d Frames", currentFrameIndex, playbackBuffer.size());
            telemetry.addData("Time (s)", "%.2f", currentElapsedTime);
            telemetry.update();
        }

        // Clean up: Stop all actions when path finishes or OpMode is aborted
        leftDrive.setPower(0);
        rightDrive.setPower(0);
        flywheelMotor.setPower(0);
        feederServo.setPower(0);
        intakeMotor.setPower(0);

        telemetry.addData("Status", "Playback Complete. Stopped.");
        telemetry.update();
    }
}
