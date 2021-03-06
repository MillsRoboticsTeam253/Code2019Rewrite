package frc.robot.commands;

import java.util.Set;

import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.Constants;
import frc.robot.RobotContainer;
import frc.robot.Constants.DriverConstants;
import frc.robot.Constants.DrivetrainConstants;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Drivetrain.WheelState;

public class Drive implements Command {
    private State state;

    public Drive(State state) {
        this.state = state;
    }

    Subsystem[] requirements = { RobotContainer.drivetrain };

    // Every time execute() is called by the Scheduler, the current XBox controller
    // joystick values are used to calculate motor speeds
    public void execute() {

        // Retrieving the deadbanded throttle and turn values (the controller joystick values)
        double throttle = RobotContainer.getThrottleValue();
        double turn = RobotContainer.getTurnValue();

        WheelState wheelSpeeds;
        double left, right;

        switch (state) {
            case OpenLoop:
                // Differential drive as long as throttle is greater than zero (deadbanded).
                if (throttle != 0) {
                    left = throttle + throttle * turn * DriverConstants.kTurnSens;
                    right = throttle - throttle * turn * DriverConstants.kTurnSens;

                // Turns in place when there is no throttle input
                } else {
                    left = turn * DriverConstants.kTurnInPlaceSens;
                    right = -turn * DriverConstants.kTurnInPlaceSens;
                }

                Drivetrain.setOpenLoop(left, right, false);
                break;

            case CheesyDriveClosedLoop:

                if (throttle != 0) {
                    wheelSpeeds = Drivetrain.DifferentialDrive.curvatureDrive(throttle, turn, false);
                } else {
                    wheelSpeeds = Drivetrain.DifferentialDrive.curvatureDrive(throttle, turn, true);
                }

                left = wheelSpeeds.left * DrivetrainConstants.kTopSpeedMPS;
                right = wheelSpeeds.right * DrivetrainConstants.kTopSpeedMPS;

                Drivetrain.setClosedLoop(left, right);
                break;

            case CheesyDriveOpenLoop:

                SmartDashboard.putNumber("throttle", throttle);
                SmartDashboard.putNumber("turn", turn);
                throttle *= Constants.DrivetrainConstants.kTopSpeedMPS;
                turn *= Constants.DriverConstants.kMaxTurnRateRadians;
                
                DifferentialDriveWheelSpeeds speeds = Drivetrain.kinematics.toWheelSpeeds(new ChassisSpeeds(throttle, 0.0, turn));
                double scaling_factor = Math.max(1, Math.max(speeds.leftMetersPerSecond, speeds.rightMetersPerSecond));
    
                SmartDashboard.putNumber("l_effort", speeds.leftMetersPerSecond/Constants.DrivetrainConstants.kTopSpeedMPS);
                SmartDashboard.putNumber("r_effort", speeds.rightMetersPerSecond/Constants.DrivetrainConstants.kTopSpeedMPS);

                Drivetrain.setOpenLoop(speeds.leftMetersPerSecond/Constants.DrivetrainConstants.kTopSpeedMPS, speeds.rightMetersPerSecond/Constants.DrivetrainConstants.kTopSpeedMPS, false);
                break;

        }

    }

    // Because this Command is default, it never needs to end -- it will simply be
    // interrupted whenever another Command requires the drivetrain
    @Override
    public boolean isFinished() {
        return false;
    }

    // When this command ends, it stops the drivetrain to guarantee safety
    @Override
    public void end(boolean interrupted) {
        Drivetrain.stopMotors();
    }

    public enum State {
        OpenLoop, CheesyDriveOpenLoop, CheesyDriveClosedLoop
    }

    @Override
    public Set<Subsystem> getRequirements() {
        return Set.of(requirements);
    }
}
