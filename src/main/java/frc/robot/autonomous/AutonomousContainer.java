package frc.robot.autonomous;

import java.util.List;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.RobotContainer;
import frc.robot.Constants.DrivetrainConstants;
import frc.robot.subsystems.Drivetrain;

public class AutonomousContainer {
    private static TrajectoryConfig config = new TrajectoryConfig(3, 5) // These numbers literally do not matter
            .setKinematics(Drivetrain.kinematics)
            .addConstraint(new DifferentialDriveVoltageConstraint(Drivetrain.feedforward, Drivetrain.kinematics, 9));

    Trajectory exampleTrajectory;

    private AutonomousContainer() {
        // An example trajectory to follow.  All units in meters.
        exampleTrajectory = TrajectoryGenerator.generateTrajectory(
        // Start at the origin facing the +X direction
        new Pose2d(0, 5, new Rotation2d(0)),
        // Pass through these two interior waypoints, making an 's' curve path
        List.of(
            new Translation2d(1, 5)
        ),
        // End 3 meters straight ahead of where we started, facing forward
        new Pose2d(2, 5, new Rotation2d(0)),
        // Pass config
        config
    );
    }

    private static AutonomousContainer instance;
    public static AutonomousContainer getInstance() {
        if (instance == null) instance = new AutonomousContainer();
        return instance;
    }

    public Trajectory getAutonomousTrajectory() {
        return exampleTrajectory;
    }

    public Command getAutonomousCommand() {
        return new TrajectoryTracker(exampleTrajectory).andThen( ()-> Drivetrain.setOpenLoop(0.0, 0.0), RobotContainer.drivetrain);
    }
}