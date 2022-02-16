package frc.robot.modules.vision.java;

import java.util.ArrayList;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTable;


public class VisionServer {

	public final NetworkTable 
		root, targets, cameras, pipelines;
	private final NetworkTableEntry 
		active_target, num_cams, cam_idx, num_pipes, pipe_idx;
	private ArrayList<VsCamera> vscameras = new ArrayList<VsCamera>();
	private ArrayList<VsPipeline> vspipelines = new ArrayList<VsPipeline>();

	// singleton
	protected VisionServer() {
		root = NetworkTableInstance.getDefault().getTable("Vision Server");
		targets = NetworkTableInstance.getDefault().getTable("Targets");
		cameras = root.getSubTable("Cameras");
		pipelines = root.getSubTable("Pipelines");

		active_target = root.getEntry("Active Target");
		num_cams = root.getEntry("Cameras Available");
		cam_idx = root.getEntry("Camera Index");
		num_pipes = root.getEntry("Pipelines Available");
		pipe_idx = root.getEntry("Pipeline Index");

		cameras.addSubTableListener(
			(parent, name, table) -> {
				this.updateCameras();
			}, false
		);
		pipelines.addSubTableListener(
			(parent, name, table) -> {
				this.updatePipelines();
			}, false
		);
	}
	public static VisionServer Get() { return VisionServer.global; }
	protected static void Sync(VisionServer inst) { global = inst; }
	private static VisionServer global = new VisionServer();

	public boolean areCamerasUpdated() { return this.vscameras.size() == (int)this.num_cams.getDouble(0.0); }
	public boolean arePipelinesUpdated() { return this.vspipelines.size() == (int)this.num_pipes.getDouble(0.0); }
	public void updateCameras() {
		System.out.println("Updated Cameras");
		this.vscameras.clear();
		int i  = 0;
		for(String subtable : this.cameras.getSubTables()) {
			this.vscameras.add(new VsCamera(this.cameras.getSubTable(subtable)));
			this.vscameras.get(i).idx = i;
			i++;
		}
	}
	public void updatePipelines() {
		System.out.println("Updated Pipelines");
		this.vspipelines.clear();
		int i = 0;
		for(String subtable : this.pipelines.getSubTables()) {
			this.vspipelines.add(new VsPipeline(this.pipelines.getSubTable(subtable)));
			this.vspipelines.get(i).idx = i;
			i++;
		}
	}
	public ArrayList<VsCamera> getCameras() { 
		// if(!this.areCamerasUpdated()) {
		// 	this.updateCameras();
		// }
		return this.vscameras; 
	}
	public ArrayList<VsPipeline> getPipelines() { 
		// if(!this.arePipelinesUpdated()) {
		// 	this.updatePipelines();
		// }
		return this.vspipelines; 
	}
	public VsCamera getCamera(int idx) { 
		// if(!this.areCamerasUpdated()) {
		// 	this.updateCameras();
		// }
		return idx < this.vscameras.size() ? idx > 0 ? this.vscameras.get(idx) : null : null; 
	}
	public VsCamera getCamera(String name) {
		// if(!this.areCamerasUpdated()) {
		// 	this.updateCameras();
		// }
		for(int i = 0; i < this.vscameras.size(); i++) {
			if(this.vscameras.get(i).name.equals(name)) {
				return this.vscameras.get(i);
			}
		}
		return null;
	}
	public int findCameraIdx(String name) {
		// if(!this.areCamerasUpdated()) {
		// 	this.updateCameras();
		// }
		for(int i = 0; i < this.vscameras.size(); i++) {
			if(this.vscameras.get(i).name.equals(name)) {
				return i;
			}
		}
		return -1;
	}
	public VsPipeline getPipeline(int idx) { 
		// if(!this.arePipelinesUpdated()) {
		// 	this.updatePipelines();
		// }
		return idx < this.vspipelines.size() ? idx >= 0 ? this.vspipelines.get(idx) : null : null; 
	}
	public VsPipeline getPipeline(String name) {
		// if(!this.arePipelinesUpdated()) {
		// 	this.updatePipelines();
		// }
		for(int i = 0; i < this.vspipelines.size(); i++) {
			if(this.vspipelines.get(i).name.equals(name)) {
				return this.vspipelines.get(i);
			}
		}
		return null;
	}
	public int findPipelineIdx(String name) {	// returns -1 on failure
		// if(!this.arePipelinesUpdated()) {
		// 	this.updatePipelines();
		// }
		for(int i = 0; i < this.vspipelines.size(); i++) {
			if(this.vspipelines.get(i).name.equals(name)) {
				return i;
			}
		}
		return -1;
	}
	public VsCamera getCurrentCamera() { 
		// if(!this.areCamerasUpdated()) {
		// 	this.updateCameras();
		// }
		return this.getCamera(this.root.getEntry("Camera Name").getString("none")); 
	}
	public VsPipeline getCurrentPipeline() { 
		// if(!this.arePipelinesUpdated()) {
		// 	this.updatePipelines();
		// }
		return this.getPipeline(this.getPipelineIdx()); 
	}

