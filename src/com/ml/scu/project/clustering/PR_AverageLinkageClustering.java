package com.ml.scu.project.clustering;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PR_AverageLinkageClustering {

	Double minVal;
	Double minAttribute;
	private static DecimalFormat df2 = new DecimalFormat(".###");

	protected void updateDataPoints(int attribute1, int attribute2, Double[][] distanceBetPoints,
			Map<Integer, String> hmRes) {
		// Updating value in distanceBetPoints - in main data matrix
		Double[][] distanceBetPointsAvg = PR_LinkageClustering.distanceBetPointsCopy;

		for (int x = 0; x < distanceBetPoints.length; x++) {
			double localVal = 0.0;
			
			//read and update the distance based on the comparison and existing cluster
			if (hmRes.get(attribute1) == null && hmRes.get(attribute2) == null) {
				localVal = getDistance(distanceBetPointsAvg[attribute1][x], distanceBetPointsAvg[x][attribute2]) / 2;
			} else {

				if (hmRes.get(attribute1) != null && hmRes.get(attribute2) == null) {
					String existingCluster = hmRes.get(attribute1);

					String[] tmp = existingCluster.split("-");
					for (String s1 : tmp) {
						localVal += getDistance(distanceBetPointsAvg[Integer.parseInt(s1)][x],
								distanceBetPointsAvg[x][attribute2]);
					}
					localVal /= ((tmp.length) + 1);

				} else if (hmRes.get(attribute2) != null && hmRes.get(attribute1) == null) {

					String existingCluster = hmRes.get(attribute2);

					String[] tmp = existingCluster.split("-");
					for (String s1 : tmp) {
						localVal += getDistance(distanceBetPointsAvg[Integer.parseInt(s1)][x],
								distanceBetPointsAvg[x][attribute1]);
					}
					localVal /= ((tmp.length) + 1);

				} else {

					String existingCluster1 = hmRes.get(attribute1);
					String existingCluster2 = hmRes.get(attribute2);

					String[] tmp1 = existingCluster1.split("-");
					String[] tmp2 = existingCluster2.split("-");

					for (String s1 : tmp1) {
						for (String s2 : tmp2) {
							localVal += getDistance(distanceBetPointsAvg[Integer.parseInt(s1)][x],
									distanceBetPointsAvg[x][Integer.parseInt(s2)]);
						}
					}

					localVal /= ((tmp1.length) + (tmp2.length));
				}
			}

			//update the value and make one of them infinity
			if (distanceBetPoints[attribute2][x] != Double.MAX_VALUE)
				distanceBetPoints[attribute2][x] = distanceBetPoints[x][attribute2] = localVal;

			distanceBetPoints[attribute1][x] = Double.MAX_VALUE;
			distanceBetPoints[x][attribute1] = Double.MAX_VALUE;
		}
	}

	private Double getDistance(Double p1, Double p2) {
		double tmp = 0.0;

		if (p1 != Double.MAX_VALUE && p2 != Double.MAX_VALUE)
			tmp = (p1 + p2);
		else if (p1 != Double.MAX_VALUE && p2 == Double.MAX_VALUE)
			tmp = p1;
		else if (p1 == Double.MAX_VALUE && p2 != Double.MAX_VALUE)
			tmp = (p2);

		return tmp;
	}

}
