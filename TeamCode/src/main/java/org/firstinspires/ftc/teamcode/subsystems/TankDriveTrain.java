package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.constants.RobotConstants;

/**
 * Represents a two-motor tank drivetrain on a robot.
 * <p>
 * This class handles hardware maps, initialization, and movement math required to drive a robot.
 */
public class TankDriveTrain {
    // Initialize hardware variables
    private final DcMotor leftDrive;
    private final DcMotor rightDrive;
    private final IMU imu;

    /**
     * Sets up the motors and stuff using the provided robot hardware info.
     * @param hardwareMap Just say hardwareMap in the parenthesis for it to work.
     */
    public TankDriveTrain(HardwareMap hardwareMap) {
        // Map motors to driver hub names
        leftDrive = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_TANK_LEFT);
        rightDrive = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_TANK_RIGHT);

        imu = hardwareMap.get(IMU.class, RobotConstants.IMU_NAME);

        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RobotConstants.IMU_LOGO_DIRECTION,
                RobotConstants.IMU_USB_DIRECTION));
        imu.initialize(parameters);

        leftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // If using encoders, uncomment this for better power input accuracy
        /*leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);*/

        // Reverse direction as needed
        leftDrive.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    /**
     * Resets the IMU position (bot rotation) to zero.
     */
    public void imuResetYaw() {
        imu.resetYaw();
    }

    /**
     * Tells the robot "drive using these stick inputs".
     *
     * @param driveY Forward/back power input
     * @param turn Rotational power input
     */
    public void drive(double driveY, double turn) {
        driveY = applyDeadzone(driveY);
        turn = applyDeadzone(turn);

        turn *= RobotConstants.TURN_MULTIPLIER;

        double leftPower = driveY + turn;
        double rightPower = driveY - turn;

        double powerClip = Math.max(Math.abs(leftPower), Math.abs(rightPower));

        if (powerClip > 1.0) {
            leftPower /= powerClip;
            rightPower /= powerClip;
        }

        leftDrive.setPower(leftPower);
        rightDrive.setPower(rightPower);
    }

    public double getLeftPower() {
        return leftDrive.getPower();
    }

    public double getRightPower() {
        return rightDrive.getPower();
    }

    // IMU telemetry

    /**
     * Gets the angles of the driver hub.
     * <br>
     * For example, you can use this to detect if the robot is on a ramp.
     * @return Orientation object with orientation data.
     */
    public YawPitchRollAngles getRobotYawPitchRollAngles() {
        return imu.getRobotYawPitchRollAngles();
    }

    /**
     * Gets the rotational velocity (speed) of the robot.
     * <br>
     * For example, you can use this to detect if the robot is rotating fast or slow (like if it
     * flips over, oh no!).
     * @return Angular velocity object (data of how fast it's turning around the three axes).
     */
    public AngularVelocity getRobotAngularVelocity() {
        return imu.getRobotAngularVelocity(AngleUnit.DEGREES);
    }

    private double applyDeadzone(double input) {
        if (Math.abs(input) < RobotConstants.CONTROLLER_DEADZONE) {
            return 0;
        } else {
            return input;
        }
    }
}
