package com.ml.scu.project.clustering;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PR_KmeansClustering {

	String numRegex = "^\\d*\\.\\d+|\\d+\\.\\d*$";
	private static DecimalFormat df2 = new DecimalFormat(".###");
	Map<String, List<String>> buildRealData = new HashMap<>();
	List<String[]> dataSetValues = new ArrayList<>();

	//find the distance between clusters and clusters in k means 
	Map<Double, List<List<String>>> findTheDistanceBetweenCenters(int[] centerInd) {

		Double[][] minDistanceLocal = new Double[dataSetValues.size()][centerInd.length];
		HashMap<Integer, List<String>> currRes = null;

		resetData();

		if (dataSetValues.size() == 0)
			readTheData();

		List<String[]> centerPoints = new ArrayList<>();
		for (int i = 0; i < centerInd.length; i++) {
			centerPoints.add(dataSetValues.get(centerInd[i]));
		}

		boolean kmeansFlag = true;
		int h=0;
		//stopping condition for converging point
		while (kmeansFlag && h<25) {
			List<String[]> centersNew = new ArrayList<>();
			Double[][] distanceBetPoints = new Double[dataSetValues.size()][centerInd.length];

			//find the distance between point
			findDistanceForKmeans(centerPoints, distanceBetPoints);
			
			//find the minimum disatance
			currRes = fillUpTheMinDistDataForKmeans(distanceBetPoints, minDistanceLocal);

			//find new center
			centersNew = findNewCenters(currRes);
			
			//compare the current and previous center for convergenece
			kmeansFlag = !compareTwoCenterPoints(centersNew, centerPoints);

			centerPoints.clear();
			centerPoints.addAll(centersNew);

			h++;
		}

//		System.out.println("**************** K-means Clustering ****************");
//		System.out.println(currRes.values());

		List<List<String>> finalRes = new ArrayList<>();
		finalRes.addAll(currRes.values());
		//
//		System.out.println(finalRes);

		//find the mean
		Double kMeansCost = findTheMean(finalRes);
		
		Map<Double, List<List<String>>> hmRes = new TreeMap<>();
		hmRes.put(kMeansCost, finalRes);
		
		return hmRes;
	}

	//find the mean
	private Double findTheMean(List<List<String>> finalRes) {
		List<String[]> meanDataVal = new ArrayList<>();
		String meanDataset[] = new String[dataSetValues.stream().findFirst().get().length];

		for (int i = 0; i < finalRes.size(); i++) {
			List<String> lstTmp = finalRes.get(i);
			String meanDatasetLocal[] = new String[dataSetValues.stream().findFirst().get().length];

			for (String ind : lstTmp) {
				String[] arr = dataSetValues.get(Integer.parseInt(ind));

				int j = 0;
				for (String attVal : arr) {
					Double tmp = 0.0;
					if (meanDatasetLocal[j] != null) {
						if (attVal != null && attVal.matches(numRegex)) {
							tmp = Double.parseDouble(meanDatasetLocal[j]) + Double.parseDouble(attVal);
						}
					} else {
						if (attVal != null && attVal.matches(numRegex)) {
							tmp = Double.parseDouble(attVal);
						}
					}
					meanDatasetLocal[j] = String.valueOf(tmp);
					j++;
				}
			}

			for (int x = 0; x < meanDataset.length; x++) {
				Double tmp = Double.parseDouble(meanDatasetLocal[x]) / lstTmp.size();
				meanDatasetLocal[x] = String.valueOf(tmp);
			}
			meanDataVal.add(meanDatasetLocal);
		}

		double sum = 0.0;

		for (int i = 0; i < finalRes.size(); i++) {
			List<String> lstTmp = finalRes.get(i);
			for (String ind : lstTmp) {
				//find distance betweeen k means
				sum += findDistKmeans(dataSetValues, meanDataVal, Integer.parseInt(ind), i);
			}
		}
		
		return sum;
	}

	private void resetData() {
		dataSetValues.clear();
		buildRealData.clear();
	}

	//compare two data center points
	private boolean compareTwoCenterPoints(List<String[]> centersNew, List<String[]> centerPoints) {
		List<Double> centerNewSum = new ArrayList<>();
		List<Double> centerPointsSum = new ArrayList<>();

		for (int i = 0; i < centerPoints.size(); i++) {
			String[] tmp = centerPoints.get(i);
			Double local = 0.0;
			for (int x = 0; x < tmp.length; x++) {
				if (tmp[x] != null && tmp[x].matches(numRegex))
					local += Double.parseDouble(tmp[x]);
			}
			centerPointsSum.add(Double.parseDouble(df2.format(local)));
		}

		for (int i = 0; i < centersNew.size(); i++) {
			String[] tmp = centersNew.get(i);
			Double local = 0.0;
			for (int x = 0; x < tmp.length; x++) {
				if (tmp[x] != null && tmp[x].matches(numRegex))
					local += Double.parseDouble(tmp[x]);
			}
			centerNewSum.add(Double.parseDouble(df2.format(local)));
		}

		List<Double> sumInterSect = new ArrayList<>(centerPointsSum);
		sumInterSect.retainAll(centerNewSum);

		if (sumInterSect.size() == centerNewSum.size())
			return true;
		else
			return false;
	}

	//find new centers
	private List<String[]> findNewCenters(HashMap<Integer, List<String>> currRes) {

		List<String[]> newCenters = new ArrayList<>();

		Iterator<Integer> itr = currRes.keySet().iterator();
		while (itr.hasNext()) {
			List<String> lst = currRes.get(itr.next());
			String[] localCenter = new String[dataSetValues.get(0).length];
			int j = 0;
			for (String i1 : lst) {
				String[] data = dataSetValues.get(Integer.parseInt(i1));
				j = 0;
				for (String s1 : data) {
					if (s1.matches(numRegex)) {
						if (localCenter[j] != null)
							localCenter[j] = String
									.valueOf((Double.parseDouble(s1) + Double.parseDouble(localCenter[j])));
						else
							localCenter[j] = String.valueOf(Double.parseDouble(s1));
					}
					j++;
				}
			}

			for (int x = 0; x < localCenter.length; x++)
				if (localCenter[x] != null)
					localCenter[x] = df2.format((Double.parseDouble(localCenter[x]) / lst.size()));

			newCenters.add(localCenter);
		}
		return newCenters;
	}

	//fill up the minimum diatnce for k emeans
	private HashMap<Integer, List<String>> fillUpTheMinDistDataForKmeans(Double[][] distanceBetPoints,
			Double[][] minDistanceLocal) {

		HashMap<Integer, List<String>> currResNew = new HashMap<>();

		int minRowNum;
		Double minRowval;

		for (int i = 0; i < distanceBetPoints.length; i++) {
			minRowval = Double.MAX_VALUE;
			minRowNum = 0;
			for (int j = 0; j < distanceBetPoints[0].length; j++) {
				Double local = distanceBetPoints[i][j];
				if (local != null && local < minRowval) {
					minRowNum = j;
					minRowval = (double) distanceBetPoints[i][j];
				}
			}
			if (currResNew.get(minRowNum) == null)
				currResNew.put(minRowNum, new ArrayList<>());
			List<String> tmp = currResNew.get(minRowNum);
			tmp.add(String.valueOf(i));
			currResNew.put(minRowNum, tmp);
		}
		return currResNew;
	}

	Double findDistKmeans(List<String[]> dataSetValuesLocal, List<String[]> centerPoints, int i, int j) {
		String[] data1 = dataSetValuesLocal.get(i);
		String[] data2 = centerPoints.get(j);

		Double local = 0.0;
		Double sum = 0.0;

		for (int x = 0; x < data1.length; x++) {
			if (data1[x]!=null && data2[x]!=null && data1[x].matches(numRegex) && data2[x].matches(numRegex)) {
				Double temp = Double.parseDouble(data1[x]) - Double.parseDouble(data2[x]);
				sum += Math.pow(temp, 2);
			}
		}
		local = Math.sqrt(sum);
		return local;
	}

	//fill up the distance for k means
	private void findDistanceForKmeans(List<String[]> centerPoints, Double[][] distanceBetPoints) {

		for (int j = 0; j < dataSetValues.size(); j++) {
			for (int i = 0; i < centerPoints.size(); i++) {
				Double localDistVal = findDistKmeans(dataSetValues, centerPoints, j, i);
				distanceBetPoints[j][i] = Double.parseDouble(df2.format(localDistVal));
			}
		}
	}

	//read the data if it was null
	private void readTheData() {
		StringBuffer filename = new StringBuffer();
		BufferedReader br1 = null;
		String currLine;

		// getting the path value for Feature file
		filename.append(Constant.path);
		filename.append(Constant.Dataset);

		try {
			br1 = new BufferedReader(new InputStreamReader(new FileInputStream(filename.toString())));
			int cnt = 0;
			while ((currLine = br1.readLine()) != null) {
				if (!currLine.equals("") || !currLine.equals(" ")) {
					dataSetValues.add(currLine.split(","));

					String classNm = currLine.substring(currLine.lastIndexOf(","), currLine.length());
					if (buildRealData.get(classNm) == null)
						buildRealData.put(classNm, new ArrayList<>());

					List<String> lstTmp = buildRealData.get(classNm);
					lstTmp.add(String.valueOf(cnt));
					buildRealData.put(classNm, lstTmp);
					cnt++;
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			if (br1 != null)
				try {
					br1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

}
