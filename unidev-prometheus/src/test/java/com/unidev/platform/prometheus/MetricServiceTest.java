package com.unidev.platform.prometheus;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetricServiceTest {

    @Test
    public void labelsMapping() throws IOException {

        MetricService metricService = new MetricService("test", 10000);

        HashMap<String, String> map = new HashMap<String, String>();
        for(int i = 0;i<10;i++) {
            map.put("" + i, "" + i);
        }

        String[] keys = metricService.mapKeys(map);
        String[] values = metricService.mapValues(map);

        assertEquals(10, keys.length);
        assertEquals(10, values.length);

        for(int i = 0;i<10;i++) {
            assertEquals(keys[i], values[i]);
        }

        metricService.stop();
    }

}
