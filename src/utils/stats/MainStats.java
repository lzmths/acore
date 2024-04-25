package utils.stats;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import optimization.specification.SpecificationSolution;

public class MainStats {

	public static void print() {
		ComputeQualityIndicators indicators = new ComputeQualityIndicators();
		List<SpecificationSolution> solutions = indicators.readFitness(new File("/tmp/fitness-0.csv"));
		Map<String, Double> indicatorToQuality = indicators.qualityFromList(solutions);
		Set<String> keys = indicatorToQuality.keySet();
		for (String key : keys) {
			System.out.println(key + " : " + indicatorToQuality.get(key));
		}
		List<SpecificationSolution> paretoFront = indicators.nonSubsumedSolutions(solutions);
		for (SpecificationSolution solution : paretoFront) {
			System.out.println(solution.objectives()[2] + " " + solution.objectives()[3]);
		}
	}
	
	public static void _main(String[] args) {
		Map<String,File> versionToFile = new HashMap<String,File>();
		versionToFile.put("nsgaiii", new File("/tmp/comparison/nsgaiii"));
		//versionToFile.put("ga", new File("/tmp/comparison/ga"));
		versionToFile.put("amosa", new File("/tmp/comparison/amosa"));
		File output = new File("/tmp/output.csv");
		CreateTable statsTable = new CreateTable();
		statsTable.createTablePerCase(versionToFile, output);
	}
	
	public static void main(String[] args) {
		Map<String,File> versionToFile = new HashMap<String,File>();
		versionToFile.put("nsgaiii", new File("/tmp/cases/nsgaiii"));
		versionToFile.put("unguided", new File("/tmp/cases/unguided"));
		versionToFile.put("wbga", new File("/tmp/cases/wbga"));
		versionToFile.put("amosa", new File("/tmp/cases/amosa"));
		File output = new File("/tmp/output.csv");
		CreatePlot statsPlot = new CreatePlot();
		statsPlot.createPlot(versionToFile, output);
	}
	
	/*
	public static void main(String[] args) {
		ComputeQualityIndicators indicators = new ComputeQualityIndicators();
		List<Map<String, Double>> quality1 = new ArrayList<Map<String, Double>>();
		List<Map<String, Double>> quality2 = new ArrayList<Map<String, Double>>();
		String caseSolution1 = "/tmp/nsgaiii/minepump";
		String caseSolution2 = "/tmp/ga/minepump";
		for (int i = 0; i < 10; ++i) {
			List<SpecificationSolution> solutions1 = indicators.readFitness(new File(caseSolution1 + "/fitness-" + i +".csv_withoutNewBCs"));
			List<SpecificationSolution> solutions2 = indicators.readFitness(new File(caseSolution2 + "/fitness-" + i +".csv_withoutNewBCs"));
			solutions2 = indicators.nonSubsumedSolutions(solutions2); //ga
			quality1.add(indicators.qualityFromList(solutions1));
			quality2.add(indicators.qualityFromList(solutions2));
		}
		Stats stats = new Stats();
		System.out.println(stats.wilcoxon(quality1, quality2, "HV", 0.05));
		System.out.println(stats.wilcoxon(quality2, quality1, "HV", 0.05));
	}
	*/
}