	public boolean applyCameraPreset(CameraPreset p) {
		boolean ret = this.vscameras.size() > 0;
		for(VsCamera camera : this.vscameras) {
			ret &= camera.applyPreset(p);
		}
		return ret;
	}

	public boolean getIsShowingStatistics() { return root.getEntry("Show Statistics").getBoolean(false); }
	public void setStatistics(boolean val) { root.getEntry("Show Statistics").setBoolean(val); }
	public void toggleStatistics() { this.setStatistics(!this.getIsShowingStatistics()); }

	public boolean getIsPipelineEnabled() { 
		if(root.containsKey("Enable Processing")) {
			return root.getEntry("Enable Processing").getBoolean(true);
		}
		return false;
	}
	public boolean setProcessingEnabled(boolean val) {
		if(root.containsKey("Enable Processing")) {
			return root.getEntry("Enable Processing").setBoolean(val);
		}
		return false;
	}
	public boolean toggleProcessingEnabled() {
		if(root.containsKey("Enable Processing")) {
			return root.getEntry("Enable Processing").setBoolean(!root.getEntry("Enable Processing").getBoolean(true));
		}
		return false;
	}

	public boolean hasActiveTarget() { return !active_target.getString("none").equals("none"); }	// returns "none" on error
	public NetworkTable getActiveTarget() { return targets.getSubTable(active_target.getString("none")); }	// returns "none" on error
	public String getActiveTargetName() { return active_target.getString("none"); }	// returns "none" on error
	public double getDistance() { return this.getActiveTarget().getEntry("distance").getDouble(0.0); }
	public double getThetaUD() { return this.getActiveTarget().getEntry("up-down").getDouble(0.0); }
	public double getThetaLR() { return this.getActiveTarget().getEntry("left-right").getDouble(0.0); }
	public TargetOffset getTargetPos() { return new TargetOffset(this.getActiveTarget()); }
	public TargetData getTargetData() { return new TargetData(this.getActiveTarget()); }
	public TargetData getTargetDataIfMatching(String target) {	// returns null on mismatch
		if(this.getActiveTargetName().equals(target)) {
			return this.getTargetData();
		}
		return null;
	}

	public int numCameras() { return (int)num_cams.getDouble(0.0); }	// returns 0 on failure
	public int getCameraIdx() { return (int)cam_idx.getDouble(-1.0); }	// returns -1 on failure
	public boolean setCamera(int idx) {		// returns whether the input index was valid or not
		return idx < this.numCameras() && idx >= 0 && cam_idx.setDouble(idx);
	}
	public boolean setCamera(String name) {
		int i = 0;
		for(String c : this.cameras.getSubTables()) {
			if(c.equals(name)) {
				return this.setCamera(i);
			}
			i++;
		}
		return false;
	}
	public boolean setCamera(VsCamera cam) {
		return this.setCamera(cam.name);
	}
	public boolean incrementCamera() {
		int idx = this.getCameraIdx();
		if(idx + 1 < this.numCameras()) {
			cam_idx.setDouble(idx + 1);
			return true;
		}
		cam_idx.setDouble(0.0);	// wrap around
		return false;
	}
	public boolean decrementCamera() {
		int idx = this.getCameraIdx();
		if(idx - 1 >= 0) {
			cam_idx.setDouble(idx - 1);
			return true;
		}
		cam_idx.setDouble(this.numCameras() - 1);	// wrap around
		return false;
	}
	public int numPipelines() { return (int)num_pipes.getDouble(0.0); }		// returns 0 on failure
	public int getPipelineIdx() { return (int)pipe_idx.getDouble(-1.0); }	// returns -1 on failure
	public boolean setPipeline(int idx) {	// returns whether the input index was valid or not
		return idx < this.numPipelines() && idx >= 0 && pipe_idx.setDouble(idx);
	}
	public boolean setPipeline(String name) {
		int i = 0;
		for(String p : this.pipelines.getSubTables()) {
			if(p.equals(name)) {
				return this.setPipeline(i);
			}
			i++;
		}
		return false;
	}
	public boolean setPipeline(VsPipeline pipe) {
		return this.setPipeline(pipe.name);
	}
	public boolean incrementPipeline() {
		int idx = this.getPipelineIdx();
		if(idx + 1 < this.numPipelines()) {
			pipe_idx.setDouble(idx + 1);
			return true;
		}
		pipe_idx.setDouble(0.0);	// wrap around
		return false;
	}
	public boolean decrementPipeline() {
		int idx = this.getPipelineIdx();
		if(idx - 1 >= 0) {
			pipe_idx.setDouble(idx - 1);
			return true;
		}
		pipe_idx.setDouble(this.numPipelines() - 1);	// wrap around
		return false;
	}



