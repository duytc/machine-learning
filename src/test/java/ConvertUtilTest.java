import com.pubvantage.dao.CoreAutoOptimizationConfigDao;
import com.pubvantage.dao.CoreAutoOptimizationConfigDaoInterface;
import com.pubvantage.utils.AppResource;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.HibernateUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.hibernate.Session;
import org.junit.Test;

import javax.persistence.Convert;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertUtilTest {

    private JavaSparkContext sparkContext;
    private static AppResource appResource;
    private static Properties properties;


    public ConvertUtilTest() {
        SparkConf sparkConf = new SparkConf()
                .setAppName("test")
                .setMaster("local[*]");
        sparkContext = new JavaSparkContext(sparkConf);

        appResource = new AppResource();
        properties = appResource.getPropValues();
    }

    @Test
    public void testTruncate() {
        Double input = 0.10365853658536585;
//        Double input = 1.6000000;
        Double output = BigDecimal.valueOf(input)
                .setScale(6, RoundingMode.HALF_UP).doubleValue();
        System.out.println(output);

    }

    @Test
    public void testConvert() {
//        Double input = 1.23456789;
        Double input = 0d;
        BigDecimal output = ConvertUtil.convertObjectToDecimal(input);
        System.out.println(output);

    }


    public void testClosure() {
        int counter = 0;
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        JavaRDD<Integer> rdd = sparkContext.parallelize(data);

        // Wrong: Don't do this!!
//        rdd.foreach(x -> counter += x);

        System.out.println("Counter value: " + counter);
    }

    @Test
    public void testRegrex() {
        String commandPattern = properties.getProperty("command.pattern");
        Pattern p = Pattern.compile(commandPattern);
        Matcher m = p.matcher("--autoOptimizationId=102,1,2,3\n" +
                "--identifier='dfdf' , f ,fdsf com,fdf@dfg.com");
        boolean b = m.matches();
        System.out.println(b);


        String autoOptimizationPattern = properties.getProperty("command.pattern.auto.optimization.id");
        String input = "--autoOptimizationId=102,1,2,3\n" +
                "--identifier='dfdf' , f ,fdsf com,fdf@dfg.com";

        Pattern r = Pattern.compile(autoOptimizationPattern);
        Matcher matcher = r.matcher(input);
        if (matcher.find()) {
            System.out.println("Found value: " + m.group(0));
            System.out.println("Found value: " + m.group(1));
            System.out.println("Found value: " + m.group(2));
        } else {
            System.out.println("NO MATCH");
        }

    }

    @Test
    public void checkToken() {
        CoreAutoOptimizationConfigDaoInterface dao = new CoreAutoOptimizationConfigDao();

//        Session session = HibernateUtil.getCurrentSession();
//        session.beginTransaction();
//        boolean x = dao.checkToken(session, 1l, "_eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNTE2Njg5ODMyLCJzdWIiOiJzdWJqZWN0IiwiaXNzIjoiaXNzdWVyIn0.aTkzk5DqatNh-fdE3b-dlMiXgHve1RoS7rJu4nwwkiw");
//
//        session.getTransaction().commit();
    }

    @Test
    public void checkSortMap() {
        Map<String, Double> unsortedMap = new LinkedHashMap<>();
        unsortedMap.put("key1", 1.2);
        unsortedMap.put("key2", 2.2);
        unsortedMap.put("key3", 5.2);
        unsortedMap.put("key4", 1.2);
        unsortedMap.put("key5", 3.2);

        Map<String, Double> ascSortedMap = ConvertUtil.ascendingSortMapByValue(unsortedMap);
        Map<String, Double> desSortedMap = ConvertUtil.descendingSortMapByValue(unsortedMap);

        return;
    }

    @Test
    public void generateSubSet() {
        List<String> set = new ArrayList<>();
        set.add("a");
        set.add("b");
        set.add("c");
        Map<Integer, List<String>> map = ConvertUtil.generateSubsets(set);
        return;
    }
}
