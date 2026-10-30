package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.constants.RobotConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@TeleOp(group = "PathRecording")
public class RecordBioBuzzPath extends LinearOpMode {
    // BioBuzz Starter Bot Configuration
    private DcMotor leftDrive;
    private DcMotor rightDrive;
    private DcMotor flywheelMotor;
    private CRServo feederServo;
    private DcMotor intakeMotor;
    private Servo pusherServo;

    // Recording Setup
    private boolean isRecording = false;
    private boolean lastAState = false;
    private final ElapsedTime runtime = new ElapsedTime();
    private double nextRecordTime = 0.0;
    private final double RECORD_INTERVAL_SECS = 0.020; // 20ms (50Hz sampling)

    // RAM Buffer to prevent I/O blocking/lag during OpMode execution
    private final ArrayList<String> logBuffer = new ArrayList<>();

    @Override
    public void runOpMode() {
        // Hardware Mapping matching REV Starter Bot defaults
        leftDrive = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_TANK_LEFT);
        rightDrive = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_TANK_LEFT);
        flywheelMotor = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_FLY);
        feederServo = hardwareMap.get(CRServo.class, RobotConstants.SERVO_FEEDER);
        intakeMotor = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_INTAKE);
        pusherServo = hardwareMap.get(Servo.class, RobotConstants.SERVO_PUSHER);

        // Starter bot basic direction setup
        leftDrive.setDirection(DcMotor.Direction.REVERSE);
        rightDrive.setDirection(DcMotor.Direction.FORWARD);

        // Control Hub Scoped Storage Path (Safe for Android 10+)
        File logFile = AppUtil.getInstance().getSettingsFile("biobuzz_path.csv");

        telemetry.addData("Status", "Initialized. Press 'A' to Start Recording.");
        telemetry.update();

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {
            // This class does not implement robot subsystems
            // because it needs direct access to motor input powers.

            // 1. Tank Drive Controls
            double drive = -gamepad1.left_stick_y;
            double turn = gamepad1.right_stick_x;
            double leftPower = drive + turn;
            double rightPower = drive - turn;

            // Normalize Drivetrain Powers
            double max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
            if (max > 1.0) {
                leftPower /= max;
                rightPower /= max;
            }
            leftDrive.setPower(leftPower);
            rightDrive.setPower(rightPower);

            // 2. Flywheel Launcher Controls
            double flywheelPower = gamepad1.right_trigger;
            flywheelMotor.setPower(flywheelPower);

            // 3. Intake Controls
            double feederPower = 0.0;
            if (gamepad1.right_bumper) {
                feederPower = RobotConstants.FEEDER_SPEED;  // Feed in
            } else if (gamepad1.left_bumper) {
                feederPower = -RobotConstants.FEEDER_SPEED; // Reverse
            }
            feederServo.setPower(feederPower);
            intakeMotor.setPower(feederPower); // Just copy the feeder power, close enough

            // 4. Pollen Pusher Servo Arm Position
            double pusherPosition = RobotConstants.PUSHER_POS_DOWN; // Default retracted
            if (gamepad1.y) {
                pusherPosition = RobotConstants.PUSHER_POS_UP; // Fully Extended position
            }
            pusherServo.setPosition(pusherPosition);

            // 5. Record Toggle System (Gamepad 1 'A')
            boolean currentAState = gamepad1.a;
            if (currentAState && !lastAState) {
                isRecording = !isRecording;
                if (isRecording) {
                    nextRecordTime = runtime.seconds();
                }
            }
            lastAState = currentAState;

            // 6. Safe RAM Logging (No disk writes while active)
            if (isRecording && runtime.seconds() >= nextRecordTime) {
                nextRecordTime += RECORD_INTERVAL_SECS;
                // CSV Scheme: time, left, right, flywheel, feeder, pusher
                String line = String.format(java.util.Locale.US, "%.3f,%.2f,%.2f,%.2f,%.2f,%.2f",
                        runtime.seconds(), leftPower, rightPower, flywheelPower, feederPower, pusherPosition);
                logBuffer.add(line);
            }

            telemetry.addData("Recording Active", isRecording);
            telemetry.addData("Buffered Frames", logBuffer.size());
            telemetry.addData("Time Elapsed (s)", Math.round(runtime.seconds() * 100.0) / 100.0);
            telemetry.update();
        }

        // 7. Flush data to disk safely AFTER OpMode stops running
        if (!logBuffer.isEmpty()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, false))) {
                for (String line : logBuffer) {
                    writer.println(line);
                }
                writer.flush();
                telemetry.log().add("Successfully saved path to: " + logFile.getName());
            } catch (IOException e) {
                telemetry.log().add("File write error: " + e.getMessage());
            }
            telemetry.update();
        }
    }
}
