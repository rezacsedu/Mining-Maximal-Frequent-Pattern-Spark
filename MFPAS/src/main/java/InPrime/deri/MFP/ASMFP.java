package InPrime.deri.MFP;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

public class ASMFP {
	public static final Pattern SPACE = Pattern.compile(" ");
	public static int max = 1;
	public static Map<String, Integer> regex = new HashMap<>();
	public static boolean[] primes = new boolean[10000];
	public static Map<String, Integer> uniqueKey = new HashMap<>();
	public static Map<Integer, String> reverseUniqueKey = new HashMap<>();
	public static ArrayList<ASP_Tree> nodeList = new ArrayList<>();
	public static ArrayList<ASP_Tree> parentList = new ArrayList<>();
	public static ArrayList<ASP_Tree> childList = new ArrayList<>();
	//public static ArrayList<ASP_Tree> L = new ArrayList<>();
	static Set<Integer> MFV = new HashSet<Integer>();
	public static Set<ASP_Tree> L = new HashSet<>();
	static Set<Integer> hs = new HashSet<>();
	public static ArrayList<ASP_Tree> tampNode = new ArrayList<>();
	public static ArrayList<ASP_Tree> finalList = new ArrayList<>();
	public static Map<Integer, ASP_Tree> combNode = new HashMap<>();
	public static int minimum_support = 8;
	
	public static List<Integer> primeFactors(int number) {
		MemoryLogger.getInstance().reset();
		MemoryLogger.getInstance().checkMemory();
		int n = number;
		List<Integer> factors = new ArrayList<Integer>();
		for (int i = 2; i <= n; i++) {
			while (n % i == 0) {
				factors.add(i);
				n /= i;
			}
		}
		return factors;
	}
	
	/////LCD calculation

	public static int gcd(int a, int b) {
		if (a == 0 || b == 0)
			return a + b; // base case
		return gcd(b, a % b);
	}

	public static int gcd(int number, ArrayList<Integer> numbers) {
		int result = number;
		for (int i = 0; i < numbers.size(); i++) {
			result = gcd(result, numbers.get(i));
		}
		return result;
	}

	public static void combination(ArrayList<ASP_Tree> array, int i, int depth,	String string) {
		//System.out.println("GCD Values are: ");
		if (depth >= minimum_support) {
			//System.out.println("GCD = {");
			// System.out.println(string);
			String[] indexList = string.split(" ");
			ArrayList<Integer> numbers = new ArrayList<>();
			for (String s : indexList) {
				// System.out.print(array.get(Integer.parseInt(s)).getValue()+ " ");
				numbers.add(array.get(Integer.parseInt(s)).getValue());
			}
			// System.out.println();
			int gcdResult = gcd(numbers.get(0), numbers);
			//System.out.println("GCD = {");
			if (!combNode.containsKey(gcdResult)) {
				//System.out.println(gcdResult);
				ASP_Tree n = new ASP_Tree();
				n.setValue(gcdResult);
				n.setCount(1);
				combNode.put(gcdResult, n);
			}
			//System.out.println("}");
			return;
		}
		
		for (int j = i + 1; j < array.size(); j++) {
			combination(array, j, depth + 1, string + " " + j);
		}
		//System.out.println();
	}

	public static boolean isPrime(int n) {
		return primes[n];
	}

	public static int nextPrime(int n) {

		while (n < primes.length) {
			n++;
			if (isPrime(n))
				break;
		}
		if (!isPrime(n))
			return -1;
		return n;
	}

	public static void generatePrimes() {
		Arrays.fill(primes, true);
		primes[0] = primes[1] = false;
		for (int i = 2; i < primes.length; i++) {
			if (primes[i]) {
				for (int j = 2; i * j < primes.length; j++) {
					primes[i * j] = false;
				}
			}
		}
	}

	public static String TVGeneration(String line) {
		String[] words = line.split(" ");
		int n = 0;
		long result = 1;
		for (String word : words) {
			if (uniqueKey.containsKey(word))
				n = uniqueKey.get(word);
			else {
				n = nextPrime(max);
				uniqueKey.put(word, n);
				reverseUniqueKey.put(n, word);
				if (max < n)
					max = n;
			}
			result = result * n;
		}
		return result + "";
	}

