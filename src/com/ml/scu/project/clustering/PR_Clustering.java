package com.ml.scu.project.clustering;

public class PR_Clustering 
{
	public static void main(String[] args) 
	{
		PR_LinkageClustering s1 = new PR_LinkageClustering();
		s1.findDistanceBetPoints(Constant.singleLinkage);
		System.out.println();
		s1.findDistanceBetPoints(Constant.completeLinkage);
		System.out.println();
		s1.findDistanceBetPoints(Constant.averageLinkage);
		System.out.println();
		s1.findTheKMeans();
		System.out.println();
		s1.findTheKMeansPlusPlus();
	}
}
