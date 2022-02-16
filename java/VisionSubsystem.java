package frc.robot.modules.vision.java;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.Subsystem;


public class VisionSubsystem extends VisionServer implements Subsystem {

	// better solution for this >>
	private static final VisionSubsystem subsystem_global = new VisionSubsystem();
	private VisionSubsystem() { 
		super(); 
		VisionServer.Sync(this);
	}
	public static VisionSubsystem Get() { return subsystem_global; }

	@Override public void periodic() {

	}

	private final IncrementCamera inc_camera = new IncrementCamera();
	private final DecrementCamera dec_camera = new DecrementCamera();
	private final IncrementPipeline inc_pipeline = new IncrementPipeline();
	private final DecrementPipeline dec_pipeline = new DecrementPipeline();
	private final ToggleStatistics toggle_stats = new ToggleStatistics();
	private final ToggleProcessing toggle_pipeline = new ToggleProcessing();

	public static IncrementCamera getCameraIncrementCommand() { return subsystem_global.inc_camera; }
	public static DecrementCamera getCameraDecrementCommand() { return subsystem_global.dec_camera; }
	public static IncrementPipeline getPipelineIncrementCommand() { return subsystem_global.inc_pipeline; }
	public static DecrementPipeline getPipelineDecrementCommand() { return subsystem_global.dec_pipeline; }
	public static ToggleStatistics getToggleStatisticsCommand() { return subsystem_global.toggle_stats; }
	public static ToggleProcessing getProcessingToggleCommand() { return subsystem_global.toggle_pipeline; }

	private class InstantGlobal extends CommandBase {
		@Override public boolean isFinished() { return true; }
		@Override public boolean runsWhenDisabled() { return true; }
	}

	private class IncrementCamera extends InstantGlobal {
		public IncrementCamera() {}
		@Override public void initialize() { 
			System.out.println("INCREMENT CAMERA");
			incrementCamera(); 
		}
	}
	private class DecrementCamera extends InstantGlobal {
		public DecrementCamera() {}
		@Override public void initialize() { 
			System.out.println("DECREMENT CAMERA");
			decrementCamera(); 
		}
	}
	private class IncrementPipeline extends InstantGlobal {
		public IncrementPipeline() {}
		@Override public void initialize() { 
			System.out.println("INCREMENT PIPELINE");
			incrementPipeline(); }
	}
	private class DecrementPipeline extends InstantGlobal {
		public DecrementPipeline() {}
		@Override public void initialize() { 
			System.out.println("DECREMENT PIPELINE");
			decrementPipeline(); 
		}
	}
	private class ToggleStatistics extends InstantGlobal {
		public ToggleStatistics() {}
		@Override public void initialize() { 
			System.out.println("TOGGLE STATISTICS");
			toggleStatistics(); 
		}
	}
	private class ToggleProcessing extends InstantGlobal {
		public ToggleProcessing() {}
		@Override public void initialize() { 
			System.out.println("TOGGLE PROCESSING");
			toggleProcessingEnabled(); 
		}
	}


}