	public static class CameraPreset {
		
		public final int brightness, exposure, whitebalance;

		public CameraPreset(int b, int e, int wb) {
			this.brightness = b;
			this.exposure = e;
			this.whitebalance = wb;
		}

	}
	public static interface EntryPreset { void setValue(NetworkTable nt); }
	public static abstract class EntryOption<type> implements EntryPreset {
		
		public final String entry;
		public final type value;

		public EntryOption(String n, type v) {
			this.entry = n;
			this.value = v;
		}
		//public abstract void setValue(NetworkTable nt);

	}
	public static class BooleanOption extends EntryOption<Boolean> {

		public BooleanOption(String n, Boolean v) { 
			super(n, v); 
		}
		public void setValue(NetworkTable nt) {
			if(nt.containsKey(this.entry)) {
				nt.getEntry(this.entry).setBoolean(this.value.booleanValue());
			}
		}

	}
	public static class NumberOption extends EntryOption<Double> {

		public NumberOption(String n, Double v) { 
			super(n, v); 
		}
		public void setValue(NetworkTable nt) {
			if(nt.containsKey(this.entry)) {
				nt.getEntry(this.entry).setDouble(this.value.doubleValue());
			}
		}

	}

	public static class VsCamera {

		private NetworkTable self;
		private String name;
		private int idx = -1;

		public void update(NetworkTable nt) {
			this.self = nt;
			this.name = NetworkTable.basenameKey(nt.getPath());
		}
		public void update(String tname) {
			this.self = VisionServer.Get().cameras.getSubTable(tname);
			this.name = tname;
		}
		public VsCamera(NetworkTable nt) { this.update(nt); }
		public VsCamera(String tname) { this.update(tname); }

		public int getIdx() { return this.idx; }
		public String getName() { return this.name; }
		public NetworkTable get() { return this.self; }

		public int getExposure() { return (int)this.self.getEntry("Exposure").getDouble(0.0); }
		public int getBrightness() { return (int)this.self.getEntry("Brightness").getDouble(0.0); }
		public int getWhiteBalance() {return (int)this.self.getEntry("WhiteBalance").getDouble(0.0); }
		public boolean setExposure(int e) { return this.self.getEntry("Exposure").setDouble(e); }
		public boolean setBrightness(int b) { return this.self.getEntry("Brightness").setDouble(b); }
		public boolean setWhiteBalance(int wb) { return this.self.getEntry("WhiteBalance").setDouble(wb); }
		public NetworkTableEntry getExposureEntry() { return this.self.getEntry("Exposure"); }
		public NetworkTableEntry getBrightnessEntry() { return this.self.getEntry("Brightness"); }
		public NetworkTableEntry getWhiteBalanceEntry() { return this.self.getEntry("WhiteBalance"); }
		public boolean applyPreset(CameraPreset p) {
			return this.setBrightness(p.brightness) && this.setExposure(p.exposure) && this.setWhiteBalance(p.whitebalance);
		}

		public String toString() {
			return this.getClass().getName() + '(' + this.name + ")@" + Integer.toHexString(this.hashCode()) + 
				": {EX: " + this.getExposure() + ", BR: " + this.getBrightness() + ", WB: " + this.getWhiteBalance() + '}';
		}

	}
	public static class VsPipeline {