	public static String filterRegex(String line) {
		String[] words = line.split(" ");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (regex.containsKey(word)) {
				result.append(word);
				result.append(" ");
			}
		}
		return result.toString();
	}

	private static void traverse(ASP_Tree node) {
		if (node.getGlobal() >= minimum_support) {
			//System.out.println(node.toString());
			tampNode.add(node);
			if (node.getParentList().size() != 0) {
				for (ASP_Tree parent : node.getParentList()) {
					traverse(parent);
				}
			}
		}
	}

	private static ASP_Tree makeTreeNode(ASP_Tree treeNode, ArrayList<ASP_Tree> nodeList) {
		int treeNodeValue = treeNode.getValue();
		// System.out.println("treeNodeValue: "+treeNodeValue);
		ASP_Tree minNode = null;
		int min = 1000000;
		// System.out.println(nodeList.size());
		for (ASP_Tree node : nodeList) {
			int value = node.getValue();
			// System.out.println("value: "+node.toString());
			if (value % treeNodeValue == 0) {
				treeNode.addParent(node);
				node.addChild(treeNode);
				treeNode.incrementGlobalValue(node.getCount());
				if (min > node.getValue()) {
					min = node.getValue();
					minNode = node;
				}
			}
		}
		treeNode.setMainParent(minNode);
		return treeNode;
	}

	@SuppressWarnings("serial")
	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("ASMFP").setMaster("local").set("spark.executor.memory","2g");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<String> distFile = sc.textFile("raw/sample_lda_data2.txt", 1);
		


		/**
		 *  Items counting
		 */
		JavaRDD<String> words = distFile.flatMap(new FlatMapFunction<String, String>() {
					@Override
					public Iterable<String> call(String s) {
						return Arrays.asList(SPACE.split(s));
					}
				});
		
		JavaPairRDD<String, Integer> ones = words.mapToPair(new PairFunction<String, String, Integer>() {
					@Override
					public Tuple2<String, Integer> call(String s) {
						return new Tuple2<String, Integer>(s, 1);
					}
				});
		
		JavaPairRDD<String, Integer> counts = ones.reduceByKey(
				new Function2<Integer, Integer, Integer>() {
					@Override
					public Integer call(Integer i1, Integer i2) {
						return i1 + i2;
					}
				}).filter(new Function<Tuple2<String, Integer>, Boolean>() {

			@Override
			public Boolean call(Tuple2<String, Integer> tuple) throws Exception {
				// TODO Auto-generated method stub
				return tuple._2 > 3;
			}
		});
		
		JavaPairRDD<String, Integer> temp = counts.sortByKey(true);		
		List<Tuple2<String, Integer>> output = temp.collect();
		
		System.out.println("Frequency count of individual items: ");
		for (Tuple2<?, ?> tuple : output) {
			System.out.println(tuple._1() + ": " + tuple._2());
			// keyCount.put(tuple._1(), tuple._2());
			regex.put((String) tuple._1(), (Integer) tuple._2());
			// reverseRegex.put((Integer) tuple._2(), (String) tuple._1());
		}
		
		//System.out.println(distFile.collect());
		
		JavaRDD<String> filteredWords = distFile.map(
				new Function<String, String>() {
					@Override
					public String call(String arg0) throws Exception {
						// TODO Auto-generated method stub
						return filterRegex(arg0);
					}
				}).filter(new Function<String, Boolean>() {

			@Override
			public Boolean call(String s) throws Exception {
				// TODO Auto-generated method stub
				return s.length() != 0;
			}
		});
    	System.out.println(filteredWords.collect());
		File f = new File("raw/filtered_dataset.txt");
		FileUtil.fullyDelete(f);
		filteredWords.saveAsTextFile(f.getAbsolutePath());

		
		/**
		 *  prime generation and genarate tv values
		 */
		generatePrimes();
		//System.out.println(generatePrimes(););
		JavaRDD<String> distFile2 = sc.textFile("raw/filtered_dataset.txt/part-00000", 1);
		JavaRDD<String> tvDist = distFile2.map(new Function<String, String>() {

			@Override
			public String call(String s) throws Exception {
				// TODO Auto-generated method stub
				return TVGeneration(s);
			}
		});
		
		/// TV Values and database
		File f3 = new File("raw/sample_tv_data.txt");
		FileUtil.fullyDelete(f3);
		tvDist.saveAsTextFile(f3.getAbsolutePath()); 
		System.out.println(tvDist.collect());

		/**
		 *  calculation of tv file
		 */
		JavaRDD<String> distFile3 = sc.textFile("raw/sample_tv_data.txt/part-00000", 1);
		JavaPairRDD<Integer, Integer> pairedRDD = distFile3.mapToPair(new PairFunction<String, Integer, Integer>() {
					@Override
					public Tuple2<Integer, Integer> call(String s)
							throws Exception {
						// TODO Auto-generated method stub
						return new Tuple2<Integer, Integer>(Integer.parseInt(s), 1);
					}
				});
		
		JavaPairRDD<Integer, Integer> countedRDD = pairedRDD.reduceByKey(
				new Function2<Integer, Integer, Integer>() {
					@Override
					public Integer call(Integer arg0, Integer arg1)	throws Exception {
						// TODO Auto-generated method stub
						return arg0 + arg1;
					}
				}).sortByKey(false, 2);
		
		List<Tuple2<Integer, Integer>> output2 = countedRDD.collect();
		ArrayList<ASP_Tree> nodeList = new ArrayList<>();
		
		System.out.println("TV database contents: ");
		System.out.println(countedRDD.collect());

		for (Tuple2<?, ?> tuple : output2) {
			ASP_Tree node = new ASP_Tree((Integer) tuple._1(), (Integer) tuple._2(), 0);
			ASP_Tree treeNode = makeTreeNode(node, nodeList);
			treeNode.setGlobal(treeNode.getGlobal() + treeNode.getCount());
			nodeList.add(treeNode);
			}
		sc.stop();

		for (ASP_Tree node : nodeList) {
			//System.out.println(node.toString());
			//System.out.println("Parents:");
			for (ASP_Tree parent : node.getParentList()) {
				//System.out.print(parent.getValue() + " ");
			}
			//System.out.println("\nChilds:");
			for (ASP_Tree child : node.getChildList()) {
				//System.out.print(child.getValue() + " ");
			}
			//System.out.println();
		}
		
		/* Printing GCD list in L before applying the pruning operation */
		for (int index = nodeList.size() - 1; index >= 0; index--) {
			//System.out.println("nodeList.get(index): "	+ nodeList.get(index).toString());
			ASP_Tree tn = nodeList.get(index);
			if (tn.getParentList().size() != 0 && !tn.isTaken())
				traverse(tn);
			//System.out.println("add to L");
			int len = tampNode.size();
			if (len != 0) {
				ASP_Tree n = tampNode.get(len - 1);
				if (n.getValue() != 1)
				//System.out.println("n:: " + n);
				L.add(n);
				for (int i = 0; i < len; i++) {
					tampNode.get(i).setParentList(null);

				}
				n.setTaken(true);
				tampNode.clear();
			}
		}
		
		 //Printing GCD list in L before applying the pruning operation 
		System.out.print("L0 = {");
		for (ASP_Tree node : L) {
			System.out.print(node.getValue() + " ");
		}
		System.out.println("}");
		//System.out.println();

		/**
		 * filter nodeList to finalList after calculate L
		 */
		for (ASP_Tree node : nodeList) {
			if (!node.isTaken() && node.getChildList().size() != 0) {
			//if (!node.isTaken() && node.getChildList().size() >= minimum_support) {
				for (int i = 0; i < node.getGlobal(); i++) {
					finalList.add(node);
				}
			}
		}
		
		/*
		 * find nCr
		 */
		//width = 4;
		for (int i = 0; i < finalList.size(); i++) {
			combination(finalList, i, 1, "" + i);
		}
		for (ASP_Tree n : L) {
			if (combNode.containsKey(n.getValue())) {
				combNode.remove(n.getValue());
			}
		}
		
		/*
		 * update L using combination
		 */
		Iterator it = combNode.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			ASP_Tree node = (ASP_Tree) pair.getValue();
			if (node.getValue() != 1)
				L.add(node);
		}
		
		
		/* Removing duplicate items from the ArrayList */
		for(ASP_Tree node : L)
		{
			hs.add(node.getValue());			
		}
		
		/* Converting HashSet into Array */
		List<Integer> list = new ArrayList<Integer>(hs);
		Object[] num = list.toArray();
		//int[] array = null;
		
		
		/////////////////////////////////////////////////////Frequent Patterns ////////////////////////////////////
		
		/* Printing the Frequent Values  */	
		//ArrayList<Integer> FV = new ArrayList<Integer>();	
		System.out.print("Frequent Values are: {");		
		for(int k = 0; k < num.length; k++)
		{
			System.out.print((int)num[k]+" ");				
		}
		System.out.println("}");
		
		/*
		 * calculate prime factors and assign proper values
		 */
		System.out.println("Prime factorizations:");
		for (ASP_Tree node : L) {
			for(Iterator<Integer> it1 = list.iterator();  it1.hasNext();)
			{
				if(it1.next() == node.getValue())
				{
					System.out.println(node.getValue() + "==>"+ primeFactors(node.getValue()));
					//for (Integer v : primeFactors(node.getValue()))
					//{
					//if(it1.next() == node.getValue())
					//	System.out.print(reverseUniqueKey.get(v) + " ");
					//}
				}
			}
			//System.out.println();

		}
		
		System.out.println("Frequent Patterns: ");
		for (ASP_Tree node : L) {
			for(Iterator<Integer> it1 = list.iterator();  it1.hasNext();)
			{
				if(it1.next() == node.getValue())
				{
					//System.out.println(node.getValue() + "==>"+ primeFactors(node.getValue()));
					for (Integer v : primeFactors(node.getValue()))
					{
					//if(it1.next() == node.getValue())
						System.out.print(reverseUniqueKey.get(v) + " ");
						//System.out.println();
					}
					System.out.println();
				}
			}
			//System.out.println();

		}
		
		
		
