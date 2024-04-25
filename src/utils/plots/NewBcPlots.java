package utils.plots;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.moeaframework.util.statistics.KruskalWallisTest;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.BoxPlot;
import tech.tablesaw.plotly.components.Figure;
import utils.stats.Stats;

public class NewBcPlots {

	private static boolean enableComputedParetoFront;
	private static boolean enableComputedtop4;

	
	public static void __main(String[] args) {
		Stats stats = new Stats();
		System.out.println("unguided vs nsgaiii");
		double [] a1 = new double[4];
		double [] a2 = new double[4];
		a1[0] = 0;
		a1[1] = 1;
		a1[2] = 0;
		a1[3] = 1;
		
		a2[0] = 1;
		a2[1] = 2;
		a2[2] = 2;
		a2[3] = 1;
		
		Pair<Double, String> pvalueAndStatus = stats.mannWhitneyUTest(a1, a2, "NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
	}
	
	public static void main(String[] args) {
		File nsgaiiiRoot = new File("/tmp/cases/nsgaiii");
		//ArrayList<Integer> nsgaiii = extractNewBCs(nsgaiiiRoot);
		enableComputedParetoFront = false;
		enableComputedtop4 = false;
		ArrayList<Double> nsgaiii = extractNewBCs(nsgaiiiRoot);
		
		File amosaRoot = new File("/tmp/cases/amosa");
		//ArrayList<Integer> amosa = extractNewBCs(amosaRoot);
		enableComputedParetoFront = false;
		enableComputedtop4 = false;
		ArrayList<Double> amosa = extractNewBCs(amosaRoot);
		
		File wbgaRoot = new File("/tmp/cases/wbga");
		enableComputedParetoFront = true;
		enableComputedtop4 = false;
		//ArrayList<Integer> wbga = extractNewBCs(wbgaRoot);
		ArrayList<Double> wbga = extractNewBCs(wbgaRoot);
		
		File unguidedRoot = new File("/tmp/cases/unguided");
		//ArrayList<Integer> unguided = extractNewBCs(unguidedRoot);
		enableComputedParetoFront = false;
		enableComputedtop4 = true;
		ArrayList<Double> unguided = extractNewBCs(unguidedRoot);
		
		System.out.println("Sizes: " + nsgaiii.size() + " " + amosa.size() + " " + wbga.size() + " " + unguided.size());
		
		System.out.println("nsgaiii (ratio of at least one that no introduce new bcs):");
		int total = 0;
		int hasAtLeastOne = 0;
		for (Double d : nsgaiii) {
			if (d >= 1) {
				++hasAtLeastOne;
			}
			++total;
		}
		System.out.println(hasAtLeastOne + " / " + total);
		System.out.println((double)hasAtLeastOne/(double)total);
		
		System.out.println("amosa (ratio: at least one that no introduce new bcs):");
		total = 0;
		hasAtLeastOne = 0;
		for (Double d : amosa) {
			if (d >= 1) {
				++hasAtLeastOne;
			}
			++total;			
		}
		System.out.println(hasAtLeastOne + " / " + total);
		System.out.println((double)hasAtLeastOne/(double)total);
		
		System.out.println("wbga (ratio: at least one that no introduce new bcs):");
		total = 0;
		hasAtLeastOne = 0;
		for (Double d : wbga) {
			if (d >= 1) {
				++hasAtLeastOne;
			}
			++total;
		}
		System.out.println(hasAtLeastOne + " / " + total);
		System.out.println((double)hasAtLeastOne/(double)total);
		
		System.out.println("unguided (ratio: at least one that no introduce new bcs):");
		total = 0;
		hasAtLeastOne = 0;
		for (Double d : unguided) {
			if (d >= 1) {
				++hasAtLeastOne;
			}
			++total;			
		}
		System.out.println(hasAtLeastOne + " / " + total);
		System.out.println((double)hasAtLeastOne/(double)total);
		
		//Column column1 = IntColumn.create("NewBCs");
		Column column1 = DoubleColumn.create("NewBCs");
		Column column2 = StringColumn.create("MOA");
		
		addNewBCsInColumns(column1,column2,nsgaiii,"nsgaiii");
		addNewBCsInColumns(column1,column2,wbga,"wbga");
		addNewBCsInColumns(column1,column2,amosa,"amosa");
		addNewBCsInColumns(column1,column2,unguided,"unguided");
		
		Table table = Table.create();
		table.addColumns(column1);
		table.addColumns(column2);
		
		/*
		Histogram histogram = new Histogram();
		NumericColumn column = IntColumn.create("NewBCs");
		for (double number : unguided) {
			column.append((int)number);
		}
		Plot.show(histogram.create("Repairs and new BCs",column));
		*/
		
		BoxPlot boxPlot = new BoxPlot();
		//Figure fig = boxPlot.create("Repairs with new BCs", table, "MOA", "NewBCs");
		Figure fig = boxPlot.create("", table, "MOA", "NewBCs");
		Plot.show(fig);
		
		KruskalWallisTest kruskalWallis = new KruskalWallisTest(4);
		kruskalWallis.addAll(getDoubleArray(nsgaiii), 0);
		kruskalWallis.addAll(getDoubleArray(amosa), 1);
		kruskalWallis.addAll(getDoubleArray(wbga), 2);
		kruskalWallis.addAll(getDoubleArray(unguided), 3);
		System.out.println("Kruskal-Wallis: " + kruskalWallis.test(0.01));

		OneWayAnova anova = new OneWayAnova();
		List<double[]> lQualities = new ArrayList<double[]>();
		lQualities.add(getDoubleArray(nsgaiii));
		lQualities.add(getDoubleArray(amosa));
		lQualities.add(getDoubleArray(wbga));
		lQualities.add(getDoubleArray(unguided));
//		double anovaPValue = anova.anovaPValue(lQualities);
	//	System.out.println("Anova p-value: " + anovaPValue);
//		System.out.println("Anova F-value: " + anova.anovaFValue(lQualities));

		Stats stats = new Stats();
		System.out.println("unguided vs nsgaiii");
		Pair<Double, String> pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArray(unguided), getDoubleArray(nsgaiii), "NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
		
		System.out.println("unguided vs amosa");
		pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArray(unguided), getDoubleArray(amosa), "NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
		
		System.out.println("unguided vs wbga");
		pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArray(unguided), getDoubleArray(wbga), "NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
		
		System.out.println("nsgaiii vs amosa");
		pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArray(nsgaiii), getDoubleArray(amosa), "NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
		
		System.out.println("nsgaiii vs wbga");
		pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArray(nsgaiii), getDoubleArray(wbga), "NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
		
		System.out.println("amosa vs wbga");
		pvalueAndStatus = stats.mannWhitneyUTest(getDoubleArray(wbga), getDoubleArray(amosa), "NewBCs", 0.05);
		System.out.println(pvalueAndStatus.getLeft());
		System.out.println(pvalueAndStatus.getRight());
	}
	
	private static double[] getDoubleArray(ArrayList<Double> lDouble) {
		double[] result = new double[lDouble.size()];
		int i = 0;
		for (Double v : lDouble) {
			result[i] = v;
			++i;
		}
		return result;
	}
	
	//public static void addNewBCsInColumns(Column column1, Column column2, ArrayList<Integer> lNewBCs, String algorithmName) {
	public static void addNewBCsInColumns(Column column1, Column column2, ArrayList<Double> lNewBCs, String algorithmName) {
		//for (Integer newBCs : lNewBCs) {
		for (Double newBCs : lNewBCs) {
			column1.append(newBCs);
			column2.append(algorithmName);
		}
	}
	
	//public static ArrayList<Integer> extractNewBCs(File root) {
	public static ArrayList<Double> extractNewBCs(File root) {
		//File root = new File("/tmp/cases/nsgaiii");
		String[] cases = root.list();
		ArrayList<Double> allNewBcs = new ArrayList<Double>();
		//ArrayList<Integer> allNewBcs = new ArrayList<Integer>();
		for (String c : cases) {
			if (c.startsWith(".")) {
				continue;
			}
			System.out.println("Case: " + c);
			File[] filesInTheCase = new File(root.getAbsoluteFile() + "/" + c + "/").listFiles();
			int repairsCount = 0;
			int repairsFileCount = 0;
			ArrayList<Double> allNewBCsPerCase = new ArrayList<Double>();
			int noNewBcsPerCase = 0;
			int totalElementsCheckedPerCase = 0;
			for (File f : filesInTheCase) {
				System.out.println(f);
				boolean check = false;
				if (enableComputedParetoFront) {
					check = f.getName().endsWith("_paretoFront");
				} else if (!enableComputedParetoFront) {
					check = f.getName().endsWith(".csv") || f.getName().endsWith(".csv_random");
				}
				if (check) {
					if (enableComputedParetoFront) {
						System.out.println("---");
					}
					++repairsFileCount;
					File absoluteFilePath = f;
					try {
						List<String> allLines = Files.readAllLines(absoluteFilePath.toPath());
						boolean firstLine = true;
						int noNewBcs = 0;
						int totalElementsChecked = 0;
						int i = 0;
						for (String line : allLines) {
							++i;
							if (enableComputedtop4 && i >= 4) {
								break;
							}
							if (firstLine) {
								firstLine = false;
							} else {
								String[] splited = line.split(",");
								String newBCsStr = splited[splited.length - 1];
								int newBCs = Integer.parseInt(newBCsStr);
								if (newBCs < 100000) {
									//allNewBcs.add((double)newBCs);
									//allNewBCsPerCase.add((double)newBCs);
									if (newBCs == 0) {
										++noNewBcs;
									} 
									++totalElementsChecked;
									//if (totalElementsChecked == 10) break;
									//allNewBcs.add((double)newBCs);
								}
								//System.out.println(newBCs);
							}
							/*
							double total = 0;
							for (Double value : allNewBCsPerCase) {
								total = total + value;
							}
							allNewBcs.add(total / ((double)allNewBCsPerCase.size()));
							*/
						} // END - LINE
						allNewBcs.add((double)noNewBcs);
						//noNewBcsPerCase += noNewBcs;
						//totalElementsCheckedPerCase += totalElementsChecked;
						
						if (totalElementsChecked != 0) {
							//allNewBCsPerCase.add((double)noNewBcs);
							//allNewBcs.add(new Double((double)noNewBcs / (double)totalElementsChecked));
							//allNewBcs.add((double)noNewBcs);
							/*
							if (noNewBcs > 0) {
								allNewBcs.add(new Double(1));
							} else {
								allNewBcs.add(new Double(0));	
							}
							*/
						}
						//System.out.println("Repairs File Count: " + repairsFileCount);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} //end - FILES


			if (totalElementsCheckedPerCase != 0) {
				//allNewBcs.add((double)noNewBcsPerCase);
				//allNewBCsPerCase.add((double)noNewBcs);
				/*
				if (noNewBcsPerCase > 0) {
					allNewBcs.add(new Double(1));
				} else {
					allNewBcs.add(new Double(0));	
				}
				*/
			}
			
			//allNewBcs.add((double)noNewBcsPerCase);
			
			/*
			double total = 0;
			for (Double value : allNewBCsPerCase) {
				total = total + value;
			}
			allNewBcs.add(total / ((double)allNewBCsPerCase.size()));
			allNewBCsPerCase.clear();
			*/
			
			//System.out.println("Repairs Count: " + repairsCount);
		} //end - CASE
		return allNewBcs;
	}
	
}
