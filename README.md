# Mining maximal frequent patterns in transactional databases and dynamic data streams: A spark-based approach
Implementation of Static mining part of ***Mining maximal frequent patterns in transactional databases and dynamic data streams: A spark-based approach" Information Sciences, Volume 432, March 2018, Pages 278-300***

Note that the dynamic streaming implementation is done but not openly available now as we're working on extension at the moment. 

# How to use this code repo
The implementation was done in Java long back probably in 2015. At that time Spark version was 1.5.0. But feel free to upgrade to Spark 2.3.0, which needs some additional efforts. However, I would highly rcommend doing it in Scala because of it's easier syntax and less verbose nature: 

1. Pull the repo
2. Import the code on Eclipse or IntelliJ IDEA as a Maven project
3. Then the IDE will pull all the dependencies
4. Then run the ***ASMFP.java*** class containing main method main method (but make sure that you specify the path of input transaction file)

The code has some limiitations too. It cannot handle a transactional database having too many items ina transaction. But feel free to tune and improve. Nevertheless, if you want to cite our work, please do so as follows: 

```
@article{KARIM2018278,
  title = "Mining maximal frequent patterns in transactional databases and dynamic data streams: A spark-based approach",
  journal = "Information Sciences",
  volume = "432",
  pages = "278 - 300",
  year = "2018",
  issn = "0020-0255",
  doi = "https://doi.org/10.1016/j.ins.2017.11.064",
  url = "http://www.sciencedirect.com/science/article/pii/S002002551731126X",
  author = "Md. Rezaul Karim and Michael Cochez and Oya Deniz Beyan and Chowdhury Farhan Ahmed and Stefan Decker",
  keywords = "Big data, Transactional databases, Dynamic data streams, Null transactions, Prime number theory, Data mining, Apache Spark,   Maximal frequent patterns"
}
```
