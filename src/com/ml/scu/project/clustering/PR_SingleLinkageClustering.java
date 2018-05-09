package com.ml.scu.project.clustering;

public class PR_SingleLinkageClustering {

	protected void updateDataPoints(int attribute1, int attribute2, Double[][] distanceBetPoints) {
		// Updating value in distanceBetPoints - in main data matrix
		for (int x = 0; x < distanceBetPoints.length; x++) {
			if (distanceBetPoints[attribute2][x] != Double.MAX_VALUE
					&& distanceBetPoints[attribute1][x] < distanceBetPoints[attribute2][x])
				distanceBetPoints[attribute2][x] = distanceBetPoints[x][attribute2] = distanceBetPoints[attribute1][x];
			distanceBetPoints[attribute1][x] = Double.MAX_VALUE;
			distanceBetPoints[x][attribute1] = Double.MAX_VALUE;
			
			//System.out.println("distanceBetPoints[attribute2][x]....ML_SingleLinkageClustering...."+distanceBetPoints[attribute2][x]);
		}
	}

}
