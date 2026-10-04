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
 * Represents a four-motor Mecanum drivetrain on a robot.
 * <p>
 * This class handles hardware maps, initialization, and movement math required to drive a robot in
 * field-centric and robot-centric modes.
 * </p>
 * It also uses the internal IMU for orientation data.
 */
public class MecanumDriveTrain {
    // Enum is a nice way to select a robot drive mode
    enum DriveMode {
        FIELD_CENTRIC,
        ROBOT_CENTRIC
    }

    // Set initial drive mode here
    DriveMode driveMode = DriveMode.FIELD_CENTRIC;

    // Initialize hardware variables
    private final DcMotor leftBack;
    private final DcMotor rightBack;
    private final DcMotor rightFront;
    private final DcMotor leftFront;
    private final IMU imu;

    // This is a constructor, it gets the stuff from outside so it can do stuff inside this file.
    // Yes I know that is a bad explanation.

    /**
     * Sets up the motors and stuff using the provided robot hardware info.
     * @param hardwareMap Just say hardwareMap in the parenthesis for it to work.
     */
    public MecanumDriveTrain(HardwareMap hardwareMap) {
        // Map motors to driver hub names
        leftBack = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_LEFT_BACK);
        rightBack = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_RIGHT_BACK);
        rightFront = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_RIGHT_FRONT);
        leftFront = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_LEFT_FRONT);

        imu = hardwareMap.get(IMU.class, RobotConstants.IMU_NAME);

        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RobotConstants.IMU_LOGO_DIRECTION,
                RobotConstants.IMU_USB_DIRECTION));
        imu.initialize(parameters);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // If the encoders are present, uncomment this
		/*leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);*/

		// Reverse direction as needed
		leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
		leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
		//rightFront.setDirection(DcMotorSimple.Direction.REVERSE);
		//rightBack.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    // Now that the constructor is set up. we code the methods.
    // These blocks of code serve as custom "shortcuts" to make it easier for people to code.
    /* YOU CAN:
     * Reset the IMU position to set the front of the robot
     * Switch the drive mode
     * Get the current drive mode
     * Make the robot drive
     * AND NOTHING ELSE.
     * How simple is that???
     */

    /**
     * Resets the IMU position.
     * <br>
     * If in field-centric drive mode, this sets the front of the robot,
     * so if the stick is pressed forward the robot will drive straight ahead.
     * <br>
     * This is helpful if someone moves their body to another location and is still driving.
     */
    public void imuResetYaw() {
        imu.resetYaw();
    }

    /**
     * Toggles the current drive mode.
     * <br>
     * Switches the control state between field-centric and robot-centric driving.
     * <br>
     * This allows the driver to change steering during operation.
     */
    public void toggleDriveMode() {
        if (this.driveMode == DriveMode.FIELD_CENTRIC) {
            this.driveMode = DriveMode.ROBOT_CENTRIC;
        } else {
            this.driveMode = DriveMode.FIELD_CENTRIC;
        }
    }

    /**
     * Retrieves the current drive mode.
     * <br>
     * Looks at the drive mode and returns either "Field-Centric" or "Robot-Centric".
     * <br>
     * This is used for telemetry output on the driver station.
     *
     * @return A string stating the active driving configuration.
     */
    public String getDriveMode() {
        if (this.driveMode == DriveMode.FIELD_CENTRIC) {
            return "Field-Centric";
        } else {
            return "Robot-Centric";
        }
    }

    /**
     * Tells the robot "drive using these stick inputs".
     *
     * @param driveX Strafe power input
     * @param driveY Forward/back power input
     * @param turn Rotational power input
     */
    public void drive(double driveX, double driveY, double turn) {
        if (driveMode == DriveMode.FIELD_CENTRIC) {
            driveFieldCentric(driveX, driveY, turn);
        } else {
            driveRobotCentric(driveX, driveY, turn);
        }
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

    // INTERNAL METHODS
    // These are inaccessible, you cannot call them manually from the TeleOp or Auton.
    // These are for organizational purposes.
    private void driveFieldCentric(double driveX, double driveY, double turn) {
        driveX = applyDeadzone(driveX);
        driveY = applyDeadzone(driveY);
        turn = applyDeadzone(turn);

        turn *= RobotConstants.TURN_MULTIPLIER;
        driveX *= RobotConstants.STRAFE_MULTIPLIER;

        double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

        // Do math or something, I don't know
        double rotX = driveX * Math.cos(-botHeading) - driveY * Math.sin(-botHeading);
        double rotY = driveX * Math.sin(-botHeading) + driveY * Math.cos(-botHeading);

        setMotorOutputs(rotX, rotY, turn);
    }

    private void driveRobotCentric(double driveX, double driveY, double turn) {
        driveX = applyDeadzone(driveX);
        driveY = applyDeadzone(driveY);
        turn = applyDeadzone(turn);

        turn *= RobotConstants.TURN_MULTIPLIER;
        driveX *= RobotConstants.STRAFE_MULTIPLIER;

        setMotorOutputs(driveX, driveY, turn);
    }

    private void setMotorOutputs(double driveX, double driveY, double turn) {
        double leftFrontPower = driveY + driveX + turn;
		double rightFrontPower = driveY - driveX - turn;
		double leftBackPower = driveY - driveX + turn;
		double rightBackPower = driveY + driveX - turn;

		double powerClip = Math.max(Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower)),
                Math.max(Math.abs(leftBackPower), Math.abs(rightBackPower)));

		if (powerClip > 1.0) {
			leftFrontPower /= powerClip;
			rightFrontPower /= powerClip;
			leftBackPower /= powerClip;
			rightBackPower /= powerClip;
		}

		leftFront.setPower(leftFrontPower);
		rightFront.setPower(rightFrontPower);
		leftBack.setPower(leftBackPower);
		rightBack.setPower(rightBackPower);
    }

    private double applyDeadzone(double input) {
        if (Math.abs(input) < RobotConstants.CONTROLLER_DEADZONE) {
            return 0;
        } else {
            return input;
        }
    }
}
