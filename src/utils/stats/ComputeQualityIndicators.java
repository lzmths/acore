package utils.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.uma.jmetal.qualityindicator.QualityIndicator;
import org.uma.jmetal.qualityindicator.QualityIndicatorUtils;
import org.uma.jmetal.util.SolutionListUtils;

import optimization.specification.SpecificationSolution;

public class ComputeQualityIndicators {

	public Map<String, Double> qualityFromCsv(File file) {
		List<SpecificationSolution> specSolutions = readFitness(file);
		return qualityFromList(specSolutions);
	}
	
	public Map<String, Double> qualityFromList(List<SpecificationSolution> specSolutions) {
		double[] refPoint1 = {0.0d, 0.0d, 0.0d, 0.0d};
		return qualityFromList(refPoint1,specSolutions);
	}
	
	public Map<String, Double> qualityFromList(double[] refPoint, List<SpecificationSolution> specSolutions) {
	    Map<String, Double> mIndicatorToQuality = new HashMap<String, Double>();
		//double[] refPoint1 = {-0.1d, -0.1d, -0.1d, -0.1d};
		//double[] refPoint1 = {0.0d, 0.0d, 0.0d, 0.0d}; 
		//double[] refPoint1 = {1.0d, 1.0d, 1.0d, 1.0d};
		double[][] refMatrix = new double[1][4];
		refMatrix[0] = refPoint;
	    List<QualityIndicator> qualityIndicators = QualityIndicatorUtils.getAvailableIndicators(refMatrix);
		if (specSolutions.isEmpty()) {
		    for (QualityIndicator indicator: qualityIndicators) {
		    	mIndicatorToQuality.put(indicator.getName(), 1.2); //TODO - IGD
		    	//mIndicatorToQuality.put(indicator.getName(), 0.0); //TODO - HV
		    }
		    return mIndicatorToQuality;
		}
		double[][] fitnessMatrix = SolutionListUtils.getMatrixWithObjectiveValues(specSolutions);
	    for (QualityIndicator indicator: qualityIndicators) {
	    	mIndicatorToQuality.put(indicator.getName(), indicator.compute(fitnessMatrix));
	    }
	    return mIndicatorToQuality;
	}
	
	public double [] getObjectives(SpecificationSolution spec) {
		double [] objectives = new double[2];
		objectives[0] = (spec.won_models_fitness + spec.lost_models_fitness) / 2.0d;	
		objectives[1] = spec.syntactic_distance;
		return objectives;
	}
	
	public Map<Integer,SpecificationSolution> iDsFromNonSubsumedSolutions(Map<Integer,SpecificationSolution> specSolutions) {
		//Compute max ID;
		Set<Integer> ids = specSolutions.keySet();
		int max = -1;
		for (Integer id : ids) {
			if (id > max) {
				max = id;
			}
		}
		return nonSubsumedSolutions(specSolutions,max);
	}
	
	public List<SpecificationSolution> top(int top, List<SpecificationSolution> specSolutions) {
		List<SpecificationSolution> lSpec = new ArrayList<SpecificationSolution>();
		int end = top;
		for (int i = 0; (i < end) && (i < specSolutions.size()); ++i) {
			lSpec.add(specSolutions.get(i));
		}
		return lSpec;
	}
	
	public List<SpecificationSolution> nonSubsumedSolutions(List<SpecificationSolution> specSolutions) {
		Map<Integer,SpecificationSolution> map = new HashMap();
		for (int i = 0; i < specSolutions.size(); ++i) {
			map.put(i, specSolutions.get(i));
		}
		map = nonSubsumedSolutions(map,specSolutions.size() - 1);
		Set<Integer> ids = map.keySet();
		List<SpecificationSolution> lSpec = new ArrayList<SpecificationSolution>();
		for (int id : ids) {
			lSpec.add(map.get(id));
		}
		return lSpec;
	}
	
	public Map<Integer,SpecificationSolution> nonSubsumedSolutions(Map<Integer,SpecificationSolution> specSolutions, int idMax) {
		SpecificationSolution [] specSolutionsArray = new SpecificationSolution[idMax + 1];
		Map<Integer,SpecificationSolution> nonSubsumedSolutions = new HashMap<Integer,SpecificationSolution>();
		Set<Integer> ids = specSolutions.keySet();
		for (Integer id : ids) {
			specSolutionsArray[id] = specSolutions.get(id);
		}
		for (int i = 0; i < idMax; ++i) {
			for (int j = 0; j < idMax; ++j) {
				if (i != j) {
					SpecificationSolution spec1 = specSolutionsArray[i];
					SpecificationSolution spec2 = specSolutionsArray[j];
					if (spec1 == null || spec2 == null) {
						continue;
					}
					//double [] objectives1 = spec1.objectives();
					//double [] objectives2 = spec2.objectives();
					double [] objectives1 = getObjectives(spec1);
					double [] objectives2 = getObjectives(spec2);
					boolean spec1IsBest = false;
					boolean spec2IsBest = false;
					for (int k = 0; k < objectives1.length; ++k) {
						if (objectives1[k] > objectives2[k]) {
							spec2IsBest = true; //min
						} else if (objectives1[k] <= objectives2[k]) {
							spec1IsBest = true; //min
						}
					}
					if (!spec1IsBest && !spec2IsBest) {
						specSolutionsArray[i] = null; //random
					} if (!spec1IsBest) {
						specSolutionsArray[i] = null;
					} if (!spec2IsBest) {
						specSolutionsArray[j] = null;
					}
				}
			}
		}
		int i = 0;
		for (SpecificationSolution spec : specSolutionsArray) {
			if (spec != null) {
				//System.out.println(i);
				nonSubsumedSolutions.put(i,spec);
			}
			++i;
		}
		return nonSubsumedSolutions;
	}
	