		private NetworkTable self;
		private String name;
		private int idx = -1;
		NetworkTableEntry debug = null, thresh = null;

		public void update(NetworkTable nt) {
			this.self = nt;
			this.name = NetworkTable.basenameKey(nt.getPath());
		}
		public void update(String tname) {
			this.self = VisionServer.Get().pipelines.getSubTable(tname);
			this.name = tname;
		}
		public VsPipeline(NetworkTable nt) {
			this.update(nt);
		}
		public VsPipeline(String tname) {
			this.update(tname);
		}

		public int getIdx() { return this.idx; }
		public String getName() { return this.name; }
		public NetworkTable get() { return this.self; }

		public void applyProperties(EntryPreset[] properties) {
			for(EntryPreset preset : properties) {
				preset.setValue(this.self);
			}
		}
		
		public NetworkTableEntry[] getEntries() {
			NetworkTableEntry entries[] = new NetworkTableEntry[this.self.getKeys().size()];
			int i = 0;
			for(String entry : this.self.getKeys()) {
				entries[i] = (this.self.getEntry(entry));
				i++;
			}
			return entries;
		}
		public NetworkTableEntry searchEntries(String segment) {
			String lower = segment.toLowerCase();
			for(String key : this.self.getKeys()) {
				if(key.toLowerCase().contains(lower)) {
					return this.self.getEntry(key);
				}
			}
			return null;
		}
		public NetworkTableEntry[] searchEntries(String[] segments) {
			NetworkTableEntry ret[] = new NetworkTableEntry[segments.length];
			String kbuff;
			for(String key : this.self.getKeys()) {
				kbuff = key.toLowerCase();
				for(int i = 0; i < segments.length; i++) {
					if(kbuff.contains(segments[i].toLowerCase())) {
						ret[i] = this.self.getEntry(key);
					}
				}
			}
			return ret;
		}
		public void searchUsableEntries() {
			String buff;
			for(String key : this.self.getKeys()) {
				buff = key.toLowerCase();
				if(buff.contains("debug")) {
					this.debug = this.self.getEntry(key);
				}
				if(buff.contains("threshold")) {	// else if?
					this.thresh = this.self.getEntry(key);
				}
			}
		}
		public boolean hasDebug() {
			if(this.debug == null) {
				this.searchUsableEntries();
				if(this.debug == null) {
					return false;
				}
			}
			return true;
		}
		public boolean hasThreshold() {
			if(this.thresh == null) {
				this.searchUsableEntries();
				if(this.debug == null) {
					return false;
				}
			}
			return true;
		}
		public boolean setDebug(boolean val) {
			if(this.debug == null) {
				this.searchUsableEntries();
				if(this.debug == null) {
					return false;
				}
			}
			return this.debug.setBoolean(val);
		}
		public boolean setThreshold(boolean val) {
			if(this.thresh == null) {
				this.searchUsableEntries();
				if(this.debug == null) {
					return false;
				}
			}
			return this.thresh.setBoolean(val);
		}

	}

	public static class TargetOffset {
		public double x, y, z;
		// rotation also when that gets implemented in the networktables
		public TargetOffset(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		public TargetOffset(NetworkTable target) {
			this.x = target.getEntry("x").getDouble(0.0);
			this.y = target.getEntry("y").getDouble(0.0);
			this.z = target.getEntry("z").getDouble(0.0);
		}
	}
	public static class TargetData {
		public TargetOffset pos;
		public double distance, ud, lr;	// ud -> + : up, - : down	lr -> + : right, - : left

		public TargetData(double x, double y, double z, double d, double ud, double lr) {
			this.pos = new TargetOffset(x, y, z);
			this.distance = d;
			this.ud = ud;
			this.lr = lr;
		}
		public TargetData(TargetOffset pos, double d, double ud, double lr) {
			this.pos = pos;
			this.distance = d;
			this.ud = ud;
			this.lr = lr;
		}
		public TargetData(NetworkTable target) {
			this.pos = new TargetOffset(target);
			this.distance = target.getEntry("distance").getDouble(0.0);
			this.ud = target.getEntry("up-down").getDouble(0.0);
			this.lr = target.getEntry("left-right").getDouble(0.0);
		}
	}


}