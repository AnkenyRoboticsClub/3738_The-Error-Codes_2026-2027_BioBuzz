package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.constants.RobotConstants;

/**
 * Represents a pollen management system of one intake motor.
 * <p>
 * This includes manual control and auto intake which collects the pollen easier
 * <p>
 * This class handles hardware maps, initialization, and math required to manipulate POLLEN.
 * <blockquote>
 *     IT IS POLLEN NOT "BALLS"!
 * </blockquote>
 */
public class PollenManipulator {
    // Initialize hardware variables
    private final DcMotor pollenIntake;
    private final CRServo pollenFeeder;
    private final DcMotorEx flyWheel;

    private final ElapsedTime launchTimer = new ElapsedTime();

    private double manualIntakePower = 0.0;
    private double manualFeederPower = 0.0;
    private double manualFlyVelocity = 0.0;
    private boolean overrideActive = false;

    /**
     * HOLD: Everything stopped<br>
     * INTAKING: Intake running forward, flywheel stopped<br>
     * FLY_SPINUP: Flywheel running, intake stopped (waiting to reach speed)<br>
     * LAUNCH: Flywheel running, intake running forward to feed pollen into flywheel<br>
     * LAUNCH_MANUAL_HALT: Same as LAUNCH, does not automatically stop after launching one pollen
     */
    public enum State {
        HOLD,
        INTAKING,
        FLY_SPINUP,
        LAUNCH,
        LAUNCH_MANUAL_HALT,
        MANUAL_CONTROL
    }

    private State currentState = State.HOLD;

    public PollenManipulator(HardwareMap hardwareMap) {
        pollenIntake = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_INTAKE);
        pollenFeeder = hardwareMap.get(CRServo.class, RobotConstants.SERVO_FEEDER);
        flyWheel = hardwareMap.get(DcMotorEx.class, RobotConstants.MOTOR_FLY);

        // Change directions if they backwards
        pollenIntake.setDirection(DcMotorSimple.Direction.FORWARD);
        pollenFeeder.setDirection(DcMotorSimple.Direction.FORWARD);
        flyWheel.setDirection(DcMotorSimple.Direction.FORWARD);

        pollenIntake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        // Don't halt, let momentum keep going for graceful spindowns
        flyWheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    // State setter and getter

    /**
     * Set manipulator state
     * @param state See State enum
     */
    public void setState(State state) {
        if (overrideActive) {
            this.currentState = State.MANUAL_CONTROL;
        } else if (this.currentState != state) {
            this.currentState = state;
            if (state == State.LAUNCH) {
                // Reset launch timer
                launchTimer.reset();
            }
        }
    }

    /**
     * Get manipulator state (for telemetry)
     * @return Current state as a string
     */
    public State getCurrentState() {
        return this.currentState;
    }

    public double getFlywheelVelocity() {
        return flyWheel.getVelocity();
    }

    /**
     * Manual Override Interface for TeleOp
     * Calling this transitions the subsystem to MANUAL_OVERRIDE state.
     *
     * @param intakePower Power for the intake (-1.0 to 1.0)
     * @param feederPower Power for the feeder CRServo (-1.0 to 1.0)
     * @param flyWheelVelocity Target velocity for the flywheel
     */
    public void setManualControls(double intakePower, double feederPower, double flyWheelVelocity) {
        this.manualIntakePower = intakePower;
        this.manualFeederPower = feederPower;
        this.manualFlyVelocity = flyWheelVelocity;

        // Lock down state changing
        this.overrideActive = true;
        this.currentState = State.MANUAL_CONTROL;
    }

    /**
     * Disables manual override
     */
    public void disableOverride() {
        this.overrideActive = false;
        this.currentState = State.HOLD;
    }

    /**
     * Tells the robot to set the intake power to current state
     */
    public void run() {
        switch (currentState) {
            case HOLD:
                pollenIntake.setPower(0.0);
                pollenFeeder.setPower(0.0);
                flyWheel.setPower(0.0);
                break;
            case INTAKING:
                pollenIntake.setPower(RobotConstants.INTAKE_SPEED);
                pollenFeeder.setPower(0.0);
                flyWheel.setPower(0.0);
                break;
            case FLY_SPINUP:
                pollenIntake.setPower(0.0);
                pollenFeeder.setPower(0.0);
                flyWheel.setVelocity(RobotConstants.FLYWHEEL_TARGET_VELOCITY);

                // Wait for spinup then launch
                if (flyWheel.getVelocity() >= RobotConstants.FLYWHEEL_TARGET_VELOCITY - 20 &&
                        flyWheel.getVelocity() <= RobotConstants.FLYWHEEL_TARGET_VELOCITY + 20) {
                    setState(State.LAUNCH);
                }
                break;
            case LAUNCH:
                pollenIntake.setPower(0.0);
                // Keep wheel spinning, then push pollen in
                flyWheel.setVelocity(RobotConstants.FLYWHEEL_TARGET_VELOCITY);
                pollenFeeder.setPower(RobotConstants.FEEDER_SPEED);

                // Reset back to HOLD
                if (launchTimer.seconds() >= RobotConstants.ROBOT_LAUNCH_SECONDS) {
                    setState(State.HOLD);
                }
                break;
            case LAUNCH_MANUAL_HALT:
                pollenIntake.setPower(0.0);
                // Keep wheel spinning, then push pollen in
                flyWheel.setVelocity(RobotConstants.FLYWHEEL_TARGET_VELOCITY);

                // Wait for spinup then launch
                if (flyWheel.getVelocity() >= RobotConstants.FLYWHEEL_TARGET_VELOCITY - 20 &&
                        flyWheel.getVelocity() <= RobotConstants.FLYWHEEL_TARGET_VELOCITY + 20) {
                    pollenFeeder.setPower(RobotConstants.FEEDER_SPEED);
                    pollenIntake.setPower(RobotConstants.INTAKE_SPEED);
                } else {
                    pollenFeeder.setPower(0.0);
                    pollenIntake.setPower(0.0);
                }

                break;
            case MANUAL_CONTROL:
                pollenIntake.setPower(manualIntakePower);
                pollenFeeder.setPower(manualFeederPower);
                flyWheel.setVelocity(manualFlyVelocity);
                break;
        }
    }
}
