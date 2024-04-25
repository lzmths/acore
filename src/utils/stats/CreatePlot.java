package utils.stats;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.moeaframework.util.statistics.KruskalWallisTest;

import optimization.specification.SpecificationSolution;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.BoxPlot;
import tech.tablesaw.plotly.components.Figure;

public class CreatePlot {

	public List<Map<String, Double>> computeIndicators(String caseSolution) {
		return computeIndicators(caseSolution, false, false);
	}
	
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
		if (keys.contains("nsgaiii")) {
			File versionFolder = versionToFile.get("nsgaiii");
			cases = getCases(versionFolder);
			return cases;
		}
		if (keys.contains("amosa")) {
			File versionFolder = versionToFile.get("amosa");
			cases = getCases(versionFolder);
			return cases;
		}
		for (String key : keys) {
			File versionFolder = versionToFile.get(key);
			cases = getCases(versionFolder);
			return cases;
		}
		return cases;
	}
	
	public List<Map<String, Double>> computeIndicators(String caseSolution, boolean nonSubsumed1, boolean top4) {
		ComputeQualityIndicators indicators = new ComputeQualityIndicators();
		List<Map<String, Double>> quality1 = new ArrayList<Map<String, Double>>();
		int executionSize = 10;
		for (int i = 0; i < executionSize; ++i) { 
			File file1 = new File(caseSolution + "/fitness-" + i +".csv_withoutNewBCs");
			if (!file1.exists()) {
				file1 = new File(caseSolution + "/fitness-" + i +".csv_random_withoutNewBCs");
			}
			List<SpecificationSolution> solutions1;
			if (file1.exists()) {
				solutions1 = indicators.readFitness(file1);
			} else {
				solutions1 = new ArrayList<SpecificationSolution>();
			}
			if (nonSubsumed1) {
				solutions1 = indicators.nonSubsumedSolutions(solutions1);
				/*
				System.out.println("CASE: " + caseSolution);
				System.out.println("BEFORE: " + solutions1.size());
				solutions1 = indicators.nonSubsumedSolutions(solutions1);
				if (solutions1.size() == 8) {
					System.out.println("---");
				}
				System.out.println("AFTER: " + solutions1.size());
				*/
			} else if (top4) {
				solutions1 = indicators.top(4, solutions1);
				/*
				System.out.println("CASE: " + caseSolution);
				System.out.println("SIZE: " + solutions1.size());
				*/
			}
			quality1.add(indicators.qualityFromList(solutions1));	
		}
		return quality1;
	}
	
	public void createPlot(Map<String,File> versionToFile, File output) {
		createPlot(versionToFile, getCases(versionToFile), output);
	}
	
	public void createPlot(Map<String,File> versionToFile, List<String> cases, File output) {
		Set<String> keys = versionToFile.keySet();
		Map<String,List<Map<String, Double>>> versionToQuality = new HashMap<String,List<Map<String, Double>>>();
		Map<String,Set<String>> checkCaseVersion = new HashMap<String,Set<String>>(); 
		for (String caseName : cases) {
			File outputCase = new File(output.getAbsoluteFile() + "/" + caseName);
			int i = 0;
			for (String key1 : keys) {
				File versionFolder1 = versionToFile.get(key1);
				String casePath1 = versionFolder1.getAbsolutePath() + "/" + caseName;
				int j = 0;
				for (String key2 : keys) {
					if (j < i) {
						//System.out.println(caseName);
						//System.out.println(key1 + " " + key2);
						File versionFolder2 = versionToFile.get(key2);
						String casePath2 = versionFolder2.getAbsolutePath() + "/" + caseName;
						List<Map<String, Double>> quality1;
						List<Map<String, Double>> quality2;
						/*
						if (key1.equals("unguided") || key1.equals("wbga")) {
							quality1 = computeIndicators(casePath1, true);
						} else {
							quality1 = computeIndicators(casePath1, false);
						} 
						*/
						if (key1.equals("unguided")) {
							quality1 = computeIndicators(casePath1, false, true);
						} else if (key1.equals("wbga")) {
							quality1 = computeIndicators(casePath1, true, false);
						} else {
							quality1 = computeIndicators(casePath1, false, false);
						} 
						
						if (key2.equals("unguided")) {
							quality2 = computeIndicators(casePath2, false, true);
						} else if (key2.equals("wbga")) {
							quality2 = computeIndicators(casePath2, true, false);
						} else {
							quality2 = computeIndicators(casePath2, false, false);
						}
						
						//List<Map<String, Double>> q1 = versionToQuality.get(key1);
						//List<Map<String, Double>> q2 = versionToQuality.get(key2);
						Set<String> check = checkCaseVersion.get(caseName);
						if (check == null) {
							check = new HashSet<String>();
						}
						if (!check.contains(key1)) {
							check.add(key1);
							checkCaseVersion.put(caseName,check);
							System.out.println(caseName + " " + key1);
							List<Map<String, Double>> q1 = versionToQuality.get(key1);
							if (q1 == null) {
								versionToQuality.put(key1,quality1);
							} else {
								q1.addAll(quality1);
							}
						} if (!check.contains(key2)) {
							check.add(key2);
							checkCaseVersion.put(caseName,check);
							System.out.println(caseName + " " + key2);
							List<Map<String, Double>> q2 = versionToQuality.get(key2);
							if (q2 == null) {
								versionToQuality.put(key2,quality2);
							} else {
								q2.addAll(quality2);
							}
						}
					}
					++j;
				}
				++i;
			}
		}
		String indicator = "IGD";
		int i = 0;
		
		List<Map<String, Double>> nsgaiii = versionToQuality.get("nsgaiii");
		List<Map<String, Double>> amosa = versionToQuality.get("amosa");
		List<Map<String, Double>> wbga = versionToQuality.get("wbga");
		List<Map<String, Double>> unguided = versionToQuality.get("unguided");
		
		System.out.println("nsgaiii size: " + nsgaiii.size());
		System.out.println("amosa size: " + amosa.size());
		System.out.println("wbga size: " + wbga.size());
		System.out.println("unguided size: " + unguided.size());
		
		
		double[] nsgaiiiArray = getDoubleArrayFromQuality(nsgaiii,indicator);
		System.out.print("[");
		for (int k = 0; k < nsgaiiiArray.length; ++k) {
			System.out.print(nsgaiiiArray[k] + ",");
		}
		System.out.println("]");
		
		System.out.print("[");
		double[] amosaArray = getDoubleArrayFromQuality(amosa,indicator);
		for (int k = 0; k < amosaArray.length; ++k) {
			System.out.print(amosaArray[k] + ",");
		}
		System.out.println("]");

		System.out.print("[");
		double[] wbgaArray = getDoubleArrayFromQuality(wbga,indicator);
		for (int k = 0; k < wbgaArray.length; ++k) {
			System.out.print(wbgaArray[k] + ",");
		}
		System.out.println("]");

		System.out.print("[");
		double[] unguidedArray = getDoubleArrayFromQuality(unguided,indicator);
		for (int k = 0; k < unguidedArray.length; ++k) {
			System.out.print(unguidedArray[k] + ",");
		}
		System.out.println("]");


		KruskalWallisTest kruskalWallis = new KruskalWallisTest(4);
		kruskalWallis.addAll(getDoubleArrayFromQuality(nsgaiii,indicator),0);
		kruskalWallis.addAll(getDoubleArrayFromQuality(amosa,indicator),1);
		kruskalWallis.addAll(getDoubleArrayFromQuality(wbga,indicator),2);
		kruskalWallis.addAll(getDoubleArrayFromQuality(unguided,indicator),3);
		System.out.println("Kruskal-Wallis: " + kruskalWallis.test(0.05));
				
		Stats stats = new Stats();
		System.out.println("MannWhitney: nsgaiii vs wbga");
		Pair<Double, String> pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArrayFromQuality(nsgaiii,indicator), 
				getDoubleArrayFromQuality(wbga,indicator), 
				"NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
		
		System.out.println("MannWhitney: nsgaiii vs amosa");
		pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArrayFromQuality(nsgaiii,indicator), 
				getDoubleArrayFromQuality(amosa,indicator), 
				"NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
		
		System.out.println("MannWhitney: nsgaiii vs unguided");
		pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArrayFromQuality(nsgaiii,indicator), 
				getDoubleArrayFromQuality(unguided,indicator), 
				"NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
		
		System.out.println("MannWhitney: wbga vs amosa");
		pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArrayFromQuality(wbga,indicator), 
				getDoubleArrayFromQuality(amosa,indicator), 
				"NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
		
		System.out.println("MannWhitney: wbga vs unguided");
		pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArrayFromQuality(wbga,indicator), 
				getDoubleArrayFromQuality(unguided,indicator), 
				"NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
		
		System.out.println("MannWhitney: amosa vs unguided");
		pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArrayFromQuality(amosa,indicator), 
				getDoubleArrayFromQuality(unguided,indicator), 
				"NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
		
		//plot
		Column column1 = DoubleColumn.create("HV");
		Column column2 = StringColumn.create("MOA");

		List<String> keysSequence = new ArrayList<String>();
		keysSequence.add("nsgaiii");
		keysSequence.add("wbga");
		keysSequence.add("amosa");
		keysSequence.add("unguided");
		System.out.println(keysSequence);
				
		for (String key : keysSequence) {
			List<Map<String, Double>> quality = versionToQuality.get(key);
			for (Map<String, Double> q : quality) {
				column1.append(q.get(indicator));
				column2.append(key);
			}
		}
		Table table = Table.create();
		table.addColumns(column1);
		table.addColumns(column2);
		
		BoxPlot boxPlot = new BoxPlot();
		Figure fig = boxPlot.create("Inverted Generational Distance", table, "MOA", "HV");
		//Figure fig = boxPlot.create("Hypervolume", table, "MOA", "HV");
		Plot.show(fig);
		
		/*
		String indicator = "HV";
		int i = 0;
		for (String key1 : keys) {
			int j = 0;
			for (String key2: keys) {
				if (j < i) {
					//stats
					System.out.println("Compare " + key1 + " and " + key2);
					Stats stats = new Stats(versionToQuality.get(key1),versionToQuality.get(key2));
					Pair<Double, String> pvalueAndStatus = stats.wilcoxon(indicator, 0.05);
					System.out.println(pvalueAndStatus.getLeft());
					System.out.println(pvalueAndStatus.getRight());
					
					//plot
					Column column1 = DoubleColumn.create("HV");
					Column column2 = StringColumn.create("MOA");

					List<Map<String, Double>> quality1 = stats.getQuality1();
					for (Map<String, Double> q1 : quality1) {
						column1.append(q1.get(indicator));
						column2.append(key1);
					}
					List<Map<String, Double>> quality2 = stats.getQuality2();
					for (Map<String, Double> q2 : quality2) {
						column1.append(q2.get(indicator));
						column2.append(key2);
					}
					
					
					Table table = Table.create();
					table.addColumns(column1);
					table.addColumns(column2);
					
					BoxPlot boxPlot = new BoxPlot();
					Figure fig = boxPlot.create("HV", table, "MOA", "HV");
					Plot.show(fig);
				
				}
				++j;
			}
			++i;
		}
		*/
	}

	private double[] getDoubleArrayFromQuality(List<Map<String, Double>> quality, String indicator) {
		double[] result = new double[quality.size()];
		int i = 0;
		for (Map<String, Double> q : quality) {
			result[i] = q.get(indicator);
			++i;
		}
		return result;
	}
	
}
