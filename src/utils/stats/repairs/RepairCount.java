package utils.stats.repairs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import optimization.specification.SpecificationSolution;
import utils.stats.ComputeQualityIndicators;

public class RepairCount {

	public static void main(String[] args) {
		File root = new File("/tmp/ga");
		final boolean callNonSubsumed = true;
		String[] cases = root.list();
		for (String c : cases) {
			if (c.startsWith(".")) {
				continue;
			}
			System.out.println("Case: " + c);
			File[] filesInTheCase = new File(root.getAbsoluteFile() + "/" + c + "/").listFiles();
			int repairsCount = 0;
			int repairsFileCount = 0;
			int groundTruthCount = 0;
			int groundTruthFileCount = 0;
			for (File f : filesInTheCase) {
				System.out.println(f);
				if (f.getName().endsWith("csv_withoutNewBCs")) {
					++repairsFileCount;
					if (callNonSubsumed) {
						repairsCount += solutionFileCallNonSubsumed(f);
					} else {
						repairsCount += solutionFile(f);
					}
				} else if (f.getName().startsWith("print-repair")) {
					++groundTruthFileCount;
					groundTruthCount += groundTruthFile(f);
				}
			}
			System.out.println("Repairs Count: " + repairsCount);
			System.out.println("Repairs File Count: " + repairsFileCount);
			System.out.println("Ground Truth Count: " + groundTruthCount);
			System.out.println("Ground Truth File Count: " + groundTruthFileCount);
		}
	}
	
	private static int solutionFileCallNonSubsumed(File f) {
		ComputeQualityIndicators indicators = new ComputeQualityIndicators();
		List<SpecificationSolution> solutions2;
		solutions2 = indicators.readFitness(f);
		solutions2 = indicators.nonSubsumedSolutions(solutions2);
		return solutions2.size();
	}
	
	private static int solutionFile(File f) {
		int count = 0;
		try {
			String line = "";
			BufferedReader br = new BufferedReader(new FileReader(f));
			while((line = br.readLine()) != null) {
				if (!line.equals("") && !line.contains("id")) {
					++count;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return count;
	}

	private static int groundTruthFile(File f) {
		int count = 0;
		try {
			//System.out.println(f.getName());
			String line = "";
			BufferedReader br = new BufferedReader(new FileReader(f));
			final String genuineStr = "Amount of equivalent solutions to some genuine:";
			while((line = br.readLine()) != null) {
				if (line.startsWith(genuineStr)) {
					System.out.println(line);
					String[] lineSplited = line.split(" ");
					final int index = 7;
					String genuinesNumber = lineSplited[index];
					count = Integer.parseInt(genuinesNumber);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}
	
}