	/**
	 * public List<SpecificationSolution> nonSubsumedSolutions(Map<Integer,SpecificationSolution> specSolutions, int max) {
		SpecificationSolution [] specSolutionsArray = new SpecificationSolution[max];
		List<SpecificationSolution> nonSubsumedSolutions = new ArrayList<SpecificationSolution>();
		int id = 0;
		Set<Integer> ids = specSolutions.keySet();
		for (SpecificationSolution spec : specSolutions) {
			specSolutionsArray[id++] = spec;
		}
		for (int i = 0; i < max; ++i) {
			for (int j = 0; j < max; ++j) {
				if (i != j) {
					SpecificationSolution spec1 = specSolutionsArray[i];
					SpecificationSolution spec2 = specSolutionsArray[j];
					if (spec1 == null || spec2 == null) {
						continue;
					}
					//double [] objectives1 = spec1.objectives();
					//double [] objectives2 = spec2.objectives();
					double [] objectives1 = getObjectives(spec1);
					double [] objectives2 = getObjectives(spec2);
					boolean spec1IsBest = false;
					boolean spec2IsBest = false;
					for (int k = 0; k < objectives1.length; ++k) {
						if (objectives1[k] > objectives2[k]) {
							spec2IsBest = true; //min
						} else if (objectives1[k] <= objectives2[k]) {
							spec1IsBest = true; //min
						}
					}
					if (!spec1IsBest && !spec2IsBest) {
						specSolutionsArray[i] = null; //random
					} if (!spec1IsBest) {
						specSolutionsArray[i] = null;
					} if (!spec2IsBest) {
						specSolutionsArray[j] = null;
					}
				}
			}
		}
		int i = 0;
		for (SpecificationSolution spec : specSolutionsArray) {
			if (spec != null) {
				System.out.println(i);
				nonSubsumedSolutions.add(spec);
			}
			++i;
		}
		return nonSubsumedSolutions;
	}
	 */
	
	/**
	 * Read csv file that contains fitness.
	 * @param csvFile
	 * @return list of specifications with fitness associated.
	 */
	public List<SpecificationSolution> readFitness(File csvFile) {
		Map<Integer,SpecificationSolution> mapIdSpecificationSolution = readIdAndFitness(csvFile);
		List<SpecificationSolution> specificationSolution = new ArrayList<SpecificationSolution>();
		for (int i = 0; i < mapIdSpecificationSolution.size(); ++i) {
			SpecificationSolution spec = mapIdSpecificationSolution.get(i);
			if (spec != null) {
				specificationSolution.add(spec);
			}
		}
		return specificationSolution;
	}
	
	
	//public List<SpecificationSolution> readFitness(File csvFile) {
	public Map<Integer,SpecificationSolution> readIdAndFitness(File csvFile) {
		//List<SpecificationSolution> lSpecificationSolution = new ArrayList<SpecificationSolution>();
		Map<Integer,SpecificationSolution> mapIdSpecificationSolution = new HashMap<Integer, SpecificationSolution>();
		try {
			boolean isHead = true;
			String line;
			ArrayList<String> head = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader(csvFile));
			while((line = br.readLine()) != null) {
				if (isHead) {
					isHead = false;
					String[] colNames = line.split(",");
					for (String name : colNames) {
						head.add(name);
					}
				} else {
					Iterator<String> ite = head.iterator();
					SpecificationSolution specSol = new SpecificationSolution();
					String[] fitness = line.split(",");
					int id = -1;
					for (int i = 0; i < fitness.length; ++i) {
						String value = fitness[i];
						String headName = ite.next();
						if (headName.equals("id")) {
							id = Integer.parseInt(value);
						} else if (headName.equals("status_fitness")) {
							//specSol.status_fitness = 1.0d - Double.parseDouble(value);
							specSol.status_fitness = 0.0d;
						} else if (headName.equals("bcGradeOfImprovement")) {
							//specSol.bcGradeOfImprovement = 1.0d - Double.parseDouble(value);
							specSol.bcGradeOfImprovement = 0.0d;
						} else if (headName.equals("removedBCLikelihood")) {
							specSol.removedBCLikelihood = 1.0d - Double.parseDouble(value);
						} else if (headName.equals("survivalBCLikelihood")) {
							specSol.survivalBCLikelihood = 1.0d - Double.parseDouble(value);
						} else if (headName.equals("lost_models_fitness")) {
							specSol.lost_models_fitness = 1.0d - Double.parseDouble(value);
						} else if (headName.equals("won_models_fitness")) {
							specSol.won_models_fitness = 1.0d - Double.parseDouble(value);
						} else if (headName.equals("syntactic_distance")) {
							specSol.syntactic_distance = 1.0d - Double.parseDouble(value);
						}
					}
					mapIdSpecificationSolution.put(id, specSol);
					//lSpecificationSolution.add(specSol);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//return lSpecificationSolution;
		return mapIdSpecificationSolution;
	}
	
}
