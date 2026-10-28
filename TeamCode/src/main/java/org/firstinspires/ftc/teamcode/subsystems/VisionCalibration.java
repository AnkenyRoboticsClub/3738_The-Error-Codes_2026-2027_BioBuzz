package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.constants.RobotConstants;

/**
 * Gets info from robot LimeLight camera and converts to Pose2d that can be used in auton
 * <p>
 * Can also be used in TeleOp at a later time??j
 */
public class VisionCalibration {
    /* TODO:
     *  Add distanceAlign method?
     *  Config in browser? - Get Jack's help
     *  Convert limelight vector to Pose2d? - pose2dOutput
     */

    // Initialize hardware variables
    private final IMU imu;
    private final Limelight3A limelight;

    /**
     * Sets up the limelight and things using the provided robot hardware info.
     * @param hardwareMap Just say hardwareMap in the parenthesis for it to work.
     */
    public VisionCalibration(HardwareMap hardwareMap) {
        imu = hardwareMap.get(IMU.class, RobotConstants.IMU_NAME);
        limelight = hardwareMap.get(Limelight3A.class, RobotConstants.LIMELIGHT_NAME);

        // Initialize IMU
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RobotConstants.IMU_LOGO_DIRECTION,
                RobotConstants.IMU_USB_DIRECTION));
        imu.initialize(parameters);

        // Initialize Limelight
        limelight.setPollRateHz(100); // This sets how often we ask Limelight for data (100 times per second)
        limelight.start(); // This tells Limelight to start looking!

        limelight.pipelineSwitch(0); // Pipeline number - "preset" of settings on limelight config
    }

    /**
     * Oh, gosh darn. I don't know if this is right please help
     */
    public boolean rotateAlign(TankDriveTrain drive) {
        LLResult result = limelight.getLatestResult(); // Get latest picture

        // If the robot can't see anything, stop
        if (result == null && !result.isValid()) {
            drive.drive(0, 0);
            return false; // Failure, oh no.
        }

        double tx = result.getTx();

        // If tag is in the "good" window then it is aligned
        if (Math.abs(tx) <= RobotConstants.LL_DEGREES_ROTATION_ALIGNED) {
            drive.drive(0, 0);
            return true; // Success, yay!
        }

        // Find direction to spin robot
        if (tx > 0) { drive.drive(0, 0.15); } // Spin right
        else { drive.drive(0, -0.15); }

        return false; // Still turning...
    }
}
