package optimization.runners.paretofront;

import java.util.List;

import optimization.specification.SpecificationSolution;

public interface HandlerParetoFront {
	
	public List<SpecificationSolution> filter(List<SpecificationSolution> specificationSolution);

}
