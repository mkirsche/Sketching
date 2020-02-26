/*
 * Here each sketch will be a uniform subset of the number in the range [0, 1,000,000).
 * For the first set, each integer in the range will be added with 10% probability.
 * For the second set, each integer in the range will be added with 1% probability.
 * 
 * Then, a number of simulations is run, where the Jaccard similarity is approximated using
 * both the bottom-K and K-partition approaches, and the average is computed over 10000 trials.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class CollisionsSim {
public static void main(String[] args)
{
	double p1 = 0.1;
	double p2 = 0.01;
	int n = 1000000;
	Random r = new Random();
	int k = 1000;
	
	double avgBottomK = 0, avgKPartition = 0;
	double avgBottomSquare = 0, avgPartitionSquare = 0;
	double average = 0, averageSquare = 0;
	
	int numTrials = 10000;
	
	for(int t = 0; t<numTrials; t++)
	{
		ArrayList<Integer> first = new ArrayList<Integer>();
		ArrayList<Integer> second = new ArrayList<Integer>();
		
		for(int i = 0; i<n; i++)
		{
			double r1 = r.nextDouble();
			if(r1 < p1)
			{
				first.add(i);
			}
			double r2 = r.nextDouble();
			if(r2 < p2)
			{
				second.add(i);
			}
		}
		
		ArrayList<Integer> bottomK1 = bottomK(first, k), bottomK2 = bottomK(second, k);
		ArrayList<Integer> kPartition1 = kPartition(first, n, k), kPartition2 = kPartition(second, n, k);
		//System.out.println("Bottom k1: " + bottomK1+"\nBottom k2: "+bottomK2+"\nK Partition1: "+kPartition1+"\nK Partition2: "+kPartition2+"\n");
		
		if(kPartition1.size() < k || kPartition2.size() < k)
		{
			t--;
			continue; // Skip cases where there are empty partitions
		}
		
		double bottomKJaccard = approximateJaccard(bottomK1, bottomK2, true);
		double kPartitionJaccard = approximateJaccard(kPartition1, kPartition2, false);
		double jaccard = jaccard(first, second);
		average += jaccard;
		averageSquare += jaccard * jaccard;
		avgBottomK += bottomKJaccard;
		avgKPartition += kPartitionJaccard;
		avgBottomSquare += bottomKJaccard * bottomKJaccard;
		avgPartitionSquare += kPartitionJaccard * kPartitionJaccard;
	}
	System.out.println("Average Jaccard: " + average / numTrials+"\nBottom k: "+avgBottomK/numTrials+"\nK Partition: "+avgKPartition/numTrials);
	System.out.println("Jaccard variance: " + (average * average - averageSquare)/(numTrials*numTrials));
	System.out.println("Bottom k variance: " + (avgBottomK*avgBottomK- avgBottomSquare)/(numTrials*numTrials));
	System.out.println("K partition variance: " + (avgKPartition*avgKPartition - avgPartitionSquare)/(numTrials*numTrials));
}

/*
 * Gets the k lowest values
 */
static ArrayList<Integer> bottomK(ArrayList<Integer> a, int k)
{
	ArrayList<Integer> res = new ArrayList<Integer>();
	for(int i = 0; i<k; i++) res.add(a.get(i));
	return res;
}

/*
 * Gets the lowest value in each of k uniform segments of [0, n-1)
 */
static ArrayList<Integer> kPartition(ArrayList<Integer> a, int n, int k)
{
	int partitionSize = n/k;
	ArrayList<Integer> res = new ArrayList<Integer>();
	for(int i = 0; i<a.size(); i++)
	{
		if(i == 0 || (a.get(i) / partitionSize > a.get(i-1) / partitionSize))
		{
			res.add(a.get(i));
		}
	}
	return res;
}

/*
 * Gets the exact Jaccard distance
 */
static double jaccard(ArrayList<Integer> first, ArrayList<Integer> second)
{
	int common = 0;
	int i = 0, j = 0;
	while(i < first.size() && j < second.size())
	{
		if(first.get(i).intValue() == second.get(j).intValue())
		{
			common++;
			i++;
			j++;
		}
		else if(first.get(i).intValue() < second.get(j).intValue())
		{
			i++;
		}
		else
		{
			j++;
		}
	}
	double res = common * 1.0 / (first.size() + second.size() - common);
	//System.out.println(common+" "+first.size()+" "+second.size() + " " + res);
	return res;
}

/*
 * Gets the union of two sketches
 */
static ArrayList<Integer> unionBottom(ArrayList<Integer> first, ArrayList<Integer> second)
{
	ArrayList<Integer> res = new ArrayList<Integer>();
	int i = 0, j = 0;
	while(res.size() < first.size())
	{
		if(i == first.size())
		{
			res.add(second.get(j));
			j++;
		}
		else if(j == second.size())
		{
			res.add(first.get(i));
			i++;
		}
		else if(first.get(i).intValue() == second.get(j).intValue())
		{
			res.add(first.get(i));
			i++;
			j++;
		}
		else if(first.get(i).intValue() < second.get(j).intValue())
		{
			res.add(first.get(i));
			i++;
		}
		else
		{
			res.add(second.get(j));
			j++;
		}
	}
	return res;
}

static ArrayList<Integer> unionPartition(ArrayList<Integer> first, ArrayList<Integer> second)
{
	ArrayList<Integer> res = new ArrayList<Integer>();
	for(int i = 0; i<first.size(); i++)
	{
		res.add(Math.min(first.get(i), second.get(i)));
	}
	return res;
}

/*
 * Gets the approximate union of two sets given their sketches (with the direct method)
 */
static double approximateJaccard(ArrayList<Integer> firstSketch, ArrayList<Integer> secondSketch, boolean bottomK)
{
	HashSet<Integer> inFirst = new HashSet<Integer>(), inSecond = new HashSet<Integer>();
	inFirst.addAll(firstSketch);
	inSecond.addAll(secondSketch);
	ArrayList<Integer> union = bottomK ? unionBottom(firstSketch, secondSketch) : unionPartition(firstSketch, secondSketch);
	int both = 0;
	for(int i = 0; i<union.size(); i++)
	{
		int x = union.get(i);
		if(inFirst.contains(x) && inSecond.contains(x))
		{
			both++;
		}
	}
	return 1.0 * both / union.size();
}
}
