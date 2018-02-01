import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.entity.CoreLearningModel;
import com.pubvantage.service.*;
import com.pubvantage.utils.AppResource;
import com.pubvantage.utils.HibernateUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.Test;

import java.util.Properties;

public class DBTest {
    private JavaSparkContext sparkContext;
    private static AppResource appResource;
    private static Properties properties;

    public DBTest() {
        SparkConf sparkConf = new SparkConf()
                .setAppName("test")
                .setMaster("local[*]");
        sparkContext = new JavaSparkContext(sparkConf);

        appResource = new AppResource();
        properties = appResource.getPropValues();
    }

    @Test
    public void findCoreOptimizationConfigById() {

        HibernateUtil.startSession();
        CoreAutoOptimizationConfigServiceInterface service = new CoreAutoOptimizationConfigService();
        CoreAutoOptimizationConfig item = service.findById(1l);

        return;
    }


    @Test
    public void getIdentifiers() {
        Long id = 1l;
        DataTrainingService service = new DataTrainingService();
        String[] identifiers = service.getIdentifiers(1l);

        return;
    }
}
