package utils.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.apache.commons.lang3.tuple.Pair;

public class Stats {
	
	List<Map<String, Double>> quality1;
	List<Map<String, Double>> quality2;
	List<Map<String, Double>> quality3;
	List<Map<String, Double>> quality4;
	
	public Stats() {
		
	}
	
	public Stats(List<Map<String, Double>> quality1, List<Map<String, Double>> quality2) {
		this.quality1 = quality1;
		this.quality2 = quality2;
	}
	
	public Stats(List<Map<String, Double>> quality1, List<Map<String, Double>> quality2, List<Map<String, Double>> quality3, List<Map<String, Double>> quality4) {
		this.quality1 = quality1;
		this.quality2 = quality2;
		this.quality3 = quality3;
		this.quality4 = quality4;
	}
	
	public Stats(ArrayList<Stats> lStats) {
		this.quality1 = new ArrayList<Map<String, Double>>();
		this.quality2 = new ArrayList<Map<String, Double>>();
		for (Stats stats : lStats) {
			this.quality1.addAll(stats.getQuality1());
			this.quality2.addAll(stats.getQuality2());
		}
	}
	
	public List<Map<String, Double>> getQuality1() {
		return this.quality1;
	}
	
	public List<Map<String, Double>> getQuality2() {
		return this.quality2;
	}
	
	public List<Map<String, Double>> getQuality3() {
		return this.quality3;
	}
	
	public List<Map<String, Double>> getQuality4() {
		return this.quality4;
	}
	
	public Pair<Double,String> wilcoxon(String indicator, double alpha) {
		return wilcoxon(this.quality1, this.quality2, indicator, alpha);
	}
	
	public Pair<Double,String> wilcoxon(List<Map<String, Double>> quality1, List<Map<String, Double>> quality2, String indicator, double alpha) throws DimensionMismatchException{
		double [] quality1Array = new double[quality1.size()];
		double [] quality2Array = new double[quality2.size()];
		int i = 0;
		for (Map<String, Double> quality : quality1) {
			quality1Array[i] = quality.get(indicator);
			++i;
		}
		i = 0;
		for (Map<String, Double> quality : quality2) {
			quality2Array[i] = quality.get(indicator);
			++i;
		}
		WilcoxonSignedRankTest wilcoxon = new WilcoxonSignedRankTest();
		//System.out.println("size1: " + quality1Array.length);
		//System.out.println("size2: " + quality2Array.length);
		//if (quality1Array.length == 2 && quality1Array.length == 10) {
		//	System.out.println("xxx");
		//}
		double pvalue = wilcoxon.wilcoxonSignedRankTest(quality1Array, quality2Array, false);
		DescriptiveStatistics stats1 = new DescriptiveStatistics(quality1Array);
		DescriptiveStatistics stats2 = new DescriptiveStatistics(quality2Array);
		String status = "";
    	if (pvalue < alpha) {
	    	if (stats1.getPercentile(50) >= stats2.getPercentile(50)) {
	    		status = "better";
	    	} else {
	    		status = "wrost";
	    	}
	    } else {
	    	status = "no diference";
	    }
	    Pair<Double, String> pvalueAndStatus = Pair.of(pvalue, status);
		return pvalueAndStatus;
	}
	
	public Pair<Double,String> mannWhitneyUTest(double [] quality1Array, double [] quality2Array, String indicator, double alpha) {
		MannWhitneyUTest mann = new MannWhitneyUTest();
		mann.mannWhitneyUTest(quality1Array, quality2Array);
		DescriptiveStatistics stats1 = new DescriptiveStatistics(quality1Array);
		DescriptiveStatistics stats2 = new DescriptiveStatistics(quality2Array);
		double pvalue = mann.mannWhitneyUTest(quality1Array, quality2Array);
		return Pair.of(pvalue, "TODO");
	}
	
}