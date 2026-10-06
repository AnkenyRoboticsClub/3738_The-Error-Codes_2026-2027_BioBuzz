package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
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
    private final Servo pollenPusher;
    private final DcMotorEx flyWheel;

    private final ElapsedTime launchTimer = new ElapsedTime();

    /**
     * HOLD: Everything stopped
     * INTAKING: Intake running forward, flywheel stopped
     * FLY_SPINUP: Flywheel running, intake stopped (waiting to reach speed)
     * LAUNCH: Flywheel running, intake running forward to feed pollen into flywheel
     */
    public enum State {
        HOLD,
        INTAKING,
        FLY_SPINUP,
        LAUNCH
    }

    private State currentState = State.HOLD;

    public PollenManipulator(HardwareMap hardwareMap) {
        pollenIntake = hardwareMap.get(DcMotor.class, RobotConstants.MOTOR_INTAKE);
        pollenFeeder = hardwareMap.get(CRServo.class, RobotConstants.SERVO_FEEDER);
        pollenPusher = hardwareMap.get(Servo.class, RobotConstants.SERVO_PUSHER);
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
        if (this.currentState != state) {
            if (state == State.LAUNCH) {
                // Reset launch timer
                launchTimer.reset();
            }
            this.currentState = state;
        }
    }

    /**
     * Get manipulator state (for telemetry)
     * @return Current state as a string
     */
    public State getCurrentState() {
        return this.currentState;
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
        }
    }
}
