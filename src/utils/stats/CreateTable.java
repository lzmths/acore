package utils.stats;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import optimization.specification.SpecificationSolution;

public class CreateTable {

	public List<String> getCases(File file) {
		String[] casesArray = file.list();
		List<String> cases = new ArrayList<String>();
		for (String caseFolder : casesArray) {
			if (!caseFolder.startsWith(".")) {
				cases.add(caseFolder);
			}
		}
		//List<String> cases = Arrays.asList(casesArray);
		return cases;
	}
	
	public List<String> getCases(Map<String,File> versionToFile) {
		Set<String> keys = versionToFile.keySet();
		List<String> cases = new ArrayList<String>();
		for (String key : keys) {
			File versionFolder = versionToFile.get(key);
			cases = getCases(versionFolder);
			return cases;
		}
		return cases;
	}
	
	public void createTablePerCase(Map<String,File> versionToFile, File output) {
		createTablePerCase(versionToFile, getCases(versionToFile), output);
	}
	
	public void createTablePerCase(Map<String,File> versionToFile, List<String> cases, File output) {
		Set<String> keys = versionToFile.keySet();
		for (String caseName : cases) {
			System.out.println(caseName);
			System.out.println("comparison,p-value,status,indicator");
			File outputCase = new File(output.getAbsoluteFile() + "/" + caseName);
			for (String key1 : keys) {
				File versionFolder1 = versionToFile.get(key1);
				String casePath1 = versionFolder1.getAbsolutePath() + "/" + caseName;
				for (String key2 : keys) {
					if (key1 != key2) {
						File versionFolder2 = versionToFile.get(key2);
						String casePath2 = versionFolder2.getAbsolutePath() + "/" + caseName;
						//Stats stats = computeIndicators(casePath1, casePath2);
						Stats stats;
						if (key1.equals("ga")) {
							stats = computeIndicators(casePath1, casePath2, true, false);
						} else if (key2.equals("ga")) {
							stats = computeIndicators(casePath1, casePath2, false, true);
						} else {
							stats = computeIndicators(casePath1, casePath2);
						}
						String indicator = "HV";
						final double alpha = 0.10;
						Pair<Double, String> pvalueAndStatus = stats.wilcoxon(indicator, alpha); //TODO - loop for indicators;
						String line = createLine(key1,key2,pvalueAndStatus.getLeft(),pvalueAndStatus.getRight(),indicator,output);
						System.out.println(line);
					}
				}
			}	
		}
	}

	private String createLine(String case1, String case2, double pvalue, String status, String indicator, File output) {
		return case1 + " > " + case2 + "," + pvalue + "," + status + "," + indicator;
	}
	
	public Stats computeIndicators(String caseSolution1, String caseSolution2) {
		return computeIndicators(caseSolution1, caseSolution2, false, false);
	}
	
	public Stats computeIndicators(String caseSolution1, String caseSolution2, boolean nonSubsumed1, boolean nonSubsumed2) {
		ComputeQualityIndicators indicators = new ComputeQualityIndicators();
		List<Map<String, Double>> quality1 = new ArrayList<Map<String, Double>>();
		List<Map<String, Double>> quality2 = new ArrayList<Map<String, Double>>();
		int executionSize = 10;
		for (int i = 0; i < executionSize; ++i) { 
			File file1 = new File(caseSolution1 + "/fitness-" + i +".csv_withoutNewBCs");
			File file2 = new File(caseSolution2 + "/fitness-" + i +".csv_withoutNewBCs");
			List<SpecificationSolution> solutions1;
			List<SpecificationSolution> solutions2;
			if (file1.exists()) {
				solutions1 = indicators.readFitness(file1);
			} else {
				solutions1 = new ArrayList<SpecificationSolution>();
			}		
			if (nonSubsumed1) {
				solutions1 = indicators.nonSubsumedSolutions(solutions1);
			}
			quality1.add(indicators.qualityFromList(solutions1));	
			if (file2.exists()) {
				solutions2 = indicators.readFitness(file2);
			} else {
				solutions2 = new ArrayList<SpecificationSolution>();
			}
			if (nonSubsumed2) {
				solutions2 = indicators.nonSubsumedSolutions(solutions2);
			}
			quality2.add(indicators.qualityFromList(solutions2));
		}
		return new Stats(quality1, quality2);
	}
	
}