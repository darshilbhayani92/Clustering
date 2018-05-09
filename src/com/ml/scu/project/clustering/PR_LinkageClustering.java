package com.ml.scu.project.clustering;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class PR_LinkageClustering {

	static List<String[]> dataSetValues = new ArrayList<>();
	Map<String, List<String>> buildRealData = new HashMap<>();
	static Double[][] distanceBetPointsCopy;

	Double minVal;
	Double minAttribute;

	String numRegex = "^\\d*\\.\\d+|\\d+\\.\\d*$";
	String numRegexInt = "[0-9]+";
	private static DecimalFormat df2 = new DecimalFormat("##0.0000");

	Map<Integer, String> hmRes = new HashMap<>();
	Map<Integer, Set<String>> finalRes = new HashMap<>();

	
	//Find the Kmeans plus plus data
	public void findTheKMeansPlusPlus() {
		
		Map<Double, List<List<List<String>>>> lioydsRes = new TreeMap<>();
		Double min = Double.MAX_VALUE;

		Random r = new Random();
		if (dataSetValues == null || dataSetValues.size() == 0)
			readTheData();

		int Low = 0;
		int High = dataSetValues.size();

		//select the first random centroid data point
		int ind = 0;
		int[] centerInd = new int[Constant.NoOfCluster];
		centerInd[ind] = r.nextInt(High - Low) + Low;

		Double[][] distanceBetPointsKmeansPlusCurr = new Double[dataSetValues.size()][1];
		Double[][] distanceBetPointsKmeansPlusPrev = new Double[dataSetValues.size()][1];
		
		
		while (ind < Constant.NoOfCluster-1) {
			ind++;
			//find the dynamic centroid which is furtherest to the pervious all centroids
			centerInd[ind] = findNextCentroids(centerInd, ind, distanceBetPointsKmeansPlusCurr, distanceBetPointsKmeansPlusPrev);
		}
		
		PR_KmeansClustering m1 = new PR_KmeansClustering();
		//find the distance between clusters and clusters in k means 
		Map<Double, List<List<String>>> hmRes = m1.findTheDistanceBetweenCenters(centerInd);

		for (Entry<Double, List<List<String>>> entry : hmRes.entrySet()) {
			if (lioydsRes.get(entry.getKey()) == null)
				lioydsRes.put(entry.getKey(), new ArrayList<>());
			List<List<List<String>>> tmp = lioydsRes.get(entry.getKey());
			tmp.add(entry.getValue());
			lioydsRes.put(entry.getKey(), tmp);

			min = Math.min(min, entry.getKey());
		} 
		
		List<List<List<String>>> lstRes = lioydsRes.get(min);

		System.out.println("Kmeans Plus cost :" + min);
		//for (List<List<String>> list : lstRes) {
			System.out.println(lstRes.stream().findFirst().get());
		//}

		//compare two clustering
		Double hamming = compareTwoClustering(lioydsRes.get(min).stream().findFirst().get());
		System.out.println("Hamming Distance : "+hamming);
		drawGraph(lioydsRes.get(min).stream().findFirst().get(), hamming, "K-Means Plus");
	}

	private int findNextCentroids(int[] centerInd, int ind, Double[][] distanceBetPointsKmeansPlusCurr, Double[][] distanceBetPointsKmeansPlusPrev) {

		//find next centeroid based on the previous one
		if (ind > 1) {
			System.arraycopy(distanceBetPointsKmeansPlusCurr, 0, distanceBetPointsKmeansPlusPrev, 0,
					distanceBetPointsKmeansPlusCurr.length);
			distanceBetPointsKmeansPlusCurr = new Double[dataSetValues.size()][1];
		}

		PR_KmeansClustering m11 = new PR_KmeansClustering();

		for (int j = 0; j < dataSetValues.size(); j++) {
			Double localDistVal = m11.findDistKmeans(dataSetValues, dataSetValues, j, ind);
			distanceBetPointsKmeansPlusCurr[j][0] = Double.parseDouble(df2.format(Math.pow(localDistVal, 2)));
		}

		for (int j = 1; j < dataSetValues.size(); j++) {
			distanceBetPointsKmeansPlusCurr[j][0] += distanceBetPointsKmeansPlusCurr[j - 1][0];
		}
		
		if (ind > 1) {
			for (int j = 1; j < dataSetValues.size(); j++) {
				distanceBetPointsKmeansPlusCurr[j][0] = Math.min(distanceBetPointsKmeansPlusCurr[j][0],
						distanceBetPointsKmeansPlusPrev[j][0]);
			}
		}

		//random value generation
		Random r = new Random();
		Double Low = distanceBetPointsKmeansPlusCurr[0][0];
		Double High = distanceBetPointsKmeansPlusCurr[dataSetValues.size() - 1][0];

		Double nextRandomNumber;
		if((High - Low)>0)
		 nextRandomNumber = r.nextInt((int) (High - Low)) + Low;
		else
			nextRandomNumber = r.nextInt() + Low;
		
		//retrun the row number of next centroid
		for (int j = 0; j < dataSetValues.size(); j++) {
			if (distanceBetPointsKmeansPlusCurr[j][0] > nextRandomNumber) {
				return j;
			}
		}

		return 0;
	}

	//Find the Kmeans
	public void findTheKMeans() {

		Map<Double, List<List<List<String>>>> lioydsRes = new TreeMap<>();
		Double min = Double.MAX_VALUE;

		//random number of running time k means and choose the best one of them
		for (int h = 0; h < 100; h++) {
			Random r = new Random();
			if (dataSetValues == null || dataSetValues.size() == 0)
				readTheData();

			int Low = 0;
			int High = dataSetValues.size();

			int[] centerInd = new int[Constant.NoOfCluster];
			for (int i = 0; i < centerInd.length; i++) {
				int localCenter = r.nextInt(High - Low) + Low;
				if (!Arrays.asList(centerInd).contains(localCenter)) {
					centerInd[i] = localCenter;
				} else
					i--;
			}
			// System.out.println(Arrays.toString(centerInd));

			PR_KmeansClustering m1 = new PR_KmeansClustering();
			//find the distance between clusters and clusters in k means 
			Map<Double, List<List<String>>> hmRes = m1.findTheDistanceBetweenCenters(centerInd);

			for (Entry<Double, List<List<String>>> entry : hmRes.entrySet()) {
				if (lioydsRes.get(entry.getKey()) == null)
					lioydsRes.put(entry.getKey(), new ArrayList<>());
				List<List<List<String>>> tmp = lioydsRes.get(entry.getKey());
				tmp.add(entry.getValue());
				lioydsRes.put(entry.getKey(), tmp);

				min = Math.min(min, entry.getKey());
			}
		}

		List<List<List<String>>> lstRes = lioydsRes.get(min);

		System.out.println("Minimum Kmeans cost :" + min);
//		for (List<List<String>> list : lstRes) {
			System.out.println(lstRes.stream().findFirst().get());
//		}

		//find the hamming distance
		Double hamming = compareTwoClustering(lioydsRes.get(min).stream().findFirst().get());
		System.out.println("Hamming Distance : "+hamming);
		drawGraph(lioydsRes.get(min).stream().findFirst().get(), hamming, "K-Means (Lioyd's Method)");
	}

	public void findDistanceBetPoints(String linkage) {
		//Reset the Data
		resetData();
		List<String> totalNoOfCluster = new ArrayList<>();

		//Read the Data first time
		readTheData();

		Double[][] distanceBetPoints = new Double[dataSetValues.size()][dataSetValues.size()];
		Double[][] minDistance = new Double[dataSetValues.size()][2];

		//Fill up the primary cluster and put each points in each cluster
		fillUpTheCluster(totalNoOfCluster);
		
		//Fill up the nxn matrix for the distance
		fillUpTheData(distanceBetPoints);
		
		//Fill up the minimum data based on the previous data
		fillUpTheMinDistData(distanceBetPoints, minDistance);

		//Copy of an original distance array
		distanceBetPointsCopy = new Double[dataSetValues.size()][dataSetValues.size()];
		System.arraycopy(distanceBetPoints, 0, distanceBetPointsCopy, 0, distanceBetPoints.length);

		Set<String> resData = new HashSet<>(hmRes.values());
		int i = dataSetValues.size();
		
		//run loop for all the attributes to get in
		while (i > -1) {
			Set<String> resDataLocal = new HashSet<>(resData);
			
			//Update the values based on various linkage
			UpdateValuesBasedOnMinValue(distanceBetPoints, minDistance, totalNoOfCluster, linkage);
			
			resData.clear();
			resData.addAll(hmRes.values());

			//logic to add the new cluster in the existing one
			if (resDataLocal.size() <= resData.size()) {
				resDataLocal.clear();
				resDataLocal.addAll(resData);
			}

			for (int p = 0; p < totalNoOfCluster.size(); p++) {
				resDataLocal.add(totalNoOfCluster.get(p));
			}

			//Finding all possible number of clusters and get the data based on the defined cluster value
			finalRes.put(resDataLocal.size(), resDataLocal);
			i--;
		}

		System.out.println("****************" + linkage + "****************");
//		System.out.println(finalRes);
		
		//Find the Hamming distance
		findHammingDistance(finalRes.get(Constant.NoOfCluster), linkage);
	}

	//Find the Hamming distance
	private void findHammingDistance(Set<String> resSet, String linkage) {
		List<String> lstOfRealData = new ArrayList<>();
		if (resSet != null && !resSet.isEmpty())
			lstOfRealData.addAll(resSet);
		List<List<String>> lstRes = new ArrayList<>();

		for (int i = 0; i < lstOfRealData.size(); i++) {
			String[] tmp = lstOfRealData.get(i).split("-");
			List<String> local = Arrays.asList(tmp);
			lstRes.add(local);
		}

		//compare two cluster and send the algorithm clustering results
		Double hamming = compareTwoClustering(lstRes);
		
		//Draw graph
		drawGraph(lstRes, hamming, linkage);
	}

	//Draw graph
	private void drawGraph(List<List<String>> lstRes, Double hamming, String linkage) {
		//check parameter for the graph
		if (Constant.drawGraph) {
			SimpleGrpah s11 = new SimpleGrpah();
			int sum = 0;
			for (List<String> list : lstRes)
				sum += list.size();

			int[] arr = new int[sum];
			int[] limit = new int[lstRes.size()];
			int cnt = 0;
			int lmt = 0;

			//creating the data based on the Graphical requierment 
			for (List<String> l11 : lstRes) {
				for (int h = 0; h < l11.size(); h++) {
					arr[cnt] = Integer.parseInt(l11.get(h));
					cnt++;
				}
				limit[lmt] = cnt;
				lmt++;
			}

			//setting up the parameter in the graph
			SimpleGrpah.data = arr;
			SimpleGrpah.limit = limit;
			SimpleGrpah.lable = linkage + " : With hamming Distance : " + df2.format(hamming);
			s11.drawGraph();
		}
	}

	//compare two cluster and send the algorithm clustering results
	private Double compareTwoClustering(List<List<String>> lstRes) {

		System.out.println("Linkage Clustering.." + lstRes);
		List<List<String>> lst = new ArrayList<>();

		//real data clustering
		Iterator<String> itr1 = buildRealData.keySet().iterator();
		while (itr1.hasNext()) {
			List<String> tmp = buildRealData.get(itr1.next());
			lst.add(tmp);
		}

		System.out.println("Real Clustering.." + lst);

		List<String> resInCluster = new ArrayList<>();
		List<String> resBetweenCluster = new ArrayList<>();

		//find combination for the real data
		findCombination(lst, 0, resInCluster, resBetweenCluster);

		//find combination and compare the real data with the real data clustering
		findCombinationCnt(lstRes, 0, resInCluster, resBetweenCluster);

		// System.out.println("AgreementCnt...." + AgreementCnt);
		// System.out.println("DisgreementCnt...." + DisgreementCnt);

		Double hammDist = (double) DisgreementCnt / ((double) DisgreementCnt + (double) AgreementCnt);
		System.out.println("Hamming Distnace : " + df2.format(hammDist));

		return hammDist;

	}

	int AgreementCnt = 0;
	int DisgreementCnt = 0;

	//find combination and compare the real data with the real data clustering
	private void findCombinationCnt(List<List<String>> lstaa, int ind, List<String> resInCluster,
			List<String> resBetweenCluster) {

		List<List<String>> tmp = new ArrayList<>();
		if (lstaa.size() > 0) {
			for (int i = 0; i < lstaa.get(ind).size(); i++) {
				List<String> lst = new ArrayList<>();
				lst.add(String.valueOf(lstaa.get(ind).get(i)));
				tmp.add(lst);
			}

			for (int i = 0; i < lstaa.size(); i++) {
				List<String> l1 = lstaa.get(i);
				// System.out.println(l1);
				for (int j = 0; j < tmp.size(); j++) {
					List<String> l2 = tmp.get(j);
					// System.out.println(l2);

					for (int h = 0; h < l1.size(); h++) {
						for (int k = 0; k < l2.size(); k++) {
							if (Integer.parseInt(l1.get(h)) != Integer.parseInt(l2.get(k))) {
								String checkPair = l1.get(h) + "-" + l2.get(k);
								if (i == ind) {
									if (resInCluster.contains(checkPair))
										AgreementCnt++;
									else if (resBetweenCluster.contains(checkPair))
										DisgreementCnt++;
								} else {
									if (resInCluster.contains(checkPair))
										DisgreementCnt++;
									else if (resBetweenCluster.contains(checkPair))
										AgreementCnt++;
								}
							}

						}
					}
				}
			}

			if (ind + 1 < lstaa.size())
				findCombination(lstaa, ind + 1, resInCluster, resBetweenCluster);
		}

	}

	//find combination for the real data
	private void findCombination(List<List<String>> lstaa, int ind, List<String> resInBetween,
			List<String> resBetweenCluster) {

		List<List<String>> tmp = new ArrayList<>();
		for (int i = 0; i < lstaa.get(ind).size(); i++) {
			List<String> lst = new ArrayList<>();
			lst.add(String.valueOf(lstaa.get(ind).get(i)));
			tmp.add(lst);
		}

		for (int i = 0; i < lstaa.size(); i++) {
			List<String> l1 = lstaa.get(i);
			// System.out.println(l1);
			for (int j = 0; j < tmp.size(); j++) {
				List<String> l2 = tmp.get(j);
				// System.out.println(l2);

				for (int h = 0; h < l1.size(); h++) {
					for (int k = 0; k < l2.size(); k++) {
						if (Integer.parseInt(l1.get(h)) != Integer.parseInt(l2.get(k))) {
							if (i == ind)
								resInBetween.add(l1.get(h) + "-" + l2.get(k));
							else
								resBetweenCluster.add(l1.get(h) + "-" + l2.get(k));
						}
					}
				}
			}
		}

		if (ind + 1 < lstaa.size())
			findCombination(lstaa, ind + 1, resInBetween, resBetweenCluster);
	}

	//reset the data
	private void resetData() {
		dataSetValues.clear();
		buildRealData.clear();
		hmRes.clear();
		finalRes.clear();
		minVal = Double.MAX_VALUE;
		minAttribute = 0.0;
	}

	private void printMainMinDistData(Double[][] minDistance) {
		for (int i = 0; i < minDistance.length; i++) {
			for (int j = 0; j < minDistance[0].length; j++) {
				if (minDistance[i][j] != Double.MAX_VALUE)
					System.out.print(minDistance[i][j] + " ");
				else
					System.out.print("- ");
			}
			System.out.println();
		}
		System.out.println();
	}

	private void printMainDistValData(Double[][] distanceBetPoints) {
		for (int i = 0; i < distanceBetPoints.length; i++) {
			for (int j = 0; j < distanceBetPoints[0].length; j++) {
				if (distanceBetPoints[i][j] != Double.MAX_VALUE)
					System.out.print(distanceBetPoints[i][j] + " ");
				else
					System.out.print("- ");
			}
			System.out.println();
		}
		System.out.println();
	}

	//Update the values based on various linkage
	private void UpdateValuesBasedOnMinValue(Double[][] distanceBetPoints, Double[][] minDistance,
			List<String> totalNoOfCluster, String linkage) {
		int attribute1 = 0, attribute2 = 0;
		double min = Double.MAX_VALUE;

		//setting the value and check with the minimum value
		for (int i = 0; i < minDistance.length; i++) {
			if (minDistance[i][1] != Double.MAX_VALUE && minDistance[i][1] < min) {
				min = minDistance[i][1];
				attribute1 = i;
				attribute2 = minDistance[i][0].intValue();
			}
		}

		//change the logic of updating the data points on the basis of linkage
		if (linkage.equals(Constant.singleLinkage)) {
			PR_SingleLinkageClustering s1 = new PR_SingleLinkageClustering();
			s1.updateDataPoints(attribute1, attribute2, distanceBetPoints);
		} else if (linkage.equals(Constant.completeLinkage)) {
			PR_CompleteLinkageClustering c1 = new PR_CompleteLinkageClustering();
			c1.updateDataPoints(attribute1, attribute2, distanceBetPoints);
		} else if (linkage.equals(Constant.averageLinkage)) {
			PR_AverageLinkageClustering a1 = new PR_AverageLinkageClustering();
			a1.updateDataPoints(attribute1, attribute2, distanceBetPoints, hmRes);
		}

		// Update the minValue Matrix
		minDistance[attribute1][0] = Double.MAX_VALUE;
		minDistance[attribute1][1] = Double.MAX_VALUE;

		// printMainDistValData(distanceBetPoints);
		// printMainMinDistData(minDistance);

		//fill up the minimum distance
		fillUpTheMinDistData(distanceBetPoints, minDistance);

		// update the existing cluster with new combined clusters
		if (hmRes.get(attribute1) == null && hmRes.get(attribute2) == null) {
			String resVal = String.valueOf(attribute1) + "-" + String.valueOf(attribute2);
			hmRes.put(attribute1, resVal);
			hmRes.put(attribute2, resVal);
		} else {
			if (hmRes.get(attribute1) != null && hmRes.get(attribute2) == null) {
				String local = hmRes.get(attribute1) + "-" + attribute2;
				hmRes.remove(attribute1);

				String[] tmp = local.split("-");
				for (String s1 : tmp)
					hmRes.put(Integer.parseInt(s1), local);

			} else if (hmRes.get(attribute2) != null && hmRes.get(attribute1) == null) {

				String local = hmRes.get(attribute2) + "-" + attribute1;
				hmRes.remove(attribute2);

				String[] tmp = local.split("-");
				for (String s1 : tmp)
					hmRes.put(Integer.parseInt(s1), local);

			} else {
				String local = hmRes.get(attribute1) + "-" + hmRes.get(attribute2);

				if (!hmRes.get(attribute1).equals(hmRes.get(attribute2))) {
					hmRes.remove(attribute1);
					hmRes.remove(attribute2);

					String[] tmp = local.split("-");
					for (String s1 : tmp)
						hmRes.put(Integer.parseInt(s1), local);
				}
			}
		}

		if (totalNoOfCluster.contains(String.valueOf(attribute1)))
			totalNoOfCluster.remove(String.valueOf(attribute1));
		if (totalNoOfCluster.contains(String.valueOf(attribute2)))
			totalNoOfCluster.remove(String.valueOf(attribute2));
	}

	//Fill up the minimum data based on the previous data
	protected void fillUpTheMinDistData(Double[][] distanceBetPoints, Double[][] minDistance) {
		for (int i = 0; i < distanceBetPoints.length; i++) {
			minVal = Double.MAX_VALUE;
			minAttribute = 0.0;
			for (int j = 0; j < distanceBetPoints[0].length; j++) {
				Double local = distanceBetPoints[i][j];
				if (local < minVal) {
					minAttribute = (double) j;
					minVal = local;
				}
			}

			//find the minimum distance
			if (minVal != Double.MAX_VALUE) {
				minDistance[i][0] = minAttribute;
				minDistance[i][1] = Double.parseDouble(df2.format(minVal));
			}
		}
	}

	//Fill up the primary cluster and put each points in each cluster
	private void fillUpTheCluster(List<String> totalNoOfCluster) {
		Set<String> local = new HashSet<>();
		for (int x = 0; x < dataSetValues.size(); x++) {
			totalNoOfCluster.add(String.valueOf(x));
			local.add(String.valueOf(x));
		}
		finalRes.put(local.size(), local);
	}

	//Fill up the nxn matrix for the distance
	private void fillUpTheData(Double[][] distanceBetPoints) {
		for (int i = 0; i < distanceBetPoints.length; i++)
			for (int j = 0; j < distanceBetPoints[0].length; j++)
				if (i == j)
					distanceBetPoints[i][j] = Double.MAX_VALUE;

		for (int i = 0; i < distanceBetPoints.length; i++) {
			for (int j = i + 1; j < distanceBetPoints[0].length; j++) {
				//find the distance between two points
				Double localDistVal = findDist(dataSetValues, i, j);
				distanceBetPoints[i][j] = distanceBetPoints[j][i] = Double.parseDouble(df2.format(localDistVal));
			}
		}
	}

	//calculate the distance between two points
	private Double findDist(List<String[]> dataSetValuesLocal, int i, int j) {

		String[] data1 = dataSetValuesLocal.get(i);
		String[] data2 = dataSetValuesLocal.get(j);

		Double local = 0.0;
		// System.out.println(Arrays.toString(data1));
		// System.out.println(Arrays.toString(data2));
		Double sum = 0.0;
		for (int x = 0; x < data1.length; x++) {
			if ((data1[x].matches(numRegex) && data2[x].matches(numRegex))
					|| (data1[x].matches(numRegexInt) && data2[x].matches(numRegexInt))) {
				Double temp = Double.parseDouble(data1[x]) - Double.parseDouble(data2[x]);
				sum += Math.pow(temp, 2);
			}
		}
		local = Math.sqrt(sum);
		return local;
	}

	
	//Read the data from file
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
