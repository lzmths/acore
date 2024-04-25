package optimization.runners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.algorithm.multiobjective.mosa.MOSA;
import org.uma.jmetal.algorithm.multiobjective.mosa.cooling.CoolingScheme;
import org.uma.jmetal.algorithm.multiobjective.mosa.cooling.impl.Exponential;
import org.uma.jmetal.algorithm.multiobjective.mosa.cooling.impl.Logarithmic;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.WFGHypervolume;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.archive.impl.HypervolumeArchive;
import org.uma.jmetal.util.archive.impl.SpatialSpreadDeviationArchive;

import optimization.runners.paretofront.HandlerParetoFront;
import optimization.runners.paretofront.MinimumBCGradeOfImprovement;
import optimization.specification.Spec;
import optimization.specification.SpecificationMutation;
import optimization.specification.SpecificationProblem;
import optimization.specification.SpecificationSolution;
import owl.ltl.Formula;

public class MOSARunner implements AlgorithmRunner {

	private List<SpecificationSolution> solutions = new ArrayList<SpecificationSolution>();

	private List<Formula> boundaryConditions;
	
	private Spec spec;
	
	private int maxPopulation;
	
	private int maxEvaluation;
	
	private double initialTemperature;
	
	private double mutationProbability;
	
	private Random random;
	
	private int gaGuaranteesPreferenceFactor;
	
	private boolean onlyInputsInAssumptions;
	
	private int gaGeneNumOfMutations;
	
	private long computingTime;
	
	public MOSARunner(int maxPopulation, int maxEvaluation, double initialTemperature, 
			double mutationProbability, Random random, int gaGuaranteesPreferenceFactor, boolean onlyInputsInAssumptions, int gaGeneNumOfMutations) {
		this.maxEvaluation = maxEvaluation;
		this.initialTemperature = initialTemperature;
		this.maxPopulation = maxPopulation;
		this.mutationProbability = mutationProbability;
		this.random = random;
		this.gaGuaranteesPreferenceFactor = gaGuaranteesPreferenceFactor;
		this.onlyInputsInAssumptions = onlyInputsInAssumptions;
		this.gaGeneNumOfMutations = gaGeneNumOfMutations;
	}
	
	@Override
	public List<SpecificationSolution> run(Spec originalSpec, List<Formula> boundaryConditions) {
		this.spec = originalSpec; 
		this.boundaryConditions = boundaryConditions;
		
		try {
			Problem<SpecificationSolution> problem = new SpecificationProblem(originalSpec, boundaryConditions);
			BoundedArchive<SpecificationSolution> archive = new CrowdingDistanceArchive<SpecificationSolution>(this.maxPopulation);
			MutationOperator<SpecificationSolution> mutationOperator = new SpecificationMutation(this.mutationProbability, 
					this.random, this.gaGuaranteesPreferenceFactor, this.onlyInputsInAssumptions, this.gaGeneNumOfMutations);
			double beta = 0.002;
			CoolingScheme coolingScheme = new Exponential(beta);
			
			MOSA<SpecificationSolution> mosa = new MOSA(
					problem,
					this.maxEvaluation, 
					archive,
					mutationOperator,
					this.initialTemperature, 
					coolingScheme);
			
			org.uma.jmetal.example.AlgorithmRunner exec = new org.uma.jmetal.example.AlgorithmRunner.Executor(mosa).execute();
			this.computingTime = exec.getComputingTime();

			this.solutions = mosa.getResult();
		    printSolutions(this.solutions);
		    HandlerParetoFront paretoFront = new MinimumBCGradeOfImprovement();
		    this.solutions = paretoFront.filter(this.solutions);
		    printSolutions(this.solutions);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return this.solutions;
	}

	private void printSolutions(List<SpecificationSolution> solutions) {
	    System.out.println("Solution SIZE: " + this.solutions.size());
	    for (SpecificationSolution sol : solutions) {
	    	System.out.println("------");
	    	System.out.println(sol.spec);
	    	System.out.println(sol.objectives()[0]);
	    	System.out.println(sol.objectives()[1]);
	    	System.out.println(sol.objectives()[2]);
	    	System.out.println(sol.objectives()[3]);
	    	System.out.println("------");
	    }
	}
	
	@Override
	public List<SpecificationSolution> getSolutions() {
		return this.solutions;
	}

	@Override
	public List<Formula> getBoundaryConditions() {
		return this.boundaryConditions;
	}

	@Override
	public Spec getOriginalSpecification() {
		return this.spec;
	}

	@Override
	public String printExecutionTime() {
		return String.valueOf(this.computingTime);
	}
	
}
