import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        int choose = 1;

        switch (choose) {
            case 1:
                runMaster();

                break;
            case 2:
                runCluster();

                break;
        }
    }

    private static void runMaster() {
        SparkConf sparkConf = new SparkConf()
                .setAppName("Example Spark App")
                .setMaster("local[*]");  // Delete this line when submitting to a cluster
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        JavaRDD<String> stringJavaRDD = sparkContext.textFile("/absolute/path/to/unified-report-scorer/dev/test.csv");
        System.out.println("Number of lines in file = " + stringJavaRDD.count());
    }

    private static void runCluster() {
        SparkConf sparkConf = new SparkConf()
                .setAppName("Example Spark App");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        JavaRDD<String> stringJavaRDD = sparkContext.textFile("/absolute/path/to/unified-report-scorer/dev/test.csv");
        System.out.println("Number of lines in file = " + stringJavaRDD.count());
    }
}