/////////////////////////////////////////////////////Maximal Frequent Patterns ////////////////////////////////////
		
		boolean[] removed = new boolean[num.length];
	    for (int i = 0; i < num.length; i++) {
	        for (int j = i + 1; j < num.length; j++) {
	            if ((int)num[i] > (int)num[j] && (int)num[i] % (int)num[j] == 0) {
	                removed[j] = true;
	            } else if ((int)num[i] < (int)num[j] && (int)num[j] % (int)num[i] == 0) {
	                removed[i] = true;
	            }
	        }
	    }
	    
	    //ArrayList<Integer> MFV = new ArrayList<Integer>();	    
	    /* Printing the Maximal Frequent Values */	    
	    System.out.print("Maximal Frequent Value are: {");
	    for (int i = 0; i < num.length; ++i) {
	        if (removed[i] != true) {
	        	MFV.add((int) num[i]);
	            System.out.print((int)num[i] + " ");
	        }
	    }
	    System.out.println("}");

		
		
/*		for (ASP_Tree node : L) {
			System.out.print(node.getValue() + " ");
		}
		System.out.println("}");*/

		/*
		 * calculate prime factors and assign proper values
		 */
		System.out.println("Prime factorizations:");
		for (ASP_Tree node : L) {
			for(Iterator<Integer> it2 = MFV.iterator();  it2.hasNext();)
			{
				if(it2.next() == node.getValue())
				{
					System.out.println(node.getValue() + "==>"+ primeFactors(node.getValue()));
					//for (Integer v : primeFactors(node.getValue()))
					//{
					//if(it1.next() == node.getValue())
					//	System.out.print(reverseUniqueKey.get(v) + " ");
					//}
				}
			}
			//System.out.println();

		}
		
		System.out.println("Maximal Frequent Patterns: ");
		for (ASP_Tree node : L) {
			for(Iterator<Integer> it3 = MFV.iterator();  it3.hasNext();)
			{
				if(it3.next() == node.getValue())
				{
					//System.out.println(node.getValue() + "==>"+ primeFactors(node.getValue()));
					for (Integer v : primeFactors(node.getValue()))
					{
					//if(it1.next() == node.getValue())
						System.out.print(reverseUniqueKey.get(v) + " ");
						//System.out.println();
					}
					System.out.println();
				}
			}
			//System.out.println();

		}
		
	    /* Printing memory usage : */;
		MemoryLogger.getInstance().checkMemory();
		System.out.println("Memory usage: " + (int) MemoryLogger.getInstance().getMaxMemory() + " MB");
		
		/* Freeing allocated memory */
		MFV.clear();
		hs.clear();
		list.clear();		
	}
	//MemoryLogger.getInstance().checkMemory();
}
