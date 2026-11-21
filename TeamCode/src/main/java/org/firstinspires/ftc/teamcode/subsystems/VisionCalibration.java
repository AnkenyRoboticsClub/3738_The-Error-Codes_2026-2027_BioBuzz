package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.constants.RobotConstants;

import java.util.List;

/**
 * Gets info from robot LimeLight camera
 * <p>
 * Can also be used in TeleOp at a later time??
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

    // Speed multiplier
    double speedDegreeMultiplier = 0.04;
    double tagXTelem = 0.0;
    double tagYTelem = 0.0;

    /**
     * Sets up the limelight and things using the provided robot hardware info.
     *
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

    // OVERLOADED METHODS EXPLAINED
    /*
     * These methods are the SAME... except...
     * the one with only targetId searches for one tag.
     * the one with 2, searches for two.
     * They have the same name.
     * The number of parameters you pass will be the one that is called.
     */
    public boolean rotateAndDistanceAlign(TankDriveTrain drive, int targetId) {
        LLResult result = limelight.getLatestResult(); // Get latest picture
        FiducialResult targetTag = null;

        // Find the target tag
        List<FiducialResult> tags = result.getFiducialResults();
        for (FiducialResult tag : tags) {
            if (tag.getFiducialId() == targetId) {
                targetTag = tag; // Found single tag
            }
        }

        // Drive output, return what it returns.
        return setDriveOutputs(drive, result, targetTag);
    }

    public boolean rotateAndDistanceAlign(TankDriveTrain drive, int targetId1, int targetId2) {
        LLResult result = limelight.getLatestResult(); // Get latest picture
        FiducialResult targetTag = null;

        // Find the target tag
        List<FiducialResult> tags = result.getFiducialResults();
        for (FiducialResult tag : tags) {
            if (tag.getFiducialId() == targetId1 || tag.getFiducialId() == targetId2) {
                targetTag = tag; // Found one of the two tags
            }
        }

        // Drive output, return what it returns.
        return setDriveOutputs(drive, result, targetTag);
    }

    private boolean setDriveOutputs(TankDriveTrain drive, LLResult result, FiducialResult targetTag) {
        // If the robot can't see anything, stop
        if (result == null || !result.isValid() || targetTag == null) {
            drive.drive(0, 0);
            return false; // Failure, oh no.
        }

        double tx = targetTag.getTargetXDegrees();
        double ty = targetTag.getTargetYDegrees();

        // Telemetry
        tagXTelem = targetTag.getTargetXDegrees();
        tagYTelem = targetTag.getTargetYDegrees();

        // If tag is in the "good" window then it is aligned
        if (Math.abs(tx) <= RobotConstants.LL_DEGREES_ROTATION_ALIGNED &&
            Math.abs(ty) <= RobotConstants.LL_DEGREES_ROTATION_ALIGNED) {
            drive.drive(0, 0);
            return true; // Success, yay!
        }

        // Find direction to spin robot
        //if (tx > 0) { drive.drive(0, 0.15); } // Spin right
        //else { drive.drive(0, -0.15); }

        // Spin robot using curve (PID in a way)
        drive.drive(speedDegreeMultiplier * ty, speedDegreeMultiplier * tx); // Spin the way that tx and ty is

        return false; // Still turning...
    }

    public double getVisionTurnSpeed() {
        return speedDegreeMultiplier * tagXTelem;
    }

    public double getVisionDriveStraightSpeed() {
        return speedDegreeMultiplier * tagYTelem;
    }
}