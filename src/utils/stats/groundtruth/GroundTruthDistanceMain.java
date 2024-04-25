package utils.stats.groundtruth;

import java.io.File;

import utils.Settings;
import utils.plots.MainSemSynPlot;
import utils.plots.SemSynGroundTruthRecovery;

public class GroundTruthDistanceMain {

	//public final static String casesFolder = "/Users/luiz.carvalho/Downloads/SOSYM/cases";
	public final static String casesFolder = "/tmp/cases/nsgaiii";
	public static boolean computeParetoFront = true;
	public static String indicatorName = "IGD";

	public static void main(String[] args) {
		Settings.MC_TIMEOUT = 500; // = 500;
		//simple_arbiter_v2();
		//detector();
		//round_robin();
		//simple_arbiter_v1();
		//ltl2dba27();
		//arbiter();
		//prioritized_arbiter();
		//simple_arbiter_v2();
		minepump();
	}
	
	private static void minepump() {
		GroundTruthDistanceStats groundTruthDistanceStats = new GroundTruthDistanceStats();
		String caseName = "minepump";
		File caseFolder = new File(casesFolder + "/" + caseName);
		System.out.println(caseFolder.getAbsolutePath());
		SemSynGroundTruthRecovery semSynRecovery = new SemSynGroundTruthRecovery();
		MainSemSynPlot.minepump(semSynRecovery);
		groundTruthDistanceStats.calculateGroundTruthFromFolder(semSynRecovery,caseFolder,indicatorName,computeParetoFront);
	}
	
	private static void detector() {
		GroundTruthDistanceStats groundTruthDistanceStats = new GroundTruthDistanceStats();
		String caseName = "detector";
		File caseFolder = new File(casesFolder + "/" + caseName);
		System.out.println(caseFolder.getAbsolutePath());
		SemSynGroundTruthRecovery semSynRecovery = new SemSynGroundTruthRecovery();
		MainSemSynPlot.detector(semSynRecovery);
		groundTruthDistanceStats.calculateGroundTruthFromFolder(semSynRecovery,caseFolder,indicatorName,computeParetoFront);
	}

	public static void simple_arbiter_v2() {
		GroundTruthDistanceStats groundTruthDistanceStats = new GroundTruthDistanceStats();
		String caseName = "simple_arbiter_v2";
		File caseFolder = new File(casesFolder + "/" + caseName);
		System.out.println(caseFolder.getAbsolutePath());
		SemSynGroundTruthRecovery semSynRecovery = new SemSynGroundTruthRecovery();
		MainSemSynPlot.simplearbiterv2(semSynRecovery);
		groundTruthDistanceStats.calculateGroundTruthFromFolder(semSynRecovery,caseFolder,indicatorName,computeParetoFront);
	}
	
	//DO
	public static void round_robin() {
		GroundTruthDistanceStats groundTruthDistanceStats = new GroundTruthDistanceStats();
		String caseName = "round-robin";
		File caseFolder = new File(casesFolder + "/" + caseName);
		System.out.println(caseFolder.getAbsolutePath());
		SemSynGroundTruthRecovery semSynRecovery = new SemSynGroundTruthRecovery();
		//String indicatorName = "HV";
		//String indicatorName = "IGD";
		MainSemSynPlot.roundrobin(semSynRecovery);
		groundTruthDistanceStats.calculateGroundTruthFromFolder(semSynRecovery,caseFolder,indicatorName,computeParetoFront);
	}
	
	//DO
	public static void ltl2dba27() {
		GroundTruthDistanceStats groundTruthDistanceStats = new GroundTruthDistanceStats();
		String caseName = "ltl2dba27";
		File caseFolder = new File(casesFolder + "/" + caseName);
		System.out.println(caseFolder.getAbsolutePath());
		SemSynGroundTruthRecovery semSynRecovery = new SemSynGroundTruthRecovery();
		//String indicatorName = "HV";
		//String indicatorName = "IGD";
		MainSemSynPlot.ltl2dba27(semSynRecovery);
		groundTruthDistanceStats.calculateGroundTruthFromFolder(semSynRecovery,caseFolder,indicatorName,computeParetoFront);
	}
	
	//DO
	public static void arbiter() {
		GroundTruthDistanceStats groundTruthDistanceStats = new GroundTruthDistanceStats();
		String caseName = "arbiter";
		File caseFolder = new File(casesFolder + "/" + caseName);
		System.out.println(caseFolder.getAbsolutePath());
		SemSynGroundTruthRecovery semSynRecovery = new SemSynGroundTruthRecovery();
		//String indicatorName = "HV";
		//String indicatorName = "IGD";
		MainSemSynPlot.arbiter(semSynRecovery);
		groundTruthDistanceStats.calculateGroundTruthFromFolder(semSynRecovery,caseFolder,indicatorName,computeParetoFront);
	}

	//DO
	public static void prioritized_arbiter() {
		GroundTruthDistanceStats groundTruthDistanceStats = new GroundTruthDistanceStats();
		String caseName = "prioritizedArbiter";
		File caseFolder = new File(casesFolder + "/" + caseName);
		System.out.println(caseFolder.getAbsolutePath());
		SemSynGroundTruthRecovery semSynRecovery = new SemSynGroundTruthRecovery();
		//String indicatorName = "HV";
		//String indicatorName = "IGD";
		MainSemSynPlot.prioritizedArbiter(semSynRecovery);
		groundTruthDistanceStats.calculateGroundTruthFromFolder(semSynRecovery,caseFolder,indicatorName,computeParetoFront);
	}
	
	//DO
	public static void simple_arbiter_v1() {
		GroundTruthDistanceStats groundTruthDistanceStats = new GroundTruthDistanceStats();
		String caseName = "simple_arbiter_v1";
		File caseFolder = new File(casesFolder + "/" + caseName);
		System.out.println(caseFolder.getAbsolutePath());
		SemSynGroundTruthRecovery semSynRecovery = new SemSynGroundTruthRecovery();
		//String indicatorName = "HV";
		//String indicatorName = "IGD";
		MainSemSynPlot.simplearbiterv1(semSynRecovery);
		groundTruthDistanceStats.calculateGroundTruthFromFolder(semSynRecovery,caseFolder,indicatorName,computeParetoFront);
	}
	
}