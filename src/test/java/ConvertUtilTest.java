import com.pubvantage.utils.AppResource;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.*;

import static org.junit.Assert.assertEquals;

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

//    @Test
//    public void testTruncate() {
//        Double input = 0.10365853658536585;
////        Double input = 1.6000000;
//        Double output = BigDecimal.valueOf(input)
//                .setScale(6, RoundingMode.HALF_UP).doubleValue();
//        System.out.println(output);
//
//    }
//
//    @Test
//    public void testConvert() {
////        Double input = 1.23456789;
//        Double input = 0d;
//        BigDecimal output = ConvertUtil.convertObjectToDecimal(input);
//        System.out.println(output);
//
//    }
//
//    @Test
//    public void testRegex() {
//        String commandPattern = properties.getProperty("command.pattern");
//        Pattern p = Pattern.compile(commandPattern);
//        Matcher m = p.matcher("--autoOptimizationId=102,1,2,3\n" +
//                "--identifier='dfdf' , f ,fdsf com,fdf@dfg.com");
//        boolean b = m.matches();
//        System.out.println(b);
//
//
//        String autoOptimizationPattern = properties.getProperty("command.pattern.auto.optimization.id");
//        String input = "--autoOptimizationId=102,1,2,3\n" +
//                "--identifier='dfdf' , f ,fdsf com,fdf@dfg.com";
//
//        Pattern r = Pattern.compile(autoOptimizationPattern);
//        Matcher matcher = r.matcher(input);
//        if (matcher.find()) {
//            System.out.println("Found value: " + m.group(0));
//            System.out.println("Found value: " + m.group(1));
//            System.out.println("Found value: " + m.group(2));
//        } else {
//            System.out.println("NO MATCH");
//        }
//
//    }
//
//    @Test
//    public void checkToken() {
//        CoreAutoOptimizationConfigDaoInterface dao = new CoreAutoOptimizationConfigDao();
//
////        Session session = HibernateUtil.getCurrentSession();
////        session.beginTransaction();
////        boolean x = dao.checkToken(session, 1l, "_eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNTE2Njg5ODMyLCJzdWIiOiJzdWJqZWN0IiwiaXNzIjoiaXNzdWVyIn0.aTkzk5DqatNh-fdE3b-dlMiXgHve1RoS7rJu4nwwkiw");
////
////        session.getTransaction().commit();
//    }
//
//    @Test
//    public void checkSortMap() {
//        Map<String, Double> unsortedMap = new LinkedHashMap<>();
//        unsortedMap.put("key1", 1.2);
//        unsortedMap.put("key2", 2.2);
//        unsortedMap.put("key3", 5.2);
//        unsortedMap.put("key4", 1.2);
//        unsortedMap.put("key5", 3.2);
//
//        Map<String, Double> ascSortedMap = ConvertUtil.ascendingSortMapByValue(unsortedMap);
//        Map<String, Double> desSortedMap = ConvertUtil.descendingSortMapByValue(unsortedMap);
//
//        return;
//    }
////
////    @Test
////    public void generateSubSet() {
////        List<String> set = new ArrayList<>();
////        set.add("a");
////        set.add("b");
////        set.add("c");
////        List<List<String>> map = ConvertUtil.generateSubsets(set);
////        return;
////    }
//
//    @Test
//    public void jsonArrayStringToArray() {
////        String input = "[\"text2_2\",\"text_3\"]";
//        String input = "[\"text2_2\",\"http://text_3\"]";
////        ArrayList<String> arrayList = JsonUtil.jsonArrayStringToJavaList(input);
//
//        Any obj = JsonIterator.deserialize(input);
//        List<String> array = JsonIterator.deserialize(input, ArrayList.class);
//
//        String listObjectInput = "[{\"field\":\"largetext2_2\",\"goal\":\"Min\",\"weight\":0.2}]";
//        List<HashMap<String, String>> anyList = JsonIterator.deserialize(listObjectInput, ArrayList.class);
//        HashMap<String, String> any = anyList.get(0);
//        String string = JsonUtil.toJson(any);
//        OptimizeField optimizeField3 = JsonUtil.jsonToObject(string, OptimizeField.class);
//
//        OptimizeField optimizeField = new OptimizeField("field", 0.3, "Min");
//        Gson gsonBuilder = new GsonBuilder().create();
//        String jsonFromPojo = gsonBuilder.toJson(optimizeField);
//
//        String opt = "{\"field\":\"largetext2_2\",\"goal\":\"Min\",\"weight\":0.2}";
//        OptimizeField optimizeField1 = JsonUtil.jsonToObject(opt, OptimizeField.class);
//
//        return;
//    }
//
////    @Test
////    public void getUniquire() {
////        List<String> oneSegmentGroup = new ArrayList<>();
////        oneSegmentGroup.add("datatime_dimension_4_4_4");
////        DataTrainingService dataTrainingService = new DataTrainingService(19L, "allenwestrepublic.com", oneSegmentGroup);
////        List<Map<String, Object>> uniqueValuesOfOneSegmentFieldGroup = dataTrainingService.getAllUniqueValuesForOneSegmentFieldGroup();
////        return;
////    }
//
//    @Test
//    public void createObjectiveAndFields() {
//        String optimizeField = "objective";
//        List<String> metrics = new ArrayList<>();
//        metrics.add("metric 1");
//        metrics.add("metric 2");
//        metrics.add("metric 3");
//        metrics.add("objective");
////        metrics = null;
//        if (metrics != null) {
//            int indexOfOptimizeField = metrics.indexOf(optimizeField);
//            if (indexOfOptimizeField > 0) {
//                metrics.remove(indexOfOptimizeField);
//            }
//        }
//        List<String> objectiveAndFields = new ArrayList<>();
//        objectiveAndFields.add(optimizeField);
//        if (metrics != null) {
//            objectiveAndFields.addAll(metrics);
//        }
//        return;
//    }
//
//
//    @Test
//    public void joinListString() {
//        List<String> metrics = new ArrayList<>();
//        metrics.add("metric 1");
//        metrics.add("metric 2");
//        metrics.add("metric 3");
//        metrics.add("objective");
//
//        String s = ConvertUtil.joinListString(metrics, ", ");
//        return;
//    }
//
//    @Test
//    public void mapToJson() {
//        Map<String, Object> map = new HashMap<>();
//        map.put("key http:// & 1", "value http:// & 1");
//        map.put("key 2", "value 2");
//        map.put("key 3", "value 3");
//        map.put("key 4", "value 4");
//
//        String s = JsonUtil.mapToJson(map);
//        return;
//    }
//
//    @Test
//    public void compare2Map() {
//        Map<String, Object> map = new HashMap<>();
//        map.put("key 3", "value 3");
//        map.put("key 2", "value 2");
//        map.put("key 4", "value 4");
//
//        Map<String, Object> map2 = new HashMap<>();
//        map2.put("key 2", "value 2");
//        map2.put("key 3", "value 3");
//        map2.put("key 4", "value 4");
//
//        boolean s = map.equals(map2);
//        return;
//    }
//
//    @Test
//    public void sort() {
//        List<String> list = new ArrayList<>();
//        list.add("2017-01-01");
//        list.add("2017-01-02");
//        list.add("2017-01-03");
//
//        Collections.sort(list);
//
//        return;
//    }
//
//    @Test
//    public void givenConcurrentMap_whenSumParallel_thenCorrect()
//            throws Exception {
//        Map<String, Integer> map = new ConcurrentHashMap<>();
//        List<Integer> sumList = parallelSum100(map, 1);
//
//        assertEquals(1, sumList
//                .stream()
//                .distinct()
//                .count());
//        long wrongResultCount = sumList
//                .stream()
//                .filter(num -> num != 100)
//                .count();
//
//        assertEquals(0, wrongResultCount);
//    }
//
//    private List<Integer> parallelSum100(Map<String, Integer> map,
//                                         int executionTimes) throws InterruptedException {
//        List<Integer> sumList = new ArrayList<>(1000);
//        for (int i = 0; i < executionTimes; i++) {
//            map.put("test", 0);
//            ExecutorService executorService =
//                    Executors.newFixedThreadPool(4);
//            for (int j = 0; j < 10; j++) {
//                executorService.execute(() -> {
//                    for (int k = 0; k < 10; k++)
//                        map.computeIfPresent(
//                                "test",
//                                (key, value) -> value + 1
//                        );
//                });
//            }
//            executorService.shutdown();
//            executorService.awaitTermination(5, TimeUnit.SECONDS);
//            sumList.add(map.get("test"));
//        }
//        return sumList;
//    }
//
//    @Test
//    public void testThread() {
//        List<String> segments = new ArrayList<>();
//        segments.add("segment 1");
//        segments.add("segment 2");
//        segments.add("segment 3");
//        segments.add("segment 4");
//        segments.add("segment 5");
//        Map<String, Map<String, Double>> map = new ConcurrentHashMap<>();
//        ExecutorService executorService = Executors.newFixedThreadPool(4);
//
//        for (String segment : segments) {
//            executorService.execute(() -> {
//                List<String> identifiers = new ArrayList<>();
//                Map<String, Double> idenMap = new ConcurrentHashMap<>();
//                identifiers.add("identifier 1");
//                identifiers.add("identifier 2");
//                for (String identifier : identifiers) {
//                    idenMap.put(identifier, 1.0d);
//                }
//                map.put(segment, idenMap);
//            });
//        }
//
//        System.out.print(segments);
//        return;
//    }
//
//    @Test
//    public void removeAvg() {
//        String s = "avg(xxx yy)";
//        String s1 = ConvertUtil.removeAvg(s);
//        return;
//    }
//
//    @Test
//    public void testJsonArrayToString() {
//        List<String> list = new ArrayList<>();
//        list.add("A");
//        list.add("B");
//        list.add("C");
//
//        String s = JsonStream.serialize(list);
//
//        return;
//    }
//
//    @Test
//    public void extractSegment() {
//        String s = "[\"tags_16\"]";
//        List<String> list = JsonUtil.jsonToList(s);
//
//        list.remove("tag_16");
//
//        return;
//    }

}
