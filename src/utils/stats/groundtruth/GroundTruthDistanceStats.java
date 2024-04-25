package utils.stats.groundtruth;

import java.io.File;
import java.util.List;
import java.util.Map;

import optimization.specification.SpecificationSolution;
import utils.plots.SemSynGroundTruthRecovery;
import utils.stats.ComputeQualityIndicators;

public class GroundTruthDistanceStats {
	
	public void calculateGroundTruth(List<SpecificationSolution> lGroundTruth, List<SpecificationSolution> lSpec, String indicatorName) {
		ComputeQualityIndicators indicators = new ComputeQualityIndicators();
		for (SpecificationSolution groundTruth : lGroundTruth) {
			double sem = (groundTruth.lost_models_fitness + groundTruth.won_models_fitness) / 2.0d;
			double syn = groundTruth.syntactic_distance;
			double[] refPoint = {0.0d, 0.0d, sem, syn};
			Map<String,Double> quality = indicators.qualityFromList(refPoint,lSpec);
			System.out.println(indicatorName + " :" + quality.get(indicatorName));		
		}
	}
	
	public List<SpecificationSolution> getGroundTruth(SemSynGroundTruthRecovery semSynRecovery) {
		return semSynRecovery.getSpecList();
	}
	
	public List<SpecificationSolution> getRepairs(File csvFile) {
		ComputeQualityIndicators indicators = new ComputeQualityIndicators();
		return indicators.readFitness(csvFile);
	}
	
	public void calculateGroundTruthFromFolder(SemSynGroundTruthRecovery semSynRecovery, File caseFolder, String indicatorName, boolean computeParetoFront) {
		ComputeQualityIndicators indicators = new ComputeQualityIndicators();
		String[] names = caseFolder.list();
		for (String name : names) {
			//if (name.startsWith("fitness-") && name.endsWith("csv_withoutNewBCs")) {
			if (name.startsWith("fitness-") && name.endsWith("withoutNewBCs")) {
				System.out.println(name);
				List<SpecificationSolution> groundTruth = semSynRecovery.getSpecList();
				List<SpecificationSolution> repairs = indicators.readFitness(new File(caseFolder.getAbsoluteFile() + "/" + name));
				System.out.println("original set: " + repairs.size());
				if (computeParetoFront) {
					ComputeQualityIndicators qIndicators = new ComputeQualityIndicators();
					repairs = qIndicators.nonSubsumedSolutions(repairs);
					System.out.println("Pareto set: " + repairs.size());
				}
				calculateGroundTruth(groundTruth,repairs,indicatorName);
			}
		}
	}
	
}
