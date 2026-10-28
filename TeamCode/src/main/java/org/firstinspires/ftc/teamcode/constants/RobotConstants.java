package org.firstinspires.ftc.teamcode.constants;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;

/**
 * All robot constant values go here
 * <p>
 * Examples include robot drive speed multipliers,
 * motor names, or anything else that doesn't change
 */
public class RobotConstants {
    // Private empty constructor because this class doesn't need multiple instances
    private RobotConstants() {}

    // ---------- TANK DRIVE ----------
    public static final String MOTOR_TANK_LEFT = "leftDrive";
    public static final String MOTOR_TANK_RIGHT = "rightDrive";

    // ---------- MECANUM DRIVE ----------
    public static final String MOTOR_LEFT_BACK = "leftBack";
    public static final String MOTOR_RIGHT_BACK = "rightBack";
    public static final String MOTOR_RIGHT_FRONT = "rightFront";
    public static final String MOTOR_LEFT_FRONT = "leftFront";

    // ---------- ROBOT MISC ELECTRONICS ----------
    public static final String IMU_NAME = "imu";
    public static final String LIMELIGHT_NAME = "limelight";

    // To adjust logo and usb see
    // https://ftc-docs.firstinspires.org/en/latest/programming_resources/imu/imu.html
    public static final RevHubOrientationOnRobot.LogoFacingDirection IMU_LOGO_DIRECTION =
            RevHubOrientationOnRobot.LogoFacingDirection.UP;
    public static final RevHubOrientationOnRobot.UsbFacingDirection IMU_USB_DIRECTION =
            RevHubOrientationOnRobot.UsbFacingDirection.RIGHT;
    public static final String MOTOR_INTAKE = "intakeMotor";
    public static final String SERVO_FEEDER = "crServo";
    public static final String SERVO_PUSHER = "pollenServo";
    public static final String MOTOR_FLY = "flywheel";

    // ---------- LIMELIGHT NUMBERS ----------
    public static final double LL_DEGREES_ROTATION_ALIGNED = 1.5;

    // ---------- GAMEPAD ----------
    public static final double CONTROLLER_DEADZONE = 0.07;


    // ---------- ROBOT SPEEDS ----------
    public static final double STRAFE_MULTIPLIER = 1.1;
    public static final double TURN_MULTIPLIER = 0.7;
    public static final double INTAKE_SPEED = 0.5;
    public static final double FEEDER_SPEED = 1.0;

    public static final double FLYWHEEL_TARGET_VELOCITY = 1160;

    public static final double ROBOT_LAUNCH_SECONDS = 2.5;

    // ---------- ROBOT POSITIONS ----------
    public static final double PUSHER_POS_UP = 0.4;
    public static final double PUSHER_POS_DOWN = 0.0;
}
