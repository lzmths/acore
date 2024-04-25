package optimization.runners;

import java.util.List;

import optimization.specification.Spec;
import optimization.specification.SpecificationSolution;
import owl.ltl.Formula;

public interface AlgorithmRunner {

	public List<SpecificationSolution> run(Spec originalSpec, List<Formula> boundaryConditions);
	
	public List<SpecificationSolution> getSolutions();
	
	public List<Formula> getBoundaryConditions();
	
	public Spec getOriginalSpecification();
	
	public String printExecutionTime();
	
}
