package optimization.runners.paretofront;

import java.util.ArrayList;
import java.util.List;

import optimization.specification.SpecificationSolution;

public class MinimumBCGradeOfImprovement implements HandlerParetoFront {

	public List<SpecificationSolution> filter(List<SpecificationSolution> lspecificationSolution) {
		List<SpecificationSolution> filteredSpecificationSolution = new ArrayList<SpecificationSolution>();
		for (SpecificationSolution specSolution: lspecificationSolution) {
			if (specSolution.bcGradeOfImprovement == 0.0d) {
				filteredSpecificationSolution.add(specSolution);
			}
		}
		return filteredSpecificationSolution;
	}
	
}
