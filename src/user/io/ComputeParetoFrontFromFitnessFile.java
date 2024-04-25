package user.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.jgap.InvalidConfigurationException;

import gov.nasa.ltl.trans.ParseErrorException;
import optimization.specification.Spec;
import optimization.specification.SpecificationSolution;
import owl.ltl.Formula;
import utils.stats.ComputeQualityIndicators;

public class ComputeParetoFrontFromFitnessFile {

	//private static List<String> domain = new LinkedList<String>();
	//private static List<String> goals = new LinkedList<String>();
	private static List<String> ins = new LinkedList<String>();
	private static List<String> outs = new LinkedList<String>();
		
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		String outname = "";
		String infitnessfolder = "";
		String outfitness = "";
		 
		for (int i = 0; i< args.length; i++ ){
			if(args[i].startsWith("-ins=")) {
				ins = Arrays.asList(args[i].replace("-ins=","").split("\\s*,\\s*"));
			}
			else if(args[i].startsWith("-outs=")) {
				outs = Arrays.asList(args[i].replace("-outs=","").split("\\s*,\\s*"));
			}
			else if(args[i].startsWith("-infitnessfolder=")) {
				infitnessfolder = args[i].replace("-infitnessfolder=","");
			}
			else if(args[i].startsWith("-out=")) {
				outname = args[i].replace("-out=","");
				solvers.LTLSolver.SATID = outname;
				solvers.LTLSolver.init();
			}
			else if(args[i].startsWith("-outfitness=")) {
				outfitness = args[i].replace("-outfitness=","");
			}
		}
		System.out.println("In:" + ins.size());
		System.out.println("Out:" + outs.size());
		
		Map<String,Map<Integer,SpecificationSolution>> fileToIdToSpecSolutions = getParetoFront(new File(infitnessfolder));
		Set<String> fitnessFiles = fileToIdToSpecSolutions.keySet();
		for (String fileName : fitnessFiles) {
			File output = new File(outfitness + "/" + fileName + "_paretoFront");
			Map<Integer,SpecificationSolution> idToSpecSolutions = fileToIdToSpecSolutions.get(fileName);
			Set<Integer> ids = idToSpecSolutions.keySet();
			output.createNewFile();
			FileUtils.write(output,"id,newBCs\n","UTF-8",true);
			for (Integer id : ids) {
				SpecificationSolution specSolution = idToSpecSolutions.get(id);
				try {
					int newBCs = Main.computeRepairsUsefulness(specSolution,"").size();
					String content = id + "," + newBCs; 
					FileUtils.write(output,content + "\n","UTF-8",true);
				} catch (ParseErrorException | InvalidConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
 		
		solvers.LTLSolver.clear();
	}
	

	/**
	 * Pareto front with fitness, IDs, and specification (domain and goals).
	 */
	public static Map<String,Map<Integer,SpecificationSolution>> getParetoFront(File folder) {
		File[] listFiles = folder.listFiles();
		Map<String,Map<Integer,SpecificationSolution>> fileToIdToSpecSolutions = new HashMap<String,Map<Integer,SpecificationSolution>>();
		for (File file : listFiles) {
			if (file.getName().endsWith(".csv") || file.getName().endsWith(".csv_random")) {
				ComputeQualityIndicators indicators = new ComputeQualityIndicators();
				Map<Integer,SpecificationSolution> idAndspecSolutions = indicators.readIdAndFitness(file);
				idAndspecSolutions = indicators.iDsFromNonSubsumedSolutions(idAndspecSolutions);
				String logName;
				if (file.getName().endsWith(".csv_random")) {
					logName = file.getName().replace(".csv_random", "");
					logName = logName.replace("fitness", "");
					logName = logName.replace("_random", "");
					logName = "print-repair" + logName + ".out_random";
				} else {
	 				logName = file.getName().replace(".csv", "");
					logName = logName.replace("fitness", "");
					logName = "print-repair" + logName + ".out";
				}
				recoveryDomainAndGoals(idAndspecSolutions, new File(file.getParentFile().getAbsoluteFile() + File.separator + logName));
				recoveryDomainAndGoals(idAndspecSolutions,file);
				fileToIdToSpecSolutions.put(file.getName(),idAndspecSolutions);
			}
		}
		return fileToIdToSpecSolutions;
	}
	
	private static List<String> splitFormula(String exp) {
		List<String> formulas = new ArrayList<String>();
		char[] expArray = exp.toCharArray();
		int parentheses = 0;
		int lastIndex = 0;
		boolean wasZero = true;
		boolean firstSubFormula = true;
		int i = 0;
		int lenght = expArray.length;
		//while (i < lenght) {
			if (expArray[i] == '(') {
				++i;
				lastIndex = i;
				lenght = lenght - 1;
			} //else {
			//	break;
			//}
		//}
		for (;i < lenght; ++i) {
			char current = expArray[i];
			if (parentheses == 0) {
				if (current == '&') {
					formulas.add(new String(expArray, lastIndex, i - lastIndex));
					lastIndex = i + 1;
				}
				wasZero = true;
			} else {
				wasZero = false;
			}
			if (current == '(') {
				++parentheses;
			} else if (current == ')') {
				--parentheses;
			}
			//if (parentheses == 0 && (!wasZero)) {
				//backtracking
				//if (firstSubFormula) {
					//firstSubFormula = false;
				//} else {
					//for (int j = lastIndex + 1; j < lenght; ++j) {
						//if (expArray[j] == '&') {
							//lastIndex = j + 1;
							//formulas.add(new String(expArray, lastIndex, i - lastIndex + 1));
							//formulas.add(new String(expArray, lastIndex, i - lastIndex + 1));
							//lastIndex = i + 1;
							//break;
						//} else if (expArray[j] == '(') {
						//	break;
						//}
					//}
				//}
				
				//formulas.add(new String(expArray, lastIndex, i - lastIndex + 1));
				//lastIndex = i;
			//}
		}
		if (lastIndex < lenght) {
			formulas.add(new String(expArray, lastIndex, lenght - lastIndex));
		}
		return formulas;
	}
	
	public static void recoveryDomainAndGoals(Map<Integer,SpecificationSolution> idAndSpecSolutions, File specFile) {
		Set<Integer> ids = idAndSpecSolutions.keySet();
		Spec spec;
		for (Integer id : ids) {
			List<Formula> domm = new ArrayList<Formula>();
			List<Formula> goalss = new ArrayList<Formula>();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(specFile), "UTF-8"))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (line.contains("Solution N: " + id)) {
						line = br.readLine();
						if (line == null) continue;

						String[] full = line.split("Specification: ");
						String specWithParenthesesAndComma = full[1];
						//line
						full = specWithParenthesesAndComma.split(",");
						//String dom = full[0].replaceFirst("(", "");
						String dom = full[0].substring(1, full[0].length());
						String goals = full[1];
						goals = goals.substring(0, goals.length() - 1);
						
						List<String> dommStr = splitFormula(dom);
						List<String> goalsStr = splitFormula(goals);
						System.out.println(specWithParenthesesAndComma);
						System.out.println(dommStr);
						System.out.println(goalsStr);
						domm = utils.Formula_Utils.toFormulaList(dommStr,ins,outs);
						goalss = utils.Formula_Utils.toFormulaList(goalsStr,ins,outs);
	
						spec = new Spec(domm,goalss,ins,outs);
						SpecificationSolution specSol = idAndSpecSolutions.get(id);
						specSol.spec = spec;
					}
			   }
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}